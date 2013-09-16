package biz.everit.jira.assets.registry.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericValue;

import biz.everit.jira.assets.registry.service.AssetsRegistryServiceImpl;
import biz.everit.jira.assets.registry.service.api.AssetsRegistryService;
import biz.everit.jira.assets.registry.service.api.JiraPluginService;
import biz.everit.jira.assets.registry.service.api.dto.Asset;
import biz.everit.jira.assets.registry.service.api.dto.AssetDetail;
import biz.everit.jira.assets.registry.service.api.dto.AssetField;
import biz.everit.jira.assets.registry.service.api.enums.ButtonActionNames;
import biz.everit.jira.assets.registry.utils.AssetRegistryConstantsUtil;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * This class is responsible for add new asset, info and edit page. All screening and operations.
 */
public class JiraAssetsRegisterAssetDetailsWebAction extends JiraWebActionSupport {

    /**
     * Generated serial version ID.
     */
    private static final long serialVersionUID = -8704123803346242094L;

    /**
     * The usage assignee field name.
     * 
     * @return the name of the assignee field.
     */
    public static String getFIELD_NAME_ASSIGNEE() {
        return AssetRegistryConstantsUtil.FIELD_NAME_ASSIGNEE;
    }

    /**
     * The usage device name field name.
     * 
     * @return the name of the device name field.
     */
    public static String getFIELD_NAME_DEVICE_NAME() {
        return AssetRegistryConstantsUtil.FIELD_NAME_DEVICE_NAME;
    }

    /**
     * The {@link AssetsRegistryService} instance.
     */
    private AssetsRegistryService arService;
    /**
     * The {@link JiraPluginService} instance.
     */
    private JiraPluginService arPlugin;

    /**
     * Showing error message or not.
     */
    private boolean error;

    /**
     * Showing info page or not.
     */
    private boolean info;

    /**
     * Showing edit page or not.
     */
    private boolean edit;

    /**
     * The logged user.
     */
    private User loggedUser;

    /**
     * Showing creation error message or not.
     */
    private boolean createError;

    /**
     * Showing creation success message or not.
     */
    private boolean createSuccess;

    /**
     * Showing editing error message or not.
     */
    private boolean editError;

    /**
     * Showing editing success message or not.
     */
    private boolean editSuccess;

    /**
     * The actual asset when showing details on the site.
     */
    private Asset asset;

    /**
     * The auxiliary variable when listing the fields on the site.
     */
    private AssetField field;

    /**
     * The logged user is administrator or not.
     */
    private boolean administrator;

    /**
     * The value (links), the back button.
     */
    private String backButtonValue;

    /**
     * Simple constructor.
     * 
     * @param ao
     *            The {@link ActiveObjects}.
     * @param arPlugin
     *            The {@link JiraPluginService}.
     */
    public JiraAssetsRegisterAssetDetailsWebAction(final ActiveObjects ao, final JiraPluginService arPlugin) {
        super();
        arService = new AssetsRegistryServiceImpl(ao);
        this.arPlugin = arPlugin;
    }

    @Override
    public String doDefault() throws Exception {
        boolean isUserLogged = arPlugin.isLoggedUser();
        if (!isUserLogged) {
            setReturnUrl("/secure/Dashboard.jspa");
            return getRedirect(NONE);
        }

        String[] backPageUrls = request.getParameterValues("backPageUrl");
        if (isValue(backPageUrls)) {
            backButtonValue = backPageUrls[0].replace(' ', '+');
        } else {
            backButtonValue = "/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa";
        }

        String[] errorArray = request.getParameterValues("error");
        String[] editErrorArray = request.getParameterValues("editError");
        String[] editSuccessArray = request.getParameterValues("editSuccess");
        if (isValue(errorArray) && errorArray[0].equals("true")) {
            error = true;
        } else if (isValue(editErrorArray) && editErrorArray[0].equals("true")) {
            editError = true;
        } else if (isValue(editSuccessArray) && editSuccessArray[0].equals("true")) {
            editSuccess = true;
        }
        // String[] info = request.getParameterValues("info");
        // String[] edit = request.getParameterValues("edit");
        String[] actionNames = request.getParameterValues("actionName");
        ButtonActionNames buttonActionName = null;
        if (isValue(actionNames)) {
            buttonActionName = validButtonActionName(actionNames[0]);
        }
        // if (isValue(info) && info[0].equals("true")) {
        // this.info = true;
        // this.edit = false;
        if (isValue(actionNames) && (buttonActionName.getActionName() != null)
                && buttonActionName.getActionName().equals(ButtonActionNames.VIEW_DETAILS.getActionName())) {
            info = true;
            edit = false;
        } else if (isValue(actionNames) && (buttonActionName.getActionName() != null)
                && buttonActionName.getActionName().equals(ButtonActionNames.EDIT_DETAILS.getActionName())) {
            edit = true;
            info = false;
            // else if (isValue(edit) && edit[0].equals("true")) {
            // this.edit = true;
            // this.info = false;
        } else {
            edit = false;
            info = false;
        }
        if (edit || info) {
            String[] issueKey = request.getParameterValues("issueKey");
            if (isValue(issueKey) && (issueKey[0] != null) && arPlugin.isValidIssueKey(issueKey[0])) {
                Map<String, GenericValue> issues = arPlugin.findIssueByIssueKey(issueKey[0]);
                AssetDetail assetDetail = arService.findAssetDetailByIssueKey(issueKey[0]);
                List<AssetDetail> assetDetails = new ArrayList<AssetDetail>();
                assetDetails.add(assetDetail);
                List<Asset> assets = arPlugin.convertIssueAndAssetDetailsToAsset(assetDetails, issues);
                if ((assets != null) && !assets.isEmpty()) {
                    asset = assets.get(0);
                }
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

        String[] backPageUrls = request.getParameterValues("backPageUrl");
        if (isValue(backPageUrls)) {
            backButtonValue = backPageUrls[0].replace(' ', '+');
        } else {
            backButtonValue = "/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa";
        }

        String[] submitAdd = request.getParameterValues("submit_add");
        String[] submitEdit = request.getParameterValues("submit_edit");
        if (isValue(submitAdd)) {
            List<AssetField> requiredFields = requiredFields();
            HashMap<String, String> fieldsValues = new HashMap<String, String>();
            for (AssetField field : requiredFields) {
                if (field.isActive()) {
                    String[] fieldValue = request.getParameterValues(field.getFieldName());
                    if (!isValue(fieldValue)) {
                        error = true;
                    } else {
                        fieldsValues.put(field.getFieldName(), fieldValue[0]);
                    }
                }
            }
            List<AssetField> optionalFields = optionalFields();
            for (AssetField field : optionalFields) {
                String[] fieldValue = request.getParameterValues(field.getFieldName());
                if (isValue(fieldValue)) {
                    fieldsValues.put(field.getFieldName(), fieldValue[0]);
                }
            }

            if (!error) {
                try {
                    if (fieldsValues.get(AssetRegistryConstantsUtil.FIELD_NAME_DEVICE_NAME) != null) {
                        String issueKey = arPlugin.createIssue(
                                fieldsValues.get(AssetRegistryConstantsUtil.FIELD_NAME_DEVICE_NAME), "");
                        if (issueKey != null) {
                            AssetDetail assetDetails = new AssetDetail(issueKey, fieldsValues);
                            if (!arService.addAsset(assetDetails, issueKey)) {
                                createError = true;
                                createSuccess = false;
                            } else {
                                createSuccess = true;
                                createError = false;
                            }
                        }
                    }
                } catch (CreateException e) {
                    createError = true;
                }
            }
        } else if (isValue(submitEdit)) {
            String[] issueKeys = request.getParameterValues("issueKey");
            String issueKey = null;
            if (isValue(issueKeys)) {
                issueKey = issueKeys[0];
            }
            List<AssetField> requiredFields = requiredFields();
            HashMap<String, String> fieldsValues = new HashMap<String, String>();
            for (AssetField field : requiredFields) {
                String[] fieldValue = request.getParameterValues(field.getFieldName());
                if (!isValue(fieldValue)) {
                    if (isValue(issueKeys)) {
                        AssetDetail asset = arService.findAssetDetailByIssueKey(issueKeys[0]);
                        if (asset.getFields().get(field.getFieldName()) != null) {
                            error = true;
                            editError = true;
                            issueKey = issueKeys[0];
                        }
                    }
                } else {
                    fieldsValues.put(field.getFieldName(), fieldValue[0]);
                }

            }

            List<AssetField> optionalFields = optionalFields();
            for (AssetField field : optionalFields) {
                String[] fieldValue = request.getParameterValues(field.getFieldName());
                if (isValue(fieldValue)) {
                    fieldsValues.put(field.getFieldName(), fieldValue[0]);
                } else {
                    if (isValue(issueKeys)) {
                        AssetDetail asset = arService.findAssetDetailByIssueKey(issueKeys[0]);
                        if (asset.getFields().get(field.getFieldName()) == null) {
                            error = true;
                            editError = true;
                            issueKey = issueKeys[0];
                        }
                    }
                }
            }
            if (!error) {
                if (issueKey != null) {
                    AssetDetail assetDetails = new AssetDetail(issueKeys[0], fieldsValues);
                    if (arService.saveAssetModification(assetDetails)) {
                        editSuccess = true;
                    }
                }
            } else {
                setReturnUrl("/secure/JiraAssetsRegisterAssetDetailsWebAction!default.jspa?error=true&actionName="
                        + ButtonActionNames.EDIT_DETAILS.getActionName().replace(' ', '+') + "&issueKey="
                        + issueKey);
            }
            if (editError) {
                setReturnUrl("/secure/JiraAssetsRegisterAssetDetailsWebAction!default.jspa?editError=true&actionName="
                        + ButtonActionNames.EDIT_DETAILS.getActionName().replace(' ', '+') + "&issueKey="
                        + issueKey);
            } else if (editSuccess) {
                setReturnUrl("/secure/JiraAssetsRegisterAssetDetailsWebAction!default.jspa?editSuccess=true&actionName="
                        + ButtonActionNames.EDIT_DETAILS.getActionName().replace(' ', '+') + "&issueKey="
                        + issueKey);
            }

            return getRedirect(NONE);
        }
        return super.doExecute();
    }

    public AssetsRegistryService getArService() {
        return arService;
    }

    public Asset getAsset() {
        return asset;
    }

    public String getBackButtonValue() {
        return backButtonValue;
    }

    public AssetField getField() {
        return field;
    }

    public User getLoggedUser() {
        loggedUser = arPlugin.getLoggedUser();
        return loggedUser;
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

    public boolean isCreateError() {
        return createError;
    }

    public boolean isCreateSuccess() {
        return createSuccess;
    }

    public boolean isEdit() {
        return edit;
    }

    public boolean isEditError() {
        return editError;
    }

    public boolean isEditSuccess() {
        return editSuccess;
    }

    public boolean isError() {
        return error;
    }

    public boolean isInfo() {
        return info;
    }

    /**
     * Checking the array is have evaluable data.
     * 
     * @param values
     *            the array to be checked.
     * @return true if have evaluable data, otherwise false.
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
     * Returns all optional fields which is declared in the assets registry.
     * 
     * @return all optional fields. If no one return empty list.
     */
    public List<AssetField> optionalFields() {
        return arService.findAllOptionalField();
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

    public void setField(final AssetField field) {
        this.field = field;
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
