package co.ostorlab.ci.jenkins.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The type Create mobile scan.
 */
public class CreateMobileScan {

    private final String assetType;
    private final String scanProfile;
    private final String application;
    private final String title;
    private final List<Integer> credentialIds;

    /**
     * Instantiates a new Create mobile scan.
     *
     * @param assetType   the asset type
     * @param scanProfile        the scanProfile
     * @param application the application
     * @param title       the title
     */
    public CreateMobileScan(String assetType, String scanProfile, String application, String title, Integer scanCredential) {
        this.assetType = assetType;
        this.scanProfile = scanProfile;
        this.application = application;
        this.title = title;
        if (scanCredential != null) {
            this.credentialIds = Collections.singletonList(scanCredential);
        } else {
            this.credentialIds = new ArrayList<>();
        }
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
     * Gets scanProfile.
     *
     * @return the scanProfile
     */
    public String getScanProfile() {
        return scanProfile;
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
