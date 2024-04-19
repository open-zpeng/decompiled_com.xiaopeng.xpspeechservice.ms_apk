package org.litepal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;
import org.litepal.crud.LitePalSupport;
import org.litepal.crud.model.AssociationsInfo;
import org.litepal.exceptions.DatabaseGenerateException;
import org.litepal.parser.LitePalAttr;
import org.litepal.tablemanager.model.AssociationsModel;
import org.litepal.tablemanager.model.ColumnModel;
import org.litepal.tablemanager.model.GenericModel;
import org.litepal.tablemanager.model.TableModel;
import org.litepal.tablemanager.typechange.BlobOrm;
import org.litepal.tablemanager.typechange.BooleanOrm;
import org.litepal.tablemanager.typechange.DateOrm;
import org.litepal.tablemanager.typechange.DecimalOrm;
import org.litepal.tablemanager.typechange.NumericOrm;
import org.litepal.tablemanager.typechange.OrmChange;
import org.litepal.tablemanager.typechange.TextOrm;
import org.litepal.util.BaseUtility;
import org.litepal.util.DBUtility;
/* loaded from: classes.dex */
public abstract class LitePalBase {
    private static final int GET_ASSOCIATIONS_ACTION = 1;
    private static final int GET_ASSOCIATION_INFO_ACTION = 2;
    public static final String TAG = "LitePalBase";
    private Collection<AssociationsInfo> mAssociationInfos;
    private Collection<AssociationsModel> mAssociationModels;
    private Collection<GenericModel> mGenericModels;
    private OrmChange[] typeChangeRules = {new NumericOrm(), new TextOrm(), new BooleanOrm(), new DecimalOrm(), new DateOrm(), new BlobOrm()};
    private Map<String, List<Field>> classFieldsMap = new HashMap();
    private Map<String, List<Field>> classGenericFieldsMap = new HashMap();

    /* JADX INFO: Access modifiers changed from: protected */
    public TableModel getTableModel(String className) {
        String tableName = DBUtility.getTableNameByClassName(className);
        TableModel tableModel = new TableModel();
        tableModel.setTableName(tableName);
        tableModel.setClassName(className);
        List<Field> supportedFields = getSupportedFields(className);
        for (Field field : supportedFields) {
            ColumnModel columnModel = convertFieldToColumnModel(field);
            tableModel.addColumnModel(columnModel);
        }
        return tableModel;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Collection<AssociationsModel> getAssociations(List<String> classNames) {
        if (this.mAssociationModels == null) {
            this.mAssociationModels = new HashSet();
        }
        if (this.mGenericModels == null) {
            this.mGenericModels = new HashSet();
        }
        this.mAssociationModels.clear();
        this.mGenericModels.clear();
        for (String className : classNames) {
            analyzeClassFields(className, 1);
        }
        return this.mAssociationModels;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Collection<GenericModel> getGenericModels() {
        return this.mGenericModels;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Collection<AssociationsInfo> getAssociationInfo(String className) {
        if (this.mAssociationInfos == null) {
            this.mAssociationInfos = new HashSet();
        }
        this.mAssociationInfos.clear();
        analyzeClassFields(className, 2);
        return this.mAssociationInfos;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public List<Field> getSupportedFields(String className) {
        List<Field> fieldList = this.classFieldsMap.get(className);
        if (fieldList == null) {
            List<Field> supportedFields = new ArrayList<>();
            try {
                Class<?> clazz = Class.forName(className);
                recursiveSupportedFields(clazz, supportedFields);
                this.classFieldsMap.put(className, supportedFields);
                return supportedFields;
            } catch (ClassNotFoundException e) {
                throw new DatabaseGenerateException(DatabaseGenerateException.CLASS_NOT_FOUND + className);
            }
        }
        return fieldList;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public List<Field> getSupportedGenericFields(String className) {
        List<Field> genericFieldList = this.classGenericFieldsMap.get(className);
        if (genericFieldList == null) {
            List<Field> supportedGenericFields = new ArrayList<>();
            try {
                Class<?> clazz = Class.forName(className);
                recursiveSupportedGenericFields(clazz, supportedGenericFields);
                this.classGenericFieldsMap.put(className, supportedGenericFields);
                return supportedGenericFields;
            } catch (ClassNotFoundException e) {
                throw new DatabaseGenerateException(DatabaseGenerateException.CLASS_NOT_FOUND + className);
            }
        }
        return genericFieldList;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isCollection(Class<?> fieldType) {
        return isList(fieldType) || isSet(fieldType);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isList(Class<?> fieldType) {
        return List.class.isAssignableFrom(fieldType);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isSet(Class<?> fieldType) {
        return Set.class.isAssignableFrom(fieldType);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public boolean isIdColumn(String columnName) {
        return "_id".equalsIgnoreCase(columnName) || "id".equalsIgnoreCase(columnName);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getForeignKeyColumnName(String associatedTableName) {
        return BaseUtility.changeCase(String.valueOf(associatedTableName) + "_id");
    }

    protected String getColumnType(String fieldType) {
        OrmChange[] ormChangeArr;
        for (OrmChange ormChange : this.typeChangeRules) {
            String columnType = ormChange.object2Relation(fieldType);
            if (columnType != null) {
                return columnType;
            }
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public Class<?> getGenericTypeClass(Field field) {
        Type genericType = field.getGenericType();
        if (genericType != null && (genericType instanceof ParameterizedType)) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            return (Class) parameterizedType.getActualTypeArguments()[0];
        }
        return null;
    }

    private void recursiveSupportedFields(Class<?> clazz, List<Field> supportedFields) {
        if (clazz == LitePalSupport.class || clazz == Object.class) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                Column annotation = (Column) field.getAnnotation(Column.class);
                if (annotation == null || !annotation.ignore()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers)) {
                        Class<?> fieldTypeClass = field.getType();
                        String fieldType = fieldTypeClass.getName();
                        if (BaseUtility.isFieldTypeSupported(fieldType)) {
                            supportedFields.add(field);
                        }
                    }
                }
            }
        }
        recursiveSupportedFields(clazz.getSuperclass(), supportedFields);
    }

    private void recursiveSupportedGenericFields(Class<?> clazz, List<Field> supportedGenericFields) {
        if (clazz == LitePalSupport.class || clazz == DataSupport.class || clazz == Object.class) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        if (fields != null && fields.length > 0) {
            for (Field field : fields) {
                Column annotation = (Column) field.getAnnotation(Column.class);
                if (annotation == null || !annotation.ignore()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && isCollection(field.getType())) {
                        String genericTypeName = getGenericTypeName(field);
                        if (BaseUtility.isGenericTypeSupported(genericTypeName) || clazz.getName().equalsIgnoreCase(genericTypeName)) {
                            supportedGenericFields.add(field);
                        }
                    }
                }
            }
        }
        recursiveSupportedGenericFields(clazz.getSuperclass(), supportedGenericFields);
    }

    private void analyzeClassFields(String className, int action) {
        Column annotation;
        try {
            Class<?> dynamicClass = Class.forName(className);
            Field[] fields = dynamicClass.getDeclaredFields();
            for (Field field : fields) {
                if (isNonPrimitive(field) && ((annotation = (Column) field.getAnnotation(Column.class)) == null || !annotation.ignore())) {
                    oneToAnyConditions(className, field, action);
                    manyToAnyConditions(className, field, action);
                }
            }
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new DatabaseGenerateException(DatabaseGenerateException.CLASS_NOT_FOUND + className);
        }
    }

    private boolean isNonPrimitive(Field field) {
        return !field.getType().isPrimitive();
    }

    private boolean isPrivate(Field field) {
        return Modifier.isPrivate(field.getModifiers());
    }

    private void oneToAnyConditions(String className, Field field, int action) throws ClassNotFoundException {
        Class<?> reverseDynamicClass;
        Field[] reverseFields;
        Class<?> fieldTypeClass = field.getType();
        if (LitePalAttr.getInstance().getClassNames().contains(fieldTypeClass.getName())) {
            Class<?> reverseDynamicClass2 = Class.forName(fieldTypeClass.getName());
            Field[] reverseFields2 = reverseDynamicClass2.getDeclaredFields();
            int length = reverseFields2.length;
            boolean reverseAssociations = false;
            int i = 0;
            while (i < length) {
                Field reverseField = reverseFields2[i];
                if (Modifier.isStatic(reverseField.getModifiers())) {
                    reverseDynamicClass = reverseDynamicClass2;
                    reverseFields = reverseFields2;
                } else {
                    Class<?> reverseFieldTypeClass = reverseField.getType();
                    if (className.equals(reverseFieldTypeClass.getName())) {
                        if (action == 1) {
                            addIntoAssociationModelCollection(className, fieldTypeClass.getName(), fieldTypeClass.getName(), 1);
                            reverseDynamicClass = reverseDynamicClass2;
                            reverseFields = reverseFields2;
                        } else if (action != 2) {
                            reverseDynamicClass = reverseDynamicClass2;
                            reverseFields = reverseFields2;
                        } else {
                            reverseDynamicClass = reverseDynamicClass2;
                            reverseFields = reverseFields2;
                            addIntoAssociationInfoCollection(className, fieldTypeClass.getName(), fieldTypeClass.getName(), field, reverseField, 1);
                        }
                        reverseAssociations = true;
                    } else {
                        reverseDynamicClass = reverseDynamicClass2;
                        reverseFields = reverseFields2;
                        if (isCollection(reverseFieldTypeClass)) {
                            String genericTypeName = getGenericTypeName(reverseField);
                            if (className.equals(genericTypeName)) {
                                if (action == 1) {
                                    addIntoAssociationModelCollection(className, fieldTypeClass.getName(), className, 2);
                                } else if (action == 2) {
                                    addIntoAssociationInfoCollection(className, fieldTypeClass.getName(), className, field, reverseField, 2);
                                }
                                reverseAssociations = true;
                            }
                        }
                    }
                }
                i++;
                reverseDynamicClass2 = reverseDynamicClass;
                reverseFields2 = reverseFields;
            }
            if (!reverseAssociations) {
                if (action == 1) {
                    addIntoAssociationModelCollection(className, fieldTypeClass.getName(), fieldTypeClass.getName(), 1);
                } else if (action == 2) {
                    addIntoAssociationInfoCollection(className, fieldTypeClass.getName(), fieldTypeClass.getName(), field, null, 1);
                }
            }
        }
    }

    private void manyToAnyConditions(String className, Field field, int action) throws ClassNotFoundException {
        int i;
        if (isCollection(field.getType())) {
            String genericTypeName = getGenericTypeName(field);
            int i2 = 1;
            if (LitePalAttr.getInstance().getClassNames().contains(genericTypeName)) {
                Class<?> reverseDynamicClass = Class.forName(genericTypeName);
                Field[] reverseFields = reverseDynamicClass.getDeclaredFields();
                int length = reverseFields.length;
                boolean reverseAssociations = false;
                int i3 = 0;
                while (i3 < length) {
                    Field reverseField = reverseFields[i3];
                    if (Modifier.isStatic(reverseField.getModifiers())) {
                        i = i3;
                    } else {
                        Class<?> reverseFieldTypeClass = reverseField.getType();
                        if (!className.equals(reverseFieldTypeClass.getName())) {
                            i = i3;
                            if (isCollection(reverseFieldTypeClass)) {
                                String reverseGenericTypeName = getGenericTypeName(reverseField);
                                if (className.equals(reverseGenericTypeName)) {
                                    if (action == 1) {
                                        if (className.equalsIgnoreCase(genericTypeName)) {
                                            GenericModel genericModel = new GenericModel();
                                            genericModel.setTableName(DBUtility.getGenericTableName(className, field.getName()));
                                            genericModel.setValueColumnName(DBUtility.getM2MSelfRefColumnName(field));
                                            genericModel.setValueColumnType("integer");
                                            genericModel.setValueIdColumnName(DBUtility.getGenericValueIdColumnName(className));
                                            this.mGenericModels.add(genericModel);
                                        } else {
                                            addIntoAssociationModelCollection(className, genericTypeName, null, 3);
                                        }
                                    } else if (action == 2 && !className.equalsIgnoreCase(genericTypeName)) {
                                        addIntoAssociationInfoCollection(className, genericTypeName, null, field, reverseField, 3);
                                    }
                                    reverseAssociations = true;
                                }
                            }
                        } else {
                            if (action == i2) {
                                addIntoAssociationModelCollection(className, genericTypeName, genericTypeName, 2);
                                i = i3;
                            } else if (action == 2) {
                                i = i3;
                                addIntoAssociationInfoCollection(className, genericTypeName, genericTypeName, field, reverseField, 2);
                            } else {
                                i = i3;
                            }
                            reverseAssociations = true;
                        }
                    }
                    i3 = i + 1;
                    i2 = 1;
                }
                if (!reverseAssociations) {
                    if (action == i2) {
                        addIntoAssociationModelCollection(className, genericTypeName, genericTypeName, 2);
                    } else if (action == 2) {
                        addIntoAssociationInfoCollection(className, genericTypeName, genericTypeName, field, null, 2);
                    }
                }
            } else if (BaseUtility.isGenericTypeSupported(genericTypeName) && action == 1) {
                GenericModel genericModel2 = new GenericModel();
                genericModel2.setTableName(DBUtility.getGenericTableName(className, field.getName()));
                genericModel2.setValueColumnName(DBUtility.convertToValidColumnName(field.getName()));
                genericModel2.setValueColumnType(getColumnType(genericTypeName));
                genericModel2.setValueIdColumnName(DBUtility.getGenericValueIdColumnName(className));
                this.mGenericModels.add(genericModel2);
            }
        }
    }

    private void addIntoAssociationModelCollection(String className, String associatedClassName, String classHoldsForeignKey, int associationType) {
        AssociationsModel associationModel = new AssociationsModel();
        associationModel.setTableName(DBUtility.getTableNameByClassName(className));
        associationModel.setAssociatedTableName(DBUtility.getTableNameByClassName(associatedClassName));
        associationModel.setTableHoldsForeignKey(DBUtility.getTableNameByClassName(classHoldsForeignKey));
        associationModel.setAssociationType(associationType);
        this.mAssociationModels.add(associationModel);
    }

    private void addIntoAssociationInfoCollection(String selfClassName, String associatedClassName, String classHoldsForeignKey, Field associateOtherModelFromSelf, Field associateSelfFromOtherModel, int associationType) {
        AssociationsInfo associationInfo = new AssociationsInfo();
        associationInfo.setSelfClassName(selfClassName);
        associationInfo.setAssociatedClassName(associatedClassName);
        associationInfo.setClassHoldsForeignKey(classHoldsForeignKey);
        associationInfo.setAssociateOtherModelFromSelf(associateOtherModelFromSelf);
        associationInfo.setAssociateSelfFromOtherModel(associateSelfFromOtherModel);
        associationInfo.setAssociationType(associationType);
        this.mAssociationInfos.add(associationInfo);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    public String getGenericTypeName(Field field) {
        Class<?> genericTypeClass = getGenericTypeClass(field);
        if (genericTypeClass != null) {
            return genericTypeClass.getName();
        }
        return null;
    }

    private ColumnModel convertFieldToColumnModel(Field field) {
        String fieldType = field.getType().getName();
        String columnType = getColumnType(fieldType);
        boolean nullable = true;
        boolean unique = false;
        String defaultValue = "";
        Column annotation = (Column) field.getAnnotation(Column.class);
        if (annotation != null) {
            nullable = annotation.nullable();
            unique = annotation.unique();
            defaultValue = annotation.defaultValue();
        }
        ColumnModel columnModel = new ColumnModel();
        columnModel.setColumnName(DBUtility.convertToValidColumnName(field.getName()));
        columnModel.setColumnType(columnType);
        columnModel.setNullable(nullable);
        columnModel.setUnique(unique);
        columnModel.setDefaultValue(defaultValue);
        return columnModel;
    }
}
