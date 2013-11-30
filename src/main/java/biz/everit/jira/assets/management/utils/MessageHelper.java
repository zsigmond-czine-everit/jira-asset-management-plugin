package biz.everit.jira.assets.management.utils;

public final class MessageHelper {

    /**
     * Getting the appropriate properties key which belongs to message code.
     * 
     * @param messageCode
     *            the message code. All message code available in {@link MessageCode}.
     * @return the appropriate properties key. If message code not exits return <code>null</code>.
     */
    public static final String getProperitesKey(final String messageCode) {
        if (messageCode.equals(MessageCode.EMPTY_MESSAGE)) {
            return null;
        }

        String properitesKey = MessageHelper.getPropertiesKeyInMyAssetPage(messageCode);
        if (properitesKey != null) {
            return properitesKey;
        }
        return null;
    }

    /**
     * Getting the appropriate properties key which belongs to message code. Only check my asset messages.
     * 
     * @param messageCode
     *            the message code.
     * @return the appropriate properties key. If message code not exits return <code>null</code>.
     */
    private static final String getPropertiesKeyInMyAssetPage(final String messageCode) {
        String propertiesKey;
        if (messageCode.equals(MessageCode.MY_ASSET_SUCCESS_CHANGE)) {
            propertiesKey = "success.my.asset.change";
        } else if (messageCode.equals(MessageCode.MY_ASSET_ERROR_CHANGE)) {
            propertiesKey = "error.my.asset.change";
        } else if (messageCode.equals(MessageCode.MY_ASSET_ERROR_NOT_CHANGE)) {
            propertiesKey = "error.my.asset.not.changed";
        } else {
            propertiesKey = null;
        }
        return propertiesKey;
    }

}
