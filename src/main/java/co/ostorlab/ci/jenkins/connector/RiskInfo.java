package co.ostorlab.ci.jenkins.connector;

import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

/**
 * The type Risk info.
 */
public class RiskInfo {
    private RISK risk;

    /**
     * The enum Risk.
     */
    public enum RISK {
        /**
         * High risk.
         */
        HIGH(0),
        /**
         * Medium risk.
         */
        MEDIUM(1),
        /**
         * Low risk.
         */
        LOW(2),
        /**
         * Potentially risk.
         */
        POTENTIALLY(3),
        /**
         * Hardening risk.
         */
        HARDENING(4);
        private final double value;
        RISK(Integer value) {
            this.value =value;
        }

        public double getValue() {
            return value;
        }
    }

    public RiskInfo(String risk) {
        this.risk = extractRisk(risk);
    }

    /**
     * Gets risk.
     *
     * @return the risk
     */
    public RISK getRisk() {
        return risk;
    }

    /**
     * Sets risk.
     *
     * @param risk the risk
     */
    public void setRisk(RISK risk) {
        this.risk = risk;
    }

    private RISK extractRisk(String risk) {
        if (risk != null) {
            switch (risk) {
                case "HARDENING":
                    return RISK.HARDENING;
                case "POTENTIALLY":
                    return RISK.POTENTIALLY;
                case "LOW":
                    return RISK.LOW;
                case "MEDIUM":
                    return RISK.MEDIUM;
                case "HIGH":
                    return RISK.HIGH;
            }
        }
        return null;
    }

    /**
     * From json risk info.
     *
     * @param json the json
     * @return the risk info
     * @throws JsonException the json exception
     */
    public static RiskInfo fromJson(String json) throws JsonException {
        JsonObject jsonObject = (JsonObject) Jsoner.deserialize(json);

        String name = (String) jsonObject.get("name");
        String message = (String) jsonObject.get("message");
        if (name != null && message != null) {
            throw new IllegalArgumentException(name + " " + message);
        }
        String risk = ((String) ((JsonObject) ((JsonObject) jsonObject.get("data")).get("scan")).get("riskRating"));
        RiskInfo riskInfo = new RiskInfo(risk);
        return riskInfo;
    }
}
