package biz.everit.jira.assets.registry.service.api.enums;

/**
 * Action names of button in the assets registry plugin.
 */
public enum ButtonActionNames {

    /**
     * The View details action name.
     */
    VIEW_DETAILS("View details", null),

    /**
     * The Edit details action name.
     */
    EDIT_DETAILS("Edit details", null),

    /**
     * The Internal handover action name which belong to the INTERNAL ASSIGNEE workflow actions.
     */
    INTERNAL_HANDOVER("Internal handover", WorkflowActions.INTERNAL_ASSIGNEE.getActionName()),

    /**
     * The External handover action name which belong to the EXTERNAL ASSIGNEE workflow actions.
     */
    EXTERNAL_HANDOVER("External handover", WorkflowActions.EXTERNAL_ASSIGNEE.getActionName()),

    /**
     * The Dispose asset action name which belong to the CULLING workflow actions.
     */
    DISPOSE_ASSET("Dispose asset", WorkflowActions.CULLING.getActionName()),

    /**
     * The Withdrawal action name which belong to the REJECT workflow actions.
     */
    WITHDRAWAL("Withdrawal", WorkflowActions.REJECT.getActionName()),

    /**
     * The Acceptance action name which belong to the ACCEPT workflow actions.
     */
    ACCEPTANCE("Acceptance", WorkflowActions.ACCEPT.getActionName()),

    /**
     * The Rejection action name which belong to the REJECT workflow actions.
     */
    REJECTION("Rejection", WorkflowActions.REJECT.getActionName()),
    /**
     * The Regain action name which belong to the RECOVER workflow actions.
     */
    REAGAIN("Regain", WorkflowActions.RECOVER.getActionName());

    /**
     * The name of the actual action.
     */
    private final String actionName;

    /**
     * The name of the actual action.
     */
    private final String workflowActionName;

    /**
     * Simple constructor.
     * 
     * @param actionName
     *            the name of the action.
     * @param workflowActionName
     *            the name of the workflow action.
     */
    private ButtonActionNames(final String actionName, final String workflowActionName) {
        this.actionName = actionName;
        this.workflowActionName = workflowActionName;
    }

    /**
     * @return the action name.
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * @return workflow action name. If not belong the workflow name the Button Actions return null.
     */
    public String getWorkflowActionName() {
        return workflowActionName;
    }

    @Override
    public String toString() {
        return "Action name: " + actionName + "\nWorkflow action name: " + workflowActionName;
    }
}
