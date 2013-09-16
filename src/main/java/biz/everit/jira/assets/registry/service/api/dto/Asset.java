package biz.everit.jira.assets.registry.service.api.dto;

import com.atlassian.crowd.embedded.api.User;

/**
 * Data model for asset which contains all basic information. Basic information the issue and the asset details.
 */
public class Asset {
    /**
     * The issue key the asset.
     */
    private String issueKey;

    /**
     * The name of the asset.
     */
    private String deviceName;

    /**
     * The assigned user.
     */
    private User assignee;

    /**
     * The reportered user.
     */
    private User reporter;

    /**
     * The issue status name.
     */
    private String status;

    /**
     * The logged user is reportered.
     */
    private boolean loggedUserIsReporter;

    /**
     * The logged user is assigned.
     */
    private boolean loggedUserIsAssignee;

    /**
     * The logged user is the previous assigned.
     */
    private boolean loggedUserIsPreviousAssigned;

    /**
     * The {@link AssetDetail}.
     */
    private AssetDetail assetDetails;

    /**
     * Default constructor.
     */
    public Asset() {
        assetDetails = new AssetDetail();
    }

    public AssetDetail getAssetDetails() {
        return assetDetails;
    }

    public User getAssignee() {
        return assignee;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public User getReporter() {
        return reporter;
    }

    public String getStatus() {
        return status;
    }

    public boolean isLoggedUserIsAssignee() {
        return loggedUserIsAssignee;
    }

    public boolean isLoggedUserIsPreviousAssigned() {
        return loggedUserIsPreviousAssigned;
    }

    public boolean isLoggedUserIsReporter() {
        return loggedUserIsReporter;
    }

    public void setAssetDetails(final AssetDetail assetDetails) {
        this.assetDetails = assetDetails;
    }

    public void setAssignee(final User assignee) {
        this.assignee = assignee;
    }

    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }

    public void setIssueKey(final String issueKey) {
        this.issueKey = issueKey;
    }

    public void setLoggedUserIsAssignee(final boolean loggedUserIsAssignee) {
        this.loggedUserIsAssignee = loggedUserIsAssignee;
    }

    public void setLoggedUserIsPreviousAssigned(final boolean loggedUserIsPreviousAssigned) {
        this.loggedUserIsPreviousAssigned = loggedUserIsPreviousAssigned;
    }

    public void setLoggedUserIsReporter(final boolean loggedUserIsReporter) {
        this.loggedUserIsReporter = loggedUserIsReporter;
    }

    public void setReporter(final User reporter) {
        this.reporter = reporter;
    }

    public void setStatus(final String status) {
        this.status = status;
    }
}
