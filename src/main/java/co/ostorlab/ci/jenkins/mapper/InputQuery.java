package co.ostorlab.ci.jenkins.mapper;

/**
 * The type Input query.
 */
public class InputQuery {
    private final String query;
    private final Object variables;

    /**
     * Instantiates a new Input query.
     *
     * @param query     the query
     * @param variables the variables
     */
    public InputQuery(String query, Object variables) {
        this.query = query;
        this.variables = variables;
    }

    /**
     * Instantiates a new Input query.
     *
     * @param query the query
     */
    public InputQuery(String query) {
        this.query = query;
        this.variables = null;
    }

    /**
     * Gets query.
     *
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Gets variables.
     *
     * @return the variables
     */
    public Object getVariables() {
        return variables;
    }
}
