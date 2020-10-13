package co.ostorlab.ci.jenkins.connector;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonException;
import com.github.cliftonlabs.json_simple.JsonObject;
import com.github.cliftonlabs.json_simple.Jsoner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Subscriptions.
 */
public class Subscriptions {
    private HashMap<String, Integer> remainingScansPerPlan;

    /**
     * Instantiates a new Subscriptions.
     *
     * @param remainingScansPerPlan the remaining scans per plan
     */
    public Subscriptions(HashMap<String, Integer> remainingScansPerPlan) {
        this.remainingScansPerPlan = remainingScansPerPlan;
    }

    /**
     * From json subscriptions.
     *
     * @param json the json
     * @return the subscriptions
     * @throws JsonException the json exception
     */
    public static Subscriptions fromJson(String json) throws JsonException {
        JsonObject jsonObject = (JsonObject) Jsoner.deserialize(json);
        // for error message
        JsonArray subscriptions = (JsonArray) ((JsonObject) ((JsonObject) jsonObject.get("data")).get("subscriptions")).get("subscriptions");
        HashMap<String, Integer> remainingScansPerPlan = new HashMap<>();
        for (Object subscription : subscriptions) {
            Integer countRemainingScan = ((BigDecimal) ((JsonObject) subscription).get("countRemainingScan")).intValue();
            String product_name = (String) ((JsonObject) ((JsonObject) ((JsonObject) subscription).get("plan")).get("product")).get("scanType");
            remainingScansPerPlan.put(product_name.toLowerCase(), countRemainingScan);
        }
        return new Subscriptions(remainingScansPerPlan);
    }

    /**
     * Gets remaining scans per plan.
     *
     * @return the remaining scans per plan
     */
    public HashMap<String, Integer> getRemainingScansPerPlan() {
        return remainingScansPerPlan;
    }

    /**
     * Sets scan id.
     *
     * @param remainingScansPerPlan the remaining scans per plan
     */
    public void setScanId(HashMap<String, Integer> remainingScansPerPlan) {
        this.remainingScansPerPlan = remainingScansPerPlan;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Integer> set : remainingScansPerPlan.entrySet()) {
            result.append(set.getKey()).append(" = ").append(set.getValue()).append("; ");
        }
        return result.toString();
    }

}
