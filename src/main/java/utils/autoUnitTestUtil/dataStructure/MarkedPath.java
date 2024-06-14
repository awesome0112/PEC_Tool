package utils.autoUnitTestUtil.dataStructure;

import utils.autoUnitTestUtil.ast.additionalNodes.Node;
import utils.autoUnitTestUtil.cfg.CfgBoolExprNode;
import utils.autoUnitTestUtil.cfg.CfgNode;
import utils.autoUnitTestUtil.concolicResult.CoveredStatement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MarkedPath {

    private static List<MarkedStatement> markedStatements = new ArrayList<>();
    private static Set<CoveredStatement> fullTestSuiteCoveredStatements;
    private static Set<CoveredStatement> totalCoveredStatement;
    private static Set<CoveredStatement> totalCoveredBranch;

    private MarkedPath() {
    }

    public static boolean markOneStatement(String statement, boolean isTrueCondition, boolean isFalseCondition) {
        addNewStatementToPath(statement, isTrueCondition, isFalseCondition);
        if (!isTrueCondition && !isFalseCondition) return true;
        return !isFalseCondition;
    }

    private static void addNewStatementToPath(String statement, boolean isTrueCondition, boolean isFalseCondition) {
        MarkedStatement markedStatement = new MarkedStatement(statement, isTrueCondition, isFalseCondition);
        markedStatements.add(markedStatement);
    }

    public static void setMarkedStatements(List<MarkedStatement> markedStatements) {
        MarkedPath.markedStatements = markedStatements;
    }

    private static void reset() {
        markedStatements = new ArrayList<>();
    }

    public static void markPathToCFGV2(CfgNode rootNode, List<MarkedStatement> markedStatements) {
        totalCoveredBranch = new HashSet<>();
        totalCoveredStatement = new HashSet<>();

        int i = 0;
        while (rootNode != null && i < markedStatements.size()) {
            // Kiểm tra những CfgNode không có content
            if (rootNode.getContent().equals("")) {
                rootNode.setMarked(true);
                rootNode = rootNode.getAfterStatementNode();
                continue;
            }

            MarkedStatement markedStatement = markedStatements.get(i);
            if (rootNode.getContent().equals(markedStatement.getStatement())) {
//                if (!rootNode.isMarked()) {
//                }
                fullTestSuiteCoveredStatements.add(new CoveredStatement(rootNode.getContent(), rootNode.getLineNumber(), ""));
                totalCoveredStatement.add(new CoveredStatement(rootNode.getContent(), rootNode.getLineNumber(), ""));
                rootNode.setMarked(true);
                markedStatement.setCfgNode(rootNode);
            } else {
                return;
            }

            if (rootNode instanceof CfgBoolExprNode) {
                CfgBoolExprNode boolExprNode = (CfgBoolExprNode) rootNode;
                if (markedStatement.isFalseConditionalStatement()) {
//                    if (!boolExprNode.isFalseMarked()) {
//                    }
                    totalCoveredBranch.add(new CoveredStatement(boolExprNode.getContent(), boolExprNode.getLineNumber(), "false"));
                    boolExprNode.setFalseMarked(true);
                    rootNode = boolExprNode.getFalseNode();
                } else if (markedStatement.isTrueConditionalStatement()) {
//                    if (!boolExprNode.isTrueMarked()) {
//                    }
                    totalCoveredBranch.add(new CoveredStatement(boolExprNode.getContent(), boolExprNode.getLineNumber(), "true"));
                    boolExprNode.setTrueMarked(true);
                    rootNode = boolExprNode.getTrueNode();
                }
                i++;
                continue;
            }

            // Updater
            i++;
            rootNode = rootNode.getAfterStatementNode();
        }
        while (rootNode != null) {
            if (rootNode.getContent().equals("")) {
                rootNode.setMarked(true);
                rootNode = rootNode.getAfterStatementNode();
            }
        }
    }

//    public static List<MarkedStatement> markPathToCFG(CfgNode rootNode) {
////        List<CfgNode> coveredStatements = new ArrayList<>();
//        List<MarkedStatement> result = markedStatements;
//        totalCoveredBranch = 0;
//        totalCoveredStatement = 0;
//
//        int i = 0;
//        while (rootNode != null && i < markedStatements.size()) {
//            // Kiểm tra những CfgNode không có content
//            if (rootNode.getContent().equals("")) {
//                rootNode.setMarked(true);
//                rootNode = rootNode.getAfterStatementNode();
//                continue;
//            }
//
//            MarkedStatement markedStatement = markedStatements.get(i);
//            if (rootNode.getContent().equals(markedStatement.getStatement())) {
//                if (!rootNode.isMarked()) {
//                    System.out.println(rootNode);
//                    totalCoveredStatement++;
//                    fullTestSuiteCoveredStatements.add(rootNode.getContent());
//                }
//                rootNode.setMarked(true);
//                markedStatement.setCfgNode(rootNode);
////                coveredStatements.add(rootNode);
//
//            } else {
//                reset();
//                return result;
////                return coveredStatements;
//            }
//
//            if (rootNode instanceof CfgBoolExprNode) {
//                CfgBoolExprNode boolExprNode = (CfgBoolExprNode) rootNode;
//                if (markedStatement.isFalseConditionalStatement()) {
//                    if (!boolExprNode.isFalseMarked()) {
//                        totalCoveredBranch++;
//                    }
//                    boolExprNode.setFalseMarked(true);
//                    rootNode = boolExprNode.getFalseNode();
//                } else if (markedStatement.isTrueConditionalStatement()) {
//                    if (!boolExprNode.isTrueMarked()) {
//                        totalCoveredBranch++;
//                    }
//                    boolExprNode.setTrueMarked(true);
//                    rootNode = boolExprNode.getTrueNode();
//                }
//                i++;
//                continue;
//            }
//
//            // Updater
//            i++;
//            rootNode = rootNode.getAfterStatementNode();
//        }
//        while (rootNode != null) {
//            if (rootNode.getContent().equals("")) {
//                rootNode.setMarked(true);
//                rootNode = rootNode.getAfterStatementNode();
//            }
//        }
//
//        reset();
//        return result;
////        return coveredStatements;
//    }

    public static int getTotalCoveredStatement() {
        return totalCoveredStatement.size();
    }

    public static int getTotalCoveredBranch() {
        return totalCoveredBranch.size();
    }

    public static void resetFullTestSuiteCoveredStatements() {
        fullTestSuiteCoveredStatements = new HashSet<>();
    }

    public static int getFullTestSuiteTotalCoveredStatements() {
        return fullTestSuiteCoveredStatements.size();
    }

//    public static List<MarkedStatement> isPathActuallyCovered(Path path) {
//        totalCoveredStatement = 0;
//        int i = 0;
//        Node currentNode = path.getCurrentFirst();
//        while (currentNode != null && i < markedStatements.size()) {
//            CfgNode cfgNode = currentNode.getData();
//            if (cfgNode.getContent().equals("")) {
//                currentNode = currentNode.getNext();
//                continue;
//            }
//
//            if (!cfgNode.getContent().equals(markedStatements.get(i).getStatement())) {
//                reset();
//                return null;
//            } else {
//                markedStatements.get(i).setCfgNode(cfgNode);
//                totalCoveredStatement++;
//                fullTestSuiteCoveredStatements.add(cfgNode.getContent());
//            }
//
//            // Updater
//            i++;
//            currentNode = currentNode.getNext();
//        }
//
//        List<MarkedStatement> result = markedStatements;
//        reset();
//        return result;
//    }

//    public static List<String> getMarkedStatementsStringList() {
//        List<String> result = new ArrayList<>();
//
//        for (MarkedStatement markedStatement : markedStatements) {
//            result.add(markedStatement.getStatement());
//        }
//
//        return result;
//    }

//    public static CfgNode findUncoveredNode(CfgNode rootNode, CfgNode duplicateNode) {
//        if (rootNode == null || !rootNode.isMarked()) {
//            return rootNode;
//        }
//        if (rootNode instanceof CfgBoolExprNode) {
//            CfgBoolExprNode boolExprNode = (CfgBoolExprNode) rootNode;
//
//
//            if (!boolExprNode.isTrueMarked()) {
//                return boolExprNode.getTrueNode();
//            }
//            if (!boolExprNode.isFalseMarked()) {
//                return boolExprNode.getFalseNode();
//            }
//
//            if (boolExprNode != duplicateNode) {
//                duplicateNode = boolExprNode;
//                return findUncoveredNode(boolExprNode.getTrueNode(), duplicateNode);
//            } else {
//                return findUncoveredNode(boolExprNode.getFalseNode(), duplicateNode);
//            }
//        }
//
//        CfgNode cfgNode = findUncoveredNode(rootNode.getAfterStatementNode(), duplicateNode);
//        return cfgNode;
//    }

//    public static CfgNode findUncoveredNode(CfgNode rootNode, CfgNode duplicateNode) {
//        if (rootNode == null || !rootNode.isMarked()) {
//            return rootNode;
//        }
//        if (rootNode instanceof CfgBoolExprNode) {
//            CfgBoolExprNode boolExprNode = (CfgBoolExprNode) rootNode;
//
//            // Check for duplicateNode. Nếu có node trùng lặp tức là đã duyệt qua 1 vòng của loop đấy và k thấy node chưa mark nên return null.
//            if(boolExprNode != duplicateNode) duplicateNode = boolExprNode;
//            else {
//                return null;
//            }
//
//            if (!boolExprNode.isTrueMarked()) {
//                return boolExprNode.getTrueNode();
//            }
//            if (!boolExprNode.isFalseMarked()) {
//                return boolExprNode.getFalseNode();
//            }
//
//            CfgNode falseBranchUncoveredNode = findUncoveredNode(boolExprNode.getFalseNode(), duplicateNode);
//            CfgNode trueBranchUncoveredNode = findUncoveredNode(boolExprNode.getTrueNode(), duplicateNode);
////            if(tmpUncoveredNode == null) {
////                return findUncoveredNode(boolExprNode.getTrueNode(), duplicateNode);
////            } else {
////                return tmpUncoveredNode;
////            }
//            return falseBranchUncoveredNode == null ? trueBranchUncoveredNode : falseBranchUncoveredNode;
//        }
//
//        CfgNode cfgNode =  findUncoveredNode(rootNode.getAfterStatementNode(), duplicateNode);
//        return cfgNode;
//    }

    private static List<CfgNode> coveredNodeInPath;

    public static CfgNode findUncoveredStatement(CfgNode rootNode) {
        coveredNodeInPath = new ArrayList<>();
        return findUncoveredStatement(rootNode, null);
    }

    private static CfgNode findUncoveredStatement(CfgNode rootNode, CfgNode duplicateNode) {
        if (rootNode == null) {
            return null;
        }
        if (!coveredNodeInPath.contains(rootNode)) {
            coveredNodeInPath.add(rootNode);
            if(!rootNode.isMarked()) return rootNode;
            if (rootNode instanceof CfgBoolExprNode) {
                CfgBoolExprNode boolExprNode = (CfgBoolExprNode) rootNode;
                CfgNode falseBranchUncoveredNode = findUncoveredStatement(boolExprNode.getFalseNode(), duplicateNode);
                CfgNode trueBranchUncoveredNode = findUncoveredStatement(boolExprNode.getTrueNode(), duplicateNode);
                return falseBranchUncoveredNode == null ? trueBranchUncoveredNode : falseBranchUncoveredNode;
            } else {
                return findUncoveredStatement(rootNode.getAfterStatementNode(), duplicateNode);
            }
        } else {
            return null;
        }
    }

    public static CfgNode findUncoveredBranch(CfgNode rootNode) {
        coveredNodeInPath = new ArrayList<>();
        return findUncoveredBranch(rootNode, null);
    }
//    private static CfgNode findUncoveredBranch(CfgNode rootNode, CfgNode duplicateNode) {
//        if (rootNode == null) {
//            return null;
//        }
//        if (rootNode instanceof CfgBoolExprNode) {
//            CfgBoolExprNode boolExprNode = (CfgBoolExprNode) rootNode;
//
//            if (!boolExprNode.isTrueMarked()) {
//                return boolExprNode.getTrueNode();
//            }
//            if (!boolExprNode.isFalseMarked()) {
//                return boolExprNode.getFalseNode();
//            }
//
////            if (boolExprNode != duplicateNode) {
////                duplicateNode = boolExprNode;
////                return findUncoveredBranch(boolExprNode.getTrueNode(), duplicateNode);
////            } else {
////                return findUncoveredBranch(boolExprNode.getFalseNode(), duplicateNode);
////            }
//            if(coveredNodeInPath.contains(boolExprNode)) {
//                return findUncoveredBranch(boolExprNode.getFalseNode(), duplicateNode);
//            } else {
//                coveredNodeInPath.add(boolExprNode);
//                CfgNode falseBranchUncoveredNode = findUncoveredBranch(boolExprNode.getFalseNode(), duplicateNode);
//                CfgNode trueBranchUncoveredNode = findUncoveredBranch(boolExprNode.getTrueNode(), duplicateNode);
//                return falseBranchUncoveredNode == null ? trueBranchUncoveredNode : falseBranchUncoveredNode;
//            }
//        }
//
//        coveredNodeInPath.add(rootNode);
//        return findUncoveredBranch(rootNode.getAfterStatementNode(), duplicateNode);
//    }

    private static CfgNode findUncoveredBranch(CfgNode rootNode, CfgNode duplicateNode) {
        if (rootNode == null) {
            return null;
        }
        if (!coveredNodeInPath.contains(rootNode)) {
            coveredNodeInPath.add(rootNode);
            if (rootNode instanceof CfgBoolExprNode) {
                CfgBoolExprNode boolExprNode = (CfgBoolExprNode) rootNode;

                if (!boolExprNode.isTrueMarked()) {
                    return boolExprNode.getTrueNode();
                }
                if (!boolExprNode.isFalseMarked()) {
                    return boolExprNode.getFalseNode();
                }

                CfgNode falseBranchUncoveredNode = findUncoveredBranch(boolExprNode.getFalseNode(), duplicateNode);
                CfgNode trueBranchUncoveredNode = findUncoveredBranch(boolExprNode.getTrueNode(), duplicateNode);
                return falseBranchUncoveredNode == null ? trueBranchUncoveredNode : falseBranchUncoveredNode;
            } else {
                return findUncoveredBranch(rootNode.getAfterStatementNode(), duplicateNode);
            }
        } else {
            return null;
        }
    }
}
