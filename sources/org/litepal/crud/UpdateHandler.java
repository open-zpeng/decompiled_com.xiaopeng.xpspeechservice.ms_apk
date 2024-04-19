package org.litepal.crud;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.litepal.LitePal;
import org.litepal.annotation.Encrypt;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;
/* loaded from: classes.dex */
public class UpdateHandler extends DataHandler {
    public UpdateHandler(SQLiteDatabase db) {
        this.mDatabase = db;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int onUpdate(LitePalSupport baseObj, long id) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
        List<Field> supportedGenericFields = getSupportedGenericFields(baseObj.getClassName());
        updateGenericTables(baseObj, supportedGenericFields, id);
        ContentValues values = new ContentValues();
        putFieldsValue(baseObj, supportedFields, values);
        putFieldsToDefaultValue(baseObj, values, id);
        if (values.size() > 0) {
            SQLiteDatabase sQLiteDatabase = this.mDatabase;
            String tableName = baseObj.getTableName();
            return sQLiteDatabase.update(tableName, values, "id = " + id, null);
        }
        return 0;
    }

    public int onUpdate(Class<?> modelClass, long id, ContentValues values) {
        if (values.size() > 0) {
            convertContentValues(values);
            SQLiteDatabase sQLiteDatabase = this.mDatabase;
            String tableName = getTableName(modelClass);
            return sQLiteDatabase.update(tableName, values, "id = " + id, null);
        }
        return 0;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int onUpdateAll(LitePalSupport baseObj, String... conditions) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        List<Field> supportedFields = getSupportedFields(baseObj.getClassName());
        List<Field> supportedGenericFields = getSupportedGenericFields(baseObj.getClassName());
        long[] ids = null;
        if (!supportedGenericFields.isEmpty()) {
            List<LitePalSupport> list = LitePal.select("id").where(conditions).find(baseObj.getClass());
            if (list.size() > 0) {
                long[] ids2 = new long[list.size()];
                for (int i = 0; i < ids2.length; i++) {
                    LitePalSupport dataSupport = list.get(i);
                    ids2[i] = dataSupport.getBaseObjId();
                }
                updateGenericTables(baseObj, supportedGenericFields, ids2);
                ids = ids2;
            }
        }
        ContentValues values = new ContentValues();
        putFieldsValue(baseObj, supportedFields, values);
        putFieldsToDefaultValue(baseObj, values, ids);
        return doUpdateAllAction(baseObj.getTableName(), values, conditions);
    }

    public int onUpdateAll(String tableName, ContentValues values, String... conditions) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        convertContentValues(values);
        return doUpdateAllAction(tableName, values, conditions);
    }

    private int doUpdateAllAction(String tableName, ContentValues values, String... conditions) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (values.size() > 0) {
            return this.mDatabase.update(tableName, values, getWhereClause(conditions), getWhereArgs(conditions));
        }
        return 0;
    }

    private void putFieldsToDefaultValue(LitePalSupport baseObj, ContentValues values, long... ids) {
        String fieldName;
        long[] jArr = ids;
        String fieldName2 = null;
        try {
            LitePalSupport emptyModel = getEmptyModel(baseObj);
            Class<?> emptyModelClass = emptyModel.getClass();
            for (String name : baseObj.getFieldsToSetToDefault()) {
                if (isIdColumn(name)) {
                    jArr = ids;
                } else {
                    String fieldName3 = name;
                    try {
                        Field field = emptyModelClass.getDeclaredField(fieldName3);
                        if (isCollection(field.getType())) {
                            if (jArr != null) {
                                try {
                                    if (jArr.length > 0) {
                                        String genericTypeName = getGenericTypeName(field);
                                        if (BaseUtility.isGenericTypeSupported(genericTypeName)) {
                                            String tableName = DBUtility.getGenericTableName(baseObj.getClassName(), field.getName());
                                            String genericValueIdColumnName = DBUtility.getGenericValueIdColumnName(baseObj.getClassName());
                                            StringBuilder whereClause = new StringBuilder();
                                            boolean needOr = false;
                                            int length = jArr.length;
                                            int i = 0;
                                            while (i < length) {
                                                long id = jArr[i];
                                                if (needOr) {
                                                    whereClause.append(" or ");
                                                }
                                                whereClause.append(genericValueIdColumnName);
                                                whereClause.append(" = ");
                                                fieldName = fieldName3;
                                                try {
                                                    whereClause.append(id);
                                                    needOr = true;
                                                    i++;
                                                    jArr = ids;
                                                    fieldName3 = fieldName;
                                                } catch (NoSuchFieldException e) {
                                                    e = e;
                                                    throw new LitePalSupportException(LitePalSupportException.noSuchFieldExceptioin(baseObj.getClassName(), fieldName), e);
                                                } catch (Exception e2) {
                                                    e = e2;
                                                    throw new LitePalSupportException(e.getMessage(), e);
                                                }
                                            }
                                            this.mDatabase.delete(tableName, whereClause.toString(), null);
                                            fieldName = fieldName3;
                                        } else {
                                            fieldName = fieldName3;
                                        }
                                    }
                                } catch (NoSuchFieldException e3) {
                                    e = e3;
                                    fieldName = fieldName3;
                                } catch (Exception e4) {
                                    e = e4;
                                }
                            }
                            fieldName = fieldName3;
                        } else {
                            fieldName = fieldName3;
                            try {
                                putContentValuesForUpdate(emptyModel, field, values);
                            } catch (NoSuchFieldException e5) {
                                e = e5;
                                throw new LitePalSupportException(LitePalSupportException.noSuchFieldExceptioin(baseObj.getClassName(), fieldName), e);
                            } catch (Exception e6) {
                                e = e6;
                                throw new LitePalSupportException(e.getMessage(), e);
                            }
                        }
                        jArr = ids;
                        fieldName2 = fieldName;
                    } catch (NoSuchFieldException e7) {
                        e = e7;
                        fieldName = fieldName3;
                    } catch (Exception e8) {
                        e = e8;
                    }
                }
            }
        } catch (NoSuchFieldException e9) {
            e = e9;
            fieldName = fieldName2;
        } catch (Exception e10) {
            e = e10;
        }
    }

    private int doUpdateAssociations(LitePalSupport baseObj, long id, ContentValues values) {
        analyzeAssociations(baseObj);
        updateSelfTableForeignKey(baseObj, values);
        int rowsAffected = 0 + updateAssociatedTableForeignKey(baseObj, id);
        return rowsAffected;
    }

    private void analyzeAssociations(LitePalSupport baseObj) {
        try {
            Collection<AssociationsInfo> associationInfos = getAssociationInfo(baseObj.getClassName());
            analyzeAssociatedModels(baseObj, associationInfos);
        } catch (Exception e) {
            throw new LitePalSupportException(e.getMessage(), e);
        }
    }

    private void updateSelfTableForeignKey(LitePalSupport baseObj, ContentValues values) {
        Map<String, Long> associatedModelMap = baseObj.getAssociatedModelsMapWithoutFK();
        for (String associatedTable : associatedModelMap.keySet()) {
            String fkName = getForeignKeyColumnName(associatedTable);
            values.put(fkName, associatedModelMap.get(associatedTable));
        }
    }

    private int updateAssociatedTableForeignKey(LitePalSupport baseObj, long id) {
        Map<String, Set<Long>> associatedModelMap = baseObj.getAssociatedModelsMapWithFK();
        ContentValues values = new ContentValues();
        for (String associatedTable : associatedModelMap.keySet()) {
            values.clear();
            String fkName = getForeignKeyColumnName(baseObj.getTableName());
            values.put(fkName, Long.valueOf(id));
            Set<Long> ids = associatedModelMap.get(associatedTable);
            if (ids != null && !ids.isEmpty()) {
                return this.mDatabase.update(associatedTable, values, getWhereOfIdsWithOr(ids), null);
            }
        }
        return 0;
    }

    private void updateGenericTables(LitePalSupport baseObj, List<Field> supportedGenericFields, long... ids) throws IllegalAccessException, InvocationTargetException {
        Iterator<?> it;
        Encrypt annotation;
        long[] jArr = ids;
        if (jArr != null && jArr.length > 0) {
            Iterator<Field> it2 = supportedGenericFields.iterator();
            while (it2.hasNext()) {
                Field field = it2.next();
                Encrypt annotation2 = (Encrypt) field.getAnnotation(Encrypt.class);
                String algorithm = null;
                String genericTypeName = getGenericTypeName(field);
                if (annotation2 != null && "java.lang.String".equals(genericTypeName)) {
                    algorithm = annotation2.algorithm();
                }
                field.setAccessible(true);
                Collection<?> collection = (Collection) field.get(baseObj);
                if (collection == null || collection.isEmpty()) {
                    jArr = ids;
                } else {
                    String tableName = DBUtility.getGenericTableName(baseObj.getClassName(), field.getName());
                    String genericValueIdColumnName = DBUtility.getGenericValueIdColumnName(baseObj.getClassName());
                    int length = jArr.length;
                    int i = 0;
                    while (i < length) {
                        long id = jArr[i];
                        SQLiteDatabase sQLiteDatabase = this.mDatabase;
                        Iterator<Field> it3 = it2;
                        sQLiteDatabase.delete(tableName, String.valueOf(genericValueIdColumnName) + " = ?", new String[]{String.valueOf(id)});
                        Iterator<?> it4 = collection.iterator();
                        while (it4.hasNext()) {
                            Object object = it4.next();
                            ContentValues values = new ContentValues();
                            values.put(genericValueIdColumnName, Long.valueOf(id));
                            Object object2 = encryptValue(algorithm, object);
                            if (baseObj.getClassName().equals(genericTypeName)) {
                                LitePalSupport dataSupport = (LitePalSupport) object2;
                                if (dataSupport != null) {
                                    long baseObjId = dataSupport.getBaseObjId();
                                    if (baseObjId > 0) {
                                        it = it4;
                                        annotation = annotation2;
                                        values.put(DBUtility.getM2MSelfRefColumnName(field), Long.valueOf(baseObjId));
                                    }
                                }
                            } else {
                                it = it4;
                                annotation = annotation2;
                                Object[] parameters = {DBUtility.convertToValidColumnName(BaseUtility.changeCase(field.getName())), object2};
                                Class[] parameterTypes = {String.class, getGenericTypeClass(field)};
                                DynamicExecutor.send(values, "put", parameters, values.getClass(), parameterTypes);
                            }
                            this.mDatabase.insert(tableName, null, values);
                            it4 = it;
                            annotation2 = annotation;
                        }
                        i++;
                        jArr = ids;
                        it2 = it3;
                    }
                }
            }
        }
    }

    private void convertContentValues(ContentValues values) {
        if (Build.VERSION.SDK_INT >= 11) {
            Map<String, Object> valuesToConvert = new HashMap<>();
            for (String key : values.keySet()) {
                if (DBUtility.isFieldNameConflictWithSQLiteKeywords(key)) {
                    valuesToConvert.put(key, values.get(key));
                }
            }
            for (String key2 : valuesToConvert.keySet()) {
                String convertedKey = DBUtility.convertToValidColumnName(key2);
                Object object = values.get(key2);
                values.remove(key2);
                if (object == null) {
                    values.putNull(convertedKey);
                } else {
                    String className = object.getClass().getName();
                    if ("java.lang.Byte".equals(className)) {
                        values.put(convertedKey, (Byte) object);
                    } else if ("[B".equals(className)) {
                        values.put(convertedKey, (byte[]) object);
                    } else if ("java.lang.Boolean".equals(className)) {
                        values.put(convertedKey, (Boolean) object);
                    } else if ("java.lang.String".equals(className)) {
                        values.put(convertedKey, (String) object);
                    } else if ("java.lang.Float".equals(className)) {
                        values.put(convertedKey, (Float) object);
                    } else if ("java.lang.Long".equals(className)) {
                        values.put(convertedKey, (Long) object);
                    } else if ("java.lang.Integer".equals(className)) {
                        values.put(convertedKey, (Integer) object);
                    } else if ("java.lang.Short".equals(className)) {
                        values.put(convertedKey, (Short) object);
                    } else if ("java.lang.Double".equals(className)) {
                        values.put(convertedKey, (Double) object);
                    }
                }
            }
        }
    }
}
