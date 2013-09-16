package biz.everit.jira.assets.registry.service.api.enums;

/**
 * Statuses of assets registry workflow.
 */
public enum WorkflowStatuses {

    /**
     * The open status.
     */
    OPEN("Open"),

    /**
     * The internal assigned status
     */
    INTERNAL_ASSIGNED("Internal assigned"),

    /**
     * The external assigned status.
     */
    EXTERNAL_ASSIGNED("External assigned"),

    /**
     * The closed status.
     */
    CLOSED("Closed");

    /**
     * The actual status name.
     */
    private final String statusName;

    /**
     * Simple constructor.
     * 
     * @param statusName
     *            the name of the status.
     */
    private WorkflowStatuses(final String statusName) {
        this.statusName = statusName;
    }

    public String getStatusName() {
        return statusName;
    }

    @Override
    public String toString() {
        return statusName;
    }
}
