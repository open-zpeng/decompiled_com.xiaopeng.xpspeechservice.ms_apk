package org.litepal.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.util.Const;
/* loaded from: classes.dex */
public class DBUtility {
    private static final String KEYWORDS_COLUMN_SUFFIX = "_lpcolumn";
    private static final String REG_COLLECTION = "\\s+(not\\s+)?(in)\\s*\\(";
    private static final String REG_FUZZY = "\\s+(not\\s+)?(like|between)\\s+";
    private static final String REG_OPERATOR = "\\s*(=|!=|<>|<|>)";
    private static final String SQLITE_KEYWORDS = ",abort,add,after,all,alter,and,as,asc,autoincrement,before,begin,between,by,cascade,check,collate,column,commit,conflict,constraint,create,cross,database,deferrable,deferred,delete,desc,distinct,drop,each,end,escape,except,exclusive,exists,foreign,from,glob,group,having,in,index,inner,insert,intersect,into,is,isnull,join,like,limit,match,natural,not,notnull,null,of,offset,on,or,order,outer,plan,pragma,primary,query,raise,references,regexp,reindex,release,rename,replace,restrict,right,rollback,row,savepoint,select,set,table,temp,temporary,then,to,transaction,trigger,union,unique,update,using,vacuum,values,view,virtual,when,where,";
    private static final String TAG = "DBUtility";

    private DBUtility() {
    }

    public static String getTableNameByClassName(String className) {
        if (TextUtils.isEmpty(className) || '.' == className.charAt(className.length() - 1)) {
            return null;
        }
        return className.substring(className.lastIndexOf(".") + 1);
    }

    public static List<String> getTableNameListByClassNameList(List<String> classNames) {
        List<String> tableNames = new ArrayList<>();
        if (classNames != null && !classNames.isEmpty()) {
            for (String className : classNames) {
                tableNames.add(getTableNameByClassName(className));
            }
        }
        return tableNames;
    }

    public static String getTableNameByForeignColumn(String foreignColumnName) {
        if (TextUtils.isEmpty(foreignColumnName) || !foreignColumnName.toLowerCase(Locale.US).endsWith("_id")) {
            return null;
        }
        return foreignColumnName.substring(0, foreignColumnName.length() - "_id".length());
    }

    public static String getIntermediateTableName(String tableName, String associatedTableName) {
        if (!TextUtils.isEmpty(tableName) && !TextUtils.isEmpty(associatedTableName)) {
            if (tableName.toLowerCase(Locale.US).compareTo(associatedTableName.toLowerCase(Locale.US)) <= 0) {
                String intermediateTableName = String.valueOf(tableName) + "_" + associatedTableName;
                return intermediateTableName;
            }
            String intermediateTableName2 = String.valueOf(associatedTableName) + "_" + tableName;
            return intermediateTableName2;
        }
        return null;
    }

    public static String getGenericTableName(String className, String fieldName) {
        String tableName = getTableNameByClassName(className);
        return BaseUtility.changeCase(String.valueOf(tableName) + "_" + fieldName);
    }

    public static String getGenericValueIdColumnName(String className) {
        return BaseUtility.changeCase(String.valueOf(getTableNameByClassName(className)) + "_id");
    }

    public static String getM2MSelfRefColumnName(Field field) {
        return BaseUtility.changeCase(String.valueOf(field.getName()) + "_id");
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x0035, code lost:
        r2 = r0.getInt(r0.getColumnIndexOrThrow(org.litepal.util.Const.TableSchema.COLUMN_TYPE));
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x0040, code lost:
        if (r2 != 1) goto L22;
     */
    /* JADX WARN: Code restructure failed: missing block: B:15:0x0043, code lost:
        r0.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0046, code lost:
        return true;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean isIntermediateTable(java.lang.String r9, android.database.sqlite.SQLiteDatabase r10) {
        /*
            boolean r0 = android.text.TextUtils.isEmpty(r9)
            if (r0 != 0) goto L60
            java.lang.String r0 = "[0-9a-zA-Z]+_[0-9a-zA-Z]+"
            boolean r0 = r9.matches(r0)
            if (r0 == 0) goto L60
            r0 = 0
            java.lang.String r2 = "table_schema"
            r3 = 0
            r4 = 0
            r5 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            r1 = r10
            android.database.Cursor r1 = r1.query(r2, r3, r4, r5, r6, r7, r8)     // Catch: java.lang.Throwable -> L4e java.lang.Exception -> L50
            r0 = r1
            boolean r1 = r0.moveToFirst()     // Catch: java.lang.Throwable -> L4e java.lang.Exception -> L50
            if (r1 == 0) goto L4d
        L23:
        L24:
            java.lang.String r1 = "name"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch: java.lang.Throwable -> L4e java.lang.Exception -> L50
            java.lang.String r1 = r0.getString(r1)     // Catch: java.lang.Throwable -> L4e java.lang.Exception -> L50
            boolean r2 = r9.equalsIgnoreCase(r1)     // Catch: java.lang.Throwable -> L4e java.lang.Exception -> L50
            if (r2 == 0) goto L47
        L35:
            java.lang.String r2 = "type"
            int r2 = r0.getColumnIndexOrThrow(r2)     // Catch: java.lang.Throwable -> L4e java.lang.Exception -> L50
            int r2 = r0.getInt(r2)     // Catch: java.lang.Throwable -> L4e java.lang.Exception -> L50
            r3 = 1
            if (r2 != r3) goto L4d
        L43:
            r0.close()
            return r3
        L47:
            boolean r1 = r0.moveToNext()     // Catch: java.lang.Throwable -> L4e java.lang.Exception -> L50
            if (r1 != 0) goto L23
        L4d:
            goto L56
        L4e:
            r1 = move-exception
            goto L5a
        L50:
            r1 = move-exception
            r1.printStackTrace()     // Catch: java.lang.Throwable -> L4e
            if (r0 == 0) goto L60
        L56:
            r0.close()
            goto L60
        L5a:
            if (r0 == 0) goto L5f
            r0.close()
        L5f:
            throw r1
        L60:
            r0 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.litepal.util.DBUtility.isIntermediateTable(java.lang.String, android.database.sqlite.SQLiteDatabase):boolean");
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x0035, code lost:
        r2 = r0.getInt(r0.getColumnIndexOrThrow(org.litepal.util.Const.TableSchema.COLUMN_TYPE));
     */
    /* JADX WARN: Code restructure failed: missing block: B:14:0x0040, code lost:
        if (r2 != 2) goto L22;
     */
    /* JADX WARN: Code restructure failed: missing block: B:15:0x0043, code lost:
        r0.close();
     */
    /* JADX WARN: Code restructure failed: missing block: B:16:0x0047, code lost:
        return true;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean isGenericTable(java.lang.String r9, android.database.sqlite.SQLiteDatabase r10) {
        /*
            boolean r0 = android.text.TextUtils.isEmpty(r9)
            if (r0 != 0) goto L61
            java.lang.String r0 = "[0-9a-zA-Z]+_[0-9a-zA-Z]+"
            boolean r0 = r9.matches(r0)
            if (r0 == 0) goto L61
            r0 = 0
            java.lang.String r2 = "table_schema"
            r3 = 0
            r4 = 0
            r5 = 0
            r6 = 0
            r7 = 0
            r8 = 0
            r1 = r10
            android.database.Cursor r1 = r1.query(r2, r3, r4, r5, r6, r7, r8)     // Catch: java.lang.Throwable -> L4f java.lang.Exception -> L51
            r0 = r1
            boolean r1 = r0.moveToFirst()     // Catch: java.lang.Throwable -> L4f java.lang.Exception -> L51
            if (r1 == 0) goto L4e
        L23:
        L24:
            java.lang.String r1 = "name"
            int r1 = r0.getColumnIndexOrThrow(r1)     // Catch: java.lang.Throwable -> L4f java.lang.Exception -> L51
            java.lang.String r1 = r0.getString(r1)     // Catch: java.lang.Throwable -> L4f java.lang.Exception -> L51
            boolean r2 = r9.equalsIgnoreCase(r1)     // Catch: java.lang.Throwable -> L4f java.lang.Exception -> L51
            if (r2 == 0) goto L48
        L35:
            java.lang.String r2 = "type"
            int r2 = r0.getColumnIndexOrThrow(r2)     // Catch: java.lang.Throwable -> L4f java.lang.Exception -> L51
            int r2 = r0.getInt(r2)     // Catch: java.lang.Throwable -> L4f java.lang.Exception -> L51
            r3 = 2
            if (r2 != r3) goto L4e
        L43:
            r0.close()
            r3 = 1
            return r3
        L48:
            boolean r1 = r0.moveToNext()     // Catch: java.lang.Throwable -> L4f java.lang.Exception -> L51
            if (r1 != 0) goto L23
        L4e:
            goto L57
        L4f:
            r1 = move-exception
            goto L5b
        L51:
            r1 = move-exception
            r1.printStackTrace()     // Catch: java.lang.Throwable -> L4f
            if (r0 == 0) goto L61
        L57:
            r0.close()
            goto L61
        L5b:
            if (r0 == 0) goto L60
            r0.close()
        L60:
            throw r1
        L61:
            r0 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.litepal.util.DBUtility.isGenericTable(java.lang.String, android.database.sqlite.SQLiteDatabase):boolean");
    }

    public static boolean isTableExists(String tableName, SQLiteDatabase db) {
        try {
            boolean exist = BaseUtility.containsIgnoreCases(findAllTableNames(db), tableName);
            return exist;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:22:0x0053, code lost:
        if (r1 == null) goto L18;
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean isColumnExists(java.lang.String r5, java.lang.String r6, android.database.sqlite.SQLiteDatabase r7) {
        /*
            boolean r0 = android.text.TextUtils.isEmpty(r5)
            if (r0 != 0) goto L5d
            boolean r0 = android.text.TextUtils.isEmpty(r6)
            if (r0 == 0) goto Ld
            goto L5d
        Ld:
            r0 = 0
            r1 = 0
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            java.lang.String r3 = "pragma table_info("
            r2.<init>(r3)     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            r2.append(r6)     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            java.lang.String r3 = ")"
            r2.append(r3)     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            java.lang.String r2 = r2.toString()     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            r3 = 0
            android.database.Cursor r3 = r7.rawQuery(r2, r3)     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            r1 = r3
            boolean r3 = r1.moveToFirst()     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            if (r3 == 0) goto L47
        L2e:
            java.lang.String r3 = "name"
            int r3 = r1.getColumnIndexOrThrow(r3)     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            java.lang.String r3 = r1.getString(r3)     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            boolean r4 = r5.equalsIgnoreCase(r3)     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            if (r4 == 0) goto L40
            r0 = 1
            goto L47
        L40:
            boolean r3 = r1.moveToNext()     // Catch: java.lang.Throwable -> L4c java.lang.Exception -> L4e
            if (r3 != 0) goto L2e
        L47:
        L48:
            r1.close()
            goto L56
        L4c:
            r2 = move-exception
            goto L57
        L4e:
            r2 = move-exception
            r2.printStackTrace()     // Catch: java.lang.Throwable -> L4c
            r0 = 0
            if (r1 == 0) goto L56
            goto L48
        L56:
            return r0
        L57:
            if (r1 == 0) goto L5c
            r1.close()
        L5c:
            throw r2
        L5d:
            r0 = 0
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.litepal.util.DBUtility.isColumnExists(java.lang.String, java.lang.String, android.database.sqlite.SQLiteDatabase):boolean");
    }

    public static List<String> findAllTableNames(SQLiteDatabase db) {
        List<String> tableNames = new ArrayList<>();
        Cursor cursor = null;
        try {
            try {
                cursor = db.rawQuery("select * from sqlite_master where type = ?", new String[]{"table"});
                if (cursor.moveToFirst()) {
                    do {
                        String tableName = cursor.getString(cursor.getColumnIndexOrThrow("tbl_name"));
                        if (!tableNames.contains(tableName)) {
                            tableNames.add(tableName);
                        }
                    } while (cursor.moveToNext());
                    cursor.close();
                    return tableNames;
                }
                cursor.close();
                return tableNames;
            } catch (Exception e) {
                e.printStackTrace();
                throw new DatabaseGenerateException(e.getMessage());
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
            throw th;
        }
    }

    public static TableModel findPragmaTableInfo(String tableName, SQLiteDatabase db) {
        if (isTableExists(tableName, db)) {
            List<String> uniqueColumns = findUniqueColumns(tableName, db);
            TableModel tableModelDB = new TableModel();
            tableModelDB.setTableName(tableName);
            String checkingColumnSQL = "pragma table_info(" + tableName + ")";
            Cursor cursor = null;
            try {
                try {
                    cursor = db.rawQuery(checkingColumnSQL, null);
                    if (cursor.moveToFirst()) {
                        do {
                            ColumnModel columnModel = new ColumnModel();
                            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                            String type = cursor.getString(cursor.getColumnIndexOrThrow(Const.TableSchema.COLUMN_TYPE));
                            boolean z = true;
                            if (cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) == 1) {
                                z = false;
                            }
                            boolean nullable = z;
                            boolean unique = uniqueColumns.contains(name);
                            String defaultValue = cursor.getString(cursor.getColumnIndexOrThrow("dflt_value"));
                            columnModel.setColumnName(name);
                            columnModel.setColumnType(type);
                            columnModel.setNullable(nullable);
                            columnModel.setUnique(unique);
                            columnModel.setDefaultValue(defaultValue != null ? defaultValue.replace("'", "") : "");
                            tableModelDB.addColumnModel(columnModel);
                        } while (cursor.moveToNext());
                        cursor.close();
                        return tableModelDB;
                    }
                    cursor.close();
                    return tableModelDB;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new DatabaseGenerateException(e.getMessage());
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                throw th;
            }
        }
        throw new DatabaseGenerateException(DatabaseGenerateException.TABLE_DOES_NOT_EXIST_WHEN_EXECUTING + tableName);
    }

    /* JADX WARN: Removed duplicated region for block: B:15:0x0070  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static java.util.List<java.lang.String> findUniqueColumns(java.lang.String r10, android.database.sqlite.SQLiteDatabase r11) {
        /*
            java.lang.String r0 = "name"
            java.lang.String r1 = ")"
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            r3 = 0
            r4 = 0
            java.lang.StringBuilder r5 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            java.lang.String r6 = "pragma index_list("
            r5.<init>(r6)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            r5.append(r10)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            r5.append(r1)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            java.lang.String r5 = r5.toString()     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            r6 = 0
            android.database.Cursor r5 = r11.rawQuery(r5, r6)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            r3 = r5
            boolean r5 = r3.moveToFirst()     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            if (r5 == 0) goto L6a
        L28:
            java.lang.String r5 = "unique"
            int r5 = r3.getColumnIndexOrThrow(r5)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            int r5 = r3.getInt(r5)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            r7 = 1
            if (r5 != r7) goto L64
            int r7 = r3.getColumnIndexOrThrow(r0)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            java.lang.String r7 = r3.getString(r7)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            java.lang.String r9 = "pragma index_info("
            r8.<init>(r9)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            r8.append(r7)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            r8.append(r1)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            java.lang.String r8 = r8.toString()     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            android.database.Cursor r8 = r11.rawQuery(r8, r6)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            r4 = r8
            boolean r8 = r4.moveToFirst()     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            if (r8 == 0) goto L64
            int r8 = r4.getColumnIndexOrThrow(r0)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            java.lang.String r8 = r4.getString(r8)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            r2.add(r8)     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
        L64:
            boolean r5 = r3.moveToNext()     // Catch: java.lang.Throwable -> L74 java.lang.Exception -> L76
            if (r5 != 0) goto L28
        L6a:
        L6b:
            r3.close()
            if (r4 == 0) goto L73
            r4.close()
        L73:
            return r2
        L74:
            r0 = move-exception
            goto L84
        L76:
            r0 = move-exception
            r0.printStackTrace()     // Catch: java.lang.Throwable -> L74
            org.litepal.exceptions.DatabaseGenerateException r1 = new org.litepal.exceptions.DatabaseGenerateException     // Catch: java.lang.Throwable -> L74
            java.lang.String r5 = r0.getMessage()     // Catch: java.lang.Throwable -> L74
            r1.<init>(r5)     // Catch: java.lang.Throwable -> L74
            throw r1     // Catch: java.lang.Throwable -> L74
        L84:
            if (r3 == 0) goto L89
            r3.close()
        L89:
            if (r4 == 0) goto L8e
            r4.close()
        L8e:
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.litepal.util.DBUtility.findUniqueColumns(java.lang.String, android.database.sqlite.SQLiteDatabase):java.util.List");
    }

    public static boolean isFieldNameConflictWithSQLiteKeywords(String fieldName) {
        if (!TextUtils.isEmpty(fieldName)) {
            String fieldNameWithComma = "," + fieldName.toLowerCase(Locale.US) + ",";
            if (SQLITE_KEYWORDS.contains(fieldNameWithComma)) {
                return true;
            }
            return false;
        }
        return false;
    }

    public static String convertToValidColumnName(String columnName) {
        if (isFieldNameConflictWithSQLiteKeywords(columnName)) {
            return String.valueOf(columnName) + KEYWORDS_COLUMN_SUFFIX;
        }
        return columnName;
    }

    public static String convertWhereClauseToColumnName(String whereClause) {
        if (!TextUtils.isEmpty(whereClause)) {
            try {
                StringBuffer convertedWhereClause = new StringBuffer();
                Pattern p = Pattern.compile("(\\w+\\s*(=|!=|<>|<|>)|\\w+\\s+(not\\s+)?(like|between)\\s+|\\w+\\s+(not\\s+)?(in)\\s*\\()");
                Matcher m = p.matcher(whereClause);
                while (m.find()) {
                    String matches = m.group();
                    String column = matches.replaceAll("(\\s*(=|!=|<>|<|>)|\\s+(not\\s+)?(like|between)\\s+|\\s+(not\\s+)?(in)\\s*\\()", "");
                    String rest = matches.replace(column, "");
                    m.appendReplacement(convertedWhereClause, String.valueOf(convertToValidColumnName(column)) + rest);
                }
                m.appendTail(convertedWhereClause);
                return convertedWhereClause.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return whereClause;
    }

    public static String[] convertSelectClauseToValidNames(String[] columns) {
        if (columns != null && columns.length > 0) {
            String[] convertedColumns = new String[columns.length];
            for (int i = 0; i < columns.length; i++) {
                convertedColumns[i] = convertToValidColumnName(columns[i]);
            }
            return convertedColumns;
        }
        return null;
    }

    public static String convertOrderByClauseToValidName(String orderBy) {
        if (!TextUtils.isEmpty(orderBy)) {
            String orderBy2 = orderBy.trim().toLowerCase(Locale.US);
            if (orderBy2.contains(",")) {
                String[] orderByItems = orderBy2.split(",");
                StringBuilder builder = new StringBuilder();
                boolean needComma = false;
                for (String orderByItem : orderByItems) {
                    if (needComma) {
                        builder.append(",");
                    }
                    builder.append(convertOrderByItem(orderByItem));
                    needComma = true;
                }
                return builder.toString();
            }
            return convertOrderByItem(orderBy2);
        }
        return null;
    }

    private static String convertOrderByItem(String orderByItem) {
        String column;
        String append;
        if (orderByItem.endsWith("asc")) {
            column = orderByItem.replace("asc", "").trim();
            append = " asc";
        } else if (orderByItem.endsWith("desc")) {
            column = orderByItem.replace("desc", "").trim();
            append = " desc";
        } else {
            column = orderByItem;
            append = "";
        }
        return String.valueOf(convertToValidColumnName(column)) + append;
    }
}
