package org.litepal.tablemanager;

import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.BaseUtility;
/* loaded from: classes.dex */
public class Dropper extends AssociationUpdater {
    private Collection<TableModel> mTableModels;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.litepal.tablemanager.AssociationUpdater, org.litepal.tablemanager.Creator, org.litepal.tablemanager.AssociationCreator, org.litepal.tablemanager.Generator
    public void createOrUpgradeTable(SQLiteDatabase db, boolean force) {
        this.mTableModels = getAllTableModels();
        this.mDb = db;
        dropTables();
    }

    private void dropTables() {
        List<String> tableNamesToDrop = findTablesToDrop();
        dropTables(tableNamesToDrop, this.mDb);
        clearCopyInTableSchema(tableNamesToDrop);
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x005e, code lost:
        if (r1 == null) goto L13;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private java.util.List<java.lang.String> findTablesToDrop() {
        /*
            r10 = this;
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            r1 = 0
            android.database.sqlite.SQLiteDatabase r2 = r10.mDb     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            java.lang.String r3 = "table_schema"
            r4 = 0
            r5 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            r9 = 0
            android.database.Cursor r2 = r2.query(r3, r4, r5, r6, r7, r8, r9)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            r1 = r2
            boolean r2 = r1.moveToFirst()     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            if (r2 == 0) goto L53
        L1b:
        L1c:
            java.lang.String r2 = "name"
            int r2 = r1.getColumnIndexOrThrow(r2)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            java.lang.String r2 = r1.getString(r2)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            java.lang.String r3 = "type"
            int r3 = r1.getColumnIndexOrThrow(r3)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            int r3 = r1.getInt(r3)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            boolean r4 = r10.shouldDropThisTable(r2, r3)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            if (r4 == 0) goto L4d
            java.lang.String r4 = "AssociationUpdater"
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            java.lang.String r6 = "need to drop "
            r5.<init>(r6)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            r5.append(r2)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            java.lang.String r5 = r5.toString()     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            org.litepal.util.LogUtil.d(r4, r5)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            r0.add(r2)     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
        L4d:
            boolean r2 = r1.moveToNext()     // Catch: java.lang.Throwable -> L58 java.lang.Exception -> L5a
            if (r2 != 0) goto L1b
        L53:
        L54:
            r1.close()
            goto L61
        L58:
            r2 = move-exception
            goto L62
        L5a:
            r2 = move-exception
            r2.printStackTrace()     // Catch: java.lang.Throwable -> L58
            if (r1 == 0) goto L61
            goto L54
        L61:
            return r0
        L62:
            if (r1 == 0) goto L67
            r1.close()
        L67:
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: org.litepal.tablemanager.Dropper.findTablesToDrop():java.util.List");
    }

    private List<String> pickTableNamesFromTableModels() {
        List<String> tableNames = new ArrayList<>();
        for (TableModel tableModel : this.mTableModels) {
            tableNames.add(tableModel.getTableName());
        }
        return tableNames;
    }

    private boolean shouldDropThisTable(String tableName, int tableType) {
        return !BaseUtility.containsIgnoreCases(pickTableNamesFromTableModels(), tableName) && tableType == 0;
    }
}
