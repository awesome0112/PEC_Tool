package utils.autoUnitTestUtil.dataStructure;

import utils.autoUnitTestUtil.ast.additionalNodes.Node;
import utils.autoUnitTestUtil.cfg.*;

public class Path {

    private Node currentFirst;

    private Node currentLast;

    public boolean isEmpty() {
        return currentFirst == null;
    }

    public void addLast(CfgNode data) {
        Node lastNode = currentLast;
        currentLast = new Node(data);
        if (isEmpty()) currentFirst = currentLast;
        else lastNode.setNext(currentLast);
    }

    public void addFirst(CfgNode data) {
        Node newNode = new Node(data);
        newNode.setNext(currentFirst);
        currentFirst = newNode;
    }

    @Override
    public String toString() {
        StringBuilder p = new StringBuilder("===============\n");
        Node tmpNode = currentFirst;
        while (tmpNode != null) {
            p.append(tmpNode.getData().toString());
            p.append("\n");
            tmpNode = tmpNode.getNext();
        }
        p.append("===============");
        return p.toString();
    }

    public Node getCurrentFirst() {
        return currentFirst;
    }

    public Node getCurrentLast() {
        return currentLast;
    }
}

