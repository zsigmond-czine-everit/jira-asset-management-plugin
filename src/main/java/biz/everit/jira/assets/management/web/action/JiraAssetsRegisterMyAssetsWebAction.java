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
import biz.everit.jira.assets.management.service.api.enums.WorkflowActions;
import biz.everit.jira.assets.management.service.api.enums.WorkflowStatuses;
import biz.everit.jira.assets.management.utils.ConstantHelper;
import biz.everit.jira.assets.management.utils.MessageCode;
import biz.everit.jira.assets.management.utils.MessageHelper;

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
     * The {@link ConstantHelper} instance.
     */
    private ConstantHelper helper = ConstantHelper.INSTANCE;

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
     * Showing changing page or not.
     */
    private boolean change;

    /**
     * The changing (workflow) action name to modification.
     */
    private String changeActionName;

    /**
     * The issueKey parameter on the request.
     */
    private String issueKey;

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
     * The all asset for the selected user.
     */
    private List<Asset> allAsset = new ArrayList<Asset>();

    /**
     * TODO
     */
    private boolean successMessage;

    /**
     * TODO
     */
    private boolean errorMessage;

    /**
     * TODO
     */
    private String messageCode;

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
        try {
            return arPlugin.findUserByName(arPlugin.findIssueByIssueKey(issueKey).get(issueKey)
                    .getString("assignee"));
        } catch (GenericEntityException e) {
            catchError = e.toString();
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
                // issueKeys.putAll(arPlugin
                // .findIssuesByStatusAndPreviousAssigned(WorkflowStatuses.EXTERNAL_ASSIGNED, selectedUserName));
                List<AssetDetail> assetDetails = arService.findAssetDetailsByIssueKeys(new ArrayList<String>(issueKeys
                        .keySet()));
                result.addAll(arPlugin.convertIssueAndAssetDetailsToAsset(assetDetails, issueKeys));
            }
        } catch (GenericEntityException e) {
            catchError = e.toString();
        }
        return result;
    }

    /**
     * {@inheritDoc}. <b>Must be call {@link MessageHandler#newRequest()} method the first.</b>
     */
    @Override
    public String doDefault() throws Exception {
        boolean isUserLogged = arPlugin.isLoggedUser();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }

        setVariable();
        return super.doDefault();
    }

    @Override
    protected String doExecute() throws Exception {
        boolean isUserLogged = arPlugin.isLoggedUser();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }
        setVariable();
        if (change) {
            if ((arPlugin.isValidIssueKey(issueKey))) {
                String[] issueAssignees = request.getParameterValues("assignee");
                String[] comments = request.getParameterValues("comments");
                ButtonActionNames validButtonActionName = null;
                validButtonActionName = validButtonActionName(changeActionName);
                if ((validButtonActionName != null)
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
                            && arPlugin.changeIssueStatus(issueKey, issueAssignees[0],
                                    validButtonActionName.getWorkflowActionName(),
                                    loggedUser.getName(), comments[0], fields)) {
                        String actualWorkflowActionName = validButtonActionName.getWorkflowActionName();
                        if (actualWorkflowActionName.equals(WorkflowActions.INTERNAL_ASSIGNEE.getActionName())
                                || actualWorkflowActionName.equals(WorkflowActions.REJECT.getActionName())) {
                            setReturnUrl("/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa?messageCode="
                                    + MessageCode.MY_ASSET_SUCCESS_CHANGE);
                        } else {
                            setReturnUrl("/secure/JiraAssetsRegisterAssetDetailsWebAction!default.jspa?actionName="
                                    + ConstantHelper.getActionEdtiDetails().replace(' ', '+')
                                    + "&issueKey="
                                    + issueKey
                                    + "&messageCode="
                                    + MessageCode.MY_ASSET_SUCCESS_CHANGE);
                        }
                        return getRedirect(NONE);
                    } else {
                        setReturnUrl("/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa?issueKey="
                                + issueKey
                                + "&actionName="
                                + changeActionName.replace(' ', '+')
                                + "&messageCode="
                                + MessageCode.MY_ASSET_ERROR_NOT_CHANGE);
                        return getRedirect(NONE);
                    }
                } else {
                    setReturnUrl("/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa?issueKey="
                            + issueKey + "&actionName=" + changeActionName.replace(' ', '+') + "&messageCode="
                            + MessageCode.MY_ASSET_ERROR_CHANGE);
                    return getRedirect(NONE);
                }
            }
        } else {
            setReturnUrl("/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa?selectAssignee=" + selectedUserName);
            return getRedirect(NONE);
        }
        return super.execute();
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

    public final ConstantHelper getHelper() {
        return helper;
    }

    public final String getIssueKey() {
        return issueKey;
    }

    public String getPropertiesKey() {
        return MessageHelper.getProperitesKey(messageCode);
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

    public final boolean isErrorMessage() {
        return errorMessage;
    }

    public final boolean isSuccessMessage() {
        return successMessage;
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
        return arPlugin.previousAssignedUser(issueKey);
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
        AssetField deviceNameField = arService.findFieldByFieldName(ConstantHelper.FIELD_NAME_DEVICE_NAME);
        AssetField assigneeField = arService.findFieldByFieldName(ConstantHelper.FIELD_NAME_ASSIGNEE);
        if (deviceNameField == null) {
            arService.addRequiredField(ConstantHelper.FIELD_NAME_DEVICE_NAME);
        }
        if (assigneeField == null) {
            arService.addRequiredField(ConstantHelper.FIELD_NAME_ASSIGNEE);
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

    private void setChangeParam() {
        if (changeActionName.equals(ButtonActionNames.EXTERNAL_HANDOVER.getActionName()) ||
                changeActionName.equals(ButtonActionNames.INTERNAL_HANDOVER.getActionName()) ||
                changeActionName.equals(ButtonActionNames.DISPOSE_ASSET.getActionName()) ||
                changeActionName.equals(ButtonActionNames.ACCEPTANCE.getActionName()) ||
                changeActionName.equals(ButtonActionNames.REJECTION.getActionName()) ||
                changeActionName.equals(ButtonActionNames.WITHDRAWAL.getActionName()) ||
                changeActionName.equals(ButtonActionNames.REAGAIN.getActionName())) {
            change = true;
        } else {
            change = false;
        }
    }

    /**
     * Set all variable. The changes. issueKeys, changeActionNames, changeActionName, changeSuccess and changeError
     * variable.
     */
    private void setVariable() {
        // getting messaCode parameter and set.
        String[] messageCodeParameter = request.getParameterValues("messageCode");
        if (isValue(messageCodeParameter)) {
            messageCode = messageCodeParameter[0];
        } else {
            messageCode = MessageCode.EMPTY_MESSAGE;
        }

        // setting the messageCode is error or success or either not.
        String[] split = messageCode.split("x");
        if (split[0].equals("01")) {
            successMessage = true;
            errorMessage = false;
        } else if (split[0].equals("02")) {
            successMessage = false;
            errorMessage = true;
        } else {
            successMessage = false;
            errorMessage = false;
        }

        // getting and set issueKey
        String[] issueKeyParameter = request.getParameterValues("issueKey");
        if (isValue(issueKeyParameter) && arPlugin.isValidIssueKey(issueKeyParameter[0])) {
            issueKey = issueKeyParameter[0];
        } else {
            issueKey = "NO_ISSUE_KEY";
        }

        // getting and set changeActionName
        String[] changeActionNameParamter = request.getParameterValues("actionName");
        if (isValue(changeActionNameParamter)) {
            changeActionName = changeActionNameParamter[0];
        } else {
            changeActionName = "NO_CHANGE_ACTION_NAME";
        }

        setChangeParam();

        // get and set the backPageUrl parameter.
        String[] backPageUrls = request.getParameterValues("backPageUrl");
        if (isValue(backPageUrls)) {
            backButtonValue = backPageUrls[0].replace(' ', '+');
        } else {
            backButtonValue = "/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa";
        }

        // get the selectAssignee and set the variable.
        String[] selectAssignees = request.getParameterValues("selectAssignee");
        if (isValue(selectAssignees) && arPlugin.isValidAssignee(selectAssignees[0])) {
            selectedUserName = selectAssignees[0];
        } else {
            selectedUserName = arPlugin.getLoggedUser().getName();
        }

        allAsset.addAll(myAssets());
        allAsset.addAll(deliveredAssets());
        allAsset.addAll(receivedAssets());
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
