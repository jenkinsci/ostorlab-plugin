package co.ostorlab.ci.jenkins.connector;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

/**
 * The type Upload info.
 */
public class UploadInfo {
    private Integer scanId;

    /**
     * Instantiates a new Upload info.
     */
    public UploadInfo() {

    }

    /**
     * From json upload info.
     *
     * @param json the json
     * @return the upload info
     * @throws JsonException the json exception
     */
    public static UploadInfo fromJson(String json) throws JsonException {
        JsonObject jsonObject = (JsonObject) Jsoner.deserialize(json);
        // for error message
        String name = (String) jsonObject.get("name");
        String message = (String) jsonObject.get("message");
        if (name != null && message != null) {
            throw new RuntimeException(name + " " + message);
        }
        //
        UploadInfo uploadInfo = new UploadInfo();
        if (jsonObject.get("data") != null &&
                ((JsonObject) jsonObject.get("data")).get("createMobileScan") != null &&
                ((JsonObject) ((JsonObject) jsonObject.get("data")).get("createMobileScan")).get("scan") != null &&
                ((JsonObject) ((JsonObject) ((JsonObject) jsonObject.get("data")).get("createMobileScan")).get("scan")).get("id") != null
        ) {
            Integer scanId = Integer.parseInt((String) ((JsonObject) ((JsonObject) ((JsonObject) jsonObject.get("data")).get("createMobileScan")).get("scan")).get("id"));
            uploadInfo.setScanId(scanId);
            return uploadInfo;
        } else {
            throw new RuntimeException("Could not extract scan id from response " + json);
        }
    }

    /**
     * Gets scan id.
     *
     * @return the scan id
     */
    public Integer getScanId() {
        return scanId;
    }

    /**
     * Sets scan id.
     *
     * @param scanId the scan id
     */
    public void setScanId(Integer scanId) {
        this.scanId = scanId;
    }


    @Override
    public String toString() {
        return "ReportInfo [scan id=" + scanId + "]";
    }

}
