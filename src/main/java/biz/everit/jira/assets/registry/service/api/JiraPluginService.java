package biz.everit.jira.assets.registry.service.api;

import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.springframework.beans.factory.InitializingBean;

import biz.everit.jira.assets.registry.service.api.dto.Asset;
import biz.everit.jira.assets.registry.service.api.dto.AssetDetail;
import biz.everit.jira.assets.registry.service.api.dto.Field;
import biz.everit.jira.assets.registry.service.api.enums.DeliveryInformationFields;
import biz.everit.jira.assets.registry.service.api.enums.WorkflowStatuses;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.Permissions;

/**
 * The JIRA plugin service and component interface. The JiraPluginService interface is connection the JiraServices and
 * defines the issue and workflow operations.
 */
public interface JiraPluginService extends InitializingBean {

    /**
     * Add deletion comment the issue.
     * 
     * @param issueKey
     *            the key of the issue. Not be <code>null</code>.
     */
    public void addAssetDeletionCommentToIssue(String issueKey);

    /**
     * Changing the issue status.
     * 
     * @param issueKey
     *            the key of the issue. Not be <code>null</code>.
     * @param issueAssignee
     *            the new assigned user. Not be <code>null</code>.
     * @param actionName
     *            the name of the action. The action name is defined the workflow. Only use the unmodified workflow! Not
     *            be <code>null</code>.
     * @param actualUserName
     *            the name of the user who actual logged in the JIRA. Not be <code>null</code>.
     * @param comment
     *            the comment that the logged user write. Not be <code>null</code>.
     * @param fields
     *            the list which contains the delivery informations. (See more {@link DeliveryInformationFields}). Not
     *            be <code>null</code>.
     * @return <code>true</code> if successful modification the issue, otherwise <code>false</code>.
     */
    boolean changeIssueStatus(final String issueKey, final String issueAssignee, final String actionName,
            final String actualUserName, final String comment, final List<Field> fields);

    /**
     * Convert correctly the JIRA issues and asset details ({@link AssetDetail}) object to assets ({@link Asset}).
     * 
     * @param assetDetails
     *            the list of the {@link AssetDetail}. Not be <code>null</code>.
     * @param issues
     *            the issues map where the key the key of the issue and the value the issue. The issues must be
     *            {@link GenericValue} format. Not be <code>null</code>.
     * @return the correct assets list. Someone parameter is empty return the empty list.
     */
    List<Asset> convertIssueAndAssetDetailsToAsset(final List<AssetDetail> assetDetails,
            final Map<String, GenericValue> issues);

    /**
     * Creating the JIRA issue.
     * 
     * @param summary
     *            the summary of the issue. Not be <code>null</code>.
     * @param description
     *            the description of the issue. Not be <code>null</code>.
     * @return the key of the issue. If not created the issue return <code>null</code>.
     * @throws CreateException
     *             {@link CreateException}.
     */
    String createIssue(final String summary, final String description) throws CreateException;

    /**
     * Finds all assignable users who have access the Asset registry project.
     * 
     * @return the list of the assignable users. If no one return empty list.
     */
    List<User> findAllAssigneesInProject();

    /**
     * Finds all issues (in Asset registry project) based on the logged user.
     * 
     * @return the all issues where the logged user is reported or assigned. If no one return empty list.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    List<GenericValue> findAllIssuesByLoggedUser() throws GenericEntityException;

    /**
     * Finds all issues in Asset registry project.
     * 
     * @return the all issues where the logged user is reported or assigned. If no one return empty list.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    List<GenericValue> findAllIssuesInProject() throws GenericEntityException;

    /**
     * Find issue based on issue key.
     * 
     * @param issueKey
     *            the key of the issue. Not be <code>null</code>.
     * @return the map which contains the issue. The key of the map is the issue key and the value is the issue. The
     *         issue is {@link GenericValue} format. If no one return empty list.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    Map<String, GenericValue> findIssueByIssueKey(final String issueKey) throws GenericEntityException;

    /**
     * Find all issues based on assigne name.
     * 
     * @param assigneeName
     *            the name of the assignee. Not be <code>null</code>.
     * @return the list which contains the issueKeys. If no one return empty list.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    List<String> findIssueKeysByAssigneName(final String assigneeName) throws GenericEntityException;

    /**
     * Find all issues based on comment. The comment text should only contain the comments.
     * 
     * @param comment
     *            the comment. Not be <code>null</code>.
     * @return the list which contains the issueKeys. If no one return empty list.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    List<String> findIssueKeysByComment(final String comment) throws GenericEntityException;

    /**
     * Find all issues based on status name.
     * 
     * @param statusName
     *            the name of the status. Not be <code>null</code>.
     * @return the list which contains the issueKeys. If no one return empty list.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    List<String> findIssueKeysByStatusName(final String statusName) throws GenericEntityException;

    /**
     * Finds issues based on status and assignee.
     * 
     * @param status
     *            the status. The statuses is finds the {@link WorkflowStatuses}. Not be <code>null</code>.
     * @param assigneeName
     *            the name of the assignee. Not be <code>null</code>.
     * @return the map which contains the issues. The key of the map is the issue key and the value is the issue. The
     *         issue is {@link GenericValue} format. If no result return empty list.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    Map<String, GenericValue> findIssuesByStatusAndAssignee(final WorkflowStatuses status, final String assigneeName)
            throws GenericEntityException;

    /**
     * Finds issues based on status and previous assigneed.
     * 
     * @param status
     *            the status. The statuses is finds the {@link WorkflowStatuses}. Not be <code>null</code>.
     * @param previousAssignedName
     *            the name of the previous assigned name. Not be <code>null</code>.
     * @return the map which contains the issues. The key of the map is the issue key and the value is the issue. The
     *         issue is {@link GenericValue} format. If no result return empty list.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    Map<String, GenericValue> findIssuesByStatusAndPreviousAssigned(final WorkflowStatuses status,
            final String previousAssignedName)
            throws GenericEntityException;

    /**
     * Finds issues based on status and reportered.
     * 
     * @param status
     *            the status. The statuses is finds the {@link WorkflowStatuses}. Not be <code>null</code>.
     * @param reporterName
     *            the name of the reporter. Not be <code>null</code>.
     * @return the map which contains the issues. The key of the map is the issue key and the value is the issue. The
     *         issue is {@link GenericValue} format. If no result return empty list.
     * @throws GenericEntityException
     *             {@link GenericEntityException}.
     */
    Map<String, GenericValue> findIssuesByStatusAndReporter(final WorkflowStatuses status, final String reporterName)
            throws GenericEntityException;

    /**
     * Find user based on userName.
     * 
     * @param userName
     *            the name of the user. Not be <code>null</code>.
     * @return the user, if not exist user return <code>null</code>.
     */
    User findUserByName(String userName);

    /**
     * Look at the who logged in the JIRA.
     * 
     * @return the logged user. If not logged user return <code>null</code>.
     */
    User getLoggedUser();

    /**
     * Find the project which using.
     * 
     * @return the project ({@link Project}) object. If not exist the project return <code>null</code>.
     */
    Project getProject();

    /**
     * Checking the user is administrator.
     * 
     * @param loggedUser
     *            the logged user. Not be <code>null</code>.
     * @return <code>true</code> if administrator otherwise <code>false</code>.
     */
    public boolean isAdministrator(User loggedUser);

    /**
     * Check the user is logged or not.
     * 
     * @return <code>true</code> if we have logged user else <code>false</code>.
     */
    boolean isLoggedUser();

    /**
     * Check the action is valid in the Asset registry workflow or not.
     * 
     * @param actionName
     *            the name of the action. Not be <code>null</code>.
     * @return <code>true</code> if valid the action, otherwise <code>false</code>.
     */
    boolean isValidAction(final String actionName);

    /**
     * Check the user is valid (exist) in the JIRA or not.
     * 
     * @param assigneeName
     *            the name of the assignee. Not be <code>null</code>.
     * @return <code>true</code> if valid (exist) the assignee, otherwise <code>false</code>.
     */
    boolean isValidAssignee(final String assigneeName);

    /**
     * Check the issue is valid (exist) in the JIRA or not.
     * 
     * @param issueKey
     *            the key of the issue. Not be <code>null</code>.
     * @return <code>true</code> if valid (exist) the issue, otherwise <code>false</code>.
     */
    boolean isValidIssueKey(final String issueKey);

    /**
     * Check the status is valid (exist) in the JIRA or not.
     * 
     * @param statusName
     *            the name of the status. Not be <code>null</code>.
     * @return <code>true</code> if valid (exist) the status, otherwise <code>false</code>.
     */
    boolean isValidStatus(final String statusName);

    /**
     * Check the user has the permission or not.
     * 
     * @param permissionId
     *            the if of the permission ({@link Permissions}). <b>FUTURE: VALIDATING THE PERMISSION ID! ACTUAL NOT
     *            CHECKING.</b>
     * @param user
     *            the user. Not be <code>null</code>.
     * @return <code>true</code> if has permission otherwise <code>false</code>.
     */
    boolean permissionCheck(int permissionId, User user);

    /**
     * Search in history and return the last valid (exist) assigned user in the issue.
     * 
     * @param issueKey
     *            the key of the issue. Not be <code>null</code>.
     * @return the user if the exist one otherwise <code>null</code>.
     */
    User previousAssignedUser(final String issueKey);

}
