package biz.everit.jira.assets.management.utils;

import biz.everit.jira.assets.management.service.api.enums.ButtonActionNames;
import biz.everit.jira.assets.management.service.api.enums.DeliveryInformationFields;
import biz.everit.jira.assets.management.service.api.enums.WorkflowStatuses;

/**
 * This class help to manage the constants which using the plugin. Usefull when need the constant value in the page.
 */
public final class ConstantHelper {

    /**
     * The {@link ConstantHelper} static instance.
     */
    public static final ConstantHelper INSTANCE = new ConstantHelper();

    /**
     * The name of the device name field.
     */
    public static final String FIELD_NAME_DEVICE_NAME = "Device name";

    /**
     * The name of the assignee field.
     */
    public static final String FIELD_NAME_ASSIGNEE = "Assignee";

    /**
     * The name of the issue type.
     */
    public static final String ISSUE_TYPE_NAME = "Asset";

    /**
     * The description of the issue type.
     */
    public static final String ISSUE_TYPE_DESCRIPTION = "Asset issue description";

    /**
     * The name of the issue type scheme.
     */
    public static final String ISSUE_TYPE_SCHEME_NAME = "Assets registy issue type scheme";

    /**
     * The description of the issue type scheme.
     */
    public static final String ISSUE_TYPE_SCHEME_DESCRIPTION = "Assets registry issue type schema.";

    /**
     * The JIRA default image path of the issue type.
     */
    public static final String ISSUE_TYPE_IMAGE_PATH = "/images/icons/genericissue.gif";

    /**
     * The resource path of the workflow.
     */
    public static final String WORKFLOW_RESOURCE_PATH = "xml/Assets_registry_workflow.xml";

    /**
     * The name of the workflow.
     */
    public static final String WORKFLOW_NAME = "Asset registry worklfow";

    /**
     * The description of the workflow.
     */
    public static final String WORKFLOW_DESCRIPTION = "Asset registry worklfow";

    /**
     * The name of the workflow scheme.
     */
    public static final String WORKFLOW_SCHEME_NAME = "Assets registry workflow scheme";

    /**
     * The description of the workflow scheme.
     */
    public static final String WORKFLOW_SCHEME_DESCRIPTION = "Assets registry scheme description.";

    /**
     * The name of the project.
     */
    public static final String PROJECT_NAME = "Assets registry";

    /**
     * The key of the project.
     */
    public static final String PROJECT_KEY = "ASSET";

    /**
     * The description of the project.
     */
    public static final String PROJECT_DESCRIPTION = "Project description";

    /**
     * The JIRA default image path of the status.
     */
    public static final String STATUS_IMAGE_PATH = "/images/icons/status_generic.gif";

    /**
     * The description of the internal assigned status.
     */
    public static final String STATUS_INTERNAL_ASSIGNED_DESCRIPTION = "Internal assigned";

    /**
     * The description of the external assigned status.
     */
    public static final String STATUS_EXTERNAL_ASSIGNED_DESCRIPTION = "External assigned";

    /**
     * The name of the acceptance button action.
     * 
     * @return the acceptance button action name.
     */
    public static final String getActionAcceptance() {
        return ButtonActionNames.ACCEPTANCE.getActionName();
    }

    /**
     * The name of the dispose asset button action.
     * 
     * @return the dispose asset culling button action name.
     */
    public static final String getActionDisposeAsset() {
        return ButtonActionNames.DISPOSE_ASSET.getActionName();
    }

    /**
     * The name of the edit details button action.
     * 
     * @return the edit details button action name.
     */
    public static final String getActionEdtiDetails() {
        return ButtonActionNames.EDIT_DETAILS.getActionName();
    }

    /**
     * The name of the external handover button action.
     * 
     * @return the external handover button action name.
     */
    public static final String getActionExternalHandover() {
        return ButtonActionNames.EXTERNAL_HANDOVER.getActionName();
    }

    /**
     * The name of the internal handover button action.
     * 
     * @return the internal handover button action name.
     */
    public static final String getActionInternalHandover() {
        return ButtonActionNames.INTERNAL_HANDOVER.getActionName();
    }

    /**
     * The name of the reagain button action.
     * 
     * @return the reagain button action name.
     */
    public static final String getActionReagain() {
        return ButtonActionNames.REAGAIN.getActionName();
    }

    /**
     * The name of the rejection button action.
     * 
     * @return the rejection button action name.
     */
    public static final String getActionRejection() {
        return ButtonActionNames.REJECTION.getActionName();
    }

    /**
     * The name of the view details button action.
     * 
     * @return the view details button action name.
     */
    public static final String getActionViewDetails() {
        return ButtonActionNames.VIEW_DETAILS.getActionName();
    }

    /**
     * The name of the withdrawal button action.
     * 
     * @return the withdrawal button action name.
     */
    public static final String getActionWithdrawal() {
        return ButtonActionNames.WITHDRAWAL.getActionName();
    }

    /**
     * The name of the address field.
     * 
     * @return the address field name.
     */
    public static String getFieldAddress() {
        return DeliveryInformationFields.ADDRESS.getFieldName();
    }

    /**
     * The name of the company field.
     * 
     * @return the company field name.
     */
    public static String getFieldCompany() {
        return DeliveryInformationFields.COMPANY.getFieldName();
    }

    /**
     * The name of the assignee field.
     * 
     * @return the assignee field name.
     */
    public static final String getFieldNameAssignee() {
        return FIELD_NAME_ASSIGNEE;
    }

    /**
     * The name of the device name field.
     * 
     * @return the device name filed name.
     */
    public static final String getFieldNameDeviceName() {
        return FIELD_NAME_DEVICE_NAME;
    }

    /**
     * The name of the recipient name field.
     * 
     * @return the recipient name field name.
     */
    public static String getFieldRecipientName() {
        return DeliveryInformationFields.RECIPIENT_NAME.getFieldName();
    }

    /**
     * The name of the tax number field.
     * 
     * @return the tax number field name.
     */
    public static String getFieldTaxNumber() {
        return DeliveryInformationFields.TAX_NUMBER.getFieldName();
    }

    /**
     * The description of the issue type.
     * 
     * @return the issue type description.
     */
    public static final String getIssueTypeDescription() {
        return ISSUE_TYPE_DESCRIPTION;
    }

    /**
     * The JIRA default image path of the issue type.
     * 
     * @return the issue type image path.
     */
    public static final String getIssueTypeImagePath() {
        return ISSUE_TYPE_IMAGE_PATH;
    }

    /**
     * The name of the issue type.
     * 
     * @return the issue type name.
     */
    public static final String getIssueTypeName() {
        return ISSUE_TYPE_NAME;
    }

    /**
     * The description of the issue type scheme.
     * 
     * @return the issue type scheme description.
     */
    public static final String getIssueTypeSchemeDescription() {
        return ISSUE_TYPE_SCHEME_DESCRIPTION;
    }

    /**
     * The name of the issue type scheme.
     * 
     * @return the issue type schema name.
     */
    public static final String getIssueTypeSchemeName() {
        return ISSUE_TYPE_SCHEME_NAME;
    }

    /**
     * The description of the project.
     * 
     * @return the project description.
     */
    public static final String getProjectDescription() {
        return PROJECT_DESCRIPTION;
    }

    /**
     * The key of the project.
     * 
     * @return the project key.
     */
    public static final String getProjectKey() {
        return PROJECT_KEY;
    }

    /**
     * The name of the project.
     * 
     * @return the project name.
     */
    public static final String getProjectName() {
        return PROJECT_NAME;
    }

    /**
     * The closed status name.
     * 
     * @return the closed status name.
     */
    public static final String getStatusClosedName() {
        return WorkflowStatuses.CLOSED.getStatusName();
    }

    /**
     * The description of the external assigned status.
     * 
     * @return the external assigned status description.
     */
    public static final String getStatusExternalAssignedDescription() {
        return STATUS_EXTERNAL_ASSIGNED_DESCRIPTION;
    }

    /**
     * The external assigned status name.
     * 
     * @return the external assigned status name.
     */
    public static final String getStatusExternalAssignedName() {
        return WorkflowStatuses.EXTERNAL_ASSIGNED.getStatusName();
    }

    /**
     * The JIRA default image path of the status.
     * 
     * @return the status image path.
     */
    public static final String getStatusImagePath() {
        return STATUS_IMAGE_PATH;
    }

    /**
     * The description of the internal assigned status.
     * 
     * @return the internal assigned status description.
     */
    public static final String getStatusInternalAssignedDescription() {
        return STATUS_INTERNAL_ASSIGNED_DESCRIPTION;
    }

    /**
     * The internal assigned status name.
     * 
     * @return the internal assigned status name.
     */
    public static final String getStatusInternalAssignedName() {
        return WorkflowStatuses.INTERNAL_ASSIGNED.getStatusName();
    }

    /**
     * The open status name.
     * 
     * @return the open status name.
     */
    public static final String getStatusOpenName() {
        return WorkflowStatuses.OPEN.getStatusName();
    }

    /**
     * The description of the workflow.
     * 
     * @return the workflow description.
     */
    public static final String getWorkflowDescription() {
        return WORKFLOW_DESCRIPTION;
    }

    /**
     * The name of the workflow.
     * 
     * @return the workflow name.
     */
    public static final String getWorkflowName() {
        return WORKFLOW_NAME;
    }

    /**
     * The resource path of the workflow.
     * 
     * @return the workflow resource path.
     */
    public static final String getWorkflowResourcePath() {
        return WORKFLOW_RESOURCE_PATH;
    }

    /**
     * The description of the workflow scheme.
     * 
     * @return the workflow scheme description.
     */
    public static final String getWorkflowSchemeDescription() {
        return WORKFLOW_SCHEME_DESCRIPTION;
    }

    /**
     * The name of the workflow scheme.
     * 
     * @return the workflwo scheme name.
     */
    public static final String getWorkflowSchemeName() {
        return WORKFLOW_SCHEME_NAME;
    }
}
