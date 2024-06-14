package utils.cloneProjectUtil.projectTreeObjects;

import java.util.ArrayList;
import java.util.List;

public class JavaFile extends ProjectTreeObject {

    private List<Unit> units;

    public JavaFile(String name) {
        units = new ArrayList<>();
        super.setName(name);
    }

    public void addUnit(Unit unit) {
        units.add(unit);
    }

    public List<Unit> getUnits() {
        return units;
    }
}
