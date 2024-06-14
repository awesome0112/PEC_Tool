package utils.autoUnitTestUtil.ast.Expression.Name;

import utils.autoUnitTestUtil.ast.Expression.ExpressionNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.SimpleName;

public class SimpleNameNode extends NameNode {
    private String identifier = "MISSING";

    public SimpleNameNode() {
    }

    public SimpleNameNode(String identifier) {
        this.identifier = identifier;
    }

    public static ExpressionNode executeSimpleName(SimpleName simpleName, MemoryModel memoryModel) {
        SimpleNameNode simpleNameNode = new SimpleNameNode();
        simpleNameNode.identifier = simpleName.getIdentifier();
//        return NameNode.executeNameNode(simpleNameNode, memoryModel);
        return simpleNameNode;
    }

    public static ExpressionNode executeSimpleNameNode(SimpleNameNode simpleNameNode, MemoryModel memoryModel) {
        return (ExpressionNode) memoryModel.getValue(simpleNameNode);
    }

    public static String getStringSimpleName(SimpleName simpleName) {
        return simpleName.getIdentifier();
    }

    public static String getStringSimpleNameNode(SimpleNameNode simpleNameNode) {
        return simpleNameNode.identifier;
    }
    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }
}
