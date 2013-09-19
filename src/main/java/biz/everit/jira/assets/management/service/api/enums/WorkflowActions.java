package biz.everit.jira.assets.management.service.api.enums;

/**
 * Action names of assets registry workflow.
 */
public enum WorkflowActions {

    /**
     * The internal assignee action name.
     */
    INTERNAL_ASSIGNEE("Internal assignee"),

    /**
     * The external assignee action name.
     */
    EXTERNAL_ASSIGNEE("External assignee"),

    /**
     * The culling action name.
     */
    CULLING("Culling"),

    /**
     * The accept action name.
     */
    ACCEPT("Accept"),

    /**
     * The reject action name.
     */
    REJECT("Reject"),

    /**
     * The recover action name.
     */
    RECOVER("Recover"),

    /**
     * The re-inventory action name.
     */
    RE_INVENTORY("Re-inventory");

    /**
     * The name of the actual action.
     */
    private final String actionName;

    /**
     * Simple constructor.
     * 
     * @param actionName
     *            the name of the action.
     */
    private WorkflowActions(final String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }

    @Override
    public String toString() {
        return actionName;
    }
}
