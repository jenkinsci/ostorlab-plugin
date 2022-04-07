package co.ostorlab.ci.jenkins.mapper;

import java.util.Arrays;
import java.util.List;

/**
 * The type Create mobile scan.
 */
public class CreateMobileScan {

    private final String assetType;
    private final String plan;
    private final String application;
    private final String title;
    private final List<Integer> credentialIds;

    /**
     * Instantiates a new Create mobile scan.
     *
     * @param assetType   the asset type
     * @param plan        the plan
     * @param application the application
     * @param title       the title
     */
    public CreateMobileScan(String assetType, String plan, String application, String title, Integer testCredential) {
        this.assetType = assetType;
        this.plan = plan;
        this.application = application;
        this.title = title;
        this.credentialIds = Arrays.asList(new Integer[]{testCredential});
    }

    /**
     * Gets asset type.
     *
     * @return the asset type
     */
    public String getAssetType() {
        return assetType;
    }

    /**
     * Gets Credential Ids.
     *
     * @return the list of Credential ids
     */
    public List<Integer> getCredentialIds() {
        return credentialIds;
    }

    /**
     * Gets plan.
     *
     * @return the plan
     */
    public String getPlan() {
        return plan;
    }

    /**
     * Gets application.
     *
     * @return the application
     */
    public String getApplication() {
        return application;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }
}
