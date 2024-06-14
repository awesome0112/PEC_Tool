package utils.autoUnitTestUtil.ast.additionalNodes;

import utils.autoUnitTestUtil.cfg.CfgNode;

public class Node {
    private CfgNode data;
    private Node next;

    public Node(CfgNode data) {
        setData(data);
        setNext(null);
    }

    public CfgNode getData() {
        return data;
    }

    public Node getNext() {
        return next;
    }

    public void setData(CfgNode data) {
        this.data = data;
    }

    public void setNext(Node next) {
        this.next = next;
    }
}
