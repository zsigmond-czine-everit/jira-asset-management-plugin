package biz.everit.jira.assets.management.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import biz.everit.jira.assets.management.utils.AssetRegistryConstantsUtil;

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
     * The auxiliary variable when listing the fields on the site.
     */
    private String fieldValue;

    /**
     * The auxiliary variable when rendering the filled optional fields.
     */
    private int idNumber = 0;

    /**
     * The auxiliary variable when listing the filled fields on the site.
     */
    private Field filledField;

    /**
     * The list of the fields, which the user filled the page.
     */
    private List<Field> filledFields = new ArrayList<Field>();

    /**
     * The logged user is administrator or not.
     */
    private boolean administrator;

    /**
     * The value (links), the back button.
     */
    private String backButtonValue;

    /**
     * The key of the actual issue.
     */
    private String issueKey;

    /**
     * The name of the action.
     */
    private String buttonActionName;

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
        setDefaultVariable();
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
        String[] backPageUrls = request.getParameterValues("backPageUrl");
        if (isValue(backPageUrls)) {
            backButtonValue = backPageUrls[0].replace(' ', '+');
        } else {
            backButtonValue = "/secure/JiraAssetsRegisterMyAssetsWebAction!default.jspa";
        }
        HashMap<String, String> fieldsValues = new HashMap<String, String>();
        String[] submitAdd = request.getParameterValues("submit_add");
        String[] submitEdit = request.getParameterValues("submit_edit");
        if (isValue(submitAdd)) {
            List<AssetField> requiredFields = requiredFields();
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
        } else if (isValue(submitEdit) || edit) {
            String[] issueKeys = request.getParameterValues("issueKey");
            if (isValue(issueKeys)) {
                issueKey = issueKeys[0];
            }
            List<AssetField> requiredFields = requiredFields();
            for (AssetField field : requiredFields) {
                String[] fieldValue = request.getParameterValues(field.getFieldName());
                if (!isValue(fieldValue)) {
                    if (isValue(issueKeys) && arPlugin.isValidIssueKey(issueKeys[0])) {
                        AssetDetail asset = arService.findAssetDetailByIssueKey(issueKeys[0]);
                        if ((asset != null) && (asset.getFields().get(field.getFieldName()) != null)) {
                            error = true;
                            editError = true;
                            issueKey = issueKeys[0];
                        } else if ((asset != null) && field.isActive()) {
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
                        if (asset.getFields().get(field.getFieldName()) != null) {
                            error = true;
                            editError = true;
                            issueKey = issueKeys[0];
                        }
                    }
                }
            }
            if (!error) {
                if ((issueKeys != null) && arPlugin.isValidIssueKey(issueKeys[0])) {
                    AssetDetail assetDetails = new AssetDetail(issueKeys[0], fieldsValues);
                    if (arService.saveAssetModification(assetDetails)) {
                        arPlugin.changeIssueSummary(
                                issueKeys[0],
                                assetDetails.getFields().get(
                                        JiraAssetsRegisterAssetDetailsWebAction.getFIELD_NAME_DEVICE_NAME()));
                        editSuccess = true;
                    }
                }
            } else {
                edit = true;
                editError = true;
                error = false;
                // setReturnUrl("/secure/JiraAssetsRegisterAssetDetailsWebAction!default.jspa?error=true&actionName="
                // + ButtonActionNames.EDIT_DETAILS.getActionName().replace(' ', '+') + "&issueKey="
                // + issueKey);
            }
            if (editError) {
                edit = true;
                editError = true;
                // setReturnUrl("/secure/JiraAssetsRegisterAssetDetailsWebAction!default.jspa?editError=true&actionName="
                // + ButtonActionNames.EDIT_DETAILS.getActionName().replace(' ', '+') + "&issueKey="
                // + issueKey);
            } else if (editSuccess) {
                edit = true;
                editSuccess = true;
                if ((issueKey != null) && arPlugin.isValidIssueKey(issueKey)) {
                    Map<String, GenericValue> issues = arPlugin.findIssueByIssueKey(issueKey);
                    AssetDetail assetDetail = arService.findAssetDetailByIssueKey(issueKey);
                    List<AssetDetail> assetDetails = new ArrayList<AssetDetail>();
                    assetDetails.add(assetDetail);
                    List<Asset> assets = arPlugin.convertIssueAndAssetDetailsToAsset(assetDetails, issues);
                    if ((assets != null) && !assets.isEmpty()) {
                        asset = assets.get(0);
                    }
                }
                // setReturnUrl("/secure/JiraAssetsRegisterAssetDetailsWebAction!default.jspa?editSuccess=true&actionName="
                // + ButtonActionNames.EDIT_DETAILS.getActionName().replace(' ', '+') + "&issueKey="
                // + issueKey);
            }
            // Set<String> keySet = fieldsValues.keySet();
            // for (String s : keySet) {
            // filledFields.add(new Field(s, fieldsValues.get(s)));
            // }
            // return getRedirect(NONE);
        }
        Set<String> keySet = fieldsValues.keySet();
        for (String s : keySet) {
            filledFields.add(new Field(s, fieldsValues.get(s)));
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

    public String getButtonActionName() {
        return buttonActionName;
    }

    public AssetField getField() {
        return field;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public Field getFilledField() {
        return filledField;
    }

    public List<Field> getFilledFields() {
        return filledFields;
    }

    public String getIdNumber() {
        int tmp = idNumber;
        idNumber += 1;
        return Integer.toString(tmp);
    }

    public String getIssueKey() {
        return issueKey;
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

    private void setDefaultVariable() throws GenericEntityException {
        filledFields = new ArrayList<Field>();
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
        String[] actionNames = request.getParameterValues("actionName");
        ButtonActionNames buttonActionName = null;
        if (isValue(actionNames)) {
            buttonActionName = validButtonActionName(actionNames[0]);
            this.buttonActionName = buttonActionName.getActionName();
        }
        // if (isValue(info) && info[0].equals("true")) {
        // this.info = true;
        // this.edit = false;
        if (isValue(actionNames) && (buttonActionName.getActionName() != null)
                && buttonActionName.getActionName().equals(ButtonActionNames.EDIT_DETAILS.getActionName())) {
            edit = true;
            // else if (isValue(edit) && edit[0].equals("true")) {
            // this.edit = true;
            // this.info = false;
        } else {
            edit = false;
        }
        if (edit) {
            String[] issueKey = request.getParameterValues("issueKey");
            this.issueKey = issueKey[0];
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
    }

    public void setField(final AssetField field) {
        this.field = field;
    }

    public void setFieldValue(final String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public void setFilledField(final Field filledField) {
        this.filledField = filledField;
    }

    /**
     * The method create the list which contains the all showeable optional fields and checking the fields not rewrite
     * the page. Only contains the active and not rewrite fields.
     * 
     * @return
     */
    public List<AssetField> showableOptinalFields() {
        List<AssetField> tmp = new ArrayList<AssetField>();
        List<AssetField> findAllOptionalField = arService.findAllOptionalField();
        for (AssetField af : findAllOptionalField) {
            for (Field f : filledFields) {
                if (af.getFieldName().equals(f.getFieldName())) {
                    tmp.add(af);
                }
            }
        }
        for (AssetField af : tmp) {
            findAllOptionalField.remove(af);
        }
        return findAllOptionalField;
    }

    /**
     * When rewrite the fields values, we must the unique id. The method generate the unique id. The id must be
     * "key_NUMBER_tr" format.
     * 
     * @return the unique id.
     */
    public String trId() {
        return "key_" + idNumber + "_tr";
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
