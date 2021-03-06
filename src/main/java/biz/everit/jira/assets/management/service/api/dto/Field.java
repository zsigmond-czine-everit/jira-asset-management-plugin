package biz.everit.jira.assets.management.service.api.dto;

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

    @Override
    public boolean equals(final Object obj) {
        boolean result = false;
        if ((obj != null) && (obj instanceof AssetField)) {
            Field field = (Field) obj;
            if (field.getFieldName().equals(fieldName) && field.getFieldValue().equals(fieldValue)) {
                result = true;
            }
        }
        return result;
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
