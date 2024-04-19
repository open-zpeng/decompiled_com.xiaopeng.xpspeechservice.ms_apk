package org.litepal.crud;

import java.lang.reflect.InvocationTargetException;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.util.DBUtility;
/* loaded from: classes.dex */
public class One2OneAnalyzer extends AssociationsAnalyzer {
    /* JADX INFO: Access modifiers changed from: package-private */
    public void analyze(LitePalSupport baseObj, AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        LitePalSupport associatedModel = getAssociatedModel(baseObj, associationInfo);
        if (associatedModel != null) {
            buildBidirectionalAssociations(baseObj, associatedModel, associationInfo);
            dealAssociatedModel(baseObj, associatedModel, associationInfo);
            return;
        }
        String tableName = DBUtility.getTableNameByClassName(associationInfo.getAssociatedClassName());
        baseObj.addAssociatedTableNameToClearFK(tableName);
    }

    private void dealAssociatedModel(LitePalSupport baseObj, LitePalSupport associatedModel, AssociationsInfo associationInfo) {
        if (associationInfo.getAssociateSelfFromOtherModel() != null) {
            bidirectionalCondition(baseObj, associatedModel);
        } else {
            unidirectionalCondition(baseObj, associatedModel);
        }
    }

    private void bidirectionalCondition(LitePalSupport baseObj, LitePalSupport associatedModel) {
        if (associatedModel.isSaved()) {
            baseObj.addAssociatedModelWithFK(associatedModel.getTableName(), associatedModel.getBaseObjId());
            baseObj.addAssociatedModelWithoutFK(associatedModel.getTableName(), associatedModel.getBaseObjId());
        }
    }

    private void unidirectionalCondition(LitePalSupport baseObj, LitePalSupport associatedModel) {
        dealsAssociationsOnTheSideWithoutFK(baseObj, associatedModel);
    }
}
