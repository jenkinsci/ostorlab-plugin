package co.ostorlab.ci.jenkins.mapper;

/**
 * The type Create mobile scan.
 */
public class CreateMobileScan {

    private final String assetType;
    private final String plan;
    private final String application;
    private final String title;

    /**
     * Instantiates a new Create mobile scan.
     *
     * @param assetType   the asset type
     * @param plan        the plan
     * @param application the application
     * @param title       the title
     */
    public CreateMobileScan(String assetType, String plan, String application, String title) {
        this.assetType = assetType;
        this.plan = plan;
        this.application = application;
        this.title = title;
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
