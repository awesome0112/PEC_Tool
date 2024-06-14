package utils.autoUnitTestUtil.ast.Statement;

import utils.autoUnitTestUtil.ast.AstNode;
import utils.autoUnitTestUtil.ast.Expression.ExpressionNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.ExpressionStatement;

public class ExpressionStatementNode extends StatementNode {

    private ExpressionNode expression = null;

    public static AstNode executeExpressionStatement(ExpressionStatement expressionStatement, MemoryModel memoryModel) {
        ExpressionStatementNode expressionStatementNode = new ExpressionStatementNode();
        expressionStatementNode.expression = (ExpressionNode) ExpressionNode.executeExpression(expressionStatement.getExpression(), memoryModel);
        return expressionStatementNode;
    }

}
