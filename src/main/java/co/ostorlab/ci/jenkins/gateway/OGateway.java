package co.ostorlab.ci.jenkins.gateway;

import co.ostorlab.ci.jenkins.connector.OParameters;
import co.ostorlab.ci.jenkins.connector.RiskInfo;
import co.ostorlab.ci.jenkins.connector.UploadInfo;
import co.ostorlab.ci.jenkins.utils.FileHelper;
import co.ostorlab.ci.jenkins.utils.RequestHandler;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.util.Secret;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * The type O gateway.
 */
public class OGateway {
    private static final String COMMON_LOG = " ostorlab-security-test";
    private static final String RESULT_UPLOADED_JSON = "/result-uploaded.json";
    private static final String TEST_RISK_JSON = "/test-risk.json";
    private static final int ONE_MINUTE = 1000 * 60;
    private static final String PLAN = "static_dynamic_backend";

    private final OParameters params;
    private final File workspace;
    private final File artifactsDir;
    private final TaskListener listener;
    private final Secret apiKey;

    /**
     * Instantiates a new O gateway.
     *
     * @param params       the params
     * @param artifactsDir the artifacts dir
     * @param workspace    the workspace
     * @param listener     the listener
     * @param apiKey       the api key
     * @throws IOException the io exception
     */
    public OGateway(OParameters params, File artifactsDir, FilePath workspace, TaskListener listener,
                    Secret apiKey) throws IOException {
        this.params = params;
        this.workspace = new File(workspace.getRemote());
        this.artifactsDir = artifactsDir;
        this.listener = listener;
        this.apiKey = apiKey;
        if (!artifactsDir.mkdirs()) {
            info("Could not create directory " + artifactsDir);
        }
        if (params.getFilePath() == null || params.getFilePath().isEmpty()) {
            throw new IOException("Binary not specified");
        }
    }

    /**
     * Build url string.
     *
     * @param path the path
     * @param api  the api
     * @return the string
     */
    public static String buildUrl(String path, URL api) {
        String baseUrl = api.getProtocol() + "://" + api.getHost();
        if (api.getPort() > 0) {
            baseUrl += ":" + api.getPort();
        }
        return baseUrl + path;
    }

    /**
     * Execute.
     *
     * @throws IOException          the io exception
     */
    public void execute() throws IOException {
        info("Executing step for " + this);
        try {
            UploadInfo uploadInfo = upload();
            if (uploadInfo != null && params.isWaitingForResults()) {
                waitForResults(uploadInfo);
            }
        } catch (RuntimeException | IOException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            throw new IOException("Failed to start the scan", e);
        }
    }

    @Override
    public String toString() {
        return "ostorlab-auto-security-test [title=" + params.getTitle()
                + ", fileName=" + params.getFilePath() + "]";
    }

    private UploadInfo upload() throws IOException, JsonException {
        File file = FileHelper.find(artifactsDir, params.getFilePath());
        if (file == null) {
            file = FileHelper.find(workspace, params.getFilePath());
        }
        if (file == null) {
            throw new IOException("Failed to find " + params.getFilePath() + " under " + artifactsDir);
        }

        String url = buildUrl();
        info("uploading binary " + file.getAbsolutePath() + " to " + url);
        String uploadJson = RequestHandler.upload(url, apiKey, file.getCanonicalPath(), PLAN, params.getPlatform());
        String path = artifactsDir.getCanonicalPath() + RESULT_UPLOADED_JSON;
        FileHelper.save(path, uploadJson);
        UploadInfo uploadInfo = UploadInfo.fromJson(uploadJson);
        info("uploaded binary with scan-id " + uploadInfo.getScanId() + " and saved output to " + path);
        return uploadInfo;
    }

    private void waitForResults(UploadInfo uploadInfo) throws IOException, JsonException {
        //
        long started = System.currentTimeMillis();
        for (int min = 0; min < params.getWaitMinutes(); min++) {
            info("waiting results for scan " + uploadInfo.getScanId() + getElapsedMinutes(started));
            try {
                Thread.sleep(ONE_MINUTE);
            } catch (InterruptedException e) {
                Thread.interrupted();
            } // wait a minute
            String progress = getProgress(uploadInfo);
            if (progress != null && progress.equals("done")) {
                RiskInfo riskInfo = getRiskInfo(uploadInfo);
                if (riskInfo != null && riskInfo.getRisk() != null &&
                        riskInfo.getRisk().getValue() <= params.getRiskThreshold().getValue()) {
                    if (params.isBreakBuildOnScore()) {
                        throw new IOException("Test failed because risk (" + riskInfo.getRisk()
                                + ") is higher than threshold " + params.getRiskThreshold());
                    } else {
                        info("Test failed because risk (" + riskInfo.getRisk()
                                + ") is higher than threshold " + params.getRiskThreshold());
                    }
                }
                if (riskInfo != null && riskInfo.getRisk() != null) {
                    info("test passed with risk " + riskInfo.getRisk() + getElapsedMinutes(started));
                }
                return;
            }
        }
        if (params.isBreakBuildOnScore()) {
            throw new IOException(
                    "Timedout" + getElapsedMinutes(started) + " while waiting for job " + uploadInfo.getScanId());
        } else {
            info("Timedout" + getElapsedMinutes(started) + " while waiting for job " + uploadInfo.getScanId());
        }
    }

    private String getProgress(UploadInfo uploadInfo) throws JsonException, IOException {
        String url = buildUrl();
        String progressJson = RequestHandler.getProgress(url, uploadInfo.getScanId(), apiKey);
        if (progressJson.isEmpty()) {
            return null;
        }
        JsonObject jsonObject = (JsonObject) Jsoner.deserialize(progressJson);
        return ((String) ((JsonObject) ((JsonObject) jsonObject.get("data")).get("scan")).get("progress"));
    }

    private RiskInfo getRiskInfo(UploadInfo uploadInfo) throws JsonException, IOException {
        String riskPath = artifactsDir.getCanonicalPath() + TEST_RISK_JSON;
        String url = buildUrl();
        String riskJson = RequestHandler.getRisk(url, uploadInfo.getScanId(), apiKey);
        if (riskJson.isEmpty()) {
            return null;
        }
        FileHelper.save(riskPath, riskJson);
        info("Saved risk report from " + url + " to " + riskPath);
        return RiskInfo.fromJson(riskJson);
    }

    private String getElapsedMinutes(long started) {
        long min = (System.currentTimeMillis() - started) / ONE_MINUTE;
        if (min == 0) {
            return "";
        }
        return " [" + min + " minutes]";
    }

    private String buildUrl() throws MalformedURLException {
        return buildUrl("/apis/graphql/", new URL(params.getApiUrl()));
    }

    /**
     * Info.
     *
     * @param msg the msg
     */
    void info(Object msg) {
        listener.getLogger().println(new Date() + COMMON_LOG + " " + msg);
    }

}

