package org.litepal.crud;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import java.util.Collection;
import java.util.List;
import org.litepal.LitePal;
import org.litepal.crud.async.AverageExecutor;
import org.litepal.crud.async.CountExecutor;
import org.litepal.crud.async.FindExecutor;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.async.SaveExecutor;
import org.litepal.crud.async.UpdateOrDeleteExecutor;
import org.litepal.exceptions.DataSupportException;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;
@Deprecated
/* loaded from: classes.dex */
public class DataSupport extends LitePalSupport {
    @Deprecated
    public static synchronized ClusterQuery select(String... columns) {
        ClusterQuery cQuery;
        synchronized (DataSupport.class) {
            cQuery = new ClusterQuery();
            cQuery.mColumns = columns;
        }
        return cQuery;
    }

    @Deprecated
    public static synchronized ClusterQuery where(String... conditions) {
        ClusterQuery cQuery;
        synchronized (DataSupport.class) {
            cQuery = new ClusterQuery();
            cQuery.mConditions = conditions;
        }
        return cQuery;
    }

    @Deprecated
    public static synchronized ClusterQuery order(String column) {
        ClusterQuery cQuery;
        synchronized (DataSupport.class) {
            cQuery = new ClusterQuery();
            cQuery.mOrderBy = column;
        }
        return cQuery;
    }

    @Deprecated
    public static synchronized ClusterQuery limit(int value) {
        ClusterQuery cQuery;
        synchronized (DataSupport.class) {
            cQuery = new ClusterQuery();
            cQuery.mLimit = String.valueOf(value);
        }
        return cQuery;
    }

    @Deprecated
    public static synchronized ClusterQuery offset(int value) {
        ClusterQuery cQuery;
        synchronized (DataSupport.class) {
            cQuery = new ClusterQuery();
            cQuery.mOffset = String.valueOf(value);
        }
        return cQuery;
    }

    @Deprecated
    public static synchronized int count(Class<?> modelClass) {
        int count;
        synchronized (DataSupport.class) {
            count = count(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
        }
        return count;
    }

    @Deprecated
    public static CountExecutor countAsync(Class<?> modelClass) {
        return countAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
    }

    @Deprecated
    public static synchronized int count(String tableName) {
        int count;
        synchronized (DataSupport.class) {
            ClusterQuery cQuery = new ClusterQuery();
            count = cQuery.count(tableName);
        }
        return count;
    }

    @Deprecated
    public static CountExecutor countAsync(final String tableName) {
        final CountExecutor executor = new CountExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.1
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final int count = DataSupport.count(tableName);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final CountExecutor countExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                countExecutor.getListener().onFinish(count);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static synchronized double average(Class<?> modelClass, String column) {
        double average;
        synchronized (DataSupport.class) {
            average = average(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
        }
        return average;
    }

    @Deprecated
    public static AverageExecutor averageAsync(Class<?> modelClass, String column) {
        return averageAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
    }

    @Deprecated
    public static synchronized double average(String tableName, String column) {
        double average;
        synchronized (DataSupport.class) {
            ClusterQuery cQuery = new ClusterQuery();
            average = cQuery.average(tableName, column);
        }
        return average;
    }

    @Deprecated
    public static AverageExecutor averageAsync(final String tableName, final String column) {
        final AverageExecutor executor = new AverageExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.2
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final double average = DataSupport.average(tableName, column);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final AverageExecutor averageExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.2.1
                            @Override // java.lang.Runnable
                            public void run() {
                                averageExecutor.getListener().onFinish(average);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static synchronized <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
        T t;
        synchronized (DataSupport.class) {
            t = (T) max(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor maxAsync(Class<?> modelClass, String columnName, Class<T> columnType) {
        return maxAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    @Deprecated
    public static synchronized <T> T max(String tableName, String columnName, Class<T> columnType) {
        T t;
        synchronized (DataSupport.class) {
            ClusterQuery cQuery = new ClusterQuery();
            t = (T) cQuery.max(tableName, columnName, columnType);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor maxAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.3
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final Object max = DataSupport.max(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.3.1
                            @Override // java.lang.Runnable
                            public void run() {
                                findExecutor.getListener().onFinish(max);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static synchronized <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
        T t;
        synchronized (DataSupport.class) {
            t = (T) min(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor minAsync(Class<?> modelClass, String columnName, Class<T> columnType) {
        return minAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    @Deprecated
    public static synchronized <T> T min(String tableName, String columnName, Class<T> columnType) {
        T t;
        synchronized (DataSupport.class) {
            ClusterQuery cQuery = new ClusterQuery();
            t = (T) cQuery.min(tableName, columnName, columnType);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor minAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.4
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final Object min = DataSupport.min(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.4.1
                            @Override // java.lang.Runnable
                            public void run() {
                                findExecutor.getListener().onFinish(min);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static synchronized <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
        T t;
        synchronized (DataSupport.class) {
            t = (T) sum(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor sumAsync(Class<?> modelClass, String columnName, Class<T> columnType) {
        return sumAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    @Deprecated
    public static synchronized <T> T sum(String tableName, String columnName, Class<T> columnType) {
        T t;
        synchronized (DataSupport.class) {
            ClusterQuery cQuery = new ClusterQuery();
            t = (T) cQuery.sum(tableName, columnName, columnType);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor sumAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.5
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final Object sum = DataSupport.sum(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.5.1
                            @Override // java.lang.Runnable
                            public void run() {
                                findExecutor.getListener().onFinish(sum);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static synchronized <T> T find(Class<T> modelClass, long id) {
        T t;
        synchronized (DataSupport.class) {
            t = (T) find(modelClass, id, false);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor findAsync(Class<T> modelClass, long id) {
        return findAsync(modelClass, id, false);
    }

    @Deprecated
    public static synchronized <T> T find(Class<T> modelClass, long id, boolean isEager) {
        T t;
        synchronized (DataSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            t = (T) queryHandler.onFind(modelClass, id, isEager);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor findAsync(final Class<T> modelClass, final long id, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.6
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final Object find = DataSupport.find(modelClass, id, isEager);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.6.1
                            @Override // java.lang.Runnable
                            public void run() {
                                findExecutor.getListener().onFinish(find);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static synchronized <T> T findFirst(Class<T> modelClass) {
        T t;
        synchronized (DataSupport.class) {
            t = (T) findFirst(modelClass, false);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor findFirstAsync(Class<T> modelClass) {
        return findFirstAsync(modelClass, false);
    }

    @Deprecated
    public static synchronized <T> T findFirst(Class<T> modelClass, boolean isEager) {
        T t;
        synchronized (DataSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            t = (T) queryHandler.onFindFirst(modelClass, isEager);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor findFirstAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.7
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final Object findFirst = DataSupport.findFirst(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.7.1
                            @Override // java.lang.Runnable
                            public void run() {
                                findExecutor.getListener().onFinish(findFirst);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static synchronized <T> T findLast(Class<T> modelClass) {
        T t;
        synchronized (DataSupport.class) {
            t = (T) findLast(modelClass, false);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor findLastAsync(Class<T> modelClass) {
        return findLastAsync(modelClass, false);
    }

    @Deprecated
    public static synchronized <T> T findLast(Class<T> modelClass, boolean isEager) {
        T t;
        synchronized (DataSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            t = (T) queryHandler.onFindLast(modelClass, isEager);
        }
        return t;
    }

    @Deprecated
    public static <T> FindExecutor findLastAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.8
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final Object findLast = DataSupport.findLast(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.8.1
                            @Override // java.lang.Runnable
                            public void run() {
                                findExecutor.getListener().onFinish(findLast);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static synchronized <T> List<T> findAll(Class<T> modelClass, long... ids) {
        List<T> findAll;
        synchronized (DataSupport.class) {
            findAll = findAll(modelClass, false, ids);
        }
        return findAll;
    }

    @Deprecated
    public static <T> FindMultiExecutor findAllAsync(Class<T> modelClass, long... ids) {
        return findAllAsync(modelClass, false, ids);
    }

    @Deprecated
    public static synchronized <T> List<T> findAll(Class<T> modelClass, boolean isEager, long... ids) {
        List<T> onFindAll;
        synchronized (DataSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            onFindAll = queryHandler.onFindAll(modelClass, isEager, ids);
        }
        return onFindAll;
    }

    @Deprecated
    public static <T> FindMultiExecutor findAllAsync(final Class<T> modelClass, final boolean isEager, final long... ids) {
        final FindMultiExecutor executor = new FindMultiExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.9
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final List findAll = DataSupport.findAll(modelClass, isEager, ids);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindMultiExecutor findMultiExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.9.1
                            @Override // java.lang.Runnable
                            public void run() {
                                findMultiExecutor.getListener().onFinish(findAll);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static synchronized Cursor findBySQL(String... sql) {
        String[] selectionArgs;
        synchronized (DataSupport.class) {
            BaseUtility.checkConditionsCorrect(sql);
            if (sql == null) {
                return null;
            }
            if (sql.length <= 0) {
                return null;
            }
            if (sql.length == 1) {
                selectionArgs = null;
            } else {
                selectionArgs = new String[sql.length - 1];
                System.arraycopy(sql, 1, selectionArgs, 0, sql.length - 1);
            }
            return Connector.getDatabase().rawQuery(sql[0], selectionArgs);
        }
    }

    @Deprecated
    public static synchronized int delete(Class<?> modelClass, long id) {
        int rowsAffected;
        synchronized (DataSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            DeleteHandler deleteHandler = new DeleteHandler(db);
            rowsAffected = deleteHandler.onDelete(modelClass, id);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return rowsAffected;
    }

    @Deprecated
    public static UpdateOrDeleteExecutor deleteAsync(final Class<?> modelClass, final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.10
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = DataSupport.delete(modelClass, id);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.10.1
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

    @Deprecated
    public static synchronized int deleteAll(Class<?> modelClass, String... conditions) {
        int onDeleteAll;
        synchronized (DataSupport.class) {
            DeleteHandler deleteHandler = new DeleteHandler(Connector.getDatabase());
            onDeleteAll = deleteHandler.onDeleteAll(modelClass, conditions);
        }
        return onDeleteAll;
    }

    @Deprecated
    public static UpdateOrDeleteExecutor deleteAllAsync(final Class<?> modelClass, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.11
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = DataSupport.deleteAll(modelClass, conditions);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.11.1
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

    @Deprecated
    public static synchronized int deleteAll(String tableName, String... conditions) {
        int onDeleteAll;
        synchronized (DataSupport.class) {
            DeleteHandler deleteHandler = new DeleteHandler(Connector.getDatabase());
            onDeleteAll = deleteHandler.onDeleteAll(tableName, conditions);
        }
        return onDeleteAll;
    }

    @Deprecated
    public static UpdateOrDeleteExecutor deleteAllAsync(final String tableName, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.12
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = DataSupport.deleteAll(tableName, conditions);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.12.1
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

    @Deprecated
    public static synchronized int update(Class<?> modelClass, ContentValues values, long id) {
        int onUpdate;
        synchronized (DataSupport.class) {
            UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
            onUpdate = updateHandler.onUpdate(modelClass, id, values);
        }
        return onUpdate;
    }

    @Deprecated
    public static UpdateOrDeleteExecutor updateAsync(final Class<?> modelClass, final ContentValues values, final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.13
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = DataSupport.update(modelClass, values, id);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.13.1
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

    @Deprecated
    public static synchronized int updateAll(Class<?> modelClass, ContentValues values, String... conditions) {
        int updateAll;
        synchronized (DataSupport.class) {
            updateAll = updateAll(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), values, conditions);
        }
        return updateAll;
    }

    @Deprecated
    public static UpdateOrDeleteExecutor updateAllAsync(Class<?> modelClass, ContentValues values, String... conditions) {
        return updateAllAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), values, conditions);
    }

    @Deprecated
    public static synchronized int updateAll(String tableName, ContentValues values, String... conditions) {
        int onUpdateAll;
        synchronized (DataSupport.class) {
            UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
            onUpdateAll = updateHandler.onUpdateAll(tableName, values, conditions);
        }
        return onUpdateAll;
    }

    @Deprecated
    public static UpdateOrDeleteExecutor updateAllAsync(final String tableName, final ContentValues values, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.14
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = DataSupport.updateAll(tableName, values, conditions);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.14.1
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

    @Deprecated
    public static synchronized <T extends DataSupport> void saveAll(Collection<T> collection) {
        synchronized (DataSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                SaveHandler saveHandler = new SaveHandler(db);
                saveHandler.onSaveAll(collection);
                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (Exception e) {
                throw new DataSupportException(e.getMessage(), e);
            }
        }
    }

    @Deprecated
    public static <T extends DataSupport> SaveExecutor saveAllAsync(final Collection<T> collection) {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.15
            @Override // java.lang.Runnable
            public void run() {
                boolean success;
                synchronized (DataSupport.class) {
                    try {
                        DataSupport.saveAll(collection);
                        success = true;
                    } catch (Exception e) {
                        success = false;
                    }
                    final boolean result = success;
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final SaveExecutor saveExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.15.1
                            @Override // java.lang.Runnable
                            public void run() {
                                saveExecutor.getListener().onFinish(result);
                            }
                        });
                    }
                }
            }
        };
        executor.submit(runnable);
        return executor;
    }

    @Deprecated
    public static <T extends DataSupport> void markAsDeleted(Collection<T> collection) {
        for (T t : collection) {
            t.clearSavedState();
        }
    }

    @Deprecated
    public static <T> boolean isExist(Class<T> modelClass, String... conditions) {
        return conditions != null && where(conditions).count((Class<?>) modelClass) > 0;
    }

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public synchronized int delete() {
        int rowsAffected;
        SQLiteDatabase db = Connector.getDatabase();
        db.beginTransaction();
        DeleteHandler deleteHandler = new DeleteHandler(db);
        rowsAffected = deleteHandler.onDelete(this);
        this.baseObjId = 0L;
        db.setTransactionSuccessful();
        db.endTransaction();
        return rowsAffected;
    }

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public UpdateOrDeleteExecutor deleteAsync() {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.16
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = DataSupport.this.delete();
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.16.1
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

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public synchronized int update(long id) {
        int rowsAffected;
        try {
            UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
            rowsAffected = updateHandler.onUpdate(this, id);
            getFieldsToSetToDefault().clear();
        } catch (Exception e) {
            throw new DataSupportException(e.getMessage(), e);
        }
        return rowsAffected;
    }

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public UpdateOrDeleteExecutor updateAsync(final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.17
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = DataSupport.this.update(id);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.17.1
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

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public synchronized int updateAll(String... conditions) {
        int rowsAffected;
        try {
            UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
            rowsAffected = updateHandler.onUpdateAll(this, conditions);
            getFieldsToSetToDefault().clear();
        } catch (Exception e) {
            throw new DataSupportException(e.getMessage(), e);
        }
        return rowsAffected;
    }

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public UpdateOrDeleteExecutor updateAllAsync(final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.18
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final int rowsAffected = DataSupport.this.updateAll(conditions);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.18.1
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

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public synchronized boolean save() {
        try {
            saveThrows();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public SaveExecutor saveAsync() {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.19
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final boolean success = DataSupport.this.save();
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final SaveExecutor saveExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.19.1
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

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public synchronized void saveThrows() {
        SQLiteDatabase db = Connector.getDatabase();
        db.beginTransaction();
        try {
            SaveHandler saveHandler = new SaveHandler(db);
            saveHandler.onSave(this);
            clearAssociatedData();
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            try {
                throw new DataSupportException(e.getMessage(), e);
            } catch (Throwable th) {
                e = th;
                db.endTransaction();
                throw e;
            }
        } catch (Throwable th2) {
            e = th2;
            db.endTransaction();
            throw e;
        }
    }

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public synchronized boolean saveOrUpdate(String... conditions) {
        if (conditions == null) {
            return save();
        }
        List<DataSupport> list = where(conditions).find(getClass());
        if (list.isEmpty()) {
            return save();
        }
        SQLiteDatabase db = Connector.getDatabase();
        db.beginTransaction();
        try {
            for (DataSupport dataSupport : list) {
                this.baseObjId = dataSupport.getBaseObjId();
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

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public SaveExecutor saveOrUpdateAsync(final String... conditions) {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.DataSupport.20
            @Override // java.lang.Runnable
            public void run() {
                synchronized (DataSupport.class) {
                    final boolean success = DataSupport.this.saveOrUpdate(conditions);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final SaveExecutor saveExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.DataSupport.20.1
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

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public boolean isSaved() {
        return this.baseObjId > 0;
    }

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public void clearSavedState() {
        this.baseObjId = 0L;
    }

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public void setToDefault(String fieldName) {
        getFieldsToSetToDefault().add(fieldName);
    }

    @Override // org.litepal.crud.LitePalSupport
    @Deprecated
    public void assignBaseObjId(int baseObjId) {
        this.baseObjId = baseObjId;
    }

    @Deprecated
    protected DataSupport() {
    }
}
