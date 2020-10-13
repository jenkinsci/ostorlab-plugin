package co.ostorlab.ci.jenkins.connector;

import java.util.HashMap;

/**
 * The interface O parameters.
 */
public interface OParameters {
    /**
     * Gets file name.
     *
     * @return the file name
     */
    String getFilePath();

    /**
     * Gets title.
     *
     * @return the title
     */
    String getTitle();

    /**
     * Is wait for results boolean.
     *
     * @return the boolean
     */
    boolean isWaitingForResults();

    /**
     * Gets wait minutes.
     *
     * @return the wait minutes
     */
    int getWaitMinutes();

    /**
     * Gets plan.
     *
     * @return the plan
     */
    String getPlan();

    /**
     * Gets platform.
     *
     * @return the platform
     */
    String getPlatform();

    /**
     * Is break build on score boolean.
     *
     * @return the boolean
     */
    boolean isBreakBuildOnScore();

    /**
     * Gets risk threshold.
     *
     * @return the risk threshold
     */
    RiskInfo.RISK getRiskThreshold();

    /**
     * Gets api url.
     *
     * @return the api url
     */
    String getApiUrl();
}
