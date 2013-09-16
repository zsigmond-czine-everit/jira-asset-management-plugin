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
import biz.everit.jira.assets.registry.service.api.dto.Field;
import biz.everit.jira.assets.registry.service.api.enums.ButtonActionNames;
import biz.everit.jira.assets.registry.service.api.enums.WorkflowStatuses;
import biz.everit.jira.assets.registry.utils.AssetRegistryConstantsUtil;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.web.action.JiraWebActionSupport;

/**
 * This class is responsible for search page. All screening and operations.
 */
public class JiraAssetsRegisterSearchWebAction extends JiraWebActionSupport {

    /**
     * Generated serial version ID.
     */
    private static final long serialVersionUID = -5660598169299821943L;

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
     * The usage assignee field name.
     * 
     * @return the name of the assignee field.
     */
    public static String getFIELD_NAME_ASSIGNEE() {
        return AssetRegistryConstantsUtil.FIELD_NAME_ASSIGNEE;
    }

    /**
     * The usage workflow closed status name.
     * 
     * @return the name of the closed status.
     */
    public static String getStatusClosedName() {
        return AssetRegistryConstantsUtil.STATUS_CLOSED_NAME;
    }

    /**
     * The usage workflow external assignee status name.
     * 
     * @return the name of the external assignee status.
     */
    public static String getStatusExternalAssignedName() {
        return AssetRegistryConstantsUtil.STATUS_EXTERNAL_ASSIGNED_NAME;
    }

    /**
     * The usage workflow internal assignee status name.
     * 
     * @return the name of the internal assignee status.
     */
    public static String getStatusInternalAssignedName() {
        return AssetRegistryConstantsUtil.STATUS_INTERNAL_ASSIGNED_NAME;
    }

    /**
     * The usage workflow open status name.
     * 
     * @return the name of the open status.
     */
    public static String getStatusOpenName() {
        return AssetRegistryConstantsUtil.STATUS_OPEN_NAME;
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
     * The auxiliary variable when showing assignable user on the site.
     */
    private User assignee;

    /**
     * The page is searched or not.
     */
    private boolean search;

    /**
     * The auxiliary variable when showing choosable status on the site.
     */
    private WorkflowStatuses status;

    /**
     * The found assets.
     */
    private List<Asset> searchedAssets;

    /**
     * The actual asset when showing details on the site.
     */
    private Asset asset;

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
     *            the {@link ActiveObjects}.
     * @param arPlugin
     *            the {@link JiraPluginService}.
     */
    public JiraAssetsRegisterSearchWebAction(final ActiveObjects ao, final JiraPluginService arPlugin) {
        super();
        arService = new AssetsRegistryServiceImpl(ao);
        this.arPlugin = arPlugin;
    }

    /**
     * Find all assignable user.
     * 
     * @return the assignable users, if no one return empty list.
     */
    public List<User> assignees() {
        return arPlugin.findAllAssigneesInProject();
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

        String[] searches = request.getParameterValues("search");
        if (isValue(searches) && searches[0].equals("true")) {
            search = true;
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

        String[] submitSearch = request.getParameterValues("submit_search");
        if (isValue(submitSearch)) {
            String params = "";
            search = true;
            List<String> issueKeys = new ArrayList<String>();
            List<Field> searchFields = new ArrayList<Field>();
            List<AssetField> requiredFields = requiredFields();
            for (AssetField field : requiredFields) {
                String[] fieldValue = request.getParameterValues(field.getFieldName());
                if (isValue(fieldValue)) {
                    if (field.getFieldName().equals(AssetRegistryConstantsUtil.FIELD_NAME_ASSIGNEE)) {
                        issueKeys = arPlugin.findIssueKeysByAssigneName(fieldValue[0]);
                    } else {
                        searchFields.add(new Field(field.getFieldName(), fieldValue[0]));
                    }
                }
            }
            List<AssetField> optionalFields = optionalFields();
            for (AssetField field : optionalFields) {
                String[] fieldValue = request.getParameterValues(field.getFieldName());
                if (isValue(fieldValue)) {
                    searchFields.add(new Field(field.getFieldName(), fieldValue[0]));
                }
            }
            String[] statuses = request.getParameterValues("status01");
            if (isValue(statuses)) {
                params += "%26status01=" + statuses[0].replace(' ', '+');
                String statusName = statuses[0];
                if (arPlugin.isValidStatus(statusName)) {
                    List<String> statusIssueKeys = arPlugin.findIssueKeysByStatusName(statusName);
                    List<String> tmpIssueKeys = new ArrayList<String>();
                    if (issueKeys.isEmpty()) {
                        issueKeys = statusIssueKeys;
                    } else {
                        for (String sik : statusIssueKeys) {
                            if (issueKeys.contains(sik)) {
                                tmpIssueKeys.add(sik);
                            }
                        }
                        issueKeys = tmpIssueKeys;
                    }
                }
            }
            String[] comments = request.getParameterValues("comment01");
            if (isValue(comments)) {
                params += "%26comment01=" + comments[0].replace(' ', '+');
                List<String> commentIssueKeys = arPlugin.findIssueKeysByComment(comments[0]);
                List<String> tmpIssueKeys = new ArrayList<String>();
                if (issueKeys.isEmpty()) {
                    issueKeys = commentIssueKeys;
                } else {
                    for (String sik : commentIssueKeys) {
                        if (issueKeys.contains(sik)) {
                            tmpIssueKeys.add(sik);
                        }
                    }
                    issueKeys = tmpIssueKeys;
                }
            }
            Map<String, GenericValue> iks = new HashMap<String, GenericValue>();
            List<AssetDetail> ads = new ArrayList<AssetDetail>();
            if (!searchFields.isEmpty()) {
                ads = arService.findAssetDetailsByFields(searchFields, issueKeys);
            } else if (issueKeys.isEmpty()) {
                List<GenericValue> allIssues = arPlugin.findAllIssuesInProject();
                List<String> allIssueKeys = new ArrayList<String>();
                for (GenericValue gv : allIssues) {
                    allIssueKeys.add(gv.getString("key"));
                }
                ads = arService.findAssetDetailsByIssueKeys(allIssueKeys);
            } else {
                ads = arService.findAssetDetailsByIssueKeys(issueKeys);
            }
            for (AssetDetail ad : ads) {
                iks.putAll(arPlugin.findIssueByIssueKey(ad.getIssueKey()));
            }

            for (Field f : searchFields) {
                params += "%26";
                params += f.getFieldName().replace(' ', '+');
                params += "=";
                params += f.getFieldValue().replace(' ', '+');
            }
            backButtonValue = "/secure/JiraAssetsRegisterSearchWebAction.jspa?submit_search=Search" + params;
            searchedAssets = arPlugin.convertIssueAndAssetDetailsToAsset(ads, iks);
            return super.doExecute();
        } else {
            setReturnUrl("JiraAssetsRegisterSearchWebAction!default.jspa");
        }
        return getRedirect(NONE);
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

    public List<Asset> getSearchedAssets() {
        return searchedAssets;
    }

    public WorkflowStatuses getStatus() {
        return status;
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

    public boolean isSearch() {
        return search;
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

    public void setAssignee(final User assignee) {
        this.assignee = assignee;
    }

    public void setStatus(final WorkflowStatuses status) {
        this.status = status;
    }

    /**
     * Find all choosable statuses in the asset registry plugin.
     * 
     * @return the all choosable statuses
     */
    public List<WorkflowStatuses> statuses() {
        List<WorkflowStatuses> statuses = new ArrayList<WorkflowStatuses>();
        for (WorkflowStatuses ws : WorkflowStatuses.values()) {
            statuses.add(ws);
        }
        return statuses;
    }

}
