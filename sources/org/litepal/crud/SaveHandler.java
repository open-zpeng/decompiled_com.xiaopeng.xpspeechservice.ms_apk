package org.litepal.crud;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.litepal.annotation.Encrypt;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;
/* loaded from: classes.dex */
public class SaveHandler extends DataHandler {
    private ContentValues values = new ContentValues();

    public SaveHandler(SQLiteDatabase db) {
        this.mDatabase = db;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void onSave(LitePalSupport baseObj) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String className = baseObj.getClassName();
        List<Field> supportedFields = getSupportedFields(className);
        List<Field> supportedGenericFields = getSupportedGenericFields(className);
        Collection<AssociationsInfo> associationInfos = getAssociationInfo(className);
        if (!baseObj.isSaved()) {
            analyzeAssociatedModels(baseObj, associationInfos);
            doSaveAction(baseObj, supportedFields, supportedGenericFields);
            analyzeAssociatedModels(baseObj, associationInfos);
            return;
        }
        analyzeAssociatedModels(baseObj, associationInfos);
        doUpdateAction(baseObj, supportedFields, supportedGenericFields);
    }

    public <T extends LitePalSupport> void onSaveAll(Collection<T> collection) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (collection != null && collection.size() > 0) {
            LitePalSupport[] array = (LitePalSupport[]) collection.toArray(new LitePalSupport[0]);
            LitePalSupport firstObj = array[0];
            String className = firstObj.getClassName();
            List<Field> supportedFields = getSupportedFields(className);
            List<Field> supportedGenericFields = getSupportedGenericFields(className);
            Collection<AssociationsInfo> associationInfos = getAssociationInfo(className);
            for (LitePalSupport baseObj : array) {
                if (!baseObj.isSaved()) {
                    analyzeAssociatedModels(baseObj, associationInfos);
                    doSaveAction(baseObj, supportedFields, supportedGenericFields);
                    analyzeAssociatedModels(baseObj, associationInfos);
                } else {
                    analyzeAssociatedModels(baseObj, associationInfos);
                    doUpdateAction(baseObj, supportedFields, supportedGenericFields);
                }
                baseObj.clearAssociatedData();
            }
        }
    }

    private void doSaveAction(LitePalSupport baseObj, List<Field> supportedFields, List<Field> supportedGenericFields) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.values.clear();
        beforeSave(baseObj, supportedFields, this.values);
        long id = saving(baseObj, this.values);
        afterSave(baseObj, supportedFields, supportedGenericFields, id);
    }

    private void beforeSave(LitePalSupport baseObj, List<Field> supportedFields, ContentValues values) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        putFieldsValue(baseObj, supportedFields, values);
        putForeignKeyValue(values, baseObj);
    }

    private long saving(LitePalSupport baseObj, ContentValues values) {
        if (values.size() == 0) {
            values.putNull("id");
        }
        return this.mDatabase.insert(baseObj.getTableName(), null, values);
    }

    private void afterSave(LitePalSupport baseObj, List<Field> supportedFields, List<Field> supportedGenericFields, long id) throws IllegalAccessException, InvocationTargetException {
        throwIfSaveFailed(id);
        assignIdValue(baseObj, getIdField(supportedFields), id);
        updateGenericTables(baseObj, supportedGenericFields, id);
        updateAssociatedTableWithFK(baseObj);
        insertIntermediateJoinTableValue(baseObj, false);
    }

    private void doUpdateAction(LitePalSupport baseObj, List<Field> supportedFields, List<Field> supportedGenericFields) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        this.values.clear();
        beforeUpdate(baseObj, supportedFields, this.values);
        updating(baseObj, this.values);
        afterUpdate(baseObj, supportedGenericFields);
    }

    private void beforeUpdate(LitePalSupport baseObj, List<Field> supportedFields, ContentValues values) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        putFieldsValue(baseObj, supportedFields, values);
        putForeignKeyValue(values, baseObj);
        for (String fkName : baseObj.getListToClearSelfFK()) {
            values.putNull(fkName);
        }
    }

    private void updating(LitePalSupport baseObj, ContentValues values) {
        if (values.size() > 0) {
            this.mDatabase.update(baseObj.getTableName(), values, "id = ?", new String[]{String.valueOf(baseObj.getBaseObjId())});
        }
    }

    private void afterUpdate(LitePalSupport baseObj, List<Field> supportedGenericFields) throws InvocationTargetException, IllegalAccessException {
        updateGenericTables(baseObj, supportedGenericFields, baseObj.getBaseObjId());
        updateAssociatedTableWithFK(baseObj);
        insertIntermediateJoinTableValue(baseObj, true);
        clearFKValueInAssociatedTable(baseObj);
    }

    private Field getIdField(List<Field> supportedFields) {
        for (Field field : supportedFields) {
            if (isIdColumn(field.getName())) {
                return field;
            }
        }
        return null;
    }

    private void throwIfSaveFailed(long id) {
        if (id == -1) {
            throw new LitePalSupportException(LitePalSupportException.SAVE_FAILED);
        }
    }

    private void assignIdValue(LitePalSupport baseObj, Field idField, long id) {
        try {
            giveBaseObjIdValue(baseObj, id);
            if (idField != null) {
                giveModelIdValue(baseObj, idField.getName(), idField.getType(), id);
            }
        } catch (Exception e) {
            throw new LitePalSupportException(e.getMessage(), e);
        }
    }

    private void giveModelIdValue(LitePalSupport baseObj, String idName, Class<?> idType, long id) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Object value;
        if (shouldGiveModelIdValue(idName, idType, id)) {
            if (idType == Integer.TYPE || idType == Integer.class) {
                value = Integer.valueOf((int) id);
            } else if (idType == Long.TYPE || idType == Long.class) {
                value = Long.valueOf(id);
            } else {
                throw new LitePalSupportException(LitePalSupportException.ID_TYPE_INVALID_EXCEPTION);
            }
            DynamicExecutor.setField(baseObj, idName, value, baseObj.getClass());
        }
    }

    private void putForeignKeyValue(ContentValues values, LitePalSupport baseObj) {
        Map<String, Long> associatedModelMap = baseObj.getAssociatedModelsMapWithoutFK();
        for (String associatedTableName : associatedModelMap.keySet()) {
            values.put(getForeignKeyColumnName(associatedTableName), associatedModelMap.get(associatedTableName));
        }
    }

    private void updateAssociatedTableWithFK(LitePalSupport baseObj) {
        Map<String, Set<Long>> associatedModelMap = baseObj.getAssociatedModelsMapWithFK();
        ContentValues values = new ContentValues();
        for (String associatedTableName : associatedModelMap.keySet()) {
            values.clear();
            String fkName = getForeignKeyColumnName(baseObj.getTableName());
            values.put(fkName, Long.valueOf(baseObj.getBaseObjId()));
            Set<Long> ids = associatedModelMap.get(associatedTableName);
            if (ids != null && !ids.isEmpty()) {
                this.mDatabase.update(associatedTableName, values, getWhereOfIdsWithOr(ids), null);
            }
        }
    }

    private void clearFKValueInAssociatedTable(LitePalSupport baseObj) {
        List<String> associatedTableNames = baseObj.getListToClearAssociatedFK();
        for (String associatedTableName : associatedTableNames) {
            String fkColumnName = getForeignKeyColumnName(baseObj.getTableName());
            ContentValues values = new ContentValues();
            values.putNull(fkColumnName);
            String whereClause = String.valueOf(fkColumnName) + " = " + baseObj.getBaseObjId();
            this.mDatabase.update(associatedTableName, values, whereClause, null);
        }
    }

    private void insertIntermediateJoinTableValue(LitePalSupport baseObj, boolean isUpdate) {
        Map<String, List<Long>> associatedIdsM2M = baseObj.getAssociatedModelsMapForJoinTable();
        ContentValues values = new ContentValues();
        for (String associatedTableName : associatedIdsM2M.keySet()) {
            String joinTableName = getIntermediateTableName(baseObj, associatedTableName);
            if (isUpdate) {
                this.mDatabase.delete(joinTableName, getWhereForJoinTableToDelete(baseObj), new String[]{String.valueOf(baseObj.getBaseObjId())});
            }
            List<Long> associatedIdsM2MSet = associatedIdsM2M.get(associatedTableName);
            for (Long l : associatedIdsM2MSet) {
                long associatedId = l.longValue();
                values.clear();
                values.put(getForeignKeyColumnName(baseObj.getTableName()), Long.valueOf(baseObj.getBaseObjId()));
                values.put(getForeignKeyColumnName(associatedTableName), Long.valueOf(associatedId));
                this.mDatabase.insert(joinTableName, null, values);
            }
        }
    }

    private String getWhereForJoinTableToDelete(LitePalSupport baseObj) {
        return getForeignKeyColumnName(baseObj.getTableName()) + " = ?";
    }

    private boolean shouldGiveModelIdValue(String idName, Class<?> idType, long id) {
        return (idName == null || idType == null || id <= 0) ? false : true;
    }

    private void updateGenericTables(LitePalSupport baseObj, List<Field> supportedGenericFields, long id) throws IllegalAccessException, InvocationTargetException {
        boolean z;
        Iterator<Field> it;
        Iterator<Field> it2 = supportedGenericFields.iterator();
        while (it2.hasNext()) {
            Field field = it2.next();
            Encrypt annotation = (Encrypt) field.getAnnotation(Encrypt.class);
            String algorithm = null;
            String genericTypeName = getGenericTypeName(field);
            if (annotation != null && "java.lang.String".equals(genericTypeName)) {
                algorithm = annotation.algorithm();
            }
            field.setAccessible(true);
            Collection<?> collection = (Collection) field.get(baseObj);
            if (collection != null) {
                Log.d(DataHandler.TAG, "updateGenericTables: class name is " + baseObj.getClassName() + " , field name is " + field.getName());
                String tableName = DBUtility.getGenericTableName(baseObj.getClassName(), field.getName());
                String genericValueIdColumnName = DBUtility.getGenericValueIdColumnName(baseObj.getClassName());
                SQLiteDatabase sQLiteDatabase = this.mDatabase;
                sQLiteDatabase.delete(tableName, String.valueOf(genericValueIdColumnName) + " = ?", new String[]{String.valueOf(id)});
                for (Object object : collection) {
                    ContentValues values = new ContentValues();
                    values.put(genericValueIdColumnName, Long.valueOf(id));
                    Object object2 = encryptValue(algorithm, object);
                    if (baseObj.getClassName().equals(genericTypeName)) {
                        LitePalSupport dataSupport = (LitePalSupport) object2;
                        if (dataSupport != null) {
                            long baseObjId = dataSupport.getBaseObjId();
                            if (baseObjId > 0) {
                                values.put(DBUtility.getM2MSelfRefColumnName(field), Long.valueOf(baseObjId));
                                it = it2;
                                z = false;
                            }
                        }
                    } else {
                        z = false;
                        Object[] parameters = {BaseUtility.changeCase(DBUtility.convertToValidColumnName(field.getName())), object2};
                        Class[] parameterTypes = {String.class, getGenericTypeClass(field)};
                        it = it2;
                        DynamicExecutor.send(values, "put", parameters, values.getClass(), parameterTypes);
                    }
                    this.mDatabase.insert(tableName, null, values);
                    it2 = it;
                }
            }
        }
    }
}
