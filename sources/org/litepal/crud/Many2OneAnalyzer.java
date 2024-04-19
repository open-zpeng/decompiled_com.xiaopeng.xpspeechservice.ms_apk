package org.litepal.crud;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.util.DBUtility;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class Many2OneAnalyzer extends AssociationsAnalyzer {
    /* JADX INFO: Access modifiers changed from: package-private */
    public void analyze(LitePalSupport baseObj, AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (baseObj.getClassName().equals(associationInfo.getClassHoldsForeignKey())) {
            analyzeManySide(baseObj, associationInfo);
        } else {
            analyzeOneSide(baseObj, associationInfo);
        }
    }

    private void analyzeManySide(LitePalSupport baseObj, AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        LitePalSupport associatedModel = getAssociatedModel(baseObj, associationInfo);
        if (associatedModel != null) {
            Collection<LitePalSupport> tempCollection = getReverseAssociatedModels(associatedModel, associationInfo);
            Collection<LitePalSupport> reverseAssociatedModels = checkAssociatedModelCollection(tempCollection, associationInfo.getAssociateSelfFromOtherModel());
            setReverseAssociatedModels(associatedModel, associationInfo, reverseAssociatedModels);
            dealAssociatedModelOnManySide(reverseAssociatedModels, baseObj, associatedModel);
            return;
        }
        mightClearFKValue(baseObj, associationInfo);
    }

    private void analyzeOneSide(LitePalSupport baseObj, AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Collection<LitePalSupport> associatedModels = getAssociatedModels(baseObj, associationInfo);
        if (associatedModels == null || associatedModels.isEmpty()) {
            String tableName = DBUtility.getTableNameByClassName(associationInfo.getAssociatedClassName());
            baseObj.addAssociatedTableNameToClearFK(tableName);
            return;
        }
        for (LitePalSupport associatedModel : associatedModels) {
            buildBidirectionalAssociations(baseObj, associatedModel, associationInfo);
            dealAssociatedModelOnOneSide(baseObj, associatedModel);
        }
    }

    private void dealAssociatedModelOnManySide(Collection<LitePalSupport> associatedModels, LitePalSupport baseObj, LitePalSupport associatedModel) {
        if (!associatedModels.contains(baseObj)) {
            associatedModels.add(baseObj);
        }
        if (associatedModel.isSaved()) {
            baseObj.addAssociatedModelWithoutFK(associatedModel.getTableName(), associatedModel.getBaseObjId());
        }
    }

    private void dealAssociatedModelOnOneSide(LitePalSupport baseObj, LitePalSupport associatedModel) {
        dealsAssociationsOnTheSideWithoutFK(baseObj, associatedModel);
    }
}
