package utils.cloneProjectUtil.projectTreeObjects;

public class Unit extends ProjectTreeObject {
    private String path;
    private String methodName;
    private String className;

    public Unit(String name, String path, String methodName, String className) {
        super.setName(name);
        this.path = path;
        this.methodName = methodName;
        this.className = className;
    }

    public String getPath() {
        return path;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClassName() {
        return className;
    }
}
