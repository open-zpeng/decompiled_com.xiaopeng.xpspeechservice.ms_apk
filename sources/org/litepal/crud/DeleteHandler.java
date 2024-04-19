package org.litepal.crud;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.xiaopeng.lib.framework.moduleinterface.carcontroller.IInputController;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.litepal.LitePal;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;
/* loaded from: classes.dex */
public class DeleteHandler extends DataHandler {
    private List<String> foreignKeyTableToDelete;

    public DeleteHandler(SQLiteDatabase db) {
        this.mDatabase = db;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public int onDelete(LitePalSupport baseObj) {
        if (baseObj.isSaved()) {
            List<Field> supportedGenericFields = getSupportedGenericFields(baseObj.getClassName());
            deleteGenericData(baseObj.getClass(), supportedGenericFields, baseObj.getBaseObjId());
            Collection<AssociationsInfo> associationInfos = analyzeAssociations(baseObj);
            int rowsAffected = deleteCascade(baseObj);
            SQLiteDatabase sQLiteDatabase = this.mDatabase;
            String tableName = baseObj.getTableName();
            int rowsAffected2 = rowsAffected + sQLiteDatabase.delete(tableName, "id = " + baseObj.getBaseObjId(), null);
            clearAssociatedModelSaveState(baseObj, associationInfos);
            return rowsAffected2;
        }
        return 0;
    }

    public int onDelete(Class<?> modelClass, long id) {
        List<Field> supportedGenericFields = getSupportedGenericFields(modelClass.getName());
        deleteGenericData(modelClass, supportedGenericFields, id);
        analyzeAssociations(modelClass);
        int rowsAffected = deleteCascade(modelClass, id);
        SQLiteDatabase sQLiteDatabase = this.mDatabase;
        String tableName = getTableName(modelClass);
        int rowsAffected2 = rowsAffected + sQLiteDatabase.delete(tableName, "id = " + id, null);
        getForeignKeyTableToDelete().clear();
        return rowsAffected2;
    }

    public int onDeleteAll(String tableName, String... conditions) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        return this.mDatabase.delete(tableName, getWhereClause(conditions), getWhereArgs(conditions));
    }

    public int onDeleteAll(Class<?> modelClass, String... conditions) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        List<Field> supportedGenericFields = getSupportedGenericFields(modelClass.getName());
        if (!supportedGenericFields.isEmpty()) {
            List<LitePalSupport> list = LitePal.select("id").where(conditions).find(modelClass);
            if (list.size() > 0) {
                long[] ids = new long[list.size()];
                for (int i = 0; i < ids.length; i++) {
                    LitePalSupport dataSupport = list.get(i);
                    ids[i] = dataSupport.getBaseObjId();
                }
                deleteGenericData(modelClass, supportedGenericFields, ids);
            }
        }
        analyzeAssociations(modelClass);
        int rowsAffected = deleteAllCascade(modelClass, conditions);
        int rowsAffected2 = rowsAffected + this.mDatabase.delete(getTableName(modelClass), getWhereClause(conditions), getWhereArgs(conditions));
        getForeignKeyTableToDelete().clear();
        return rowsAffected2;
    }

    private void analyzeAssociations(Class<?> modelClass) {
        Collection<AssociationsInfo> associationInfos = getAssociationInfo(modelClass.getName());
        for (AssociationsInfo associationInfo : associationInfos) {
            String associatedTableName = DBUtility.getTableNameByClassName(associationInfo.getAssociatedClassName());
            if (associationInfo.getAssociationType() == 2 || associationInfo.getAssociationType() == 1) {
                String classHoldsForeignKey = associationInfo.getClassHoldsForeignKey();
                if (!modelClass.getName().equals(classHoldsForeignKey)) {
                    getForeignKeyTableToDelete().add(associatedTableName);
                }
            } else if (associationInfo.getAssociationType() == 3) {
                String joinTableName = DBUtility.getIntermediateTableName(getTableName(modelClass), associatedTableName);
                getForeignKeyTableToDelete().add(BaseUtility.changeCase(joinTableName));
            }
        }
    }

    private int deleteCascade(Class<?> modelClass, long id) {
        int rowsAffected = 0;
        for (String associatedTableName : getForeignKeyTableToDelete()) {
            String fkName = getForeignKeyColumnName(getTableName(modelClass));
            SQLiteDatabase sQLiteDatabase = this.mDatabase;
            rowsAffected += sQLiteDatabase.delete(associatedTableName, String.valueOf(fkName) + " = " + id, null);
        }
        return rowsAffected;
    }

    private int deleteAllCascade(Class<?> modelClass, String... conditions) {
        int rowsAffected = 0;
        for (String associatedTableName : getForeignKeyTableToDelete()) {
            String tableName = getTableName(modelClass);
            String fkName = getForeignKeyColumnName(tableName);
            StringBuilder whereClause = new StringBuilder();
            whereClause.append(fkName);
            whereClause.append(" in (select id from ");
            whereClause.append(tableName);
            if (conditions != null && conditions.length > 0) {
                whereClause.append(" where ");
                whereClause.append(buildConditionString(conditions));
            }
            whereClause.append(")");
            rowsAffected += this.mDatabase.delete(associatedTableName, BaseUtility.changeCase(whereClause.toString()), null);
        }
        return rowsAffected;
    }

    private String buildConditionString(String... conditions) {
        int argCount = conditions.length - 1;
        String whereClause = conditions[0];
        for (int i = 0; i < argCount; i++) {
            whereClause = whereClause.replaceFirst("\\?", "'" + conditions[i + 1] + "'");
        }
        return whereClause;
    }

    private Collection<AssociationsInfo> analyzeAssociations(LitePalSupport baseObj) {
        try {
            Collection<AssociationsInfo> associationInfos = getAssociationInfo(baseObj.getClassName());
            analyzeAssociatedModels(baseObj, associationInfos);
            return associationInfos;
        } catch (Exception e) {
            throw new LitePalSupportException(e.getMessage(), e);
        }
    }

    private void clearAssociatedModelSaveState(LitePalSupport baseObj, Collection<AssociationsInfo> associationInfos) {
        LitePalSupport model;
        try {
            for (AssociationsInfo associationInfo : associationInfos) {
                if (associationInfo.getAssociationType() == 2 && !baseObj.getClassName().equals(associationInfo.getClassHoldsForeignKey())) {
                    Collection<LitePalSupport> associatedModels = getAssociatedModels(baseObj, associationInfo);
                    if (associatedModels != null && !associatedModels.isEmpty()) {
                        for (LitePalSupport model2 : associatedModels) {
                            if (model2 != null) {
                                model2.clearSavedState();
                            }
                        }
                    }
                } else if (associationInfo.getAssociationType() == 1 && (model = getAssociatedModel(baseObj, associationInfo)) != null) {
                    model.clearSavedState();
                }
            }
        } catch (Exception e) {
            throw new LitePalSupportException(e.getMessage(), e);
        }
    }

    private int deleteCascade(LitePalSupport baseObj) {
        int rowsAffected = deleteAssociatedForeignKeyRows(baseObj);
        return rowsAffected + deleteAssociatedJoinTableRows(baseObj);
    }

    private int deleteAssociatedForeignKeyRows(LitePalSupport baseObj) {
        int rowsAffected = 0;
        Map<String, Set<Long>> associatedModelMap = baseObj.getAssociatedModelsMapWithFK();
        for (String associatedTableName : associatedModelMap.keySet()) {
            String fkName = getForeignKeyColumnName(baseObj.getTableName());
            SQLiteDatabase sQLiteDatabase = this.mDatabase;
            rowsAffected += sQLiteDatabase.delete(associatedTableName, String.valueOf(fkName) + " = " + baseObj.getBaseObjId(), null);
        }
        return rowsAffected;
    }

    private int deleteAssociatedJoinTableRows(LitePalSupport baseObj) {
        int rowsAffected = 0;
        Set<String> associatedTableNames = baseObj.getAssociatedModelsMapForJoinTable().keySet();
        for (String associatedTableName : associatedTableNames) {
            String joinTableName = DBUtility.getIntermediateTableName(baseObj.getTableName(), associatedTableName);
            String fkName = getForeignKeyColumnName(baseObj.getTableName());
            SQLiteDatabase sQLiteDatabase = this.mDatabase;
            rowsAffected += sQLiteDatabase.delete(joinTableName, String.valueOf(fkName) + " = " + baseObj.getBaseObjId(), null);
        }
        return rowsAffected;
    }

    private List<String> getForeignKeyTableToDelete() {
        if (this.foreignKeyTableToDelete == null) {
            this.foreignKeyTableToDelete = new ArrayList();
        }
        return this.foreignKeyTableToDelete;
    }

    private void deleteGenericData(Class<?> modelClass, List<Field> supportedGenericFields, long... ids) {
        for (Field field : supportedGenericFields) {
            String tableName = DBUtility.getGenericTableName(modelClass.getName(), field.getName());
            String genericValueIdColumnName = DBUtility.getGenericValueIdColumnName(modelClass.getName());
            int length = ids.length;
            int loopCount = (length - 1) / IInputController.KEYCODE_KNOB_WIND_SPD_UP;
            for (int i = 0; i <= loopCount; i++) {
                StringBuilder whereClause = new StringBuilder();
                boolean needOr = false;
                for (int j = IInputController.KEYCODE_KNOB_WIND_SPD_UP * i; j < (i + 1) * IInputController.KEYCODE_KNOB_WIND_SPD_UP && j < length; j++) {
                    long id = ids[j];
                    if (needOr) {
                        whereClause.append(" or ");
                    }
                    whereClause.append(genericValueIdColumnName);
                    whereClause.append(" = ");
                    whereClause.append(id);
                    needOr = true;
                }
                if (!TextUtils.isEmpty(whereClause.toString())) {
                    this.mDatabase.delete(tableName, whereClause.toString(), null);
                }
            }
        }
    }
}
