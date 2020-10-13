package co.ostorlab.ci.jenkins.mapper;

/**
 * The type Get mobile scan.
 */
public class GetMobileScan {

    private final int scanId;


    /**
     * Instantiates a new Get mobile scan.
     *
     * @param scanId the scan id
     */
    public GetMobileScan(int scanId) {
        this.scanId = scanId;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public int getScanId() {
        return scanId;
    }
}
