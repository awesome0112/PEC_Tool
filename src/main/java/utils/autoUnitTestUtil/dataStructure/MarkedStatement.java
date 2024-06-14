package utils.autoUnitTestUtil.dataStructure;

import utils.autoUnitTestUtil.cfg.CfgNode;

public class MarkedStatement {
    private String statement;
    private boolean isTrueConditionalStatement;
    private boolean isFalseConditionalStatement;
    private CfgNode cfgNode;

    public MarkedStatement(String statement, boolean isTrueConditionalStatement, boolean isFalseConditionalStatement) {
        this.statement = statement;
        this.isTrueConditionalStatement = isTrueConditionalStatement;
        this.isFalseConditionalStatement = isFalseConditionalStatement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public void setTrueConditionalStatement(boolean trueConditionalStatement) {
        isTrueConditionalStatement = trueConditionalStatement;
    }

    public void setFalseConditionalStatement(boolean falseConditionalStatement) {
        isFalseConditionalStatement = falseConditionalStatement;
    }

    public String getStatement() {
        return statement;
    }

    public boolean isTrueConditionalStatement() {
        return isTrueConditionalStatement;
    }

    public boolean isFalseConditionalStatement() {
        return isFalseConditionalStatement;
    }

    public CfgNode getCfgNode() {
        return cfgNode;
    }

    public void setCfgNode(CfgNode cfgNode) {
        this.cfgNode = cfgNode;
    }
}
