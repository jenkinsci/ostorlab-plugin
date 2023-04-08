package co.ostorlab.ci.jenkins.plugin;

import co.ostorlab.ci.jenkins.connector.OParameters;
import co.ostorlab.ci.jenkins.connector.RiskInfo;
import co.ostorlab.ci.jenkins.connector.Credentials;
import co.ostorlab.ci.jenkins.gateway.OGateway;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.Jsoner;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.util.Secret;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONException;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines how to upload mobile binary and retrieve scan results and overall scan risk.
 * It would fail the job if the risk is below the user-defined threshold.
 */
public class OPlugin extends Builder implements SimpleBuildStep, OParameters {

    private static final String apiUrl = "https://api.ostorlab.co";
    private static final int DEFAULT_WAIT_MINUTES = 30;

    private Secret apiKey;
    private String filePath;
    private String title;
    private String scanProfile;
    private String platform;
    private boolean waitForResults;
    private int waitMinutes;
    private boolean breakBuildOnScore;
    private RiskInfo.RISK riskThreshold;
    private List<Credentials> credentials;

    /**
     * Instantiates a new O plugin.
     *
     * @param filePath          the mobile application file path
     * @param title             the scan title
     * @param scanProfile       the scan scanProfile to use
     * @param platform          the application platform
     * @param waitForResults    Boolean to wait for the scan results before finishing the job
     * @param waitMinutes       the number of minutes to wait before resuming the job
     * @param breakBuildOnScore Boolean to break build if the risk is higher than the threshold
     * @param riskThreshold     the risk threshold
     * @param apiKey            the API key to authenticate the requests
     * @param JsonCredentials   the credentials in JSON format to use for the scan
     */
    @DataBoundConstructor
    public OPlugin(String filePath, String title, String scanProfile, String platform, boolean waitForResults, int waitMinutes,
                   boolean breakBuildOnScore, RiskInfo.RISK riskThreshold, String apiKey,
                   String JsonCredentials) throws JsonException {
        this.filePath = filePath;
        this.title = title;
        this.scanProfile = scanProfile;
        this.platform = platform;
        this.waitForResults = waitForResults;
        this.waitMinutes = waitMinutes;
        this.breakBuildOnScore = breakBuildOnScore;
        this.riskThreshold = riskThreshold;
        this.apiKey = Secret.fromString(apiKey);
        if (JsonCredentials != null && !JsonCredentials.isEmpty()) {
            JsonArray parsedCredentials = (JsonArray) Jsoner.deserialize(JsonCredentials);
            this.credentials = new ArrayList<>();
            for (Object credential : parsedCredentials) {
                this.credentials.add(new Credentials(
                        (String) ((JsonObject) credential).get("name"),
                        (String) ((JsonObject) credential).get("value")));
            }
        }
    }

    /**
     * Gets api key.
     *
     * @return the api key
     */
    public Secret getApiKey() {
        if (null != this.apiKey && !this.apiKey.getPlainText().isEmpty()) {
            return this.apiKey;
        } else {
            String value = System.getenv("apiKey");
            if (value == null || value.isEmpty()) {
                throw new RuntimeException(System.getenv().toString());
            } else {
                return Secret.fromString(value);
            }
        }
    }

    /**
     * Sets api key.
     *
     * @param apiKey the api key
     */
    public void setApiKey(String apiKey) {
        if (
                (this.apiKey == null || this.apiKey.getPlainText().isEmpty())
                        && (apiKey != null && !apiKey.isEmpty())
        ) {
            this.apiKey = Secret.fromString(apiKey);
        }
    }

    @Override
    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    @Nonnull
    public String getFilePath() {
        return filePath;
    }

    /**
     * Sets file name.
     *
     * @param filePath the file name
     */
    @DataBoundSetter
    public void setFilePath(@Nonnull String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    @DataBoundSetter
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean isWaitingForResults() {
        return waitForResults;
    }

    /**
     * Sets wait for results.
     *
     * @param waitForResults the wait for results
     */
    @DataBoundSetter
    public void setWaitingForResults(boolean waitForResults) {
        this.waitForResults = waitForResults;
    }

    @Override
    public int getWaitMinutes() {
        return waitMinutes == 0 ? DEFAULT_WAIT_MINUTES : waitMinutes;
    }

    /**
     * Sets wait minutes.
     *
     * @param waitMinutes the wait minutes
     */
    @DataBoundSetter
    public void setWaitMinutes(int waitMinutes) {
        this.waitMinutes = waitMinutes;
    }

    @Override
    public String getScanProfile() {
        return scanProfile;
    }

    /**
     * Sets scanProfile.
     *
     * @param scanProfile the scanProfile
     */
    @DataBoundSetter
    public void setScanProfile(String scanProfile) {
        this.scanProfile = scanProfile;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    /**
     * Sets platform.
     *
     * @param platform the platform
     */
    @DataBoundSetter
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Override
    public boolean isBreakBuildOnScore() {
        return breakBuildOnScore;
    }

    /**
     * Sets break build on score.
     *
     * @param breakBuildOnScore the break build on score
     */
    @DataBoundSetter
    public void setBreakBuildOnScore(boolean breakBuildOnScore) {
        this.breakBuildOnScore = breakBuildOnScore;
    }

    @Override
    public RiskInfo.RISK getRiskThreshold() {
        return riskThreshold;
    }

    /**
     * Sets score threshold.
     *
     * @param riskThreshold the risk threshold
     */
    @DataBoundSetter
    public void setScoreThreshold(RiskInfo.RISK riskThreshold) {
        this.riskThreshold = riskThreshold;
    }

    /**
     * Get the list of credentials passed from the task config
     *
     * @return
     */
    public List<Credentials> getCredentials() {
        return credentials;
    }

    /**
     * Set the list of credentials passed from the task config
     *
     * @param credentials
     */
    @DataBoundSetter
    public void setCredentials(List<Credentials> credentials) {
        this.credentials = credentials;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void perform(Run<?, ?> run, @NonNull FilePath workspace, @NonNull Launcher launcher, @NonNull TaskListener listener) throws InterruptedException, IOException {
        try {
            String token = run.getEnvironment(listener).get("apiKey");
            this.setApiKey(token);
            new OGateway(this, run.getArtifactsDir(), workspace, listener, apiKey).execute();
        } catch (Exception e) {
            listener.error(e.toString());
            run.setResult(Result.FAILURE);
        }
    }


    /**
     * Descriptor class for Build step.
     */
    @Symbol("ostorlabScan")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        /**
         * Do validate params form validation.
         *
         * @param apiKey     the api key
         * @param binaryName the binary name
         * @param project    the project
         * @param owner      the owner
         * @return the form validation
         * @throws JSONException the json exception
         */
        public FormValidation doValidateParams(@QueryParameter("apiKey") String apiKey,
                                               @QueryParameter("binaryName") final String binaryName,
                                               @SuppressWarnings("rawtypes") @AncestorInPath AbstractProject project,
                                               @AncestorInPath final Job<?, ?> owner)
                throws JSONException {
            if (binaryName == null || binaryName.isEmpty()) {
                return FormValidation.errorWithMarkup(Messages.OPlugin_DescriptorImpl_errors_missingBinary());
            }
            if (apiKey != null) {
                try {
                    return FormValidation.ok();
                } catch (Exception e) {
                    return FormValidation.errorWithMarkup(Messages.OPlugin_DescriptorImpl_errors_invalidKey());
                }
            } else {
                return FormValidation.errorWithMarkup(Messages.OPlugin_DescriptorImpl_errors_missingKey());
            }
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.OPlugin_DescriptorImpl_DisplayName();
        }

    }

}

