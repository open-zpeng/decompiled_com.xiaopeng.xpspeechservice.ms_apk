package org.litepal.crud;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.litepal.LitePal;
import org.litepal.LitePalBase;
import org.litepal.annotation.Encrypt;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.tablemanager.model.GenericModel;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;
import org.litepal.util.cipher.CipherUtil;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class DataHandler extends LitePalBase {
    public static final String TAG = "DataHandler";
    private List<AssociationsInfo> fkInCurrentModel;
    private List<AssociationsInfo> fkInOtherModel;
    SQLiteDatabase mDatabase;
    private LitePalSupport tempEmptyModel;

    /* JADX INFO: Access modifiers changed from: protected */
    /* JADX WARN: Removed duplicated region for block: B:34:0x00ad  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public <T> java.util.List<T> query(java.lang.Class<T> r21, java.lang.String[] r22, java.lang.String r23, java.lang.String[] r24, java.lang.String r25, java.lang.String r26, java.lang.String r27, java.lang.String r28, java.util.List<org.litepal.crud.model.AssociationsInfo> r29) {
        /*
            r20 = this;
            r7 = r20
            r8 = r29
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r9 = r0
            r1 = 0
            java.lang.String r0 = r21.getName()     // Catch: java.lang.Throwable -> L97 java.lang.Exception -> L9c
            java.util.List r3 = r7.getSupportedFields(r0)     // Catch: java.lang.Throwable -> L97 java.lang.Exception -> L9c
            java.lang.String r0 = r21.getName()     // Catch: java.lang.Throwable -> L97 java.lang.Exception -> L9c
            java.util.List r0 = r7.getSupportedGenericFields(r0)     // Catch: java.lang.Throwable -> L97 java.lang.Exception -> L9c
            r10 = r22
            java.lang.String[] r2 = r7.getCustomizedColumns(r10, r0, r8)     // Catch: java.lang.Exception -> L95 java.lang.Throwable -> La9
            java.lang.String[] r13 = org.litepal.util.DBUtility.convertSelectClauseToValidNames(r2)     // Catch: java.lang.Exception -> L95 java.lang.Throwable -> La9
            java.lang.String r12 = r20.getTableName(r21)     // Catch: java.lang.Exception -> L95 java.lang.Throwable -> La9
            android.database.sqlite.SQLiteDatabase r11 = r7.mDatabase     // Catch: java.lang.Exception -> L95 java.lang.Throwable -> La9
            r14 = r23
            r15 = r24
            r16 = r25
            r17 = r26
            r18 = r27
            r19 = r28
            android.database.Cursor r2 = r11.query(r12, r13, r14, r15, r16, r17, r18, r19)     // Catch: java.lang.Exception -> L95 java.lang.Throwable -> La9
            r11 = r2
            boolean r1 = r11.moveToFirst()     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            if (r1 == 0) goto L8a
            android.util.SparseArray r6 = new android.util.SparseArray     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r6.<init>()     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            java.util.HashMap r1 = new java.util.HashMap     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r1.<init>()     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r14 = r1
        L4e:
            java.lang.Object r1 = r20.createInstanceFromClass(r21)     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r15 = r1
            r1 = r15
            org.litepal.crud.LitePalSupport r1 = (org.litepal.crud.LitePalSupport) r1     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            java.lang.String r2 = "id"
            int r2 = r11.getColumnIndexOrThrow(r2)     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            long r4 = r11.getLong(r2)     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r7.giveBaseObjIdValue(r1, r4)     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r1 = r20
            r2 = r15
            r4 = r29
            r5 = r11
            r1.setValueToModel(r2, r3, r4, r5, r6)     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r1 = r15
            org.litepal.crud.LitePalSupport r1 = (org.litepal.crud.LitePalSupport) r1     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r7.setGenericValueToModel(r1, r0, r14)     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            if (r8 == 0) goto L7a
            r1 = r15
            org.litepal.crud.LitePalSupport r1 = (org.litepal.crud.LitePalSupport) r1     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r7.setAssociatedModel(r1)     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
        L7a:
            r9.add(r15)     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            boolean r1 = r11.moveToNext()     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            if (r1 != 0) goto L4e
            r6.clear()     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
            r14.clear()     // Catch: java.lang.Throwable -> L90 java.lang.Exception -> L92
        L8a:
            r11.close()
            return r9
        L90:
            r0 = move-exception
            goto Lab
        L92:
            r0 = move-exception
            r1 = r11
            goto L9f
        L95:
            r0 = move-exception
            goto L9f
        L97:
            r0 = move-exception
            r10 = r22
        L9a:
            r11 = r1
            goto Lab
        L9c:
            r0 = move-exception
            r10 = r22
        L9f:
            org.litepal.exceptions.LitePalSupportException r2 = new org.litepal.exceptions.LitePalSupportException     // Catch: java.lang.Throwable -> La9
            java.lang.String r3 = r0.getMessage()     // Catch: java.lang.Throwable -> La9
            r2.<init>(r3, r0)     // Catch: java.lang.Throwable -> La9
            throw r2     // Catch: java.lang.Throwable -> La9
        La9:
            r0 = move-exception
            goto L9a
        Lab:
            if (r11 == 0) goto Lb0
            r11.close()
        Lb0:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.litepal.crud.DataHandler.query(java.lang.Class, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List):java.util.List");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public <T> T mathQuery(String tableName, String[] columns, String[] conditions, Class<T> type) {
        BaseUtility.checkConditionsCorrect(conditions);
        Cursor cursor = null;
        T result = null;
        try {
            try {
                cursor = this.mDatabase.query(tableName, columns, getWhereClause(conditions), getWhereArgs(conditions), null, null, null);
                if (cursor.moveToFirst()) {
                    Class<?> cursorClass = cursor.getClass();
                    Method method = cursorClass.getMethod(genGetColumnMethod((Class<?>) type), Integer.TYPE);
                    result = method.invoke(cursor, 0);
                }
                cursor.close();
                return result;
            } catch (Exception e) {
                throw new LitePalSupportException(e.getMessage(), e);
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void giveBaseObjIdValue(LitePalSupport baseObj, long id) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (id > 0) {
            DynamicExecutor.set(baseObj, "baseObjId", Long.valueOf(id), LitePalSupport.class);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void putFieldsValue(LitePalSupport baseObj, List<Field> supportedFields, ContentValues values) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (Field field : supportedFields) {
            if (!isIdColumn(field.getName())) {
                putFieldsValueDependsOnSaveOrUpdate(baseObj, field, values);
            }
        }
    }

    protected void putContentValuesForSave(LitePalSupport baseObj, Field field, ContentValues values) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object fieldValue = DynamicExecutor.getField(baseObj, field.getName(), baseObj.getClass());
        if (fieldValue != null) {
            if ("java.util.Date".equals(field.getType().getName())) {
                Date date = (Date) fieldValue;
                fieldValue = Long.valueOf(date.getTime());
            }
            Encrypt annotation = (Encrypt) field.getAnnotation(Encrypt.class);
            if (annotation != null && "java.lang.String".equals(field.getType().getName())) {
                fieldValue = encryptValue(annotation.algorithm(), fieldValue);
            }
            Object[] parameters = {BaseUtility.changeCase(DBUtility.convertToValidColumnName(field.getName())), fieldValue};
            Class[] parameterTypes = getParameterTypes(field, fieldValue, parameters);
            DynamicExecutor.send(values, "put", parameters, values.getClass(), parameterTypes);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void putContentValuesForUpdate(LitePalSupport baseObj, Field field, ContentValues values) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Object fieldValue = getFieldValue(baseObj, field);
        if ("java.util.Date".equals(field.getType().getName()) && fieldValue != null) {
            Date date = (Date) fieldValue;
            fieldValue = Long.valueOf(date.getTime());
        }
        Encrypt annotation = (Encrypt) field.getAnnotation(Encrypt.class);
        if (annotation != null && "java.lang.String".equals(field.getType().getName())) {
            fieldValue = encryptValue(annotation.algorithm(), fieldValue);
        }
        Object[] parameters = {BaseUtility.changeCase(DBUtility.convertToValidColumnName(field.getName())), fieldValue};
        Class[] parameterTypes = getParameterTypes(field, fieldValue, parameters);
        DynamicExecutor.send(values, "put", parameters, values.getClass(), parameterTypes);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Object encryptValue(String algorithm, Object fieldValue) {
        if (algorithm != null && fieldValue != null) {
            if ("AES".equalsIgnoreCase(algorithm)) {
                return CipherUtil.aesEncrypt((String) fieldValue);
            }
            if ("MD5".equalsIgnoreCase(algorithm)) {
                return CipherUtil.md5Encrypt((String) fieldValue);
            }
            return fieldValue;
        }
        return fieldValue;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Object getFieldValue(LitePalSupport dataSupport, Field field) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (shouldGetOrSet(dataSupport, field)) {
            return DynamicExecutor.getField(dataSupport, field.getName(), dataSupport.getClass());
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setFieldValue(LitePalSupport dataSupport, Field field, Object parameter) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (shouldGetOrSet(dataSupport, field)) {
            DynamicExecutor.setField(dataSupport, field.getName(), parameter, dataSupport.getClass());
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void analyzeAssociatedModels(LitePalSupport baseObj, Collection<AssociationsInfo> associationInfos) {
        try {
            for (AssociationsInfo associationInfo : associationInfos) {
                if (associationInfo.getAssociationType() == 2) {
                    new Many2OneAnalyzer().analyze(baseObj, associationInfo);
                } else if (associationInfo.getAssociationType() == 1) {
                    new One2OneAnalyzer().analyze(baseObj, associationInfo);
                } else if (associationInfo.getAssociationType() == 3) {
                    new Many2ManyAnalyzer().analyze(baseObj, associationInfo);
                }
            }
        } catch (Exception e) {
            throw new LitePalSupportException(e.getMessage(), e);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public LitePalSupport getAssociatedModel(LitePalSupport baseObj, AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return (LitePalSupport) getFieldValue(baseObj, associationInfo.getAssociateOtherModelFromSelf());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Collection<LitePalSupport> getAssociatedModels(LitePalSupport baseObj, AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return (Collection) getFieldValue(baseObj, associationInfo.getAssociateOtherModelFromSelf());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public LitePalSupport getEmptyModel(LitePalSupport baseObj) {
        LitePalSupport litePalSupport = this.tempEmptyModel;
        if (litePalSupport != null) {
            return litePalSupport;
        }
        String className = null;
        try {
            className = baseObj.getClassName();
            Class<?> modelClass = Class.forName(className);
            this.tempEmptyModel = (LitePalSupport) modelClass.newInstance();
            return this.tempEmptyModel;
        } catch (ClassNotFoundException e) {
            throw new DatabaseGenerateException(DatabaseGenerateException.CLASS_NOT_FOUND + className);
        } catch (InstantiationException e2) {
            throw new LitePalSupportException(String.valueOf(className) + LitePalSupportException.INSTANTIATION_EXCEPTION, e2);
        } catch (Exception e3) {
            throw new LitePalSupportException(e3.getMessage(), e3);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getWhereClause(String... conditions) {
        if (isAffectAllLines(conditions) || conditions == null || conditions.length <= 0) {
            return null;
        }
        return conditions[0];
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String[] getWhereArgs(String... conditions) {
        if (isAffectAllLines(conditions) || conditions == null || conditions.length <= 1) {
            return null;
        }
        String[] whereArgs = new String[conditions.length - 1];
        System.arraycopy(conditions, 1, whereArgs, 0, conditions.length - 1);
        return whereArgs;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isAffectAllLines(Object... conditions) {
        if (conditions != null && conditions.length == 0) {
            return true;
        }
        return false;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getWhereOfIdsWithOr(Collection<Long> ids) {
        StringBuilder whereClause = new StringBuilder();
        boolean needOr = false;
        for (Long l : ids) {
            long id = l.longValue();
            if (needOr) {
                whereClause.append(" or ");
            }
            needOr = true;
            whereClause.append("id = ");
            whereClause.append(id);
        }
        return BaseUtility.changeCase(whereClause.toString());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getWhereOfIdsWithOr(long... ids) {
        StringBuilder whereClause = new StringBuilder();
        boolean needOr = false;
        for (long id : ids) {
            if (needOr) {
                whereClause.append(" or ");
            }
            needOr = true;
            whereClause.append("id = ");
            whereClause.append(id);
        }
        return BaseUtility.changeCase(whereClause.toString());
    }

    protected boolean shouldGetOrSet(LitePalSupport dataSupport, Field field) {
        return (dataSupport == null || field == null) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getIntermediateTableName(LitePalSupport baseObj, String associatedTableName) {
        return BaseUtility.changeCase(DBUtility.getIntermediateTableName(baseObj.getTableName(), associatedTableName));
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getTableName(Class<?> modelClass) {
        return BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName()));
    }

    protected Object createInstanceFromClass(Class<?> modelClass) {
        try {
            Constructor<?> constructor = findBestSuitConstructor(modelClass);
            return constructor.newInstance(getConstructorParams(modelClass, constructor));
        } catch (Exception e) {
            throw new LitePalSupportException(e.getMessage(), e);
        }
    }

    protected Constructor<?> findBestSuitConstructor(Class<?> modelClass) {
        Constructor<?>[] constructors = modelClass.getDeclaredConstructors();
        SparseArray<Constructor<?>> map = new SparseArray<>();
        int minKey = Integer.MAX_VALUE;
        for (Constructor<?> constructor : constructors) {
            int key = constructor.getParameterTypes().length;
            Class<?>[] types = constructor.getParameterTypes();
            int key2 = key;
            for (Class<?> parameterType : types) {
                if (parameterType == modelClass) {
                    key2 += 10000;
                } else if (parameterType.getName().startsWith("com.android") && parameterType.getName().endsWith("InstantReloadException")) {
                    key2 += 10000;
                }
            }
            if (map.get(key2) == null) {
                map.put(key2, constructor);
            }
            if (key2 < minKey) {
                minKey = key2;
            }
        }
        Constructor<?> bestSuitConstructor = map.get(minKey);
        if (bestSuitConstructor != null) {
            bestSuitConstructor.setAccessible(true);
        }
        return bestSuitConstructor;
    }

    protected Object[] getConstructorParams(Class<?> modelClass, Constructor<?> constructor) {
        Class[] paramTypes = constructor.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            params[i] = getInitParamValue(modelClass, paramTypes[i]);
        }
        return params;
    }

    protected void setValueToModel(Object modelInstance, List<Field> supportedFields, List<AssociationsInfo> foreignKeyAssociations, Cursor cursor, SparseArray<QueryInfoCache> sparseArray) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int cacheSize = sparseArray.size();
        if (cacheSize > 0) {
            for (int i = 0; i < cacheSize; i++) {
                int columnIndex = sparseArray.keyAt(i);
                QueryInfoCache cache = sparseArray.get(columnIndex);
                setToModelByReflection(modelInstance, cache.field, columnIndex, cache.getMethodName, cursor);
            }
        } else {
            for (Field field : supportedFields) {
                String getMethodName = genGetColumnMethod(field);
                String columnName = isIdColumn(field.getName()) ? "id" : DBUtility.convertToValidColumnName(field.getName());
                int columnIndex2 = cursor.getColumnIndex(BaseUtility.changeCase(columnName));
                if (columnIndex2 != -1) {
                    setToModelByReflection(modelInstance, field, columnIndex2, getMethodName, cursor);
                    QueryInfoCache cache2 = new QueryInfoCache();
                    cache2.getMethodName = getMethodName;
                    cache2.field = field;
                    sparseArray.put(columnIndex2, cache2);
                }
            }
        }
        if (foreignKeyAssociations != null) {
            for (AssociationsInfo associationInfo : foreignKeyAssociations) {
                String foreignKeyColumn = getForeignKeyColumnName(DBUtility.getTableNameByClassName(associationInfo.getAssociatedClassName()));
                int columnIndex3 = cursor.getColumnIndex(foreignKeyColumn);
                if (columnIndex3 != -1) {
                    long associatedClassId = cursor.getLong(columnIndex3);
                    try {
                        LitePalSupport associatedObj = (LitePalSupport) LitePal.find(Class.forName(associationInfo.getAssociatedClassName()), associatedClassId);
                        if (associatedObj != null) {
                            setFieldValue((LitePalSupport) modelInstance, associationInfo.getAssociateOtherModelFromSelf(), associatedObj);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected void setGenericValueToModel(LitePalSupport baseObj, List<Field> supportedGenericFields, Map<Field, GenericModel> genericModelMap) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        String getMethodName;
        String genericValueColumnName;
        String tableName;
        String genericValueIdColumnName;
        String genericValueColumnName2;
        String getMethodName2;
        for (Field field : supportedGenericFields) {
            Cursor cursor = null;
            GenericModel genericModel = genericModelMap.get(field);
            if (genericModel == null) {
                String genericTypeName = getGenericTypeName(field);
                if (baseObj.getClassName().equals(genericTypeName)) {
                    genericValueColumnName2 = DBUtility.getM2MSelfRefColumnName(field);
                    getMethodName2 = "getLong";
                } else {
                    String genericValueColumnName3 = field.getName();
                    genericValueColumnName2 = DBUtility.convertToValidColumnName(genericValueColumnName3);
                    getMethodName2 = genGetColumnMethod(field);
                }
                String tableName2 = DBUtility.getGenericTableName(baseObj.getClassName(), field.getName());
                String genericValueIdColumnName2 = DBUtility.getGenericValueIdColumnName(baseObj.getClassName());
                GenericModel model = new GenericModel();
                model.setTableName(tableName2);
                model.setValueColumnName(genericValueColumnName2);
                model.setValueIdColumnName(genericValueIdColumnName2);
                model.setGetMethodName(getMethodName2);
                genericModelMap.put(field, model);
                genericValueColumnName = genericValueColumnName2;
                getMethodName = getMethodName2;
                tableName = tableName2;
                genericValueIdColumnName = genericValueIdColumnName2;
            } else {
                String tableName3 = genericModel.getTableName();
                String genericValueColumnName4 = genericModel.getValueColumnName();
                String genericValueIdColumnName3 = genericModel.getValueIdColumnName();
                getMethodName = genericModel.getGetMethodName();
                genericValueColumnName = genericValueColumnName4;
                tableName = tableName3;
                genericValueIdColumnName = genericValueIdColumnName3;
            }
            try {
                SQLiteDatabase sQLiteDatabase = this.mDatabase;
                Cursor cursor2 = sQLiteDatabase.query(tableName, null, String.valueOf(genericValueIdColumnName) + " = ?", new String[]{String.valueOf(baseObj.getBaseObjId())}, null, null, null);
                try {
                    if (cursor2.moveToFirst()) {
                        do {
                            int columnIndex = cursor2.getColumnIndex(BaseUtility.changeCase(genericValueColumnName));
                            if (columnIndex != -1) {
                                setToModelByReflection(baseObj, field, columnIndex, getMethodName, cursor2);
                            }
                        } while (cursor2.moveToNext());
                    }
                    cursor2.close();
                } catch (Throwable th) {
                    th = th;
                    cursor = cursor2;
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public List<AssociationsInfo> getForeignKeyAssociations(String className, boolean isEager) {
        if (isEager) {
            analyzeAssociations(className);
            return this.fkInCurrentModel;
        }
        return null;
    }

    protected Class<?>[] getParameterTypes(Field field, Object fieldValue, Object[] parameters) {
        if (isCharType(field)) {
            parameters[1] = String.valueOf(fieldValue);
            Class[] parameterTypes = {String.class, String.class};
            return parameterTypes;
        } else if (field.getType().isPrimitive()) {
            Class[] parameterTypes2 = {String.class, getObjectType(field.getType())};
            return parameterTypes2;
        } else if ("java.util.Date".equals(field.getType().getName())) {
            Class[] parameterTypes3 = {String.class, Long.class};
            return parameterTypes3;
        } else {
            Class[] parameterTypes4 = {String.class, field.getType()};
            return parameterTypes4;
        }
    }

    private Class<?> getObjectType(Class<?> primitiveType) {
        if (primitiveType != null && primitiveType.isPrimitive()) {
            String basicTypeName = primitiveType.getName();
            if ("int".equals(basicTypeName)) {
                return Integer.class;
            }
            if ("short".equals(basicTypeName)) {
                return Short.class;
            }
            if ("long".equals(basicTypeName)) {
                return Long.class;
            }
            if ("float".equals(basicTypeName)) {
                return Float.class;
            }
            if ("double".equals(basicTypeName)) {
                return Double.class;
            }
            if ("boolean".equals(basicTypeName)) {
                return Boolean.class;
            }
            if ("char".equals(basicTypeName)) {
                return Character.class;
            }
            return null;
        }
        return null;
    }

    private Object getInitParamValue(Class<?> modelClass, Class<?> paramType) {
        String paramTypeName = paramType.getName();
        if ("boolean".equals(paramTypeName) || "java.lang.Boolean".equals(paramTypeName)) {
            return false;
        }
        if ("float".equals(paramTypeName) || "java.lang.Float".equals(paramTypeName)) {
            return Float.valueOf(0.0f);
        }
        if ("double".equals(paramTypeName) || "java.lang.Double".equals(paramTypeName)) {
            return Double.valueOf(0.0d);
        }
        if ("int".equals(paramTypeName) || "java.lang.Integer".equals(paramTypeName)) {
            return 0;
        }
        if ("long".equals(paramTypeName) || "java.lang.Long".equals(paramTypeName)) {
            return 0L;
        }
        if ("short".equals(paramTypeName) || "java.lang.Short".equals(paramTypeName)) {
            return 0;
        }
        if ("char".equals(paramTypeName) || "java.lang.Character".equals(paramTypeName)) {
            return ' ';
        }
        if ("[B".equals(paramTypeName) || "[Ljava.lang.Byte;".equals(paramTypeName)) {
            return new byte[0];
        }
        if ("java.lang.String".equals(paramTypeName)) {
            return "";
        }
        if (modelClass == paramType) {
            return null;
        }
        return createInstanceFromClass(paramType);
    }

    private boolean isCharType(Field field) {
        String type = field.getType().getName();
        return type.equals("char") || type.endsWith("Character");
    }

    private boolean isPrimitiveBooleanType(Field field) {
        Class<?> fieldType = field.getType();
        if ("boolean".equals(fieldType.getName())) {
            return true;
        }
        return false;
    }

    private void putFieldsValueDependsOnSaveOrUpdate(LitePalSupport baseObj, Field field, ContentValues values) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (isUpdating()) {
            if (!isFieldWithDefaultValue(baseObj, field)) {
                putContentValuesForUpdate(baseObj, field, values);
            }
        } else if (isSaving()) {
            putContentValuesForSave(baseObj, field, values);
        }
    }

    private boolean isUpdating() {
        return UpdateHandler.class.getName().equals(getClass().getName());
    }

    private boolean isSaving() {
        return SaveHandler.class.getName().equals(getClass().getName());
    }

    private boolean isFieldWithDefaultValue(LitePalSupport baseObj, Field field) throws IllegalAccessException, SecurityException, IllegalArgumentException, NoSuchMethodException, InvocationTargetException {
        LitePalSupport emptyModel = getEmptyModel(baseObj);
        Object realReturn = getFieldValue(baseObj, field);
        Object defaultReturn = getFieldValue(emptyModel, field);
        if (realReturn == null || defaultReturn == null) {
            return realReturn == defaultReturn;
        }
        String realFieldValue = realReturn.toString();
        String defaultFieldValue = defaultReturn.toString();
        return realFieldValue.equals(defaultFieldValue);
    }

    private String makeGetterMethodName(Field field) {
        String getterMethodPrefix;
        String fieldName = field.getName();
        if (isPrimitiveBooleanType(field)) {
            if (fieldName.matches("^is[A-Z]{1}.*$")) {
                fieldName = fieldName.substring(2);
            }
            getterMethodPrefix = "is";
        } else {
            getterMethodPrefix = "get";
        }
        if (fieldName.matches("^[a-z]{1}[A-Z]{1}.*")) {
            return String.valueOf(getterMethodPrefix) + fieldName;
        }
        return String.valueOf(getterMethodPrefix) + BaseUtility.capitalize(fieldName);
    }

    private String makeSetterMethodName(Field field) {
        if (isPrimitiveBooleanType(field) && field.getName().matches("^is[A-Z]{1}.*$")) {
            String setterMethodName = String.valueOf("set") + field.getName().substring(2);
            return setterMethodName;
        }
        String setterMethodName2 = field.getName();
        if (setterMethodName2.matches("^[a-z]{1}[A-Z]{1}.*")) {
            String setterMethodName3 = String.valueOf("set") + field.getName();
            return setterMethodName3;
        }
        String setterMethodName4 = String.valueOf("set") + BaseUtility.capitalize(field.getName());
        return setterMethodName4;
    }

    private String genGetColumnMethod(Field field) {
        Class<?> fieldType;
        if (isCollection(field.getType())) {
            fieldType = getGenericTypeClass(field);
        } else {
            fieldType = field.getType();
        }
        return genGetColumnMethod(fieldType);
    }

    private String genGetColumnMethod(Class<?> fieldType) {
        String typeName;
        if (fieldType.isPrimitive()) {
            typeName = BaseUtility.capitalize(fieldType.getName());
        } else {
            typeName = fieldType.getSimpleName();
        }
        String methodName = "get" + typeName;
        if ("getBoolean".equals(methodName)) {
            return "getInt";
        }
        if ("getChar".equals(methodName) || "getCharacter".equals(methodName)) {
            return "getString";
        }
        if ("getDate".equals(methodName)) {
            return "getLong";
        }
        if ("getInteger".equals(methodName)) {
            return "getInt";
        }
        if ("getbyte[]".equalsIgnoreCase(methodName)) {
            return "getBlob";
        }
        return methodName;
    }

    private String[] getCustomizedColumns(String[] columns, List<Field> supportedGenericFields, List<AssociationsInfo> foreignKeyAssociations) {
        if (columns != null && columns.length > 0) {
            boolean columnsContainsId = false;
            List<String> convertList = Arrays.asList(columns);
            List<String> columnList = new ArrayList<>(convertList);
            List<String> supportedGenericFieldNames = new ArrayList<>();
            List<Integer> columnToRemove = new ArrayList<>();
            List<String> genericColumnsForQuery = new ArrayList<>();
            List<Field> tempSupportedGenericFields = new ArrayList<>();
            for (Field supportedGenericField : supportedGenericFields) {
                supportedGenericFieldNames.add(supportedGenericField.getName());
            }
            for (int i = 0; i < columnList.size(); i++) {
                String columnName = columnList.get(i);
                if (BaseUtility.containsIgnoreCases(supportedGenericFieldNames, columnName)) {
                    columnToRemove.add(Integer.valueOf(i));
                } else if (isIdColumn(columnName)) {
                    columnsContainsId = true;
                    if ("_id".equalsIgnoreCase(columnName)) {
                        columnList.set(i, BaseUtility.changeCase("id"));
                    }
                }
            }
            int i2 = columnToRemove.size();
            for (int i3 = i2 - 1; i3 >= 0; i3--) {
                int index = columnToRemove.get(i3).intValue();
                String genericColumn = columnList.remove(index);
                genericColumnsForQuery.add(genericColumn);
            }
            for (Field supportedGenericField2 : supportedGenericFields) {
                String fieldName = supportedGenericField2.getName();
                if (BaseUtility.containsIgnoreCases(genericColumnsForQuery, fieldName)) {
                    tempSupportedGenericFields.add(supportedGenericField2);
                }
            }
            supportedGenericFields.clear();
            supportedGenericFields.addAll(tempSupportedGenericFields);
            if (foreignKeyAssociations != null && foreignKeyAssociations.size() > 0) {
                for (int i4 = 0; i4 < foreignKeyAssociations.size(); i4++) {
                    String associatedTable = DBUtility.getTableNameByClassName(foreignKeyAssociations.get(i4).getAssociatedClassName());
                    columnList.add(getForeignKeyColumnName(associatedTable));
                }
            }
            if (!columnsContainsId) {
                columnList.add(BaseUtility.changeCase("id"));
            }
            return (String[]) columnList.toArray(new String[columnList.size()]);
        }
        return null;
    }

    private void analyzeAssociations(String className) {
        Collection<AssociationsInfo> associationInfos = getAssociationInfo(className);
        List<AssociationsInfo> list = this.fkInCurrentModel;
        if (list == null) {
            this.fkInCurrentModel = new ArrayList();
        } else {
            list.clear();
        }
        List<AssociationsInfo> list2 = this.fkInOtherModel;
        if (list2 == null) {
            this.fkInOtherModel = new ArrayList();
        } else {
            list2.clear();
        }
        for (AssociationsInfo associationInfo : associationInfos) {
            if (associationInfo.getAssociationType() == 2 || associationInfo.getAssociationType() == 1) {
                if (associationInfo.getClassHoldsForeignKey().equals(className)) {
                    this.fkInCurrentModel.add(associationInfo);
                } else {
                    this.fkInOtherModel.add(associationInfo);
                }
            } else if (associationInfo.getAssociationType() == 3) {
                this.fkInOtherModel.add(associationInfo);
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:43:0x0182 A[LOOP:1: B:23:0x0102->B:43:0x0182, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:68:0x017b A[EDGE_INSN: B:68:0x017b->B:41:0x017b ?: BREAK  , SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void setAssociatedModel(org.litepal.crud.LitePalSupport r28) {
        /*
            Method dump skipped, instructions count: 421
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: org.litepal.crud.DataHandler.setAssociatedModel(org.litepal.crud.LitePalSupport):void");
    }

    private void setToModelByReflection(Object modelInstance, Field field, int columnIndex, String getMethodName, Cursor cursor) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> cursorClass = cursor.getClass();
        Method method = cursorClass.getMethod(getMethodName, Integer.TYPE);
        Object value = method.invoke(cursor, Integer.valueOf(columnIndex));
        if (field.getType() == Boolean.TYPE || field.getType() == Boolean.class) {
            if ("0".equals(String.valueOf(value))) {
                value = false;
            } else if (BuildInfoUtils.BID_WAN.equals(String.valueOf(value))) {
                value = true;
            }
        } else if (field.getType() == Character.TYPE || field.getType() == Character.class) {
            value = Character.valueOf(((String) value).charAt(0));
        } else if (field.getType() == Date.class) {
            long date = ((Long) value).longValue();
            if (date <= 0) {
                value = null;
            } else {
                value = new Date(date);
            }
        }
        if (isCollection(field.getType())) {
            Collection<Object> collection = (Collection) DynamicExecutor.getField(modelInstance, field.getName(), modelInstance.getClass());
            if (collection == null) {
                if (isList(field.getType())) {
                    collection = new ArrayList<>();
                } else {
                    collection = new HashSet<>();
                }
                DynamicExecutor.setField(modelInstance, field.getName(), collection, modelInstance.getClass());
            }
            String genericTypeName = getGenericTypeName(field);
            if ("java.lang.String".equals(genericTypeName)) {
                Encrypt annotation = (Encrypt) field.getAnnotation(Encrypt.class);
                if (annotation != null) {
                    value = decryptValue(annotation.algorithm(), value);
                }
            } else if (modelInstance.getClass().getName().equals(genericTypeName) && ((value instanceof Long) || (value instanceof Integer))) {
                value = LitePal.find(modelInstance.getClass(), ((Long) value).longValue());
            }
            collection.add(value);
            return;
        }
        Encrypt annotation2 = (Encrypt) field.getAnnotation(Encrypt.class);
        if (annotation2 != null && "java.lang.String".equals(field.getType().getName())) {
            value = decryptValue(annotation2.algorithm(), value);
        }
        DynamicExecutor.setField(modelInstance, field.getName(), value, modelInstance.getClass());
    }

    protected Object decryptValue(String algorithm, Object fieldValue) {
        if (algorithm != null && fieldValue != null && "AES".equalsIgnoreCase(algorithm)) {
            return CipherUtil.aesDecrypt((String) fieldValue);
        }
        return fieldValue;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public class QueryInfoCache {
        Field field;
        String getMethodName;

        QueryInfoCache() {
        }
    }
}
