package biz.everit.jira.assets.registry.service.api.enums;

/**
 * The fields name of delivery information in the assets registry plugin.
 */
public enum DeliveryInformationFields {

    /**
     * The recipient field name.
     */
    RECIPIENT_NAME("Recipient name"),

    /**
     * The company field name.
     */
    COMPANY("Comapany"),

    /**
     * The address field name.
     */
    ADDRESS("Address"),

    /**
     * The tax number field name.
     */
    TAX_NUMBER("Tax number");

    /**
     * The name of the field.
     */
    private final String fieldName;

    /**
     * Simple constructor.
     * 
     * @param statusName
     *            the name of the status.
     */
    private DeliveryInformationFields(final String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
