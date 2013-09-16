package biz.everit.jira.assets.registry.web.action;

import java.util.List;

import biz.everit.jira.assets.registry.service.AssetsRegistryServiceImpl;
import biz.everit.jira.assets.registry.service.api.AssetsRegistryService;
import biz.everit.jira.assets.registry.service.api.JiraPluginService;
import biz.everit.jira.assets.registry.service.api.dto.AssetField;
import biz.everit.jira.assets.registry.utils.AssetRegistryConstantsUtil;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * This class is responsible for filed management page. All screening and operations.
 */
public class JiraAssetsRegisterFieldManagementWebAction extends JiraWebActionSupport {

    /**
     * Generated serial version ID.
     */
    private static final long serialVersionUID = -3171355480317954327L;

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
     * The auxiliary variable when listing the fields on the site.
     */
    private AssetField field;
    /**
     * Showing a creation error message or not.
     */
    private boolean createError;
    /**
     * Showing a deletion error message or not.
     */
    private boolean deleteError;
    /**
     * Showing a activation error message or not.
     */
    private boolean activeError;

    /**
     * Showing a creation success message or not.
     */
    private boolean createSuccess;

    /**
     * Showing a deletion success message or not.
     */
    private boolean deleteSuccess;

    /**
     * Showing a activation success message or not.
     */
    private boolean activeSuccess;

    /**
     * The logged user is administrator or not.
     */
    private boolean administrator;

    /**
     * Showing a edition success message or not.
     */
    private boolean editSuccess;

    /**
     * Showing a edition error message or not.
     */
    private boolean editError;

    /**
     * Showing the edit page or not.
     */
    private boolean edit;

    /**
     * The name of the field which editing.
     */
    private String editFieldName;

    /**
     * Showing a empty error message or not. If the field name is empty only show the empty error message.
     */
    private boolean emptyError;

    /**
     * Showing clear database error message or not.
     */
    private boolean deleteAllError;

    /**
     * Showing clear database success message or not.
     */
    private boolean deleteAllSuccess;

    /**
     * Simple constructor.
     * 
     * @param ao
     *            The {@link ActiveObjects}.
     * @param arPlugin
     *            The {@link JiraPluginService}.
     */
    public JiraAssetsRegisterFieldManagementWebAction(final ActiveObjects ao, final JiraPluginService arPlugin) {
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
            backButtonValue = "/secure/JiraAssetsRegisterFieldManagementWebAction!default.jspa";
        }

        String[] deletes = request.getParameterValues("deleteAll");
        String[] acceptChekboxs = request.getParameterValues("acceptChekbox");
        String[] editFields = request.getParameterValues("editField");
        String[] editErrors = request.getParameterValues("editError");
        String[] fieldNames = request.getParameterValues("fieldName");
        if (isValue(deletes) && deletes[0].equals("true")) {
            if (isValue(acceptChekboxs) && acceptChekboxs[0].equals("accept")) {
                List<String> deleteAllEntity = arService.deleteAllEntity();
                for (String issueKey : deleteAllEntity) {
                    arPlugin.addAssetDeletionCommentToIssue(issueKey);
                }
                deleteAllSuccess = true;
                // setReturnUrl("/secure/JiraAssetsRegisterFieldManagementWebAction!default.jspa");
                // return getRedirect(NONE);
            } else {
                deleteAllError = true;
            }

        } else if (isValue(editFields) && editFields[0].equals("true") && isValue(fieldNames)) {
            editFieldName = fieldNames[0];
            edit = true;
            if (isValue(editErrors) && editErrors[0].equals("true")) {
                editError = true;
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
            backButtonValue = "/secure/JiraAssetsRegisterFieldManagementWebAction!default.jspa";
        }

        String[] requiredField = request.getParameterValues("newRequiredField");
        String[] optionalField = request.getParameterValues("newOptionalField");
        String[] trashes = request.getParameterValues("trashes[]");
        String[] actives = request.getParameterValues("actives[]");
        String[] editFields = request.getParameterValues("editField");
        String[] fieldNames = request.getParameterValues("fieldName");
        String[] newFieldNames = request.getParameterValues("newfieldName");
        if (isValue(requiredField)) {
            Long fieldId = arService.addRequiredField(requiredField[0]);
            if (fieldId == null) {
                createError = true;
            } else {
                createSuccess = true;
            }
        } else if (isValue(optionalField)) {
            Long fieldId = arService.addOptionalField(optionalField[0]);
            if (fieldId == null) {
                createError = true;
            } else {
                createSuccess = true;
            }
        } else if (isValue(trashes)) {
            if (arService.deleteField(trashes[0])) {
                deleteSuccess = true;
            } else {
                deleteError = true;
            }
        } else if (isValue(actives)) {
            if (arService.changeFieldActivate(actives[0])) {
                activeSuccess = true;
            } else {
                activeError = true;
            }
        } else if (!isValue(requiredField)) {
            emptyError = true;
        } else if (!isValue(optionalField)) {
            emptyError = true;
        }
        if (isValue(editFields) && editFields[0].equals("true") && isValue(fieldNames) && isValue(newFieldNames)) {
            if (arService.modifyFieldName(fieldNames[0], newFieldNames[0])) {
                editSuccess = true;
            } else {
                editError = true;
                setReturnUrl("/secure/JiraAssetsRegisterFieldManagementWebAction!default.jspa?editField=true&fieldName="
                        + fieldNames[0] + "&editError=true");
                return getRedirect(NONE);
            }
        }
        return super.doExecute();
    }

    public JiraPluginService getArPlugin() {
        return arPlugin;
    }

    public AssetsRegistryService getArService() {
        return arService;
    }

    public String getBackButtonValue() {
        return backButtonValue;
    }

    public String getEditFieldName() {
        return editFieldName;
    }

    public AssetField getField() {
        return field;
    }

    public boolean isActiveError() {
        return activeError;
    }

    public boolean isActiveSuccess() {
        return activeSuccess;
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

    public boolean isDeleteAllError() {
        return deleteAllError;
    }

    public boolean isDeleteAllSuccess() {
        return deleteAllSuccess;
    }

    public boolean isDeleteError() {
        return deleteError;
    }

    public boolean isDeleteSuccess() {
        return deleteSuccess;
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

    public boolean isEmptyError() {
        return emptyError;
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

    public void setEmptyError(final boolean emptyError) {
        this.emptyError = emptyError;
    }

    public void setField(final AssetField field) {
        this.field = field;
    }

}
