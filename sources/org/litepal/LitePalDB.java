package org.litepal;

import java.util.ArrayList;
import java.util.List;
import org.litepal.parser.LitePalConfig;
import org.litepal.parser.LitePalParser;
/* loaded from: classes.dex */
public class LitePalDB {
    private List<String> classNames;
    private String dbName;
    private boolean isExternalStorage = false;
    private String storage;
    private int version;

    public static LitePalDB fromDefault(String dbName) {
        LitePalConfig config = LitePalParser.parseLitePalConfiguration();
        LitePalDB litePalDB = new LitePalDB(dbName, config.getVersion());
        litePalDB.setStorage(config.getStorage());
        litePalDB.setClassNames(config.getClassNames());
        return litePalDB;
    }

    public LitePalDB(String dbName, int version) {
        this.dbName = dbName;
        this.version = version;
    }

    public int getVersion() {
        return this.version;
    }

    public String getDbName() {
        return this.dbName;
    }

    public String getStorage() {
        return this.storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public boolean isExternalStorage() {
        return this.isExternalStorage;
    }

    public void setExternalStorage(boolean isExternalStorage) {
        this.isExternalStorage = isExternalStorage;
    }

    public List<String> getClassNames() {
        List<String> list = this.classNames;
        if (list == null) {
            this.classNames = new ArrayList();
            this.classNames.add("org.litepal.model.Table_Schema");
        } else if (list.isEmpty()) {
            this.classNames.add("org.litepal.model.Table_Schema");
        }
        return this.classNames;
    }

    public void addClassName(String className) {
        getClassNames().add(className);
    }

    void setClassNames(List<String> className) {
        this.classNames = className;
    }
}
