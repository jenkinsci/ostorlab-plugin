package co.ostorlab.ci.jenkins.connector;


import org.kohsuke.stapler.DataBoundConstructor;

public class Credentials {
    private String name;
    private String value;

    @DataBoundConstructor
    public Credentials(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
