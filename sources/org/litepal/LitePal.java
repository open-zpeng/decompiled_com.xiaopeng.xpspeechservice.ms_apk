package org.litepal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import java.io.File;
import java.util.Collection;
import java.util.List;
import org.litepal.crud.DeleteHandler;
import org.litepal.crud.LitePalSupport;
import org.litepal.crud.QueryHandler;
import org.litepal.crud.SaveHandler;
import org.litepal.crud.UpdateHandler;
import org.litepal.crud.async.AverageExecutor;
import org.litepal.crud.async.CountExecutor;
import org.litepal.crud.async.FindExecutor;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.crud.async.SaveExecutor;
import org.litepal.crud.async.UpdateOrDeleteExecutor;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.parser.LitePalAttr;
import org.litepal.parser.LitePalConfig;
import org.litepal.parser.LitePalParser;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.Const;
import org.litepal.util.DBUtility;
import org.litepal.util.SharedUtil;
import org.litepal.util.cipher.CipherUtil;
/* loaded from: classes.dex */
public class LitePal {
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void initialize(Context context) {
        LitePalApplication.sContext = context;
    }

    public static SQLiteDatabase getDatabase() {
        SQLiteDatabase database;
        synchronized (LitePalSupport.class) {
            database = Connector.getDatabase();
        }
        return database;
    }

    public static Handler getHandler() {
        return handler;
    }

    public static void use(LitePalDB litePalDB) {
        synchronized (LitePalSupport.class) {
            LitePalAttr litePalAttr = LitePalAttr.getInstance();
            litePalAttr.setDbName(litePalDB.getDbName());
            litePalAttr.setVersion(litePalDB.getVersion());
            litePalAttr.setStorage(litePalDB.getStorage());
            litePalAttr.setClassNames(litePalDB.getClassNames());
            if (!isDefaultDatabase(litePalDB.getDbName())) {
                litePalAttr.setExtraKeyName(litePalDB.getDbName());
                litePalAttr.setCases(Const.Config.CASES_LOWER);
            }
            Connector.clearLitePalOpenHelperInstance();
        }
    }

    public static void useDefault() {
        synchronized (LitePalSupport.class) {
            LitePalAttr.clearInstance();
            Connector.clearLitePalOpenHelperInstance();
        }
    }

    public static boolean deleteDatabase(String dbName) {
        synchronized (LitePalSupport.class) {
            if (TextUtils.isEmpty(dbName)) {
                return false;
            }
            if (!dbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                dbName = String.valueOf(dbName) + Const.Config.DB_NAME_SUFFIX;
            }
            File dbFile = LitePalApplication.getContext().getDatabasePath(dbName);
            if (dbFile.exists()) {
                boolean result = dbFile.delete();
                if (result) {
                    removeVersionInSharedPreferences(dbName);
                    Connector.clearLitePalOpenHelperInstance();
                }
                return result;
            }
            String path = LitePalApplication.getContext().getExternalFilesDir("") + "/databases/";
            boolean result2 = new File(String.valueOf(path) + dbName).delete();
            if (result2) {
                removeVersionInSharedPreferences(dbName);
                Connector.clearLitePalOpenHelperInstance();
            }
            return result2;
        }
    }

    public static void aesKey(String key) {
        CipherUtil.aesKey = key;
    }

    private static void removeVersionInSharedPreferences(String dbName) {
        if (isDefaultDatabase(dbName)) {
            SharedUtil.removeVersion(null);
        } else {
            SharedUtil.removeVersion(dbName);
        }
    }

    private static boolean isDefaultDatabase(String dbName) {
        if (BaseUtility.isLitePalXMLExists()) {
            if (!dbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                dbName = String.valueOf(dbName) + Const.Config.DB_NAME_SUFFIX;
            }
            LitePalConfig config = LitePalParser.parseLitePalConfiguration();
            String defaultDbName = config.getDbName();
            if (!defaultDbName.endsWith(Const.Config.DB_NAME_SUFFIX)) {
                defaultDbName = String.valueOf(defaultDbName) + Const.Config.DB_NAME_SUFFIX;
            }
            return dbName.equalsIgnoreCase(defaultDbName);
        }
        return false;
    }

    public static FluentQuery select(String... columns) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mColumns = columns;
        return cQuery;
    }

    public static FluentQuery where(String... conditions) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mConditions = conditions;
        return cQuery;
    }

    public static FluentQuery order(String column) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mOrderBy = column;
        return cQuery;
    }

    public static FluentQuery limit(int value) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mLimit = String.valueOf(value);
        return cQuery;
    }

    public static FluentQuery offset(int value) {
        FluentQuery cQuery = new FluentQuery();
        cQuery.mOffset = String.valueOf(value);
        return cQuery;
    }

    public static int count(Class<?> modelClass) {
        return count(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
    }

    public static CountExecutor countAsync(Class<?> modelClass) {
        return countAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
    }

    public static int count(String tableName) {
        int count;
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            count = cQuery.count(tableName);
        }
        return count;
    }

    public static CountExecutor countAsync(final String tableName) {
        final CountExecutor executor = new CountExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.1
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int count = LitePal.count(tableName);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final CountExecutor countExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.1.1
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

    public static double average(Class<?> modelClass, String column) {
        return average(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
    }

    public static AverageExecutor averageAsync(Class<?> modelClass, String column) {
        return averageAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
    }

    public static double average(String tableName, String column) {
        double average;
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            average = cQuery.average(tableName, column);
        }
        return average;
    }

    public static AverageExecutor averageAsync(final String tableName, final String column) {
        final AverageExecutor executor = new AverageExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.2
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final double average = LitePal.average(tableName, column);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final AverageExecutor averageExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.2.1
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

    public static <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
        return (T) max(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    public static <T> FindExecutor maxAsync(Class<?> modelClass, String columnName, Class<T> columnType) {
        return maxAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    public static <T> T max(String tableName, String columnName, Class<T> columnType) {
        T t;
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            t = (T) cQuery.max(tableName, columnName, columnType);
        }
        return t;
    }

    public static <T> FindExecutor maxAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.3
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object max = LitePal.max(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.3.1
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

    public static <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
        return (T) min(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    public static <T> FindExecutor minAsync(Class<?> modelClass, String columnName, Class<T> columnType) {
        return minAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    public static <T> T min(String tableName, String columnName, Class<T> columnType) {
        T t;
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            t = (T) cQuery.min(tableName, columnName, columnType);
        }
        return t;
    }

    public static <T> FindExecutor minAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.4
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object min = LitePal.min(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.4.1
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

    public static <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
        return (T) sum(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    public static <T> FindExecutor sumAsync(Class<?> modelClass, String columnName, Class<T> columnType) {
        return sumAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    public static <T> T sum(String tableName, String columnName, Class<T> columnType) {
        T t;
        synchronized (LitePalSupport.class) {
            FluentQuery cQuery = new FluentQuery();
            t = (T) cQuery.sum(tableName, columnName, columnType);
        }
        return t;
    }

    public static <T> FindExecutor sumAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.5
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object sum = LitePal.sum(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.5.1
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

    public static <T> T find(Class<T> modelClass, long id) {
        return (T) find(modelClass, id, false);
    }

    public static <T> FindExecutor findAsync(Class<T> modelClass, long id) {
        return findAsync(modelClass, id, false);
    }

    public static <T> T find(Class<T> modelClass, long id, boolean isEager) {
        T t;
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            t = (T) queryHandler.onFind(modelClass, id, isEager);
        }
        return t;
    }

    public static <T> FindExecutor findAsync(final Class<T> modelClass, final long id, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.6
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object find = LitePal.find(modelClass, id, isEager);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.6.1
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

    public static <T> T findFirst(Class<T> modelClass) {
        return (T) findFirst(modelClass, false);
    }

    public static <T> FindExecutor findFirstAsync(Class<T> modelClass) {
        return findFirstAsync(modelClass, false);
    }

    public static <T> T findFirst(Class<T> modelClass, boolean isEager) {
        T t;
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            t = (T) queryHandler.onFindFirst(modelClass, isEager);
        }
        return t;
    }

    public static <T> FindExecutor findFirstAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.7
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object findFirst = LitePal.findFirst(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.7.1
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

    public static <T> T findLast(Class<T> modelClass) {
        return (T) findLast(modelClass, false);
    }

    public static <T> FindExecutor findLastAsync(Class<T> modelClass) {
        return findLastAsync(modelClass, false);
    }

    public static <T> T findLast(Class<T> modelClass, boolean isEager) {
        T t;
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            t = (T) queryHandler.onFindLast(modelClass, isEager);
        }
        return t;
    }

    public static <T> FindExecutor findLastAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.8
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object findLast = LitePal.findLast(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.8.1
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

    public static <T> List<T> findAll(Class<T> modelClass, long... ids) {
        return findAll(modelClass, false, ids);
    }

    public static <T> FindMultiExecutor findAllAsync(Class<T> modelClass, long... ids) {
        return findAllAsync(modelClass, false, ids);
    }

    public static <T> List<T> findAll(Class<T> modelClass, boolean isEager, long... ids) {
        List<T> onFindAll;
        synchronized (LitePalSupport.class) {
            QueryHandler queryHandler = new QueryHandler(Connector.getDatabase());
            onFindAll = queryHandler.onFindAll(modelClass, isEager, ids);
        }
        return onFindAll;
    }

    public static <T> FindMultiExecutor findAllAsync(final Class<T> modelClass, final boolean isEager, final long... ids) {
        final FindMultiExecutor executor = new FindMultiExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.9
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final List findAll = LitePal.findAll(modelClass, isEager, ids);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final FindMultiExecutor findMultiExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.9.1
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

    public static Cursor findBySQL(String... sql) {
        String[] selectionArgs;
        synchronized (LitePalSupport.class) {
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

    public static int delete(Class<?> modelClass, long id) {
        int rowsAffected;
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            DeleteHandler deleteHandler = new DeleteHandler(db);
            rowsAffected = deleteHandler.onDelete(modelClass, id);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
        return rowsAffected;
    }

    public static UpdateOrDeleteExecutor deleteAsync(final Class<?> modelClass, final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.10
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = LitePal.delete(modelClass, id);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.10.1
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

    public static int deleteAll(Class<?> modelClass, String... conditions) {
        int onDeleteAll;
        synchronized (LitePalSupport.class) {
            DeleteHandler deleteHandler = new DeleteHandler(Connector.getDatabase());
            onDeleteAll = deleteHandler.onDeleteAll(modelClass, conditions);
        }
        return onDeleteAll;
    }

    public static UpdateOrDeleteExecutor deleteAllAsync(final Class<?> modelClass, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.11
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = LitePal.deleteAll(modelClass, conditions);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.11.1
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

    public static int deleteAll(String tableName, String... conditions) {
        int onDeleteAll;
        synchronized (LitePalSupport.class) {
            DeleteHandler deleteHandler = new DeleteHandler(Connector.getDatabase());
            onDeleteAll = deleteHandler.onDeleteAll(tableName, conditions);
        }
        return onDeleteAll;
    }

    public static UpdateOrDeleteExecutor deleteAllAsync(final String tableName, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.12
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = LitePal.deleteAll(tableName, conditions);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.12.1
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

    public static int update(Class<?> modelClass, ContentValues values, long id) {
        int onUpdate;
        synchronized (LitePalSupport.class) {
            UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
            onUpdate = updateHandler.onUpdate(modelClass, id, values);
        }
        return onUpdate;
    }

    public static UpdateOrDeleteExecutor updateAsync(final Class<?> modelClass, final ContentValues values, final long id) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.13
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = LitePal.update(modelClass, values, id);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.13.1
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

    public static int updateAll(Class<?> modelClass, ContentValues values, String... conditions) {
        return updateAll(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), values, conditions);
    }

    public static UpdateOrDeleteExecutor updateAllAsync(Class<?> modelClass, ContentValues values, String... conditions) {
        return updateAllAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), values, conditions);
    }

    public static int updateAll(String tableName, ContentValues values, String... conditions) {
        int onUpdateAll;
        synchronized (LitePalSupport.class) {
            UpdateHandler updateHandler = new UpdateHandler(Connector.getDatabase());
            onUpdateAll = updateHandler.onUpdateAll(tableName, values, conditions);
        }
        return onUpdateAll;
    }

    public static UpdateOrDeleteExecutor updateAllAsync(final String tableName, final ContentValues values, final String... conditions) {
        final UpdateOrDeleteExecutor executor = new UpdateOrDeleteExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.14
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int rowsAffected = LitePal.updateAll(tableName, values, conditions);
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final UpdateOrDeleteExecutor updateOrDeleteExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.14.1
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

    public static <T extends LitePalSupport> void saveAll(Collection<T> collection) {
        synchronized (LitePalSupport.class) {
            SQLiteDatabase db = Connector.getDatabase();
            db.beginTransaction();
            try {
                SaveHandler saveHandler = new SaveHandler(db);
                saveHandler.onSaveAll(collection);
                db.setTransactionSuccessful();
                db.endTransaction();
            } catch (Exception e) {
                throw new LitePalSupportException(e.getMessage(), e);
            }
        }
    }

    public static <T extends LitePalSupport> SaveExecutor saveAllAsync(final Collection<T> collection) {
        final SaveExecutor executor = new SaveExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.LitePal.15
            @Override // java.lang.Runnable
            public void run() {
                boolean success;
                synchronized (LitePalSupport.class) {
                    try {
                        LitePal.saveAll(collection);
                        success = true;
                    } catch (Exception e) {
                        success = false;
                    }
                    final boolean result = success;
                    if (executor.getListener() != null) {
                        Handler handler2 = LitePal.getHandler();
                        final SaveExecutor saveExecutor = executor;
                        handler2.post(new Runnable() { // from class: org.litepal.LitePal.15.1
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

    public static <T extends LitePalSupport> void markAsDeleted(Collection<T> collection) {
        for (T t : collection) {
            t.clearSavedState();
        }
    }

    public static <T> boolean isExist(Class<T> modelClass, String... conditions) {
        return conditions != null && where(conditions).count((Class<?>) modelClass) > 0;
    }
}
