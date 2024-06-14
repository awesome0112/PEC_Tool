package utils.autoUnitTestUtil.algorithms;

import utils.autoUnitTestUtil.cfg.CfgBoolExprNode;
import utils.autoUnitTestUtil.cfg.CfgForEachExpressionNode;
import utils.autoUnitTestUtil.cfg.CfgNode;
import utils.autoUnitTestUtil.dataStructure.Path;

import java.util.ArrayList;
import java.util.List;

public class FindPath {

    private List<CfgNode> currentPath = new ArrayList<>();
    private Path path;
    private final int DEPTH = 1;
    private CfgNode currentDuplicateNode;

    private FindPath() {}

    public FindPath(CfgNode beginNode, CfgNode middleNode, CfgNode endNode) {
        findPath(beginNode, middleNode);
        Path firstHaft = path;
        path = null;
        findPath(middleNode.getAfterStatementNode(), endNode);
        Path lastHaft = path;
        if(lastHaft != null) {
            firstHaft.getCurrentLast().setNext(lastHaft.getCurrentFirst());
        }
        path = firstHaft;
    }

//    private void findPath(CfgNode beginNode, CfgNode endNode) {
//        if(beginNode == null || path != null) return;
//
//        // Add a path to the list of path if the node is endNode
//        if(beginNode == endNode) {
//            currentPath.add(beginNode);
//            path = new Path();
//            for(CfgNode node : currentPath) {
//                path.addLast(node);
//            }
//            return;
//        } else if (beginNode.getIsEndCfgNode()) {
//            return;
//        } else {
//            currentPath.add(beginNode);
//            if(beginNode instanceof CfgBoolExprNode) {
//                CfgBoolExprNode boolExprNode = (CfgBoolExprNode) beginNode;
//
//                CfgNode falseNode = boolExprNode.getFalseNode();
//                falseNode.setIsFalseNode(true);
//                findPath(falseNode, endNode);
//                // CfgBoolExprNode has 2 child node is trueNode and falseNode
//                if(beginNode != currentDuplicateNode) {
//                    currentDuplicateNode = beginNode;
//                    findPath(boolExprNode.getTrueNode(), endNode);
//                }
//
//            } else if(beginNode instanceof CfgForEachExpressionNode) {
//
//                // CfgForEachExpressionNode has 2 child node is hasElementNode and noMoreElementNode
//                if(beginNode != currentDuplicateNode) {
//                    currentDuplicateNode = beginNode;
//                    findPath(((CfgForEachExpressionNode) beginNode).getHasElementAfterNode(), endNode);
//                }
//                findPath(((CfgForEachExpressionNode) beginNode).getNoMoreElementAfterNode(), endNode);
//
//            } else {
//
//                // Every other node has only one child node
//                findPath(beginNode.getAfterStatementNode(), endNode);
//
//            }
//            currentPath.remove(currentPath.size() - 1);
//        }
//    }

    private void findPath(CfgNode beginNode, CfgNode endNode) {
        if(beginNode == null || path != null) return;

        // Add a path to the list of path if the node is endNode
        if(beginNode == endNode) {
            currentPath.add(beginNode);
            path = new Path();
            for(CfgNode node : currentPath) {
                path.addLast(node);
            }
            return;
        } else if (beginNode.getIsEndCfgNode()) {
            return;
        } else {
            int duplicateNode = numberOfDuplicateNode(beginNode);
            currentPath.add(beginNode);
            if(beginNode instanceof CfgBoolExprNode) {
                CfgBoolExprNode boolExprNode = (CfgBoolExprNode) beginNode;

                // CfgBoolExprNode has 2 child node is trueNode and falseNode
                if(duplicateNode < DEPTH) {
                    findPath(boolExprNode.getTrueNode(), endNode);
                }
                CfgNode falseNode = boolExprNode.getFalseNode();
                falseNode.setIsFalseNode(true);
                findPath(falseNode, endNode);

            } else if(beginNode instanceof CfgForEachExpressionNode) {

                // CfgForEachExpressionNode has 2 child node is hasElementNode and noMoreElementNode
                if(duplicateNode < DEPTH) {
                    findPath(((CfgForEachExpressionNode) beginNode).getHasElementAfterNode(), endNode);
                }
                findPath(((CfgForEachExpressionNode) beginNode).getNoMoreElementAfterNode(), endNode);

            } else {

                // Every other node has only one child node
                findPath(beginNode.getAfterStatementNode(), endNode);

            }
            currentPath.remove(currentPath.size() - 1);
        }
    }

    private int numberOfDuplicateNode(CfgNode node) {
        int duplicateNode = 0;
        for(CfgNode nodeI : currentPath) {
            if(nodeI == node) duplicateNode++;
        }
        return duplicateNode;
    }

    public Path getPath() {
        return path;
    }
}
