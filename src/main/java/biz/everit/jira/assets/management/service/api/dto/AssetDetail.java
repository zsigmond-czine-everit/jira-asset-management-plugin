package biz.everit.jira.assets.management.service.api.dto;

import java.util.HashMap;
import java.util.Map;

import biz.everit.jira.assets.management.ao.entity.AssetEntity;

/**
 * Data model for the asset details. Representation the {@link AssetEntity}.
 */
public class AssetDetail {

    /**
     * The issue key for the asset.
     */
    private String issueKey;

    /**
     * Contains field name and field value. Representation the asset fields for example Device name or assignee.
     */
    private Map<String, String> fields;

    /**
     * Default constructor.
     */
    public AssetDetail() {
        super();
    }

    /**
     * Simple constructor.
     * 
     * @param issueKey
     *            the issue key.
     * @param fields
     *            the fields Map<String, String> where key the field name and the value the field value.
     */
    public AssetDetail(final String issueKey, final Map<String, String> fields) {
        super();
        this.issueKey = issueKey;
        this.fields = fields;
    }

    @Override
    public boolean equals(final Object obj) {
        boolean result = false;
        if ((obj != null) && (obj instanceof AssetDetail)) {
            AssetDetail ad = (AssetDetail) obj;
            Map<String, String> objFields = ad.getFields();
            if (ad.getIssueKey().equals(issueKey) && (objFields.size() == fields.size())) {
                int countSame = 0;
                for (String key : objFields.keySet()) {
                    if (objFields.get(key).equals(fields.get(key))) {
                        countSame++;
                    }
                }
                if (countSame == objFields.size()) {
                    result = true;
                }
            }
        }
        return result;
    }

    public Map<String, String> getFields() {
        if (fields == null) {
            fields = new HashMap<String, String>();
        }
        return fields;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public void setFields(final Map<String, String> fields) {
        this.fields = fields;
    }

    public void setIssueKey(final String issueKey) {
        this.issueKey = issueKey;
    }

}
