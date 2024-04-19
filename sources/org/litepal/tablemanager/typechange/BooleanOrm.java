package org.litepal.tablemanager.typechange;
/* loaded from: classes.dex */
public class BooleanOrm extends OrmChange {
    @Override // org.litepal.tablemanager.typechange.OrmChange
    public String object2Relation(String fieldType) {
        if (fieldType != null) {
            if (fieldType.equals("boolean") || fieldType.equals("java.lang.Boolean")) {
                return "integer";
            }
            return null;
        }
        return null;
    }
}
