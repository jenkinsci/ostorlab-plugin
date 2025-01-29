package co.ostorlab.ci.jenkins.gateway;

import co.ostorlab.ci.jenkins.connector.Credentials;
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
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import static java.lang.Integer.parseInt;

/**
 * The type O gateway.
 */
public class OGateway {
    private static final String COMMON_LOG = " ostorlab-security-test";
    private static final String RESULT_UPLOADED_JSON = "/result-uploaded.json";
    private static final String TEST_RISK_JSON = "/test-risk.json";
    private static final int ONE_MINUTE = 1000 * 60;
    private static final String PROFILE = "Full Scan";

    private final OParameters params;
    private final FilePath workspace;
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
        this.listener = listener;
        this.apiKey = apiKey;
        this.workspace = workspace;
        this.artifactsDir = artifactsDir;
        info("Artifacts directory: " + this.artifactsDir.getAbsolutePath());
        if (!this.artifactsDir.exists() && !this.artifactsDir.mkdirs()) {
            info("Could not find Artifacts directory " + this.artifactsDir.getAbsolutePath());
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
            if (params.isWaitingForResults()) {
                waitForResults(uploadInfo);
            }
        } catch (RuntimeException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to start the scan", e);
        }
    }

    @Override
    public String toString() {
        return "ostorlab-auto-security-test [title=" + params.getTitle()
                + ", fileName=" + params.getFilePath() + "]";
    }

    private UploadInfo upload() throws IOException, JsonException {
        byte[] fileContent = null;
        FilePath path = null;
        try {
            if(workspace != null) {
                info("Searching for " + params.getFilePath() + " under " + workspace);
                path = new FilePath(workspace, params.getFilePath());
                if(path.exists()) {
                    info("Found the application.");
                    fileContent = IOUtils.toByteArray(path.read());
                }
            } else {
                throw new IOException("Failed to find workspace");
            }
        } catch (Exception e) {
            info("Failed to find " + params.getFilePath() + " under " + workspace + " Error: " + e);
        }

        if (fileContent == null) {
            throw new IOException("Failed to find application file");
        }

        List<Credentials> credentialsList = params.getCredentials();
        String url = buildUrl();

        Integer testCredId = null;
        if (credentialsList != null && credentialsList.size() > 0) {
            String createCreds = RequestHandler.createTestCredential(url, credentialsList, apiKey);
            JsonObject createCredsResult = (JsonObject) Jsoner.deserialize(createCreds);
            testCredId = parseInt((String)((JsonObject) ((JsonObject)((JsonObject)createCredsResult.get("data")).get("createTestCredentials")).get("testCredentials")).get("id"));
        }
        info("uploading binary "  + path + " to " + url);
        String uploadJson = RequestHandler.upload(url, apiKey, params.getFilePath(), fileContent, params.getScanProfile(), params.getPlatform(), testCredId);
        info("Done uploading the binary.");
        String artifactPath = artifactsDir.getCanonicalPath() + RESULT_UPLOADED_JSON;
        FileHelper.save(artifactPath, uploadJson);
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

