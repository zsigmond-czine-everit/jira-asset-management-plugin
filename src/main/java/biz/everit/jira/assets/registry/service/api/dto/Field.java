package biz.everit.jira.assets.registry.service.api.dto;

/**
 * Data model for the field. Only the name and value of the field.
 */
public class Field {

    /**
     * The name of the field.
     */
    private String fieldName;

    /**
     * The value of the field.
     */
    private String fieldValue;

    /**
     * Simple constructor.
     * 
     * @param fieldName
     *            the name of the field.
     * @param fieldValue
     *            the value of the field.
     */
    public Field(final String fieldName, final String fieldValue) {
        super();
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldName(final String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFieldValue(final String fieldValue) {
        this.fieldValue = fieldValue;
    }

}
