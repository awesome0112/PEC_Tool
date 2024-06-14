package utils.autoUnitTestUtil.ast;

import utils.autoUnitTestUtil.ast.Expression.ExpressionNode;
import utils.autoUnitTestUtil.ast.Statement.StatementNode;
import utils.autoUnitTestUtil.ast.VariableDeclaration.VariableDeclarationNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.*;

public abstract class AstNode {

    public static AstNode executeASTNode(ASTNode astNode, MemoryModel memoryModel) {
        if(astNode instanceof Expression) {
            return ExpressionNode.executeExpression((Expression) astNode, memoryModel);
        } else if (astNode instanceof Statement) {
            return StatementNode.executeStatement((Statement) astNode, memoryModel);
        } else if (astNode instanceof VariableDeclaration) {
            VariableDeclarationNode.executeVariableDeclaration((VariableDeclaration) astNode, memoryModel);
            return null;
        } else if (astNode instanceof Dimension) {
            return DimensionNode.executeDimension((Dimension) astNode);
        } else {
//            throw new RuntimeException(astNode.getClass() + " is not an ASTNode");
            return null;
        }
    }

}
