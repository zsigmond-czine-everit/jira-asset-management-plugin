package biz.everit.jira.assets.management.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import biz.everit.jira.assets.management.service.api.AssetsRegistryService;
import biz.everit.jira.assets.management.service.api.JiraPluginService;
import biz.everit.jira.assets.management.service.api.dto.Asset;
import biz.everit.jira.assets.management.service.api.dto.AssetDetail;
import biz.everit.jira.assets.management.service.api.dto.Field;
import biz.everit.jira.assets.management.service.api.enums.WorkflowStatuses;
import biz.everit.jira.assets.management.utils.AssetRegistryConstantsUtil;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueInputParametersImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryItem;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.ProjectContext;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.usercompatibility.UserCompatibilityHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;

/**
 * The implementation of the {@link JiraPluginService}.
 */
public class JiraPluginServiceImpl implements JiraPluginService {

    /**
     * The {@link AssetsRegistryService} instance.
     */
    private AssetsRegistryService arService;

    /**
     * Simple constructor.
     * 
     * @param ao
     *            the {@link ActiveObjects}.
     */
    public JiraPluginServiceImpl(final ActiveObjects ao) {
        super();
        arService = new AssetsRegistryServiceImpl(ao);
    }

    public void addAssetDeletionCommentToIssue(final String issueKey) {
        if ((issueKey == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
        User loggedUser = getLoggedUser();
        if ((issue != null) && (loggedUser != null)) {
            IssueService issueService = ComponentManager.getInstance().getIssueService();
            StringBuilder sb = new StringBuilder();
            sb.append("*Action name:* Delete asset (Delete asset in the database)");
            sb.append("\n");
            sb.append("*Old assignee display name:* " + issue.getAssigneeUser().getDisplayName());
            sb.append("\n");
            sb.append("*New assignee display name:* " + issue.getAssigneeUser().getDisplayName());
            sb.append("\n");
            sb.append("*Delete the asset:* " + loggedUser.getDisplayName());
            sb.append("\n");
            sb.append("*User comments:*\n ");
            sb.append("\n");
            IssueInputParameters parameters = new IssueInputParametersImpl();
            parameters.setComment(sb.toString());
            UpdateValidationResult validateUpdate = issueService.validateUpdate(loggedUser, issue.getId(),
                    parameters);
            if (validateUpdate.isValid()) {
                issueService.update(loggedUser, validateUpdate);
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void afterPropertiesSet() throws Exception {
        /*
         * Set the user who install the plugin, if not find set the first JIRA administrator user.
         */
        JiraAuthenticationContext authenticationContext = ComponentManager
                .getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        if (user == null) {
            Collection<User> jiraAdministrators = ComponentAccessor.getUserUtil().getJiraAdministrators();
            for (User u : jiraAdministrators) {
                user = u;
                break;
            }
        }

        /*
         * Create issue type if not exist.
         */
        IssueType existIssueType = existIssueType(AssetRegistryConstantsUtil.ISSUE_TYPE_NAME);
        if (existIssueType == null) {
            ComponentAccessor.getConstantsManager()
                    .createIssueType(AssetRegistryConstantsUtil.ISSUE_TYPE_NAME, null, null,
                            AssetRegistryConstantsUtil.ISSUE_TYPE_DESCRIPTION,
                            AssetRegistryConstantsUtil.ISSUE_TYPE_IMAGE_PATH);
            existIssueType = existIssueType(AssetRegistryConstantsUtil.ISSUE_TYPE_NAME);
        }

        /*
         * Create issue type scheme if not exist.
         */
        FieldConfigScheme existIssueTypeSceme = existIssueTypeSceme(AssetRegistryConstantsUtil.ISSUE_TYPE_SCHEME_NAME);
        if (existIssueTypeSceme == null) {
            List<String> options = new ArrayList<String>();
            Object assetIssueTypeId = existIssueType.getGenericValue().getAllFields().get("id");
            options.add(assetIssueTypeId.toString());
            existIssueTypeSceme = ComponentAccessor.getIssueTypeSchemeManager()
                    .create(AssetRegistryConstantsUtil.ISSUE_TYPE_SCHEME_NAME,
                            AssetRegistryConstantsUtil.ISSUE_TYPE_SCHEME_DESCRIPTION, options);
        }

        /*
         * Create external assigned status if not exits.
         */
        Status existStatusExternalAssigned = existStatus(AssetRegistryConstantsUtil.STATUS_EXTERNAL_ASSIGNED_NAME);
        if (existStatusExternalAssigned == null) {
            existStatusExternalAssigned = createStatus(AssetRegistryConstantsUtil.STATUS_EXTERNAL_ASSIGNED_NAME,
                    AssetRegistryConstantsUtil.STATUS_EXTERNAL_ASSIGNED_DESCRIPTION);
        }
        /*
         * Create internal assigned status if not exits.
         */
        Status existStatusInternalAssigned = existStatus(AssetRegistryConstantsUtil.STATUS_INTERNAL_ASSIGNED_NAME);
        if (existStatusInternalAssigned == null) {
            existStatusInternalAssigned = createStatus(AssetRegistryConstantsUtil.STATUS_INTERNAL_ASSIGNED_NAME,
                    AssetRegistryConstantsUtil.STATUS_INTERNAL_ASSIGNED_DESCRIPTION);
        }

        /*
         * Create workflow if not exist.
         */
        JiraWorkflow existWorkflow = existWorkflow(AssetRegistryConstantsUtil.WORKFLOW_NAME);
        if (existWorkflow == null) {
            /*
             * Import the workflow.
             */
            BundleContext bc = FrameworkUtil.getBundle(JiraPluginServiceImpl.class).getBundleContext();
            Bundle bundle = bc.getBundle();
            URL resource = bundle.getResource(AssetRegistryConstantsUtil.WORKFLOW_RESOURCE_PATH);
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.openConnection().getInputStream()));
            String xml = "";
            while (br.ready()) {
                String tmp = br.readLine();
                if (tmp.contains("<meta name=\"jira.status.id\">CHANGE_STATUS_INTERNAL_ASSIGNED_ID</meta>")) {
                    tmp = "<meta name=\"jira.status.id\">" + existStatusInternalAssigned.getId()
                            + "</meta>";
                } else if (tmp.contains("<meta name=\"jira.status.id\">CHANGE_STATUS_EXTERNAL_ASSIGNED_ID</meta>")) {
                    tmp = "<meta name=\"jira.status.id\">" + existStatusExternalAssigned.getId()
                            + "</meta>";
                }
                xml += tmp;
            }
            br.close();
            WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
            WorkflowDescriptor workflowDescriptor = WorkflowLoader.load(
                    new ByteArrayInputStream(xml.getBytes("UTF-8")),
                    true);
            ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(
                    AssetRegistryConstantsUtil.WORKFLOW_NAME,
                    workflowDescriptor,
                    workflowManager);
            newWorkflow.setDescription(AssetRegistryConstantsUtil.WORKFLOW_DESCRIPTION);
            workflowManager.createWorkflow(user, newWorkflow);
            existWorkflow = existWorkflow(AssetRegistryConstantsUtil.WORKFLOW_NAME);
        }

        /*
         * Create workflow scheme and connect workflow, if not exist the workflow scheme.
         */
        Scheme existWorkflowScheme = existWorkflowScheme(AssetRegistryConstantsUtil.WORKFLOW_SCHEME_NAME);
        if (existWorkflowScheme == null) {
            GenericValue createScheme = ComponentAccessor.getWorkflowSchemeManager().createScheme(
                    AssetRegistryConstantsUtil.WORKFLOW_SCHEME_NAME,
                    AssetRegistryConstantsUtil.WORKFLOW_SCHEME_DESCRIPTION);
            ComponentAccessor.getWorkflowSchemeManager().addWorkflowToScheme(createScheme,
                    AssetRegistryConstantsUtil.WORKFLOW_NAME,
                    existIssueType.getId());
            existWorkflowScheme = existWorkflowScheme(AssetRegistryConstantsUtil.WORKFLOW_SCHEME_NAME);
        }

        /*
         * Create project if not exist. Add issue type scheme, workflow, screen scheme to the project, only if not exist
         * the project.
         */
        Project existProject = existProject(AssetRegistryConstantsUtil.PROJECT_NAME,
                AssetRegistryConstantsUtil.PROJECT_KEY);
        if (existProject == null) {
            existProject = ComponentAccessor.getProjectManager().createProject(AssetRegistryConstantsUtil.PROJECT_NAME,
                    AssetRegistryConstantsUtil.PROJECT_KEY, AssetRegistryConstantsUtil.PROJECT_DESCRIPTION,
                    user.getName(), "", AssigneeTypes.PROJECT_LEAD);
            IssueTypeScreenSchemeManager component = ComponentAccessor.getComponent(IssueTypeScreenSchemeManager.class);
            IssueTypeScreenScheme defaultScheme = component.getDefaultScheme();
            component.addSchemeAssociation(existProject.getGenericValue(), defaultScheme);
            ComponentAccessor.getPermissionSchemeManager().addDefaultSchemeToProject(existProject.getGenericValue());
            GenericValue shcemeGeneric = ComponentAccessor.getWorkflowSchemeManager().getScheme(
                    AssetRegistryConstantsUtil.WORKFLOW_SCHEME_NAME);
            ComponentAccessor.getWorkflowSchemeManager().addSchemeToProject(existProject.getGenericValue(),
                    shcemeGeneric);
            FieldConfigSchemeManager component2 = ComponentAccessor.getComponent(FieldConfigSchemeManager.class);
            ArrayList<JiraContextNode> newContext = new ArrayList<JiraContextNode>();
            newContext.add(new ProjectContext(existProject.getId()));
            component2.updateFieldConfigScheme(existIssueTypeSceme, newContext, existIssueTypeSceme.getField());
        }
    }

    public boolean changeIssueStatus(final String issueKey, final String issueAssignee, final String actionName,
            final String actualUserName, final String comment, final List<Field> fields) {
        if ((issueKey == null) || (issueAssignee == null) || (actionName == null) || (actualUserName == null)
                || (comment == null) || (fields == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        boolean result = false;
        ComponentManager cm = ComponentManager.getInstance();
        User actUser = getUser(actualUserName);
        User loggedUser = getLoggedUser();
        // if (loggedUser.getName().equals(actualUserName)) {
        if ((loggedUser != null) && (actUser != null) && loggedUser.equals(actUser)) {
            // if ((actUser != null) && permissionCheck(Permissions.EDIT_ISSUE, actUser)) {
            if (permissionCheck(Permissions.EDIT_ISSUE, actUser)) {
                User assigneeUser = getUser(issueAssignee);
                MutableIssue issue = cm.getIssueManager().getIssueObject(issueKey);
                if ((assigneeUser != null)
                        && (issue != null)
                        // && ((issue.getAssigneeUser().getName().equals(actualUserName))
                        && ((issue.getAssigneeUser().equals(actUser))
                                // || (issue.getReporterUser().getName().equals(actualUserName))
                                || (issue.getReporterUser().equals(actUser))
                                || (previousAssignedUser(issueKey) != null))) {
                    IssueService issueService = ComponentManager.getInstance().getIssueService();
                    ActionDescriptor action = null;
                    Collection<ActionDescriptor> allActions = cm.getWorkflowManager().getWorkflow(issue)
                            .getAllActions();
                    for (ActionDescriptor ad : allActions) {
                        if (ad.getName().equals(actionName)) {
                            action = ad;
                            break;
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append("*Action name:* " + action.getName());
                    sb.append("\n");
                    sb.append("*Old assignee display name:* " + issue.getAssigneeUser().getDisplayName());
                    sb.append("\n");
                    sb.append("*New assignee display name:* " + assigneeUser.getDisplayName());
                    sb.append("\n");
                    sb.append("*User comments:* \n");
                    sb.append("\n");
                    sb.append(comment);
                    if (!fields.isEmpty()) {
                        sb.append("\n\n");
                        sb.append("*Delivery informations:* \n\n");
                        for (Field f : fields) {
                            sb.append("*" + f.getFieldName() + ":* \t" + f.getFieldValue() + "\n");
                        }
                    }
                    IssueInputParameters parameters = new IssueInputParametersImpl();
                    parameters.setComment(sb.toString());
                    parameters.setAssigneeId(assigneeUser.getName());
                    TransitionValidationResult transitionValidationResult = issueService.validateTransition(actUser,
                            issue.getId(),
                            action.getId(), parameters);

                    if (transitionValidationResult.isValid()) {
                        IssueResult transitionResult = issueService.transition(actUser, transitionValidationResult);
                        if (transitionResult.isValid()) {
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    public void changeIssueSummary(final String issueKey, final String issueSummary) {
        if ((issueKey == null) || (issueSummary == null) || issueSummary.trim().equals("")) {
            throw new IllegalArgumentException(
                    "The parameter(s) not be null! Or the issueSummary parameter is only contains space or empty.");
        }
        MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
        User loggedUser = getLoggedUser();
        if ((issue != null) && (loggedUser != null) && !(issue.getSummary().equals(issueSummary))) {
            IssueService issueService = ComponentManager.getInstance().getIssueService();
            StringBuilder sb = new StringBuilder();
            sb.append("*Action name:* Change the issueSummary");
            sb.append("\n");
            sb.append("*Old assignee display name:* " + issue.getAssigneeUser().getDisplayName());
            sb.append("\n");
            sb.append("*New assignee display name:* " + issue.getAssigneeUser().getDisplayName());
            sb.append("\n");
            sb.append("*Change the issue summary:* " + loggedUser.getDisplayName());
            sb.append("\n");
            sb.append("*Old issue summary:* " + issue.getSummary());
            sb.append("\n");
            sb.append("*New issue summary:* " + issueSummary);
            sb.append("\n");
            sb.append("*User comments:*\n ");
            sb.append("\n");
            IssueInputParameters parameters = new IssueInputParametersImpl();
            parameters.setSummary(issueSummary);
            parameters.setComment(sb.toString());
            UpdateValidationResult validateUpdate = issueService.validateUpdate(loggedUser, issue.getId(),
                    parameters);
            if (validateUpdate.isValid()) {
                issueService.update(loggedUser, validateUpdate);
            }
        }
    }

    public List<Asset> convertIssueAndAssetDetailsToAsset(final List<AssetDetail> assetDetails,
            final Map<String, GenericValue> issues) {
        if ((assetDetails == null) || (issues == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<Asset> assets = new ArrayList<Asset>();
        if (!assetDetails.isEmpty() && !issues.isEmpty()) {
            for (AssetDetail ad : assetDetails) {
                GenericValue issue = issues.get(ad.getIssueKey());
                User loggedUser = getLoggedUser();
                if ((issue != null) && (loggedUser != null)) {
                    Asset asset = new Asset();
                    asset.setAssetDetails(ad);
                    asset.setAssignee(getUser(issue.getString("assignee")));
                    // if (asset.getAssignee().getName().equals(loggedUser.getName())) {
                    if (asset.getAssignee().equals(loggedUser)) {
                        asset.setLoggedUserIsAssignee(true);
                    } else {
                        asset.setLoggedUserIsAssignee(false);
                    }
                    asset.setDeviceName(issue.getString("summary"));
                    asset.setIssueKey(ad.getIssueKey());
                    asset.setReporter(getUser(issue.getString("reporter")));
                    // if (asset.getReporter().getName().equals(loggedUser.getName())) {
                    if (asset.getReporter().equals(loggedUser)) {
                        asset.setLoggedUserIsReporter(true);
                    } else {
                        asset.setLoggedUserIsReporter(false);
                    }
                    asset.setStatus(ComponentManager.getInstance().getConstantsManager()
                            .getStatusObject((String) issue.get("status")).getName());
                    User previousAssignedUser = previousAssignedUser(ad.getIssueKey());
                    // if ((previousAssignedUser != null) &&
                    // previousAssignedUser.getName().equals(loggedUser.getName())) {
                    if ((previousAssignedUser != null) && previousAssignedUser.equals(loggedUser)) {
                        asset.setLoggedUserIsPreviousAssigned(true);
                    } else {
                        asset.setLoggedUserIsPreviousAssigned(false);
                    }
                    assets.add(asset);
                }
            }
        }
        return assets;
    }

    @SuppressWarnings("deprecation")
    public String createIssue(final String summary, final String description) throws CreateException {
        if ((summary == null) || (description == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        ComponentManager cm = ComponentManager.getInstance();
        MutableIssue issueObject = cm.getIssueFactory().getIssue();
        Project project = getProject();
        User loggedUser = getLoggedUser();
        if ((project != null) && (loggedUser != null)) {
            issueObject.setProject(project.getGenericValue());
            issueObject.setIssueType(getIssueType().getGenericValue());
            issueObject.setSummary(summary);
            issueObject.setReporter(loggedUser);
            issueObject.setAssignee(loggedUser);
            issueObject.setPriority(cm.getConstantsManager().getPriorityObject("3").getGenericValue());
            issueObject.setDescription(description);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("issue", issueObject);
            GenericValue issue = cm.getIssueManager().createIssue(loggedUser, params);
            return issue.getString("key");
        }
        return null;
    }

    /**
     * Create status.
     * 
     * @param statusName
     *            the name of the status. Not be <code>null</code>.
     * @param description
     *            the description of the status. Not be <code>null</code>.
     * @return the status object, if not created status return <code>null</code>.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    private Status createStatus(final String statusName, final String description) throws GenericEntityException {
        if ((statusName == null) || (description == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Status result = null;
        String sequence = null;
        String iconurl = AssetRegistryConstantsUtil.STATUS_IMAGE_PATH;
        Map<String, Object> fields = MapBuilder.<String, Object> newBuilder()
                .add("id", EntityUtils.getNextStringId(ConstantsManager.STATUS_CONSTANT_TYPE))
                .add("name", statusName)
                .add("sequence", sequence)
                .add("description", description)
                .add("iconurl", iconurl).toMap();
        EntityUtils.createValue(ConstantsManager.STATUS_CONSTANT_TYPE, fields).getString("id");
        ComponentAccessor.getConstantsManager().refresh();
        result = existStatus(statusName);
        return result;
    }

    /**
     * Checking the issue type is exist or not.
     * 
     * @param issueTypeName
     *            the name of the issue type. Not be <code>null</code>.
     * @return the issue type object if exist otherwise <code>null</code>.
     */
    private IssueType existIssueType(final String issueTypeName) {
        if (issueTypeName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Collection<IssueType> issueTypes = ComponentAccessor.getConstantsManager().getRegularIssueTypeObjects();
        IssueType issueType = null;
        for (IssueType it : issueTypes) {
            if (it.getName().equals(issueTypeName)) {
                issueType = it;
                break;
            }
        }
        return issueType;
    }

    /**
     * Checking the issue type scheme is exist or not.
     * 
     * @param issueTypeScemeName
     *            the name of the issue type scheme. Not be <code>null</code>.
     * @return the issue type schema (FieldConfigScheme) object if exist otherwise <code>null</code>.
     */
    private FieldConfigScheme existIssueTypeSceme(final String issueTypeScemeName) {
        if (issueTypeScemeName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        IssueTypeSchemeManager issueTypeSchemeManager = ComponentAccessor.getIssueTypeSchemeManager();
        List<FieldConfigScheme> allSchemes = issueTypeSchemeManager.getAllSchemes();
        FieldConfigScheme issueTypeScheme = null;
        for (FieldConfigScheme fcs : allSchemes) {
            if (fcs.getName().equals(issueTypeScemeName)) {
                issueTypeScheme = fcs;
                break;
            }
        }
        return issueTypeScheme;
    }

    /**
     * Checking the project is exist or not. Exist project if key or name is already used in the JIRA.
     * 
     * @param projectName
     *            the name of the project. Not be <code>null</code>.
     * @param projectKey
     *            the key of the project. Not be <code>null</code>.
     * @return the project object if exist otherwise <code>null</code>.
     */
    private Project existProject(final String projectName, final String projectKey) {
        if ((projectName == null) || (projectKey == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Project project = null;
        List<Project> projectObjects = ComponentAccessor.getProjectManager().getProjectObjects();
        for (Project p : projectObjects) {
            if (p.getKey().equals(projectKey) || p.getName().equals(projectName)) {
                project = p;
                break;
            }
        }

        return project;
    }

    /**
     * Checking the status is exist or not.
     * 
     * @param statusName
     *            the name of the status. Not be <code>null</code>.
     * @return the status object if exits status otherwise <code>null</code>;
     */
    private Status existStatus(final String statusName) {
        if (statusName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Status status = ComponentAccessor.getConstantsManager().getStatusByName(statusName);
        if (status != null) {
            return status;
        } else {
            return null;
        }
    }

    /**
     * Checking the workflow is exist or not.
     * 
     * @param workflowName
     *            the name of the workflow. Not be <code>null</code>.
     * @return the jiraWorkflow object if exist otherwise <code>null</code>.
     */
    private JiraWorkflow existWorkflow(final String workflowName) {
        if (workflowName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        JiraWorkflow workflow = null;
        Collection<JiraWorkflow> workflows = ComponentAccessor.getWorkflowManager().getWorkflows();
        for (JiraWorkflow jw : workflows) {
            if (jw.getName().equals(workflowName)) {
                workflow = jw;
                break;
            }
        }
        return workflow;
    }

    /**
     * Checking the workflow scheme is exist or not.
     * 
     * @param workflowSchemeName
     *            the name of the workflow scheme. Not be <code>null</code>.
     * @return the workflow scheme (Scheme) object if exist otherwise <code>null</code>.
     */
    private Scheme existWorkflowScheme(final String workflowSchemeName) {
        if (workflowSchemeName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<Scheme> schemeObjects = ComponentAccessor.getWorkflowSchemeManager().getSchemeObjects();
        Scheme scheme = null;
        for (Scheme sch : schemeObjects) {
            if (sch.getName().equals(workflowSchemeName)) {
                scheme = sch;
                break;
            }
        }
        return scheme;
    }

    public List<User> findAllAssigneesInProject() {
        List<User> assignableUser = new ArrayList<User>();
        Collection<User> users = ComponentAccessor.getUserManager().getUsers();
        for (User u : users) {
            if (permissionCheck(Permissions.ASSIGNABLE_USER, u)) {
                assignableUser.add(u);
            }
        }
        return assignableUser;
    }

    public List<GenericValue> findAllIssuesByLoggedUser() throws GenericEntityException {
        List<GenericValue> issueKeys = new ArrayList<GenericValue>();
        IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
        List<GenericValue> issues = issueManager.getIssues(issueManager.getIssueIdsForProject(getProject().getId()));
        User loggedUser = getLoggedUser();
        if (loggedUser != null) {
            for (GenericValue gv : issues) {
                // if (gv.getString("assignee").equals(loggedUser.getName())) {
                User assigneeUser = getUser(gv.getString("assignee"));
                User reporterUser = getUser(gv.getString("reporter"));
                if ((assigneeUser != null) && assigneeUser.equals(loggedUser)) {
                    issueKeys.add(gv);
                    // } else if (gv.getString("reporter").equals(loggedUser.getName())) {
                } else if ((reporterUser != null) && reporterUser.equals(loggedUser)) {
                    issueKeys.add(gv);
                }
            }
        }
        return issueKeys;
    }

    /**
     * Find all issues based on user name.
     * 
     * @param userName
     *            the name of the user. Not be <code>null</code>.
     * @return the all issues where the user is assignee or reportered.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    private List<GenericValue> findAllIssuesByUserName(final String userName) throws GenericEntityException {
        if (userName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<GenericValue> issueKeys = new ArrayList<GenericValue>();
        IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
        List<GenericValue> issues = issueManager.getIssues(issueManager.getIssueIdsForProject(getProject().getId()));
        for (GenericValue gv : issues) {
            User assigneeUser = getUser(gv.getString("assignee"));
            User reporterUser = getUser(gv.getString("reporter"));
            User user = getUser(userName);
            // if (gv.getString("assignee").equals(userName)) {
            if ((assigneeUser != null) && (user != null) && assigneeUser.equals(user)) {
                issueKeys.add(gv);
                // } else if (gv.getString("reporter").equals(userName)) {
            } else if ((reporterUser != null) && (user != null) && reporterUser.equals(user)) {
                issueKeys.add(gv);
            }
        }
        return issueKeys;
    }

    public List<GenericValue> findAllIssuesInProject() throws GenericEntityException {
        IssueManager issueManager = ComponentManager.getInstance().getIssueManager();
        List<GenericValue> issues = issueManager.getIssues(issueManager.getIssueIdsForProject(getProject().getId()));
        if (issues == null) {
            issues = new ArrayList<GenericValue>();
        }
        return issues;
    }

    @SuppressWarnings("deprecation")
    public Map<String, GenericValue> findIssueByIssueKey(final String issueKey) throws GenericEntityException {
        if (issueKey == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        GenericValue issue = ComponentManager.getInstance().getIssueManager().getIssue(issueKey);
        Map<String, GenericValue> result = new HashMap<String, GenericValue>();
        if (issue != null) {
            result.put(issue.getString("key"), issue);
        }
        return result;
    }

    public List<String> findIssueKeysByAssigneName(final String assigneeName) throws GenericEntityException {
        if (assigneeName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<String> issueKeys = new ArrayList<String>();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        Long projectId = ComponentAccessor.getProjectManager()
                .getProjectObjByKey(AssetRegistryConstantsUtil.PROJECT_KEY).getId();
        if (projectId != null) {
            Collection<Long> issueIdsForProject = issueManager.getIssueIdsForProject(projectId);
            for (Long issueId : issueIdsForProject) {
                MutableIssue issue = issueManager.getIssueObject(issueId);
                // String assignedName = issue.getAssigneeUser().getName();
                // if (assignedName.equals(assigneeName)) {
                User assignedUser = issue.getAssigneeUser();
                User user = getUser(assigneeName);
                if ((assignedUser != null) && (user != null) && assignedUser.equals(user)) {
                    issueKeys.add(issue.getKey());
                }
            }
        }
        return issueKeys;
    }

    public List<String> findIssueKeysByComment(final String comment) throws GenericEntityException {
        if (comment == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<String> issueKeys = new ArrayList<String>();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        Long projectId = ComponentAccessor.getProjectManager()
                .getProjectObjByKey(AssetRegistryConstantsUtil.PROJECT_KEY).getId();
        if (projectId != null) {
            CommentManager commentManager = ComponentAccessor.getCommentManager();
            Collection<Long> issueIdsForProject = issueManager.getIssueIdsForProject(projectId);
            for (Long issueId : issueIdsForProject) {
                MutableIssue issue = issueManager.getIssueObject(issueId);
                List<Comment> comments = commentManager.getComments(issue);
                for (Comment c : comments) {
                    if (c.getBody().contains(comment) && !issueKeys.contains(issue.getKey())) {
                        issueKeys.add(issue.getKey());
                        break;
                    }
                }
            }
        }
        return issueKeys;
    }

    public List<String> findIssueKeysByStatusName(final String statusName) throws GenericEntityException {
        if (statusName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<String> issueKeys = new ArrayList<String>();
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        Long projectId = ComponentAccessor.getProjectManager()
                .getProjectObjByKey(AssetRegistryConstantsUtil.PROJECT_KEY).getId();
        if (projectId != null) {
            Collection<Long> issueIdsForProject = issueManager.getIssueIdsForProject(projectId);
            for (Long issueId : issueIdsForProject) {
                MutableIssue issue = issueManager.getIssueObject(issueId);
                String issueStatusName = issue.getStatusObject().getName();
                if (issueStatusName.equals(statusName)) {
                    issueKeys.add(issue.getKey());
                }
            }
        }
        return issueKeys;
    }

    public Map<String, GenericValue> findIssuesByStatusAndAssignee(final WorkflowStatuses status,
            final String assigneeName) throws GenericEntityException {
        if ((assigneeName == null) || (status == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Map<String, GenericValue> result = new HashMap<String, GenericValue>();
        List<GenericValue> workflowStatuses = workflowStatusesInProject();
        GenericValue statusObject = null;
        for (GenericValue ws : workflowStatuses) {
            if (ws.getString("name").equals(status.toString())) {
                statusObject = ws;
                break;
            }
        }
        if (statusObject != null) {
            List<GenericValue> allIssues = findAllIssuesByUserName(assigneeName);
            for (GenericValue gv : allIssues) {
                // if (gv.get("status").equals(statusObject.get("id")) && gv.getString("assignee").equals(assigneeName))
                // {
                User assigneeUser = getUser(gv.getString("assignee"));
                User user = getUser(assigneeName);
                if ((assigneeUser != null) && (user != null) && gv.get("status").equals(statusObject.get("id"))
                        && assigneeUser.equals(user)) {
                    result.put(gv.getString("key"), gv);
                }
            }
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    public Map<String, GenericValue> findIssuesByStatusAndPreviousAssigned(final WorkflowStatuses status,
            final String previousAssignedName) throws GenericEntityException {
        if ((previousAssignedName == null) || (status == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Map<String, GenericValue> result = new HashMap<String, GenericValue>();
        List<GenericValue> workflowStatuses = workflowStatusesInProject();
        GenericValue statusObject = null;
        for (GenericValue ws : workflowStatuses) {
            if (ws.getString("name").equals(status.toString())) {
                statusObject = ws;
                break;
            }
        }
        if (statusObject != null) {
            Collection<Long> issueIdsForProject = ComponentAccessor.getIssueManager().getIssueIdsForProject(
                    ComponentAccessor.getProjectManager().getProjectObjByKey(AssetRegistryConstantsUtil.PROJECT_KEY)
                            .getId());
            for (Long id : issueIdsForProject) {
                GenericValue gv = ComponentAccessor.getIssueManager().getIssue(id);
                User previousAssignedUser = previousAssignedUser(gv.getString("key"));
                if (previousAssignedUser != null) {
                    // boolean previousAssigned = previousAssignedUser.getName().equals(
                    // previousAssignedName);
                    User user = getUser(previousAssignedName);
                    boolean previousAssigned = previousAssignedUser.equals(user);
                    if (gv.get("status").equals(statusObject.get("id")) && previousAssigned) {
                        result.put(gv.getString("key"), gv);
                    }
                }
            }
        }
        return result;
    }

    public Map<String, GenericValue> findIssuesByStatusAndReporter(final WorkflowStatuses status,
            final String reporterName)
            throws GenericEntityException {
        if ((reporterName == null) || (status == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Map<String, GenericValue> result = new HashMap<String, GenericValue>();
        List<GenericValue> workflowStatuses = workflowStatusesInProject();
        GenericValue statusObject = null;
        for (GenericValue ws : workflowStatuses) {
            if (ws.getString("name").equals(status.toString())) {
                statusObject = ws;
                break;
            }
        }
        if (statusObject != null) {
            List<GenericValue> allIssues = findAllIssuesByUserName(reporterName);
            for (GenericValue gv : allIssues) {
                // if (gv.get("status").equals(statusObject.get("id")) && gv.getString("reporter").equals(reporterName))
                // {
                User reporterUser = getUser(gv.getString("reporter"));
                User user = getUser(reporterName);
                if (gv.get("status").equals(statusObject.get("id")) && reporterUser.equals(user)) {
                    result.put(gv.getString("key"), gv);
                }
            }
        }
        return result;
    }

    public User findUserByName(final String userName) {
        if (userName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        return getUser(userName);
    }

    public AssetsRegistryService getArService() {
        return arService;
    }

    /**
     * Find the issue type which using the Asset registry project.
     * 
     * @return the issue type ({@link IssueType}).
     */
    private IssueType getIssueType() {
        IssueType result = null;
        Collection<IssueType> regularIssueTypeObjects = ComponentManager.getInstance().getConstantsManager()
                .getRegularIssueTypeObjects();
        for (IssueType it : regularIssueTypeObjects) {
            if (it.getName().equals(AssetRegistryConstantsUtil.ISSUE_TYPE_NAME)) {
                result = it;
                break;
            }
        }
        return result;
    }

    public User getLoggedUser() {
        JiraAuthenticationContext authenticationContext = ComponentManager
                .getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        return user;
    }

    /**
     * Find the project which using.
     * 
     * @return the project ({@link Project}) object. If not exist the project return <code>null</code>.
     */
    public Project getProject() {
        return ComponentManager.getInstance().getProjectManager()
                .getProjectObjByKey(AssetRegistryConstantsUtil.PROJECT_KEY);
    }

    /**
     * The method return the user based on user name or user key (JIRA 6).
     * 
     * @param userKeyOrName
     *            the name or key of the user.
     * @return the user, if not exist user return <code>null</code>.
     */
    private User getUser(final String userKeyOrName) {
        User user = UserCompatibilityHelper.getUserForKey(userKeyOrName);
        if (user == null) {
            user = ComponentAccessor.getUserManager().getUserObject(userKeyOrName);
        }
        return user;
    }

    public boolean isAdministrator(final User loggedUser) {
        if (loggedUser == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Project project = getProject();
        if (project != null) {
            ProjectRoleManager component = ComponentAccessor.getComponent(ProjectRoleManager.class);
            Collection<ProjectRole> projectRoles = component.getProjectRoles(loggedUser, project);
            for (ProjectRole pr : projectRoles) {
                if (pr.getName().equals("Administrators")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isLoggedUser() {
        boolean result = false;
        JiraAuthenticationContext authenticationContext = ComponentManager
                .getInstance().getJiraAuthenticationContext();
        User user = authenticationContext.getLoggedInUser();
        if (user != null) {
            result = true;
        }
        return result;
    }

    public boolean isValidAction(final String actionName) {
        if (actionName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Collection<ActionDescriptor> allActions = ComponentAccessor.getWorkflowManager()
                .getWorkflow(AssetRegistryConstantsUtil.WORKFLOW_NAME)
                .getAllActions();
        for (ActionDescriptor ad : allActions) {
            if (ad.getName().equals(actionName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidAssignee(final String assigneeName) {
        if (assigneeName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        User user = getUser(assigneeName);
        if (user != null) {
            return true;
        }
        return false;
    }

    public boolean isValidIssueKey(final String issueKey) {
        if (issueKey == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        MutableIssue issue = ComponentManager.getInstance().getIssueManager().getIssueObject(issueKey);
        if (issue != null) {
            return true;
        }
        return false;
    }

    public boolean isValidStatus(final String statusName) {
        if (statusName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Status status = ComponentManager.getInstance().getConstantsManager().getStatusByName(statusName);
        if (status != null) {
            return true;
        }
        return false;
    }

    public boolean permissionCheck(final int permissionId, final User user) {
        if ((user == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<Project> projects = (List<Project>) ComponentAccessor.getPermissionManager().getProjectObjects(
                permissionId,
                user);
        Project project = ComponentAccessor.getProjectManager().getProjectObjByKey(
                AssetRegistryConstantsUtil.PROJECT_KEY);
        if (projects.contains(project)) {
            return true;
        }
        return false;
    }

    public User previousAssignedUser(final String issueKey) {
        MutableIssue issue = ComponentAccessor.getIssueManager().getIssueObject(issueKey);
        User oldUserName = null;
        if (issue != null) {
            List<ChangeHistoryItem> allChangeItems = ComponentAccessor.getChangeHistoryManager().getAllChangeItems(
                    issue);
            if ((allChangeItems != null) && !allChangeItems.isEmpty()) {
                int size = allChangeItems.size() - 1;
                for (int i = size; i >= 0; i--) {
                    ChangeHistoryItem chi = allChangeItems.get(i);
                    if (chi.getField().equals("assignee") && isValidAssignee(chi.getFromValue())) {
                        oldUserName = getUser(chi.getFromValue());
                        break;
                    }
                }
            }
        }
        return oldUserName;
    }

    /**
     * Finds workflow statuses which using the Asset registry workflow.
     * 
     * @return the workflow statuses list which use in the workflow. The statuses is {@link GenericValue} format.
     */
    @SuppressWarnings("deprecation")
    private List<GenericValue> workflowStatusesInProject() {
        List<GenericValue> workflowStatus = new ArrayList<GenericValue>();
        Collection<GenericValue> statuses = ComponentManager.getInstance().getConstantsManager().getStatuses();
        for (GenericValue s : statuses) {
            for (WorkflowStatuses ws : WorkflowStatuses.values()) {
                if (s.getString("name").equals(ws.toString())) {
                    workflowStatus.add(s);
                }
            }
        }
        return workflowStatus;
    }
}
