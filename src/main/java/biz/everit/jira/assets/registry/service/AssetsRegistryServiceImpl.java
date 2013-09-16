package biz.everit.jira.assets.registry.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.sal.api.transaction.TransactionCallback;

import biz.everit.jira.assets.registry.ao.entity.AssetEntity;
import biz.everit.jira.assets.registry.ao.entity.AssetFieldEntity;
import biz.everit.jira.assets.registry.service.api.AssetsRegistryService;
import biz.everit.jira.assets.registry.service.api.dto.AssetDetail;
import biz.everit.jira.assets.registry.service.api.dto.AssetField;
import biz.everit.jira.assets.registry.service.api.dto.Field;

/**
 * The implementation of the {@link AssetsRegistryService}.
 */
public class AssetsRegistryServiceImpl implements AssetsRegistryService {

    /**
     * The {@link ActiveObjects} instance.
     */
    private ActiveObjects ao;

    /**
     * Simple constructor.
     * 
     * @param ao
     *            the {@link ActiveObjects}.
     */
    public AssetsRegistryServiceImpl(final ActiveObjects ao) {
        super();
        this.ao = ao;
    }

    public boolean addAsset(final AssetDetail asset, final String issueKey) {
        if ((asset == null) || (issueKey == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Boolean result = ao.executeInTransaction(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction() {
                Boolean result = false;
                Map<String, String> fields = asset.getFields();
                if (fields != null) {
                    Set<String> keySet = fields.keySet();
                    for (String fieldKey : keySet) {
                        String value = fields.get(fieldKey);
                        Map<String, Object> attribute = new HashMap<String, Object>();
                        AssetFieldEntity fe = findFieldEntityByFieldName(fieldKey);
                        if (fe != null) {
                            attribute.put("FIELD_ID", fe);
                            final AssetEntity are = ao.create(AssetEntity.class, attribute);
                            are.setARId(new Long(are.getID()));
                            are.setField(fe);
                            are.setFieldValue(value);
                            are.setIssueKey(issueKey);
                            are.save();
                            ao.flushAll();
                            result = true;
                        }
                    }
                }
                return result;
            }
        });
        return result;
    }

    public Long addOptionalField(final String fieldName) {
        if (fieldName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Long fieldId = ao.executeInTransaction(new TransactionCallback<Long>() {
            public Long doInTransaction() {
                Long result = null;
                AssetField findField = findFieldByFieldName(fieldName);
                if (findField == null) {
                    Map<String, Object> attribute = new HashMap<String, Object>();
                    attribute.put("FIELD_NAME", fieldName);
                    final AssetFieldEntity field = ao.create(AssetFieldEntity.class, attribute);
                    field.setActive(true);
                    field.setRequired(false);
                    field.setFieldName(fieldName);
                    field.setFieldId(new Long(field.getID()));
                    field.save();
                    ao.flushAll();
                    result = field.getFieldId();
                }
                return result;
            }
        });
        return fieldId;
    }

    public Long addRequiredField(final String fieldName) {
        if (fieldName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Long fieldId = ao.executeInTransaction(new TransactionCallback<Long>() {
            public Long doInTransaction() {
                Long result = null;
                AssetField findField = findFieldByFieldName(fieldName);
                if (findField == null) {
                    Map<String, Object> attribute = new HashMap<String, Object>();
                    attribute.put("FIELD_NAME", fieldName);
                    final AssetFieldEntity field = ao.create(AssetFieldEntity.class, attribute);
                    field.setActive(true);
                    field.setRequired(true);
                    field.setFieldName(fieldName);
                    field.setFieldId(new Long(field.getID()));
                    field.save();
                    ao.flushAll();
                    result = field.getFieldId();
                }
                return result;
            }
        });
        return fieldId;
    }

    public boolean changeFieldActivate(final String fieldName) {
        if (fieldName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Boolean change = ao.executeInTransaction(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction() {
                boolean result = false;
                AssetFieldEntity fe = findFieldEntityByFieldName(fieldName);
                if (fe != null) {
                    fe.setActive(!fe.isActive());
                    fe.save();
                    ao.flushAll();
                    result = true;
                }
                return result;
            }
        });
        return change;
    }

    /**
     * Checking the field has already used.
     * 
     * @param fe
     *            the {@link AssetFieldEntity}. Not be <code>null</code>.
     * @return <code>true</code> if usage the field, otherwise <code>false</code>.
     */
    private boolean checkUsingField(final AssetFieldEntity fe) {
        if (fe == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        boolean result = false;
        Boolean using = ao.executeInTransaction(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction() {
                boolean result = false;
                AssetEntity[] ares = ao.find(AssetEntity.class,
                        "FIELD_ID = ?",
                        fe);
                if ((ares != null) && (ares.length > 0)) {
                    result = true;
                }
                return result;
            }
        });
        if (using) {
            result = true;
        }
        return result;
    }

    /**
     * Convert {@link AssetFieldEntity} to {@link AssetField}.
     * 
     * @param arfe
     *            the {@link AssetFieldEntity}. Not be <code>null</code>.
     * @return The {@link AssetField}.
     */
    private AssetField convertFieldEntityToFieldDTO(final AssetFieldEntity arfe) {
        if (arfe == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        AssetField arf = new AssetField();
        arf.setId(arfe.getFieldId());
        arf.setFieldName(arfe.getFieldName());
        arf.setRequired(arfe.isRequired());
        arf.setActive(arfe.isActive());
        return arf;
    }

    /**
     * Create {@link AssetEntity} in the database.
     * 
     * @param fieldName
     *            the name of the field. Not be <code>null</code>.
     * @param fieldValue
     *            the value of the field. Not be <code>null</code>.
     * @param issueKey
     *            the key of the issue. Not be <code>null</code>.
     */
    private void createAssetEntity(final String fieldName, final String fieldValue, final String issueKey) {
        if ((fieldName == null) || (fieldValue == null) || (issueKey == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        ao.executeInTransaction(new TransactionCallback<Void>() {
            public Void doInTransaction() {
                Map<String, Object> attribute = new HashMap<String, Object>();
                AssetFieldEntity fe = findFieldEntityByFieldName(fieldName);
                if (fe != null) {
                    attribute.put("FIELD_ID", fe);
                    final AssetEntity are = ao.create(AssetEntity.class, attribute);
                    are.setARId(new Long(are.getID()));
                    are.setField(fe);
                    are.setFieldValue(fieldValue);
                    are.setIssueKey(issueKey);
                    are.save();
                    ao.flushAll();
                }
                return null;
            }
        });
    }

    public List<String> deleteAllEntity() {
        return ao.executeInTransaction(new TransactionCallback<List<String>>() {
            public List<String> doInTransaction() {
                List<String> result = new ArrayList<String>();
                AssetEntity[] aes = ao.find(AssetEntity.class);
                for (AssetEntity ae : aes) {
                    if (!result.contains(ae.getIssueKey())) {
                        result.add(ae.getIssueKey());
                    }
                    ao.delete(ae);
                }
                AssetFieldEntity[] afes = ao.find(AssetFieldEntity.class);
                for (AssetFieldEntity afe : afes) {
                    ao.delete(afe);
                }
                ao.flushAll();
                return result;
            }
        });
    }

    public void deleteAsset(final String issueKey) {
        if (issueKey == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        ao.executeInTransaction(new TransactionCallback<Void>() {
            public Void doInTransaction() {
                AssetEntity[] aes = ao.find(AssetEntity.class, "ISSUE_KEY = ", issueKey);
                for (AssetEntity ae : aes) {
                    ao.delete(ae);
                }
                ao.flushAll();
                return null;
            }
        });
    }

    public boolean deleteField(final String fieldName) {
        if (fieldName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        Boolean delete = ao.executeInTransaction(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction() {
                boolean result = false;
                AssetFieldEntity fe = findFieldEntityByFieldName(fieldName);
                if ((fe != null) && !checkUsingField(fe)) {
                    ao.delete(fe);
                    ao.flushAll();
                    result = true;
                }
                return result;
            }
        });
        return delete;
    }

    public List<AssetField> findAllOptionalField() {
        List<AssetField> resultFields = new ArrayList<AssetField>();
        List<AssetFieldEntity> findFields = ao
                .executeInTransaction(new TransactionCallback<List<AssetFieldEntity>>() {
                    public List<AssetFieldEntity> doInTransaction() {
                        final AssetFieldEntity[] fields = ao.find(AssetFieldEntity.class,
                                "REQUIRED = FALSE");
                        if ((fields != null) && (fields.length > 0)) {
                            return Arrays.asList(fields);
                        } else {
                            return new ArrayList<AssetFieldEntity>();
                        }
                    }
                });
        for (AssetFieldEntity arfe : findFields) {
            AssetField field = convertFieldEntityToFieldDTO(arfe);
            if (field != null) {
                resultFields.add(field);
            }
        }
        return resultFields;
    }

    public List<AssetField> findAllRequiredField() {
        List<AssetField> resultFields = new ArrayList<AssetField>();
        List<AssetFieldEntity> findFields = ao
                .executeInTransaction(new TransactionCallback<List<AssetFieldEntity>>() {
                    public List<AssetFieldEntity> doInTransaction() {
                        final AssetFieldEntity[] fields = ao.find(AssetFieldEntity.class,
                                "REQUIRED = TRUE");
                        if ((fields != null) && (fields.length > 0)) {
                            return Arrays.asList(fields);
                        } else {
                            return new ArrayList<AssetFieldEntity>();
                        }
                    }
                });
        for (AssetFieldEntity arfe : findFields) {
            AssetField field = convertFieldEntityToFieldDTO(arfe);
            if (field != null) {
                resultFields.add(field);
            }
        }
        return resultFields;
    }

    public AssetDetail findAssetDetailByIssueKey(final String issueKey) {
        if (issueKey == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        return ao.executeInTransaction(new TransactionCallback<AssetDetail>() {
            public AssetDetail doInTransaction() {
                AssetEntity[] ares = ao.find(AssetEntity.class,
                        "ISSUE_KEY = ?",
                        issueKey);
                AssetDetail ad = null;
                if ((ares != null) && (ares.length > 0)) {
                    ad = new AssetDetail();
                    ad.setIssueKey(ares[0].getIssueKey());
                    for (AssetEntity are : ares) {
                        ad.getFields().put(are.getField().getFieldName(), are.getFieldValue());
                    }
                    if (ad.getFields().isEmpty()) {
                        ad = null;
                    }
                }
                return ad;
            }
        });
    }

    public List<AssetDetail> findAssetDetailsByField(final Field field) {
        if (field == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<AssetDetail> result = new ArrayList<AssetDetail>();
        if (field.getFieldName() != null) {
            final AssetField findField = findFieldByFieldName(field.getFieldName());
            if (findField != null) {
                result = ao.executeInTransaction(new TransactionCallback<List<AssetDetail>>() {
                    public List<AssetDetail> doInTransaction() {
                        List<AssetDetail> resultTmp = new ArrayList<AssetDetail>();
                        AssetEntity[] ares = ao.find(AssetEntity.class,
                                "FIELD_ID = ? AND LCASE(FIELD_VALUE) LIKE '%" + field.getFieldValue().toLowerCase()
                                        + "%'",
                                findField.getId());
                        if ((ares != null) && (ares.length > 0)) {
                            int aresLength = ares.length;
                            for (int i = 0; i < aresLength; i++) {
                                AssetEntity ae = ares[i];
                                if (ae.getIssueKey() != null) {
                                    AssetDetail ad = findAssetDetailByIssueKey(ae.getIssueKey());
                                    if (ad != null) {
                                        resultTmp.add(ad);
                                    }
                                }
                            }
                        }
                        return resultTmp;
                    }
                });
            }
        }
        return result;
    }

    public List<AssetDetail> findAssetDetailsByFields(final List<Field> fields, final List<String> issueKeys) {
        if ((fields == null) || (issueKeys == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<AssetDetail> result = new ArrayList<AssetDetail>();
        if (!issueKeys.isEmpty()) {
            for (String key : issueKeys) {
                AssetDetail ad = findAssetDetailByIssueKey(key);
                if (ad != null) {
                    result.add(ad);
                }
            }
        }
        boolean empty = true;
        for (Field sf : fields) {
            if (result.isEmpty() && empty) {
                result.addAll(findAssetDetailsByField(sf));
                if (!result.isEmpty()) {
                    empty = false;
                }
            } else if (result.isEmpty() && !empty) {
                break;
            } else {
                empty = false;
                List<AssetDetail> tmp = new ArrayList<AssetDetail>();
                List<AssetDetail> list = findAssetDetailsByField(sf);
                for (AssetDetail ad : list) {
                    if (result.contains(ad) && !tmp.contains(ad)) {
                        tmp.add(ad);
                    }
                }
                result = tmp;
            }
        }
        return result;
    }

    public List<AssetDetail> findAssetDetailsByIssueKeys(final List<String> keys) {
        if (keys == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        List<AssetDetail> result = new ArrayList<AssetDetail>();
        for (final String key : keys) {
            AssetDetail ad = ao.executeInTransaction(new TransactionCallback<AssetDetail>() {
                public AssetDetail doInTransaction() {
                    return findAssetDetailByIssueKey(key);
                }
            });
            if (ad != null) {
                result.add(ad);
            }
        }
        return result;
    }

    public AssetField findFieldByFieldName(final String fieldName) {
        if (fieldName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        AssetField resultField = null;
        AssetFieldEntity findField = ao
                .executeInTransaction(new TransactionCallback<AssetFieldEntity>() {
                    public AssetFieldEntity doInTransaction() {
                        final AssetFieldEntity[] fields = ao.find(AssetFieldEntity.class,
                                "FIELD_NAME = ?",
                                fieldName);
                        if ((fields != null) && (fields.length > 0)) {
                            return fields[0];
                        }
                        return null;
                    }
                });
        if (findField != null) {
            resultField = convertFieldEntityToFieldDTO(findField);
        }
        return resultField;
    }

    /**
     * Find field entity based on field name.
     * 
     * @param fieldName
     *            Name of the field. Not be <code>null</code>.
     * @return the field. If no one return <code>null</code>.
     */
    private AssetFieldEntity findFieldEntityByFieldName(final String fieldName) {
        if (fieldName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        AssetFieldEntity field = findFieldEntityByFieldNameAndRequired(fieldName, true);
        if (field == null) {
            field = findFieldEntityByFieldNameAndRequired(fieldName, false);
        }
        return field;
    }

    /**
     * Find field entity based on field name and required attribute.
     * 
     * @param fieldName
     *            Name of the field. Not be <code>null</code>.
     * @param required
     *            The required attribute of the field.
     * @return the field. If no one return <code>null</code>.
     */
    private AssetFieldEntity findFieldEntityByFieldNameAndRequired(final String fieldName,
            final boolean required) {
        if (fieldName == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        return ao.executeInTransaction(new TransactionCallback<AssetFieldEntity>() {
            public AssetFieldEntity doInTransaction() {
                final AssetFieldEntity[] fields = ao.find(AssetFieldEntity.class,
                        "FIELD_NAME = ? AND REQUIRED = ?",
                        fieldName, required);
                if ((fields != null) && (fields.length > 0)) {
                    return fields[0];
                }
                return null;
            }
        });
    }

    public boolean modifyFieldName(final String oldFieldName, final String newFieldName) {
        if ((oldFieldName == null) || (newFieldName == null)) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        if (oldFieldName.equals(newFieldName)) {
            return true;
        }
        final AssetField oldField = findFieldByFieldName(oldFieldName);
        AssetField newField = findFieldByFieldName(newFieldName);
        if ((oldField != null) && (newField == null)) {
            return ao.executeInTransaction(new TransactionCallback<Boolean>() {
                public Boolean doInTransaction() {
                    final AssetFieldEntity[] fields = ao.find(AssetFieldEntity.class,
                            "FIELD_NAME = ?",
                            oldFieldName);
                    if ((fields != null) && (fields.length == 1)) {
                        fields[0].setFieldName(newFieldName);
                        fields[0].save();
                        return true;
                    }
                    return false;
                }
            });
        }
        return false;
    }

    public boolean saveAssetModification(final AssetDetail asset) {
        if (asset == null) {
            throw new IllegalArgumentException("The parameter(s) not be null!");
        }
        if ((asset.getIssueKey() != null) && (asset.getFields() != null)) {
            return ao.executeInTransaction(new TransactionCallback<Boolean>() {
                public Boolean doInTransaction() {
                    AssetEntity[] ares = ao.find(AssetEntity.class,
                            "ISSUE_KEY = ?",
                            asset.getIssueKey());
                    if ((ares != null) && (ares.length > 0)) {
                        for (AssetEntity are : ares) {
                            String assetFieldValue = asset.getFields().get(are.getField().getFieldName());
                            if ((assetFieldValue != null) && !(are.getFieldValue().equals(assetFieldValue))) {
                                are.setFieldValue(assetFieldValue);
                                are.save();
                            }
                        }
                        String issueKey = asset.getIssueKey();
                        if (issueKey != null) {
                            AssetDetail assetInDB = findAssetDetailByIssueKey(issueKey);
                            if (assetInDB != null) {
                                Set<String> keySet = asset.getFields().keySet();
                                for (String key : keySet) {
                                    String keyValue = assetInDB.getFields().get(key);
                                    String assetFieldValue = asset.getFields().get(key);
                                    if ((keyValue == null) && (assetFieldValue != null)
                                            && (assetFieldValue.trim() != "")) {
                                        createAssetEntity(key, assetFieldValue, issueKey);
                                    }
                                }
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
        return false;
    }

    public void setAo(final ActiveObjects ao) {
        this.ao = ao;
    }

}
