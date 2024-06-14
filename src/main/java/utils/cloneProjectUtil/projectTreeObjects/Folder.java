package utils.cloneProjectUtil.projectTreeObjects;

import java.util.ArrayList;
import java.util.List;

public class Folder extends ProjectTreeObject {
    private List<ProjectTreeObject> children;

    public Folder(String name) {
        children = new ArrayList<>();
        super.setName(name);
    }

    public void addChild(ProjectTreeObject child) {
        children.add(child);
    }

    public List<ProjectTreeObject> getChildren() {
        return children;
    }
}
