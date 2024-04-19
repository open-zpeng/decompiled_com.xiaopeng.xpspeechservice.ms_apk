package org.litepal.tablemanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.tablemanager.model.AssociationsModel;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.GenericModel;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;
import org.litepal.util.LogUtil;
/* loaded from: classes.dex */
public abstract class AssociationCreator extends Generator {
    @Override // org.litepal.tablemanager.Generator
    protected abstract void createOrUpgradeTable(SQLiteDatabase sQLiteDatabase, boolean z);

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.litepal.tablemanager.Generator
    public void addOrUpdateAssociation(SQLiteDatabase db, boolean force) {
        addAssociations(getAllAssociations(), db, force);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String generateCreateTableSQL(String tableName, List<ColumnModel> columnModels, boolean autoIncrementId) {
        StringBuilder createTableSQL = new StringBuilder("create table ");
        createTableSQL.append(tableName);
        createTableSQL.append(" (");
        if (autoIncrementId) {
            createTableSQL.append("id integer primary key autoincrement,");
        }
        if (isContainsOnlyIdField(columnModels)) {
            createTableSQL.deleteCharAt(createTableSQL.length() - 1);
        }
        boolean needSeparator = false;
        for (ColumnModel columnModel : columnModels) {
            if (!columnModel.isIdColumn()) {
                if (needSeparator) {
                    createTableSQL.append(", ");
                }
                needSeparator = true;
                createTableSQL.append(columnModel.getColumnName());
                createTableSQL.append(" ");
                createTableSQL.append(columnModel.getColumnType());
                if (!columnModel.isNullable()) {
                    createTableSQL.append(" not null");
                }
                if (columnModel.isUnique()) {
                    createTableSQL.append(" unique");
                }
                String defaultValue = columnModel.getDefaultValue();
                if (!TextUtils.isEmpty(defaultValue)) {
                    createTableSQL.append(" default ");
                    createTableSQL.append(defaultValue);
                }
            }
        }
        createTableSQL.append(")");
        LogUtil.d(Generator.TAG, "create table sql is >> " + ((Object) createTableSQL));
        return createTableSQL.toString();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String generateDropTableSQL(String tableName) {
        return "drop table if exists " + tableName;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String generateAddColumnSQL(String tableName, ColumnModel columnModel) {
        StringBuilder addColumnSQL = new StringBuilder();
        addColumnSQL.append("alter table ");
        addColumnSQL.append(tableName);
        addColumnSQL.append(" add column ");
        addColumnSQL.append(columnModel.getColumnName());
        addColumnSQL.append(" ");
        addColumnSQL.append(columnModel.getColumnType());
        if (!columnModel.isNullable()) {
            addColumnSQL.append(" not null");
        }
        if (columnModel.isUnique()) {
            addColumnSQL.append(" unique");
        }
        String defaultValue = columnModel.getDefaultValue();
        if (!TextUtils.isEmpty(defaultValue)) {
            addColumnSQL.append(" default ");
            addColumnSQL.append(defaultValue);
        } else if (!columnModel.isNullable()) {
            if ("integer".equalsIgnoreCase(columnModel.getColumnType())) {
                defaultValue = "0";
            } else if ("text".equalsIgnoreCase(columnModel.getColumnType())) {
                defaultValue = "''";
            } else if ("real".equalsIgnoreCase(columnModel.getColumnType())) {
                defaultValue = "0.0";
            }
            addColumnSQL.append(" default ");
            addColumnSQL.append(defaultValue);
        }
        LogUtil.d(Generator.TAG, "add column sql is >> " + ((Object) addColumnSQL));
        return addColumnSQL.toString();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isForeignKeyColumnFormat(String columnName) {
        return (TextUtils.isEmpty(columnName) || !columnName.toLowerCase(Locale.US).endsWith("_id") || columnName.equalsIgnoreCase("_id")) ? false : true;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void giveTableSchemaACopy(String tableName, int tableType, SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder("select * from ");
        sql.append(Const.TableSchema.TABLE_NAME);
        LogUtil.d(Generator.TAG, "giveTableSchemaACopy SQL is >> " + ((Object) sql));
        Cursor cursor = null;
        try {
            try {
                cursor = db.rawQuery(sql.toString(), null);
                if (isNeedtoGiveACopy(cursor, tableName)) {
                    ContentValues values = new ContentValues();
                    values.put("name", BaseUtility.changeCase(tableName));
                    values.put(Const.TableSchema.COLUMN_TYPE, Integer.valueOf(tableType));
                    db.insert(Const.TableSchema.TABLE_NAME, null, values);
                }
                if (cursor == null) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (cursor == null) {
                    return;
                }
            }
            cursor.close();
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    private boolean isNeedtoGiveACopy(Cursor cursor, String tableName) {
        return (isValueExists(cursor, tableName) || isSpecialTable(tableName)) ? false : true;
    }

    /* JADX WARN: Incorrect condition in loop: B:6:0x0016 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private boolean isValueExists(android.database.Cursor r4, java.lang.String r5) {
        /*
            r3 = this;
            r0 = 0
            boolean r1 = r4.moveToFirst()
            if (r1 == 0) goto L20
        L7:
        L8:
            java.lang.String r1 = "name"
            int r1 = r4.getColumnIndexOrThrow(r1)
            java.lang.String r1 = r4.getString(r1)
            boolean r2 = r1.equalsIgnoreCase(r5)
            if (r2 == 0) goto L1a
            r0 = 1
            goto L20
        L1a:
            boolean r1 = r4.moveToNext()
            if (r1 != 0) goto L7
        L20:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.litepal.tablemanager.AssociationCreator.isValueExists(android.database.Cursor, java.lang.String):boolean");
    }

    private boolean isSpecialTable(String tableName) {
        return Const.TableSchema.TABLE_NAME.equalsIgnoreCase(tableName);
    }

    private void addAssociations(Collection<AssociationsModel> associatedModels, SQLiteDatabase db, boolean force) {
        for (AssociationsModel associationModel : associatedModels) {
            if (2 == associationModel.getAssociationType() || 1 == associationModel.getAssociationType()) {
                addForeignKeyColumn(associationModel.getTableName(), associationModel.getAssociatedTableName(), associationModel.getTableHoldsForeignKey(), db);
            } else if (3 == associationModel.getAssociationType()) {
                createIntermediateTable(associationModel.getTableName(), associationModel.getAssociatedTableName(), db, force);
            }
        }
        for (GenericModel genericModel : getGenericModels()) {
            createGenericTable(genericModel, db, force);
        }
    }

    private void createIntermediateTable(String tableName, String associatedTableName, SQLiteDatabase db, boolean force) {
        List<ColumnModel> columnModelList = new ArrayList<>();
        ColumnModel column1 = new ColumnModel();
        column1.setColumnName(String.valueOf(tableName) + "_id");
        column1.setColumnType("integer");
        ColumnModel column2 = new ColumnModel();
        column2.setColumnName(String.valueOf(associatedTableName) + "_id");
        column2.setColumnType("integer");
        columnModelList.add(column1);
        columnModelList.add(column2);
        String intermediateTableName = DBUtility.getIntermediateTableName(tableName, associatedTableName);
        List<String> sqls = new ArrayList<>();
        if (DBUtility.isTableExists(intermediateTableName, db)) {
            if (force) {
                sqls.add(generateDropTableSQL(intermediateTableName));
                sqls.add(generateCreateTableSQL(intermediateTableName, columnModelList, false));
            }
        } else {
            sqls.add(generateCreateTableSQL(intermediateTableName, columnModelList, false));
        }
        execute(sqls, db);
        giveTableSchemaACopy(intermediateTableName, 1, db);
    }

    private void createGenericTable(GenericModel genericModel, SQLiteDatabase db, boolean force) {
        String tableName = genericModel.getTableName();
        String valueColumnName = genericModel.getValueColumnName();
        String valueColumnType = genericModel.getValueColumnType();
        String valueIdColumnName = genericModel.getValueIdColumnName();
        List<ColumnModel> columnModelList = new ArrayList<>();
        ColumnModel column1 = new ColumnModel();
        column1.setColumnName(valueColumnName);
        column1.setColumnType(valueColumnType);
        ColumnModel column2 = new ColumnModel();
        column2.setColumnName(valueIdColumnName);
        column2.setColumnType("integer");
        columnModelList.add(column1);
        columnModelList.add(column2);
        List<String> sqls = new ArrayList<>();
        if (DBUtility.isTableExists(tableName, db)) {
            if (force) {
                sqls.add(generateDropTableSQL(tableName));
                sqls.add(generateCreateTableSQL(tableName, columnModelList, false));
            }
        } else {
            sqls.add(generateCreateTableSQL(tableName, columnModelList, false));
        }
        execute(sqls, db);
        giveTableSchemaACopy(tableName, 2, db);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void addForeignKeyColumn(String tableName, String associatedTableName, String tableHoldsForeignKey, SQLiteDatabase db) {
        if (DBUtility.isTableExists(tableName, db)) {
            if (DBUtility.isTableExists(associatedTableName, db)) {
                String foreignKeyColumn = null;
                if (tableName.equals(tableHoldsForeignKey)) {
                    foreignKeyColumn = getForeignKeyColumnName(associatedTableName);
                } else if (associatedTableName.equals(tableHoldsForeignKey)) {
                    foreignKeyColumn = getForeignKeyColumnName(tableName);
                }
                if (!DBUtility.isColumnExists(foreignKeyColumn, tableHoldsForeignKey, db)) {
                    ColumnModel columnModel = new ColumnModel();
                    columnModel.setColumnName(foreignKeyColumn);
                    columnModel.setColumnType("integer");
                    List<String> sqls = new ArrayList<>();
                    sqls.add(generateAddColumnSQL(tableHoldsForeignKey, columnModel));
                    execute(sqls, db);
                    return;
                }
                LogUtil.d(Generator.TAG, "column " + foreignKeyColumn + " is already exist, no need to add one");
                return;
            }
            throw new DatabaseGenerateException(DatabaseGenerateException.TABLE_DOES_NOT_EXIST + associatedTableName);
        }
        throw new DatabaseGenerateException(DatabaseGenerateException.TABLE_DOES_NOT_EXIST + tableName);
    }

    private boolean isContainsOnlyIdField(List<ColumnModel> columnModels) {
        return columnModels.size() == 0 || (columnModels.size() == 1 && isIdColumn(columnModels.get(0).getColumnName())) || (columnModels.size() == 2 && isIdColumn(columnModels.get(0).getColumnName()) && isIdColumn(columnModels.get(1).getColumnName()));
    }
}
