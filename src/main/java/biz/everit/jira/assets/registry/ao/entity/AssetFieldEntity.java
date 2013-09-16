package biz.everit.jira.assets.registry.ao.entity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;
import net.java.ao.schema.Table;
import net.java.ao.schema.Unique;

/**
 * Representing the one field.
 */
@Preload
@Table("afe_001")
public interface AssetFieldEntity extends Entity {

    @OneToMany
    public AssetEntity[] getAssetRegistrys();

    /**
     * The field entity id. Not the default id!
     * 
     * @return the id of the field entity.
     */
    public Long getFieldId();

    /**
     * The name of the field.
     * 
     * @return the name of the field.
     */
    @Unique
    public String getFieldName();

    /**
     * The active the field or not. Showing the field when add new asset or not.
     * 
     * @return the active or not.
     */
    public boolean isActive();

    /**
     * The field is required or not.
     * 
     * @return the required or not.
     */
    public boolean isRequired();

    /**
     * Set the active variable. <code>True</code> if active and <code>false</code> is inactive.
     * 
     * @param active
     *            active or not.
     */
    public void setActive(boolean active);

    /**
     * Set the field id variable. Not the default id!
     * 
     * @param fieldId
     *            the if of the field.
     */
    public void setFieldId(Long fieldId);

    /**
     * Set the field name variable.
     * 
     * @param fieldName
     *            the name of the field.
     */
    public void setFieldName(String fieldName);

    /**
     * Set the required variable. <code>True</code> if required and <code>false</code> is optional.
     * 
     * @param required
     *            required or not.
     */
    public void setRequired(boolean required);
}
