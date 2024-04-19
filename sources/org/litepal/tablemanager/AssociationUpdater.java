package org.litepal.tablemanager;

import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.litepal.parser.LitePalAttr;
import org.litepal.tablemanager.model.AssociationsModel;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.GenericModel;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;
import org.litepal.util.LogUtil;
/* loaded from: classes.dex */
public abstract class AssociationUpdater extends Creator {
    public static final String TAG = "AssociationUpdater";
    private Collection<AssociationsModel> mAssociationModels;
    protected SQLiteDatabase mDb;

    @Override // org.litepal.tablemanager.Creator, org.litepal.tablemanager.AssociationCreator, org.litepal.tablemanager.Generator
    protected abstract void createOrUpgradeTable(SQLiteDatabase sQLiteDatabase, boolean z);

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.litepal.tablemanager.AssociationCreator, org.litepal.tablemanager.Generator
    public void addOrUpdateAssociation(SQLiteDatabase db, boolean force) {
        this.mAssociationModels = getAllAssociations();
        this.mDb = db;
        removeAssociations();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public List<String> getForeignKeyColumns(TableModel tableModel) {
        List<String> foreignKeyColumns = new ArrayList<>();
        List<ColumnModel> columnModelList = getTableModelFromDB(tableModel.getTableName()).getColumnModels();
        for (ColumnModel columnModel : columnModelList) {
            String columnName = columnModel.getColumnName();
            if (isForeignKeyColumnFormat(columnModel.getColumnName()) && !tableModel.containsColumn(columnName)) {
                LogUtil.d(TAG, "getForeignKeyColumnNames >> foreign key column is " + columnName);
                foreignKeyColumns.add(columnName);
            }
        }
        return foreignKeyColumns;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isForeignKeyColumn(TableModel tableModel, String columnName) {
        return BaseUtility.containsIgnoreCases(getForeignKeyColumns(tableModel), columnName);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public TableModel getTableModelFromDB(String tableName) {
        return DBUtility.findPragmaTableInfo(tableName, this.mDb);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void dropTables(List<String> dropTableNames, SQLiteDatabase db) {
        if (dropTableNames != null && !dropTableNames.isEmpty()) {
            List<String> dropTableSQLS = new ArrayList<>();
            for (int i = 0; i < dropTableNames.size(); i++) {
                dropTableSQLS.add(generateDropTableSQL(dropTableNames.get(i)));
            }
            execute(dropTableSQLS, db);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void removeColumns(Collection<String> removeColumnNames, String tableName) {
        if (removeColumnNames != null && !removeColumnNames.isEmpty()) {
            execute(getRemoveColumnSQLs(removeColumnNames, tableName), this.mDb);
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void clearCopyInTableSchema(List<String> tableNames) {
        if (tableNames != null && !tableNames.isEmpty()) {
            StringBuilder deleteData = new StringBuilder("delete from ");
            deleteData.append(Const.TableSchema.TABLE_NAME);
            deleteData.append(" where");
            boolean needOr = false;
            for (String tableName : tableNames) {
                if (needOr) {
                    deleteData.append(" or ");
                }
                needOr = true;
                deleteData.append(" lower(");
                deleteData.append("name");
                deleteData.append(") ");
                deleteData.append("=");
                deleteData.append(" lower('");
                deleteData.append(tableName);
                deleteData.append("')");
            }
            LogUtil.d(TAG, "clear table schema value sql is " + ((Object) deleteData));
            List<String> sqls = new ArrayList<>();
            sqls.add(deleteData.toString());
            execute(sqls, this.mDb);
        }
    }

    private void removeAssociations() {
        removeForeignKeyColumns();
        removeIntermediateTables();
        removeGenericTables();
    }

    private void removeForeignKeyColumns() {
        for (String className : LitePalAttr.getInstance().getClassNames()) {
            TableModel tableModel = getTableModel(className);
            removeColumns(findForeignKeyToRemove(tableModel), tableModel.getTableName());
        }
    }

    private void removeIntermediateTables() {
        List<String> tableNamesToDrop = findIntermediateTablesToDrop();
        dropTables(tableNamesToDrop, this.mDb);
        clearCopyInTableSchema(tableNamesToDrop);
    }

    private void removeGenericTables() {
        List<String> tableNamesToDrop = findGenericTablesToDrop();
        dropTables(tableNamesToDrop, this.mDb);
        clearCopyInTableSchema(tableNamesToDrop);
    }

    private List<String> findForeignKeyToRemove(TableModel tableModel) {
        List<String> removeRelations = new ArrayList<>();
        List<String> foreignKeyColumns = getForeignKeyColumns(tableModel);
        String selfTableName = tableModel.getTableName();
        for (String foreignKeyColumn : foreignKeyColumns) {
            String associatedTableName = DBUtility.getTableNameByForeignColumn(foreignKeyColumn);
            if (shouldDropForeignKey(selfTableName, associatedTableName)) {
                removeRelations.add(foreignKeyColumn);
            }
        }
        LogUtil.d(TAG, "findForeignKeyToRemove >> " + tableModel.getTableName() + " " + removeRelations);
        return removeRelations;
    }

    private List<String> findIntermediateTablesToDrop() {
        List<String> intermediateTables = new ArrayList<>();
        for (String tableName : DBUtility.findAllTableNames(this.mDb)) {
            if (DBUtility.isIntermediateTable(tableName, this.mDb)) {
                boolean dropIntermediateTable = true;
                for (AssociationsModel associationModel : this.mAssociationModels) {
                    if (associationModel.getAssociationType() == 3) {
                        String intermediateTableName = DBUtility.getIntermediateTableName(associationModel.getTableName(), associationModel.getAssociatedTableName());
                        if (tableName.equalsIgnoreCase(intermediateTableName)) {
                            dropIntermediateTable = false;
                        }
                    }
                }
                if (dropIntermediateTable) {
                    intermediateTables.add(tableName);
                }
            }
        }
        LogUtil.d(TAG, "findIntermediateTablesToDrop >> " + intermediateTables);
        return intermediateTables;
    }

    private List<String> findGenericTablesToDrop() {
        List<String> genericTablesToDrop = new ArrayList<>();
        for (String tableName : DBUtility.findAllTableNames(this.mDb)) {
            if (DBUtility.isGenericTable(tableName, this.mDb)) {
                boolean dropGenericTable = true;
                for (GenericModel genericModel : getGenericModels()) {
                    String genericTableName = genericModel.getTableName();
                    if (tableName.equalsIgnoreCase(genericTableName)) {
                        dropGenericTable = false;
                    }
                }
                if (dropGenericTable) {
                    genericTablesToDrop.add(tableName);
                }
            }
        }
        return genericTablesToDrop;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String generateAlterToTempTableSQL(String tableName) {
        return "alter table " + tableName + " rename to " + getTempTableName(tableName);
    }

    private String generateCreateNewTableSQL(Collection<String> removeColumnNames, TableModel tableModel) {
        for (String removeColumnName : removeColumnNames) {
            tableModel.removeColumnModelByName(removeColumnName);
        }
        return generateCreateTableSQL(tableModel);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String generateDataMigrationSQL(TableModel tableModel) {
        String tableName = tableModel.getTableName();
        List<ColumnModel> columnModels = tableModel.getColumnModels();
        if (!columnModels.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("insert into ");
            sql.append(tableName);
            sql.append("(");
            boolean needComma = false;
            for (ColumnModel columnModel : columnModels) {
                if (needComma) {
                    sql.append(", ");
                }
                needComma = true;
                sql.append(columnModel.getColumnName());
            }
            sql.append(") ");
            sql.append("select ");
            boolean needComma2 = false;
            for (ColumnModel columnModel2 : columnModels) {
                if (needComma2) {
                    sql.append(", ");
                }
                needComma2 = true;
                sql.append(columnModel2.getColumnName());
            }
            sql.append(" from ");
            sql.append(getTempTableName(tableName));
            return sql.toString();
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String generateDropTempTableSQL(String tableName) {
        return generateDropTableSQL(getTempTableName(tableName));
    }

    protected String getTempTableName(String tableName) {
        return String.valueOf(tableName) + "_temp";
    }

    private List<String> getRemoveColumnSQLs(Collection<String> removeColumnNames, String tableName) {
        TableModel tableModelFromDB = getTableModelFromDB(tableName);
        String alterToTempTableSQL = generateAlterToTempTableSQL(tableName);
        LogUtil.d(TAG, "generateRemoveColumnSQL >> " + alterToTempTableSQL);
        String createNewTableSQL = generateCreateNewTableSQL(removeColumnNames, tableModelFromDB);
        LogUtil.d(TAG, "generateRemoveColumnSQL >> " + createNewTableSQL);
        String dataMigrationSQL = generateDataMigrationSQL(tableModelFromDB);
        LogUtil.d(TAG, "generateRemoveColumnSQL >> " + dataMigrationSQL);
        String dropTempTableSQL = generateDropTempTableSQL(tableName);
        LogUtil.d(TAG, "generateRemoveColumnSQL >> " + dropTempTableSQL);
        List<String> sqls = new ArrayList<>();
        sqls.add(alterToTempTableSQL);
        sqls.add(createNewTableSQL);
        sqls.add(dataMigrationSQL);
        sqls.add(dropTempTableSQL);
        return sqls;
    }

    private boolean shouldDropForeignKey(String selfTableName, String associatedTableName) {
        for (AssociationsModel associationModel : this.mAssociationModels) {
            if (associationModel.getAssociationType() == 1) {
                if (!selfTableName.equalsIgnoreCase(associationModel.getTableHoldsForeignKey())) {
                    continue;
                } else if (associationModel.getTableName().equalsIgnoreCase(selfTableName)) {
                    if (isRelationCorrect(associationModel, selfTableName, associatedTableName)) {
                        return false;
                    }
                } else if (associationModel.getAssociatedTableName().equalsIgnoreCase(selfTableName) && isRelationCorrect(associationModel, associatedTableName, selfTableName)) {
                    return false;
                }
            } else if (associationModel.getAssociationType() == 2 && isRelationCorrect(associationModel, associatedTableName, selfTableName)) {
                return false;
            }
        }
        return true;
    }

    private boolean isRelationCorrect(AssociationsModel associationModel, String tableName1, String tableName2) {
        return associationModel.getTableName().equalsIgnoreCase(tableName1) && associationModel.getAssociatedTableName().equalsIgnoreCase(tableName2);
    }
}
