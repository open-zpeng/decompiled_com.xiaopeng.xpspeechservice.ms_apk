package org.litepal.crud;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.litepal.LitePal;
import org.litepal.crud.async.SaveExecutor;
import org.litepal.crud.async.UpdateOrDeleteExecutor;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;
/* loaded from: classes.dex */
public class LitePalSupport {
    protected static final String AES = "AES";
    protected static final String MD5 = "MD5";
    Map<String, List<Long>> associatedModelsMapForJoinTable;
    private Map<String, Set<Long>> associatedModelsMapWithFK;
    private Map<String, Long> associatedModelsMapWithoutFK;
    long baseObjId;
    private List<String> fieldsToSetToDefault;
    private List<String> listToClearAssociatedFK;
    private List<String> listToClearSelfFK;

    public int delete() {
        int rowsAffected;
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            DeleteHandler deleteHandler = new DeleteHandler(db);
            rowsAffected = deleteHandler.onDelete(this);
            this.baseObjId = 0L;
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return rowsAffected;
    }

    public UpdateOrDeleteExecutor deleteAsync() {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.LitePalSupport.1
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = LitePalSupport.this.delete();
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.LitePalSupport.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                updateOrDeleteExecutor.getListener().onFinish(rowsAffected);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public int update(long id) {
        int rowsAffected;
        synchronized (LitePalSupport.class) {
            try {
                try {
                    UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
                    rowsAffected = updateHandler.onUpdate(this, id);
                    getFieldsToSetToDefault().clear();
                } catch (Exception e) {
                    throw new LitePalSupportException(e.getMessage(), e);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return rowsAffected;
    }

    public UpdateOrDeleteExecutor updateAsync(final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.LitePalSupport.2
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = LitePalSupport.this.update(id);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.LitePalSupport.2.1
                            @Override // java.lang.Runnable
                            public void run() {
                                updateOrDeleteExecutor.getListener().onFinish(rowsAffected);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public int updateAll(String... conditions) {
        int rowsAffected;
        synchronized (LitePalSupport.class) {
            try {
                try {
                    UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
                    rowsAffected = updateHandler.onUpdateAll(this, conditions);
                    getFieldsToSetToDefault().clear();
                } catch (Exception e) {
                    throw new LitePalSupportException(e.getMessage(), e);
                }
            } catch (Throwable th) {
                throw th;
            }
        }
        return rowsAffected;
    }

    public UpdateOrDeleteExecutor updateAllAsync(final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.LitePalSupport.3
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = LitePalSupport.this.updateAll(conditions);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.LitePalSupport.3.1
                            @Override // java.lang.Runnable
                            public void run() {
                                updateOrDeleteExecutor.getListener().onFinish(rowsAffected);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public boolean save() {
        try {
            saveThrows();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public SaveExecutor saveAsync() {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.LitePalSupport.4
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final boolean success = LitePalSupport.this.save();
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final SaveExecutor saveExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.LitePalSupport.4.1
                            @Override // java.lang.Runnable
                            public void run() {
                                saveExecutor.getListener().onFinish(success);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public void saveThrows() {
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                SaveHandler saveHandler = new SaveHandler(db);
                saveHandler.onSave(this);
                clearAssociatedData();
                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (Exception e) {
                throw new LitePalSupportException(e.getMessage(), e);
            }
        }
    }

    @Deprecated
    public boolean saveIfNotExist(String... conditions) {
        if (!LitePal.isExist(getClass(), conditions)) {
            return save();
        }
        return false;
    }

    public boolean saveOrUpdate(String... conditions) {
        synchronized (LitePalSupport.class) {
            if (conditions == null) {
                return save();
            }
            List<LitePalSupport> list = LitePal.where(conditions).find(getClass());
            if (list.isEmpty()) {
                return save();
            }
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                for (LitePalSupport support : list) {
                    this.baseObjId = support.getBaseObjId();
                    SaveHandler saveHandler = new SaveHandler(db);
                    saveHandler.onSave(this);
                    clearAssociatedData();
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                db.endTransaction();
                return false;
            }
        }
    }

    public SaveExecutor saveOrUpdateAsync(final String... conditions) {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.LitePalSupport.5
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final boolean success = LitePalSupport.this.saveOrUpdate(conditions);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final SaveExecutor saveExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.LitePalSupport.5.1
                            @Override // java.lang.Runnable
                            public void run() {
                                saveExecutor.getListener().onFinish(success);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    public boolean isSaved() {
        return this.baseObjId > 0;
    }

    public void clearSavedState() {
        this.baseObjId = 0L;
    }

    public void setToDefault(String fieldName) {
        getFieldsToSetToDefault().add(fieldName);
    }

    public void assignBaseObjId(int baseObjId) {
        this.baseObjId = baseObjId;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public long getBaseObjId() {
        return this.baseObjId;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getClassName() {
        return getClass().getName();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getTableName() {
        return BaseUtility.changeCase(DBUtility.getTableNameByClassName(getClassName()));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<String> getFieldsToSetToDefault() {
        if (this.fieldsToSetToDefault == null) {
            this.fieldsToSetToDefault = new ArrayList();
        }
        return this.fieldsToSetToDefault;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addAssociatedModelWithFK(String associatedTableName, long associatedId) {
        Set<Long> associatedIdsWithFKSet = getAssociatedModelsMapWithFK().get(associatedTableName);
        if (associatedIdsWithFKSet == null) {
            Set<Long> associatedIdsWithFKSet2 = new HashSet<>();
            associatedIdsWithFKSet2.add(Long.valueOf(associatedId));
            this.associatedModelsMapWithFK.put(associatedTableName, associatedIdsWithFKSet2);
            return;
        }
        associatedIdsWithFKSet.add(Long.valueOf(associatedId));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Map<String, Set<Long>> getAssociatedModelsMapWithFK() {
        if (this.associatedModelsMapWithFK == null) {
            this.associatedModelsMapWithFK = new HashMap();
        }
        return this.associatedModelsMapWithFK;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addAssociatedModelForJoinTable(String associatedModelName, long associatedId) {
        List<Long> associatedIdsM2MSet = getAssociatedModelsMapForJoinTable().get(associatedModelName);
        if (associatedIdsM2MSet == null) {
            List<Long> associatedIdsM2MSet2 = new ArrayList<>();
            associatedIdsM2MSet2.add(Long.valueOf(associatedId));
            this.associatedModelsMapForJoinTable.put(associatedModelName, associatedIdsM2MSet2);
            return;
        }
        associatedIdsM2MSet.add(Long.valueOf(associatedId));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addEmptyModelForJoinTable(String associatedModelName) {
        List<Long> associatedIdsM2MSet = getAssociatedModelsMapForJoinTable().get(associatedModelName);
        if (associatedIdsM2MSet == null) {
            List<Long> associatedIdsM2MSet2 = new ArrayList<>();
            this.associatedModelsMapForJoinTable.put(associatedModelName, associatedIdsM2MSet2);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Map<String, List<Long>> getAssociatedModelsMapForJoinTable() {
        if (this.associatedModelsMapForJoinTable == null) {
            this.associatedModelsMapForJoinTable = new HashMap();
        }
        return this.associatedModelsMapForJoinTable;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addAssociatedModelWithoutFK(String associatedTableName, long associatedId) {
        getAssociatedModelsMapWithoutFK().put(associatedTableName, Long.valueOf(associatedId));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public Map<String, Long> getAssociatedModelsMapWithoutFK() {
        if (this.associatedModelsMapWithoutFK == null) {
            this.associatedModelsMapWithoutFK = new HashMap();
        }
        return this.associatedModelsMapWithoutFK;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addFKNameToClearSelf(String foreignKeyName) {
        List<String> list = getListToClearSelfFK();
        if (!list.contains(foreignKeyName)) {
            list.add(foreignKeyName);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<String> getListToClearSelfFK() {
        if (this.listToClearSelfFK == null) {
            this.listToClearSelfFK = new ArrayList();
        }
        return this.listToClearSelfFK;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void addAssociatedTableNameToClearFK(String associatedTableName) {
        List<String> list = getListToClearAssociatedFK();
        if (!list.contains(associatedTableName)) {
            list.add(associatedTableName);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public List<String> getListToClearAssociatedFK() {
        if (this.listToClearAssociatedFK == null) {
            this.listToClearAssociatedFK = new ArrayList();
        }
        return this.listToClearAssociatedFK;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void clearAssociatedData() {
        clearIdOfModelWithFK();
        clearIdOfModelWithoutFK();
        clearIdOfModelForJoinTable();
        clearFKNameList();
    }

    private void clearIdOfModelWithFK() {
        for (String associatedModelName : getAssociatedModelsMapWithFK().keySet()) {
            this.associatedModelsMapWithFK.get(associatedModelName).clear();
        }
        this.associatedModelsMapWithFK.clear();
    }

    private void clearIdOfModelWithoutFK() {
        getAssociatedModelsMapWithoutFK().clear();
    }

    private void clearIdOfModelForJoinTable() {
        for (String associatedModelName : getAssociatedModelsMapForJoinTable().keySet()) {
            this.associatedModelsMapForJoinTable.get(associatedModelName).clear();
        }
        this.associatedModelsMapForJoinTable.clear();
    }

    private void clearFKNameList() {
        getListToClearSelfFK().clear();
        getListToClearAssociatedFK().clear();
    }
}
