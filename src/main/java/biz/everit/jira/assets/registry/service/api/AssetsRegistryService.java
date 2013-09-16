package biz.everit.jira.assets.registry.service.api;

import java.util.List;

import biz.everit.jira.assets.registry.service.api.dto.AssetDetail;
import biz.everit.jira.assets.registry.service.api.dto.AssetField;
import biz.everit.jira.assets.registry.service.api.dto.Field;

/**
 * The AssetsRegistryService interface. The interface defined the database operations (create, select, delete, update).
 */
public interface AssetsRegistryService {

    /**
     * Add new asset the database.
     * 
     * @param asset
     *            The details of the assets. Not be <code>null</code>.
     * @param issueKey
     *            the issueKey for the asset. Not be <code>null</code>.
     * @return <code>true</code> if successful add, otherwise <code>false</code>.
     */
    public boolean addAsset(AssetDetail asset, String issueKey);

    /**
     * Add new optional field the database.
     * 
     * @param fieldName
     *            Name of the field. Not be <code>null</code>.
     * @return id of field if successful adding, otherwise <code>null</code>.
     */
    public Long addOptionalField(String fieldName);

    /**
     * Add new required field the database.
     * 
     * @param fieldName
     *            Name of the field.Not be <code>null</code>.
     * @return id of field if successful adding, otherwise <code>null</code>.
     */
    public Long addRequiredField(String fieldName);

    /**
     * Changing the field activate attribute.
     * 
     * @param fieldName
     *            Name of the field. Not be <code>null</code>.
     * @return <code>true</code> if successful activate attribute modifying, otherwise <code>false</code>.
     */
    public boolean changeFieldActivate(String fieldName);

    /**
     * Delete all entity which can be found in the asset registry tables.
     * 
     * @return return the issue keys (in list) which belonged to the assets. If no one return empty list.
     */
    public List<String> deleteAllEntity();

    /**
     * Delete asset.
     * 
     * @param issueKey
     *            the key of the issue. Not be <code>null</code>.
     */
    public void deleteAsset(String issueKey);

    /**
     * Delete field based on field name. Not be delete if it's using.
     * 
     * @param fieldName
     *            Name of the field. Not be <code>null</code>.
     * @return <code>true</code> if successful deletion, otherwise <code>false</code>.
     */
    public boolean deleteField(String fieldName);

    /**
     * Finds all optional fields in the database.
     * 
     * @return the all optional fields. If no one return empty list.
     */
    public List<AssetField> findAllOptionalField();

    /**
     * Finds all required fields in the database.
     * 
     * @return the all required fields. If no one return empty list.
     */
    public List<AssetField> findAllRequiredField();

    /**
     * Finds asset based on issueId.
     * 
     * @param issueKey
     *            The issue key for the asset. Not be <code>null</code>.
     * @return the asset details if exist the asset, otherwise <code>null</code>.
     */
    public AssetDetail findAssetDetailByIssueKey(String issueKey);

    /**
     * Finds all asset details based on search fields. The search is not only full match.
     * 
     * @param field
     *            the {@link Field}. Not be <code>null</code>.
     * @return the all asset details, if no one return empty list.
     */
    public List<AssetDetail> findAssetDetailsByField(Field field);

    /**
     * Finds all asset details based on search fields. The searched values (fields) simultaneously occur. The search is
     * not only full match.
     * 
     * @param fields
     *            the {@link Field} list. Not be <code>null</code>.
     * @param issueKeys
     *            the keys of the issues. Not be <code>null</code>, but allowed the empty list.
     * @return the all asset details, if no one return empty list.
     */
    public List<AssetDetail> findAssetDetailsByFields(List<Field> fields, List<String> issueKeys);

    /**
     * Finds all assets based on issue keys.
     * 
     * @param keys
     *            Issue keys. Not be <code>null</code>.
     * @return the asset details list if the issue key(s) belongs to assets, otherwise empty list.
     */
    public List<AssetDetail> findAssetDetailsByIssueKeys(List<String> keys);

    /**
     * Find field based on field name.
     * 
     * @param fieldName
     *            Name of the field. Not be <code>null</code>.
     * @return the field. If no one return <code>null</code>.
     */
    public AssetField findFieldByFieldName(String fieldName);

    /**
     * Save the field modifications.
     * 
     * @param oldFieldName
     *            the old name of the field. Not be <code>null</code>.
     * @param newFieldName
     *            the new name of the field. Not be <code>null</code>.
     * @return <code>true</code> if successful save modification, otherwise <code>false</code>. If oldFieldName and
     *         newFieldName equals return <code>true</code>.
     */
    public boolean modifyFieldName(String oldFieldName, String newFieldName);

    /**
     * Save the asset modifications.
     * 
     * @param asset
     *            The details of the assets. Not be <code>null</code>.
     * @return <code>true</code> if successful saving, otherwise <code>false</code>.
     */
    public boolean saveAssetModification(AssetDetail asset);
}
