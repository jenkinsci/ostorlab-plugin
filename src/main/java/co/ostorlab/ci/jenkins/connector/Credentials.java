package co.ostorlab.ci.jenkins.connector;


import org.kohsuke.stapler.DataBoundConstructor;

public class Credentials {
    private String name;
    private String value;

    /**
     * Class Constructor bounded to the Jenkins Data.
     * @param name
     * @param value
     */
    @DataBoundConstructor
    public Credentials(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    /**
     * Set Credential Name
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    /**
     * Set Credential value
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }
}
