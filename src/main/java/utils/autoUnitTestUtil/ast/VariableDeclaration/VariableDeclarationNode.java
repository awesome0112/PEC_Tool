package utils.autoUnitTestUtil.ast.VariableDeclaration;

import utils.autoUnitTestUtil.ast.AstNode;
import utils.autoUnitTestUtil.ast.Expression.ExpressionNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.List;

public abstract class VariableDeclarationNode extends AstNode {
    int extraArrayDimensions = 0;
    List<AstNode> extraDimensions = null;
    ExpressionNode optionalInitializer = null;

    public static void executeVariableDeclaration(VariableDeclaration variableDeclaration, MemoryModel memoryModel) {
        if (variableDeclaration instanceof SingleVariableDeclaration) {
            SingleVariableDeclarationNode.executeSingleVariableDeclaration((SingleVariableDeclaration) variableDeclaration,
                    memoryModel);
        } else if (variableDeclaration instanceof VariableDeclarationFragment) {
//           /*????*/
        } else {
            throw new RuntimeException(variableDeclaration.getClass() + " is not a VariableDeclaration");
        }
    }
}
