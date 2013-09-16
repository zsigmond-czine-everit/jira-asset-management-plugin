package biz.everit.jira.assets.registry.ao.entity;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

/**
 * Representing the asset one field with value and issue key.
 */
@Preload
@Table("ae_001")
public interface AssetEntity extends Entity {

    /**
     * The asset entity id. Not the default id!
     * 
     * @return the id of the asset entity.
     */
    public Long getARId();

    /**
     * The asset field entity which connecting the asset entity.
     * 
     * @return the field entity (ID).
     */
    public AssetFieldEntity getField();

    /**
     * The field value which connected the asset.
     * 
     * @return the field value.
     */
    public String getFieldValue();

    /**
     * The key of the issue which connecting the asset.
     * 
     * @return
     */
    public String getIssueKey();

    /**
     * Set the ARId variable. Not the default id!
     * 
     * @param ARid
     *            the id of the asset entity.
     */
    public void setARId(Long ARid);

    /**
     * Set the field entity variable.
     * 
     * @param assetFieldEntity
     *            the {@link AssetFieldEntity} which connecting the asset.
     */
    public void setField(AssetFieldEntity assetFieldEntity);

    /**
     * Set the field value variable.
     * 
     * @param value
     *            the value of the field.
     */
    public void setFieldValue(String value);

    /**
     * Set the issue key variable.
     * 
     * @param issueKey
     *            the key of the issue which connecting the asset.
     */
    public void setIssueKey(String issueKey);
}
