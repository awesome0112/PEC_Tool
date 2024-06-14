package utils.cloneProjectUtil;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ClassData {

    private String typeOfClass;
    private String className;
    private String superClassName;
    private List<String> superInterfaceName;

    private String fields = "";

    ClassData() {
    }

    ClassData(TypeDeclaration typeDeclaration) {
        if(typeDeclaration.isInterface()) {
            typeOfClass = "interface";
        } else {
            typeOfClass = "class";
        }
        className = typeDeclaration.getName().getIdentifier();
        if(typeDeclaration.getSuperclassType() != null) {
            superClassName = typeDeclaration.getSuperclassType().toString();
        }
        if(typeDeclaration.superInterfaceTypes().size() != 0) {
            List interfaceList = typeDeclaration.superInterfaceTypes();
            superInterfaceName = new ArrayList<>();
            for (int i = 0; i < interfaceList.size(); i++) {
                superInterfaceName.add(interfaceList.get(i).toString());
            }
        }

        FieldDeclaration[] fieldDeclarations = typeDeclaration.getFields();
        StringBuilder extractedFields = new StringBuilder();
        for(FieldDeclaration fieldDeclaration : fieldDeclarations) {
            extractedFields.append(fieldDeclaration).append("\n");
        }
        fields = extractedFields.toString();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public String getTypeOfClass() {
        return typeOfClass;
    }

    public void setTypeOfClass(String typeOfClass) {
        this.typeOfClass = typeOfClass;
    }

    public List<String> getSuperInterfaceName() {
        return superInterfaceName;
    }

    public void setSuperInterfaceName(List<String> superInterfaceName) {
        this.superInterfaceName = superInterfaceName;
    }

    public String getFields() {
        return fields;
    }
}

