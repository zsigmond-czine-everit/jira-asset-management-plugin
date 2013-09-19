package biz.everit.jira.assets.management.service.api.dto;

import biz.everit.jira.assets.management.ao.entity.AssetFieldEntity;

/**
 * Data model for field from the assets registry. Representation the {@link AssetFieldEntity}.
 */
public class AssetField {

    /**
     * The id of the field.
     */
    private Long id;

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

    @Override
    public boolean equals(final Object obj) {
        boolean result = false;
        if ((obj != null) && (obj instanceof AssetField)) {
            AssetField af = (AssetField) obj;
            if (af.getFieldName().equals(fieldName) && Boolean.toString(af.isActive()).equals(Boolean.toString(active))
                    && Boolean.toString(af.isRequired()).equals(Boolean.toString(required))) {
                result = true;
            }
        }
        return result;
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
