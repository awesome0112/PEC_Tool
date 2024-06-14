package utils.autoUnitTestUtil.ast.VariableDeclaration;

import utils.autoUnitTestUtil.ast.Expression.ExpressionNode;
import utils.autoUnitTestUtil.ast.Type.AnnotatableType.PrimitiveTypeNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.*;

public class VariableDeclarationFragmentNode extends VariableDeclarationNode {

    public static void executeVariableDeclarationFragment(VariableDeclarationFragment fragment,
                                                          Type baseType,
                                                          MemoryModel memoryModel) {
        String name = fragment.getName().getIdentifier();
        Expression initializer = fragment.getInitializer();

        if(initializer != null) {
            if(baseType instanceof PrimitiveType) {
                PrimitiveType type = (PrimitiveType) baseType;
                memoryModel.declarePrimitiveTypeVariable(type.getPrimitiveTypeCode(), name, ExpressionNode.executeExpression(initializer, memoryModel));
            } else if(baseType instanceof ArrayType) {
                ArrayType type = (ArrayType) baseType;
                memoryModel.declareArrayTypeVariable(type, name, ExpressionNode.executeExpression(initializer, memoryModel));
            } else {
                throw new RuntimeException(baseType.getClass() + " is invalid!!");
            }
            /*Error!!!!: char x = 5;*/
        } else {
            if(baseType instanceof PrimitiveType) {
                PrimitiveType type = (PrimitiveType) baseType;
                memoryModel.declarePrimitiveTypeVariable(type.getPrimitiveTypeCode(), name, PrimitiveTypeNode.changePrimitiveTypeToLiteralInitialization(type));
            } else {
                throw new RuntimeException("Only deal with PrimitiveType!!");
            }
        }
    }

}
