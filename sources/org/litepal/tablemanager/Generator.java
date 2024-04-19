package org.litepal.tablemanager;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.litepal.LitePalBase;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.parser.LitePalAttr;
import org.litepal.tablemanager.model.AssociationsModel;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.BaseUtility;
/* loaded from: classes.dex */
public abstract class Generator extends LitePalBase {
    public static final String TAG = "Generator";
    private Collection<AssociationsModel> mAllRelationModels;
    private Collection<TableModel> mTableModels;

    protected abstract void addOrUpdateAssociation(SQLiteDatabase sQLiteDatabase, boolean z);

    protected abstract void createOrUpgradeTable(SQLiteDatabase sQLiteDatabase, boolean z);

    /* JADX INFO: Access modifiers changed from: protected */
    public Collection<TableModel> getAllTableModels() {
        if (this.mTableModels == null) {
            this.mTableModels = new ArrayList();
        }
        if (!canUseCache()) {
            this.mTableModels.clear();
            for (String className : LitePalAttr.getInstance().getClassNames()) {
                this.mTableModels.add(getTableModel(className));
            }
        }
        return this.mTableModels;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Collection<AssociationsModel> getAllAssociations() {
        Collection<AssociationsModel> collection = this.mAllRelationModels;
        if (collection == null || collection.isEmpty()) {
            this.mAllRelationModels = getAssociations(LitePalAttr.getInstance().getClassNames());
        }
        return this.mAllRelationModels;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void execute(List<String> sqls, SQLiteDatabase db) {
        String throwSQL = "";
        if (sqls != null) {
            try {
                if (!sqls.isEmpty()) {
                    for (String sql : sqls) {
                        if (!TextUtils.isEmpty(sql)) {
                            throwSQL = BaseUtility.changeCase(sql);
                            db.execSQL(throwSQL);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseGenerateException(DatabaseGenerateException.SQL_ERROR + throwSQL);
            }
        }
    }

    private static void addAssociation(SQLiteDatabase db, boolean force) {
        AssociationCreator associationsCreator = new Creator();
        associationsCreator.addOrUpdateAssociation(db, force);
    }

    private static void updateAssociations(SQLiteDatabase db) {
        AssociationUpdater associationUpgrader = new Upgrader();
        associationUpgrader.addOrUpdateAssociation(db, false);
    }

    private static void upgradeTables(SQLiteDatabase db) {
        Upgrader upgrader = new Upgrader();
        upgrader.createOrUpgradeTable(db, false);
    }

    private static void create(SQLiteDatabase db, boolean force) {
        Creator creator = new Creator();
        creator.createOrUpgradeTable(db, force);
    }

    private static void drop(SQLiteDatabase db) {
        Dropper dropper = new Dropper();
        dropper.createOrUpgradeTable(db, false);
    }

    private boolean canUseCache() {
        Collection<TableModel> collection = this.mTableModels;
        return collection != null && collection.size() == LitePalAttr.getInstance().getClassNames().size();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void create(SQLiteDatabase db) {
        create(db, true);
        addAssociation(db, true);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static void upgrade(SQLiteDatabase db) {
        drop(db);
        create(db, false);
        updateAssociations(db);
        upgradeTables(db);
        addAssociation(db, false);
    }
}
