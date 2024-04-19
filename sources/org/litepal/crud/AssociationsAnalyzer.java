package org.litepal.crud;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.LitePalSupportException;
import org.litepal.util.DBUtility;
/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public abstract class AssociationsAnalyzer extends DataHandler {
    /* JADX INFO: Access modifiers changed from: protected */
    public Collection<LitePalSupport> getReverseAssociatedModels(LitePalSupport associatedModel, AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return (Collection) getFieldValue(associatedModel, associationInfo.getAssociateSelfFromOtherModel());
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void setReverseAssociatedModels(LitePalSupport associatedModel, AssociationsInfo associationInfo, Collection<LitePalSupport> associatedModelCollection) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        setFieldValue(associatedModel, associationInfo.getAssociateSelfFromOtherModel(), associatedModelCollection);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Collection<LitePalSupport> checkAssociatedModelCollection(Collection<LitePalSupport> associatedModelCollection, Field associatedField) {
        Collection<LitePalSupport> collection;
        if (isList(associatedField.getType())) {
            collection = new ArrayList<>();
        } else if (isSet(associatedField.getType())) {
            collection = new HashSet<>();
        } else {
            throw new LitePalSupportException(LitePalSupportException.WRONG_FIELD_TYPE_FOR_ASSOCIATIONS);
        }
        if (associatedModelCollection != null) {
            collection.addAll(associatedModelCollection);
        }
        return collection;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void buildBidirectionalAssociations(LitePalSupport baseObj, LitePalSupport associatedModel, AssociationsInfo associationInfo) throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        setFieldValue(associatedModel, associationInfo.getAssociateSelfFromOtherModel(), baseObj);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void dealsAssociationsOnTheSideWithoutFK(LitePalSupport baseObj, LitePalSupport associatedModel) {
        if (associatedModel != null) {
            if (associatedModel.isSaved()) {
                baseObj.addAssociatedModelWithFK(associatedModel.getTableName(), associatedModel.getBaseObjId());
            } else if (baseObj.isSaved()) {
                associatedModel.addAssociatedModelWithoutFK(baseObj.getTableName(), baseObj.getBaseObjId());
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public void mightClearFKValue(LitePalSupport baseObj, AssociationsInfo associationInfo) {
        baseObj.addFKNameToClearSelf(getForeignKeyName(associationInfo));
    }

    private String getForeignKeyName(AssociationsInfo associationInfo) {
        return getForeignKeyColumnName(DBUtility.getTableNameByClassName(associationInfo.getAssociatedClassName()));
    }
}
