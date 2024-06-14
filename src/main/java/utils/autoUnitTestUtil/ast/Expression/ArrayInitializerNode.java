package utils.autoUnitTestUtil.ast.Expression;

import utils.autoUnitTestUtil.ast.AstNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;

import java.util.List;

public class ArrayInitializerNode extends ExpressionNode {
    private List<AstNode> expressions;

    public static ArrayNode executeArrayInitializer(ArrayInitializer arrayInitializer, MemoryModel memoryModel) {
        List<ASTNode> expressions = arrayInitializer.expressions();
        ArrayNode arrayNode = new ArrayNode(expressions.size());

        for(int i = 0; i < expressions.size(); i++) {
            arrayNode.set(AstNode.executeASTNode(expressions.get(i), memoryModel), i);
        }
        return arrayNode;

    }

}
