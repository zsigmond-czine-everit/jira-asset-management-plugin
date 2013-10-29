package biz.everit.jira.assets.management.web.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import biz.everit.jira.assets.management.service.AssetsRegistryServiceImpl;
import biz.everit.jira.assets.management.service.api.AssetsRegistryService;
import biz.everit.jira.assets.management.service.api.JiraPluginService;
import biz.everit.jira.assets.management.service.api.dto.Asset;
import biz.everit.jira.assets.management.service.api.dto.AssetDetail;
import biz.everit.jira.assets.management.service.api.dto.AssetField;
import biz.everit.jira.assets.management.service.api.dto.Field;
import biz.everit.jira.assets.management.service.api.enums.ButtonActionNames;
import biz.everit.jira.assets.management.service.api.enums.DeliveryInformationFields;
import biz.everit.jira.assets.management.service.api.enums.WorkflowStatuses;
import biz.everit.jira.assets.management.utils.AssetRegistryConstantsUtil;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * This class is responsible for my asset and changing issue status page. All screening and operations.
 */
public class JiraAssetsRegisterMyAssetsWebAction extends JiraWebActionSupport {

    /**
     * Generated serial version ID.
     */
    private static final long serialVersionUID = 2631028649881573072L;

    /**
     * The name of the acceptance button action.
     * 
     * @return the acceptance button action name.
     */
    public static String actionAcceptance() {
        return ButtonActionNames.ACCEPTANCE.getActionName();
    }

    /**
     * The name of the dispose asset button action.
     * 
     * @return the dispose asset culling button action name.
     */
    public static String actionDisposeAsset() {
        return ButtonActionNames.DISPOSE_ASSET.getActionName();
    }

    /**
     * The name of the edit details button action.
     * 
     * @return the edit details button action name.
     */
    public static String actionEdtiDetails() {
        return ButtonActionNames.EDIT_DETAILS.getActionName();
    }

    /**
     * The name of the external handover button action.
     * 
     * @return the external handover button action name.
     */
    public static String actionExternalHandover() {
        return ButtonActionNames.EXTERNAL_HANDOVER.getActionName();
    }

    /**
     * The name of the internal handover button action.
     * 
     * @return the internal handover button action name.
     */
    public static String actionInternalHandover() {
        return ButtonActionNames.INTERNAL_HANDOVER.getActionName();
    }

    /**
     * The name of the reagain button action.
     * 
     * @return the reagain button action name.
     */
    public static String actionReagain() {
        return ButtonActionNames.REAGAIN.getActionName();
    }

    /**
     * The name of the rejection button action.
     * 
     * @return the rejection button action name.
     */
    public static String actionRejection() {
        return ButtonActionNames.REJECTION.getActionName();
    }

    /**
     * The name of the view details button action.
     * 
     * @return the view details button action name.
     */
    public static String actionViewDetails() {
        return ButtonActionNames.VIEW_DETAILS.getActionName();
    }

    /**
     * The name of the withdrawal button action.
     * 
     * @return the withdrawal button action name.
     */
    public static String actionWithdrawal() {
        return ButtonActionNames.WITHDRAWAL.getActionName();
    }

    /**
     * The name of the address field.
     * 
     * @return the address field name.
     */
    public static String fieldAddress() {
        return DeliveryInformationFields.ADDRESS.getFieldName();
    }

    /**
     * The name of the company field.
     * 
     * @return the company field name.
     */
    public static String fieldCompany() {
        return DeliveryInformationFields.COMPANY.getFieldName();
    }

    /**
     * The name of the recipient name field.
     * 
     * @return the recipient name field name.
     */
    public static String fieldRecipientName() {
        return DeliveryInformationFields.RECIPIENT_NAME.getFieldName();
    }

    /**
     * The name of the tax number field.
     * 
     * @return the tax number field name.
     */
    public static String fieldTaxNumber() {
        return DeliveryInformationFields.TAX_NUMBER.getFieldName();
    }

    /**
     * The usage workflow external assignee status name.
     * 
     * @return the external assignee status name.
     */
    public static String statusExternalAssignee() {
        return WorkflowStatuses.EXTERNAL_ASSIGNED.getStatusName();
    }

    /**
     * The usage workflow internal assignee status name.
     * 
     * @return the internal assignee status name.
     */
    public static String statusInternalAssignee() {
        return WorkflowStatuses.INTERNAL_ASSIGNED.getStatusName();
    }

    /**
     * The value (links), the back button.
     */
    private String backButtonValue;

    /**
     * The {@link AssetsRegistryService} instance.
     */
    private AssetsRegistryService arService;

    /**
     * The {@link JiraPluginService} instance.
     */
    private JiraPluginService arPlugin;

    /**
     * The auxiliary variable when showing assignable user on the site.
     */
    private User assignee;

    /**
     * The actual issue key to modification.
     */
    private String actualIssueKey;

    /**
     * Showing changing error message or not.
     */
    private boolean changeError;

    /**
     * Showing changing page or not.
     */
    private boolean change;

    /**
     * The changing (workflow) action name to modification.
     */
    private String changeActionName;

    /**
     * The change parameter on the request.
     */
    private String[] changes;

    /**
     * The issueKey parameter on the request.
     */
    private String[] issueKeys;

    /**
     * The actionName parameter on the request.
     */
    private String[] changeActionNames;

    /**
     * The actual asset when showing details on the site.
     */
    private Asset asset;

    /**
     * The logged user is administrator or not.
     */
    private boolean administrator;

    /**
     * The name of the selected user.
     */
    private String selectedUserName;

    /**
     * The system error message.
     */
    private String catchError;

    /**
     * The show not changed message.
     */
    private boolean notChanged;

    /**
     * The all asset for the selected user.
     */
    private List<Asset> allAsset = new ArrayList<Asset>();

    /**
     * Simple constructor.
     * 
     * @param ao
     *            The {@link ActiveObjects}.
     * @param arPlugin
     *            The {@link JiraPluginService}.
     */
    public JiraAssetsRegisterMyAssetsWebAction(final ActiveObjects ao, final JiraPluginService arPlugin) {
        super();
        arService = new AssetsRegistryServiceImpl(ao);
        this.arPlugin = arPlugin;
    }

    /**
     * Find actual assigned user in the issue.
     * 
     * @return the user if have one otherwise <code>null</code>.
     */
    public User actualAssignedUser() {
        if (actualIssueKey != null) {
            try {
                return arPlugin.findUserByName(arPlugin.findIssueByIssueKey(actualIssueKey).get(actualIssueKey)
                        .getString("assignee"));
            } catch (GenericEntityException e) {
                catchError = e.toString();
            }
        }
        return null;
    }

    /**
     * Find all assignable user.
     * 
     * @return the assignable users, if no one return empty list.
     */
    public List<User> assignees() {
        return arPlugin.findAllAssigneesInProject();
    }

    /**
     * Find all delivered assets. Search in INTERNAL ASSIGNED and EXTERNAL ASSIGNED statuses.
     * 
     * @return the all delivered assets, if no one return empty list.
     */
    public List<Asset> deliveredAssets() {
        List<Asset> result = new ArrayList<Asset>();
        try {
            if ((selectedUserName != null) && arPlugin.isValidAssignee(selectedUserName)) {
                Map<String, GenericValue> issueKeys = arPlugin
                        .findIssuesByStatusAndPreviousAssigned(WorkflowStatuses.INTERNAL_ASSIGNED, selectedUserName);

                issueKeys.putAll(arPlugin
                        .findIssuesByStatusAndAssignee(WorkflowStatuses.EXTERNAL_ASSIGNED, selectedUserName));
                issueKeys.putAll(arPlugin
                        .findIssuesByStatusAndPreviousAssigned(WorkflowStatuses.EXTERNAL_ASSIGNED, selectedUserName));
                List<AssetDetail> assetDetails = arService.findAssetDetailsByIssueKeys(new ArrayList<String>(issueKeys
                        .keySet()));
                result.addAll(arPlugin.convertIssueAndAssetDetailsToAsset(assetDetails, issueKeys));
            }
        } catch (GenericEntityException e) {
            catchError = e.toString();
        }
        return result;
    }

    @Override
    public String doDefault() throws Exception {
        boolean isUserLogged = arPlugin.isLoggedUser();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }
        setDefaultVariable();
        if (isValue(changes) && changes[0].equals("true")) {
            if (isValue(issueKeys) && (arPlugin.isValidIssueKey(issueKeys[0]) && isValue(changeActionNames))) {
                change = true;
                changeActionName = changeActionNames[0];
                actualIssueKey = issueKeys[0];
            }
        }
        return super.doDefault();
    }

    @Override
    protected String doExecute() throws Exception {
        boolean isUserLogged = arPlugin.isLoggedUser();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }
        setDefaultVariable();
        if (isValue(changes) && changes[0].equals("true")) {
            if (isValue(issueKeys) && (arPlugin.isValidIssueKey(issueKeys[0]))) {
                actualIssueKey = issueKeys[0];
                String[] issueAssignees = request.getParameterValues("assignee");
                String[] comments = request.getParameterValues("comments");
                ButtonActionNames validButtonActionName = null;
                if (isValue(changeActionNames)) {
                    validButtonActionName = validButtonActionName(changeActionNames[0]);
                }
                if (isValue(changeActionNames) && (validButtonActionName != null)
                        && arPlugin.isValidAction(validButtonActionName.getWorkflowActionName())
                        && isValue(issueAssignees)
                        && arPlugin.isValidAssignee(issueAssignees[0]) && isValue(comments)) {
                    User loggedUser = arPlugin.getLoggedUser();
                    List<Field> fields = new ArrayList<Field>();
                    for (DeliveryInformationFields dif : DeliveryInformationFields.values()) {
                        String[] fieldValue = request.getParameterValues(dif.getFieldName());
                        if (isValue(fieldValue)) {
                            fields.add(new Field(dif.getFieldName(), fieldValue[0]));
                        }
                    }
                    if ((loggedUser != null)
                            && arPlugin.changeIssueStatus(issueKeys[0], issueAssignees[0],
                                    validButtonActionName.getWorkflowActionName(),
                                    loggedUser.getName(), comments[0], fields)) {
                        setReturnUrl("/secure/JiraAssetsRegisterAssetDetailsWebAction!default.jspa?changeSuccess=true&actionName="
                                + JiraAssetsRegisterMyAssetsWebAction.actionEdtiDetails().replace(' ', '+')
                                + "&issueKey=" + issueKeys[0]);
                        return getRedirect(NONE);
                    } else {
                        notChanged = true;
                        setReturnUrl("/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa?notChanged=true&change=true&issueKey="
                                + issueKeys[0] + "&actionName=" + changeActionName.replace(' ', '+'));
                        return getRedirect(NONE);
                    }
                } else {
                    changeError = true;
                    setReturnUrl("/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa?changeError=true&change=true&issueKey="
                            + issueKeys[0] + "&actionName=" + changeActionName.replace(' ', '+'));
                    return getRedirect(NONE);
                }
            }
        } else {
            setReturnUrl("/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa?selectAssignee=" + selectedUserName);
            return getRedirect(NONE);
        }
        return super.execute();
    }

    public String getActualIssueKey() {
        return actualIssueKey;
    }

    public List<Asset> getAllAsset() {
        return allAsset;
    }

    public JiraPluginService getArPlugin() {
        return arPlugin;
    }

    public AssetsRegistryService getArService() {
        return arService;
    }

    public Asset getAsset() {
        return asset;
    }

    public User getAssignee() {
        return assignee;
    }

    public String getBackButtonValue() {
        return backButtonValue;
    }

    public String getCatchError() {
        return catchError;
    }

    public String getChangeActionName() {
        return changeActionName;
    }

    public String getSelectedUserName() {
        return selectedUserName;
    }

    /**
     * Checking the logged user is administrator.
     * 
     * @return <code>true</code> if administrator, otherwise <code>false</code>.
     */
    public boolean isAdministrator() {
        administrator = false;
        User loggedUser = arPlugin.getLoggedUser();
        if (loggedUser != null) {
            administrator = arPlugin.isAdministrator(loggedUser);
        }
        return administrator;
    }

    public boolean isChange() {
        return change;
    }

    public boolean isChangeError() {
        return changeError;
    }

    public boolean isNotChanged() {
        return notChanged;
    }

    /**
     * Checking the array is have evaluable data.
     * 
     * @param values
     *            the array to be checked.
     * @return <code>true</code> if have evaluable data, otherwise <code>false</code>.
     */
    private boolean isValue(final String[] values) {
        boolean result = false;
        if (values != null) {
            for (String value : values) {
                if ((!value.equals("")) && (!value.trim().equals(""))) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * The logged user.
     * 
     * @return the logged user if have logged user otherwise return <code>null</code>.
     */
    public User loggedUserName() {
        return arPlugin.getLoggedUser();
    }

    /**
     * Find all my assets (Assets issued to me). Search in OPEN status.
     * 
     * @return the all delivered assets, if no one return empty list..
     */
    public List<Asset> myAssets() {
        List<Asset> result = new ArrayList<Asset>();
        try {
            if ((selectedUserName != null) && arPlugin.isValidAssignee(selectedUserName)) {
                Map<String, GenericValue> issueKeys = arPlugin
                        .findIssuesByStatusAndAssignee(WorkflowStatuses.OPEN, selectedUserName);
                List<AssetDetail> assetDetails = arService
                        .findAssetDetailsByIssueKeys(new ArrayList<String>(issueKeys.keySet()));
                result = arPlugin.convertIssueAndAssetDetailsToAsset(assetDetails, issueKeys);
            }
        } catch (GenericEntityException e) {
            catchError = e.toString();
        }
        return result;
    }

    /**
     * Returns all optional fields which is declared in the assets registry.
     * 
     * @return all optional fields. If no one return empty list.
     */
    public List<AssetField> optionalFields() {
        return arService.findAllOptionalField();
    }

    /**
     * Finds and return the last exist assigned user in the issue.
     * 
     * @return the find exist user, if no one return <code>null</code>.
     */
    public User previousAssignedUser() {
        if (actualIssueKey != null) {
            return arPlugin.previousAssignedUser(actualIssueKey);
        }
        return null;
    }

    /**
     * Find all received but not ACCEPT or REJECT assets (Assets waiting to confirmation panel). Search in INTERNAL
     * ASSIGNED status.
     * 
     * @return the all received assets, if no one return empty list..
     */
    public List<Asset> receivedAssets() {
        List<Asset> result = new ArrayList<Asset>();
        try {
            if ((selectedUserName != null) && arPlugin.isValidAssignee(selectedUserName)) {
                Map<String, GenericValue> issueKeys = arPlugin
                        .findIssuesByStatusAndAssignee(WorkflowStatuses.INTERNAL_ASSIGNED, selectedUserName);
                List<AssetDetail> assetDetails = arService
                        .findAssetDetailsByIssueKeys(new ArrayList<String>(issueKeys.keySet()));
                result = arPlugin.convertIssueAndAssetDetailsToAsset(assetDetails, issueKeys);
            }
        } catch (GenericEntityException e) {
            catchError = e.toString();
        }
        return result;
    }

    /**
     * Returns all required fields which is declared in the assets registry.
     * 
     * @return all required fields. If no one return empty list.
     */
    public List<AssetField> requiredFields() {
        requiredFieldsNotDeclared();
        return arService.findAllRequiredField();
    }

    /**
     * Checking the default fields. If not declared creating the fields.
     */
    private void requiredFieldsNotDeclared() {
        AssetField deviceNameField = arService.findFieldByFieldName(AssetRegistryConstantsUtil.FIELD_NAME_DEVICE_NAME);
        AssetField assigneeField = arService.findFieldByFieldName(AssetRegistryConstantsUtil.FIELD_NAME_ASSIGNEE);
        if (deviceNameField == null) {
            arService.addRequiredField(AssetRegistryConstantsUtil.FIELD_NAME_DEVICE_NAME);
        }
        if (assigneeField == null) {
            arService.addRequiredField(AssetRegistryConstantsUtil.FIELD_NAME_ASSIGNEE);
        }
    }

    public void setAsset(final Asset asset) {
        this.asset = asset;
    }

    public void setAssignee(final User assignee) {
        this.assignee = assignee;
    }

    public void setCatchError(final String catchError) {
        this.catchError = catchError;
    }

    /**
     * Set all default variable. The changes. issueKeys, changeActionNames, changeActionName, changeSuccess and
     * changeError variable.
     */
    private void setDefaultVariable() {
        changes = request.getParameterValues("change");
        issueKeys = request.getParameterValues("issueKey");
        changeActionNames = request.getParameterValues("actionName");
        String[] selectAssignees = request.getParameterValues("selectAssignee");
        User loggedUser = arPlugin.getLoggedUser();
        if (loggedUser != null) {
            if (isValue(selectAssignees) && arPlugin.isValidAssignee(selectAssignees[0])) {
                selectedUserName = selectAssignees[0];
            } else {
                selectedUserName = loggedUser.getName();
            }
            if (isValue(changeActionNames)) {
                changeActionName = changeActionNames[0];
            }

            String[] changeErrors = request.getParameterValues("changeError");
            String[] notChangeds = request.getParameterValues("notChanged");
            if (isValue(changeErrors) && changeErrors[0].equals("true")) {
                changeError = true;
            }
            if (isValue(notChangeds) && notChangeds[0].equals("true")) {
                notChanged = true;
            }

            String[] backPageUrls = request.getParameterValues("backPageUrl");
            if (isValue(backPageUrls)) {
                backButtonValue = backPageUrls[0].replace(' ', '+');
            } else {
                backButtonValue = "/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa";
            }
            allAsset.addAll(myAssets());
            allAsset.addAll(deliveredAssets());
            allAsset.addAll(receivedAssets());
        }
    }

    /**
     * 
     * @param buttonActionName
     *            the name of the button action.
     * @return the correct {@link ButtonActionNames}. If not valid the button action return <code>null</code>.
     */
    private ButtonActionNames validButtonActionName(final String buttonActionName) {
        if (buttonActionName != null) {
            for (ButtonActionNames ban : ButtonActionNames.values()) {
                if (ban.getActionName().equals(buttonActionName)) {
                    return ban;
                }
            }
        }
        return null;
    }

}
