package org.litepal.crud;

import android.os.Handler;
import java.util.List;
import org.litepal.LitePal;
import org.litepal.crud.async.AverageExecutor;
import org.litepal.crud.async.CountExecutor;
import org.litepal.crud.async.FindExecutor;
import org.litepal.crud.async.FindMultiExecutor;
import org.litepal.tablemanager.Connector;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;
@Deprecated
/* loaded from: classes.dex */
public class ClusterQuery {
    String[] mColumns;
    String[] mConditions;
    String mLimit;
    String mOffset;
    String mOrderBy;

    @Deprecated
    public ClusterQuery select(String... columns) {
        this.mColumns = columns;
        return this;
    }

    @Deprecated
    public ClusterQuery where(String... conditions) {
        this.mConditions = conditions;
        return this;
    }

    @Deprecated
    public ClusterQuery order(String column) {
        this.mOrderBy = column;
        return this;
    }

    @Deprecated
    public ClusterQuery limit(int value) {
        this.mLimit = String.valueOf(value);
        return this;
    }

    @Deprecated
    public ClusterQuery offset(int value) {
        this.mOffset = String.valueOf(value);
        return this;
    }

    @Deprecated
    public <T> List<T> find(Class<T> modelClass) {
        return find(modelClass, false);
    }

    @Deprecated
    public <T> FindMultiExecutor findAsync(Class<T> modelClass) {
        return findAsync(modelClass, false);
    }

    @Deprecated
    public synchronized <T> List<T> find(Class<T> modelClass, boolean isEager) {
        QueryHandler queryHandler;
        String limit;
        queryHandler = new QueryHandler(Connector.getDatabase());
        if (this.mOffset == null) {
            String limit2 = this.mLimit;
            limit = limit2;
        } else {
            String limit3 = this.mLimit;
            if (limit3 == null) {
                this.mLimit = "0";
            }
            limit = String.valueOf(this.mOffset) + "," + this.mLimit;
        }
        return queryHandler.onFind(modelClass, this.mColumns, this.mConditions, this.mOrderBy, limit, isEager);
    }

    @Deprecated
    public <T> FindMultiExecutor findAsync(final Class<T> modelClass, final boolean isEager) {
        final FindMultiExecutor executor = new FindMultiExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.ClusterQuery.1
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final List find = ClusterQuery.this.find(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindMultiExecutor findMultiExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.ClusterQuery.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                findMultiExecutor.getListener().onFinish(find);
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
    public <T> T findFirst(Class<T> modelClass) {
        return (T) findFirst(modelClass, false);
    }

    @Deprecated
    public <T> FindExecutor findFirstAsync(Class<T> modelClass) {
        return findFirstAsync(modelClass, false);
    }

    @Deprecated
    public <T> T findFirst(Class<T> modelClass, boolean isEager) {
        List<T> list = find(modelClass, isEager);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Deprecated
    public <T> FindExecutor findFirstAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.ClusterQuery.2
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object findFirst = ClusterQuery.this.findFirst(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.ClusterQuery.2.1
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
    public <T> T findLast(Class<T> modelClass) {
        return (T) findLast(modelClass, false);
    }

    @Deprecated
    public <T> FindExecutor findLastAsync(Class<T> modelClass) {
        return findLastAsync(modelClass, false);
    }

    @Deprecated
    public <T> T findLast(Class<T> modelClass, boolean isEager) {
        List<T> list = find(modelClass, isEager);
        int size = list.size();
        if (size > 0) {
            return list.get(size - 1);
        }
        return null;
    }

    @Deprecated
    public <T> FindExecutor findLastAsync(final Class<T> modelClass, final boolean isEager) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.ClusterQuery.3
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object findLast = ClusterQuery.this.findLast(modelClass, isEager);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.ClusterQuery.3.1
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
    public synchronized int count(Class<?> modelClass) {
        return count(BaseUtility.changeCase(modelClass.getSimpleName()));
    }

    @Deprecated
    public CountExecutor countAsync(Class<?> modelClass) {
        return countAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())));
    }

    @Deprecated
    public synchronized int count(String tableName) {
        QueryHandler queryHandler;
        queryHandler = new QueryHandler(Connector.getDatabase());
        return queryHandler.onCount(tableName, this.mConditions);
    }

    @Deprecated
    public CountExecutor countAsync(final String tableName) {
        final CountExecutor executor = new CountExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.ClusterQuery.4
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final int count = ClusterQuery.this.count(tableName);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final CountExecutor countExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.ClusterQuery.4.1
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
    public synchronized double average(Class<?> modelClass, String column) {
        return average(BaseUtility.changeCase(modelClass.getSimpleName()), column);
    }

    @Deprecated
    public AverageExecutor averageAsync(Class<?> modelClass, String column) {
        return averageAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), column);
    }

    @Deprecated
    public synchronized double average(String tableName, String column) {
        QueryHandler queryHandler;
        queryHandler = new QueryHandler(Connector.getDatabase());
        return queryHandler.onAverage(tableName, column, this.mConditions);
    }

    @Deprecated
    public AverageExecutor averageAsync(final String tableName, final String column) {
        final AverageExecutor executor = new AverageExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.ClusterQuery.5
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final double average = ClusterQuery.this.average(tableName, column);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final AverageExecutor averageExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.ClusterQuery.5.1
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
    public synchronized <T> T max(Class<?> modelClass, String columnName, Class<T> columnType) {
        return (T) max(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
    }

    @Deprecated
    public <T> FindExecutor maxAsync(Class<?> modelClass, String columnName, Class<T> columnType) {
        return maxAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    @Deprecated
    public synchronized <T> T max(String tableName, String columnName, Class<T> columnType) {
        QueryHandler queryHandler;
        queryHandler = new QueryHandler(Connector.getDatabase());
        return (T) queryHandler.onMax(tableName, columnName, this.mConditions, columnType);
    }

    @Deprecated
    public <T> FindExecutor maxAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.ClusterQuery.6
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object max = ClusterQuery.this.max(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.ClusterQuery.6.1
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
    public synchronized <T> T min(Class<?> modelClass, String columnName, Class<T> columnType) {
        return (T) min(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
    }

    @Deprecated
    public <T> FindExecutor minAsync(Class<?> modelClass, String columnName, Class<T> columnType) {
        return minAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    @Deprecated
    public synchronized <T> T min(String tableName, String columnName, Class<T> columnType) {
        QueryHandler queryHandler;
        queryHandler = new QueryHandler(Connector.getDatabase());
        return (T) queryHandler.onMin(tableName, columnName, this.mConditions, columnType);
    }

    @Deprecated
    public <T> FindExecutor minAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.ClusterQuery.7
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object min = ClusterQuery.this.min(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.ClusterQuery.7.1
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
    public synchronized <T> T sum(Class<?> modelClass, String columnName, Class<T> columnType) {
        return (T) sum(BaseUtility.changeCase(modelClass.getSimpleName()), columnName, columnType);
    }

    @Deprecated
    public <T> FindExecutor sumAsync(Class<?> modelClass, String columnName, Class<T> columnType) {
        return sumAsync(BaseUtility.changeCase(DBUtility.getTableNameByClassName(modelClass.getName())), columnName, columnType);
    }

    @Deprecated
    public synchronized <T> T sum(String tableName, String columnName, Class<T> columnType) {
        QueryHandler queryHandler;
        queryHandler = new QueryHandler(Connector.getDatabase());
        return (T) queryHandler.onSum(tableName, columnName, this.mConditions, columnType);
    }

    @Deprecated
    public <T> FindExecutor sumAsync(final String tableName, final String columnName, final Class<T> columnType) {
        final FindExecutor executor = new FindExecutor();
        Runnable runnable = new Runnable() { // from class: org.litepal.crud.ClusterQuery.8
            @Override // java.lang.Runnable
            public void run() {
                synchronized (LitePalSupport.class) {
                    final Object sum = ClusterQuery.this.sum(tableName, columnName, columnType);
                    if (executor.getListener() != null) {
                        Handler handler = LitePal.getHandler();
                        final FindExecutor findExecutor = executor;
                        handler.post(new Runnable() { // from class: org.litepal.crud.ClusterQuery.8.1
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
}
