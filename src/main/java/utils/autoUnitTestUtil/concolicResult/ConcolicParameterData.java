package utils.autoUnitTestUtil.concolicResult;

public class ConcolicParameterData {
    private String name;
    private String type;
    private String value;

    public ConcolicParameterData() {
    }

    public ConcolicParameterData(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return type + " " + name + " = " + value;
    }
}

