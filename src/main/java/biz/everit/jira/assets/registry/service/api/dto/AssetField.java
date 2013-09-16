package biz.everit.jira.assets.registry.service.api.dto;

import biz.everit.jira.assets.registry.ao.entity.AssetFieldEntity;

/**
 * Data model for field from the assets registry. Representation the {@link AssetFieldEntity}.
 */
public class AssetField {

    /**
     * The id of the field.
     */
    public Long id;

    /**
     * The name of the field.
     */
    private String fieldName;

    /**
     * Required field.
     */
    private boolean required;

    /**
     * Active field.
     */
    private boolean active;

    /**
     * Default constructor.
     */
    public AssetField() {
        super();
    }

    /**
     * Simple constructor.
     * 
     * @param id
     *            the id of the field.
     * @param fieldName
     *            the name of the field.
     * @param required
     *            the field is required
     * @param active
     *            the field is active.
     */
    public AssetField(final Long id, final String fieldName, final boolean required, final boolean active) {
        super();
        this.id = id;
        this.fieldName = fieldName;
        this.required = required;
        this.active = active;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Long getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isRequired() {
        return required;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }
}
