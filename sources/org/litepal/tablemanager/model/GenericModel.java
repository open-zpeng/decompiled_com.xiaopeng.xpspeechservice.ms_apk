package org.litepal.tablemanager.model;
/* loaded from: classes.dex */
public class GenericModel {
    private String getMethodName;
    private String tableName;
    private String valueColumnName;
    private String valueColumnType;
    private String valueIdColumnName;

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getValueColumnName() {
        return this.valueColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public String getValueColumnType() {
        return this.valueColumnType;
    }

    public void setValueColumnType(String valueColumnType) {
        this.valueColumnType = valueColumnType;
    }

    public String getValueIdColumnName() {
        return this.valueIdColumnName;
    }

    public void setValueIdColumnName(String valueIdColumnName) {
        this.valueIdColumnName = valueIdColumnName;
    }

    public String getGetMethodName() {
        return this.getMethodName;
    }

    public void setGetMethodName(String getMethodName) {
        this.getMethodName = getMethodName;
    }
}
