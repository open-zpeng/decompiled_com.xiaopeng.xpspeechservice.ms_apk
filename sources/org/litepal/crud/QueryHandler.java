package org.litepal.crud;

import android.database.sqlite.SQLiteDatabase;
import com.xiaopeng.lib.utils.info.BuildInfoUtils;
import java.util.List;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;
/* loaded from: classes.dex */
public class QueryHandler extends DataHandler {
    public QueryHandler(SQLiteDatabase db) {
        this.mDatabase = db;
    }

    public <T> T onFind(Class<T> modelClass, long id, boolean isEager) {
        List<T> dataList = query(modelClass, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null, null, getForeignKeyAssociations(modelClass.getName(), isEager));
        if (dataList.size() > 0) {
            return dataList.get(0);
        }
        return null;
    }

    public <T> T onFindFirst(Class<T> modelClass, boolean isEager) {
        List<T> dataList = query(modelClass, null, null, null, null, null, "id", BuildInfoUtils.BID_WAN, getForeignKeyAssociations(modelClass.getName(), isEager));
        if (dataList.size() > 0) {
            return dataList.get(0);
        }
        return null;
    }

    public <T> T onFindLast(Class<T> modelClass, boolean isEager) {
        List<T> dataList = query(modelClass, null, null, null, null, null, "id desc", BuildInfoUtils.BID_WAN, getForeignKeyAssociations(modelClass.getName(), isEager));
        if (dataList.size() > 0) {
            return dataList.get(0);
        }
        return null;
    }

    public <T> List<T> onFindAll(Class<T> modelClass, boolean isEager, long... ids) {
        if (isAffectAllLines(ids)) {
            List<T> dataList = query(modelClass, null, null, null, null, null, "id", null, getForeignKeyAssociations(modelClass.getName(), isEager));
            return dataList;
        }
        List<T> dataList2 = query(modelClass, null, getWhereOfIdsWithOr(ids), null, null, null, "id", null, getForeignKeyAssociations(modelClass.getName(), isEager));
        return dataList2;
    }

    public <T> List<T> onFind(Class<T> modelClass, String[] columns, String[] conditions, String orderBy, String limit, boolean isEager) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        return query(modelClass, columns, getWhereClause(conditions), getWhereArgs(conditions), null, null, DBUtility.convertOrderByClauseToValidName(orderBy), limit, getForeignKeyAssociations(modelClass.getName(), isEager));
    }

    public int onCount(String tableName, String[] conditions) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        return ((Integer) mathQuery(tableName, new String[]{"count(1)"}, conditions, Integer.TYPE)).intValue();
    }

    public double onAverage(String tableName, String column, String[] conditions) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        return ((Double) mathQuery(tableName, new String[]{"avg(" + column + ")"}, conditions, Double.TYPE)).doubleValue();
    }

    public <T> T onMax(String tableName, String column, String[] conditions, Class<T> type) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        return (T) mathQuery(tableName, new String[]{"max(" + column + ")"}, conditions, type);
    }

    public <T> T onMin(String tableName, String column, String[] conditions, Class<T> type) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        return (T) mathQuery(tableName, new String[]{"min(" + column + ")"}, conditions, type);
    }

    public <T> T onSum(String tableName, String column, String[] conditions, Class<T> type) {
        BaseUtility.checkConditionsCorrect(conditions);
        if (conditions != null && conditions.length > 0) {
            conditions[0] = DBUtility.convertWhereClauseToColumnName(conditions[0]);
        }
        return (T) mathQuery(tableName, new String[]{"sum(" + column + ")"}, conditions, type);
    }
}
