package biz.everit.jira.assets.management.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericValue;

import biz.everit.jira.assets.management.service.AssetsRegistryServiceImpl;
import biz.everit.jira.assets.management.service.api.AssetsRegistryService;
import biz.everit.jira.assets.management.service.api.JiraPluginService;
import biz.everit.jira.assets.management.service.api.dto.Asset;
import biz.everit.jira.assets.management.service.api.dto.AssetDetail;
import biz.everit.jira.assets.management.service.api.dto.AssetField;
import biz.everit.jira.assets.management.service.api.dto.Field;
import biz.everit.jira.assets.management.service.api.enums.WorkflowStatuses;
import biz.everit.jira.assets.management.utils.ConstantHelper;

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

    private ConstantHelper helper = ConstantHelper.INSTANCE;

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
        List<Field> searchFields = new ArrayList<Field>();
        if (isValue(submitSearch)) {
            String params = "";
            boolean searchInProgress = false;
            search = true;
            List<String> issueKeys = new ArrayList<String>();
            List<AssetField> requiredFields = requiredFields();
            Field assigneeField = null;
            for (AssetField field : requiredFields) {
                String[] fieldValue = request.getParameterValues(field.getFieldName());
                if (isValue(fieldValue)) {
                    if (field.getFieldName().equals(ConstantHelper.FIELD_NAME_ASSIGNEE)) {
                        issueKeys = arPlugin.findIssueKeysByAssigneName(fieldValue[0]);
                        assigneeField = new Field(ConstantHelper.getFieldNameAssignee(),
                                fieldValue[0]);
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
                    searchFields.add(new Field("status01", statuses[0]));
                    List<String> statusIssueKeys = arPlugin.findIssueKeysByStatusName(statusName);
                    List<String> tmpIssueKeys = new ArrayList<String>();
                    searchInProgress = true;
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
                searchFields.add(new Field("comment01", comments[0]));
                params += "%26comment01=" + comments[0].replace(' ', '+');
                List<String> commentIssueKeys = arPlugin.findIssueKeysByComment(comments[0]);
                List<String> tmpIssueKeys = new ArrayList<String>();
                searchInProgress = true;
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
            if (!searchFields.isEmpty() && !searchInProgress) {
                ads = arService.findAssetDetailsByFields(searchFields, issueKeys);
            } else if (issueKeys.isEmpty() && !searchInProgress) {
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
            if (assigneeField != null) {
                searchFields.add(assigneeField);
            }
            filledFields = searchFields;
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

    public Field getFilledField() {
        return filledField;
    }

    public List<Field> getFilledFields() {
        return filledFields;
    }

    public final ConstantHelper getHelper() {
        return helper;
    }

    public String getIdNumber() {
        int tmp = idNumber;
        idNumber += 1;
        return Integer.toString(tmp);
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

    public void setStatus(final WorkflowStatuses status) {
        this.status = status;
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

    /**
     * When rewrite the fields values, we must the unique id. The method generate the unique id. The id must be
     * "key_NUMBER_tr" format.
     * 
     * @return the unique id.
     */
    public String trId() {
        return "key_" + idNumber + "_tr";
    }

}
