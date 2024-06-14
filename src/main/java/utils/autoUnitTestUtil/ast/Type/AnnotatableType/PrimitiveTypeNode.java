package utils.autoUnitTestUtil.ast.Type.AnnotatableType;

import utils.autoUnitTestUtil.ast.Expression.Literal.BooleanLiteralNode;
import utils.autoUnitTestUtil.ast.Expression.Literal.CharacterLiteralNode;
import utils.autoUnitTestUtil.ast.Expression.Literal.LiteralNode;
import utils.autoUnitTestUtil.ast.Expression.Literal.NumberLiteral.DoubleLiteralNode;
import utils.autoUnitTestUtil.ast.Expression.Literal.NumberLiteral.IntegerLiteralNode;
import org.eclipse.jdt.core.dom.PrimitiveType;

public class PrimitiveTypeNode extends AnnotatableTypeNode {
    private PrimitiveType.Code typeCode;

    public PrimitiveType.Code getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(PrimitiveType.Code typeCode) {
        this.typeCode = typeCode;
    }

    public static LiteralNode changePrimitiveTypeToLiteralInitialization(PrimitiveType primitiveType) {
        PrimitiveTypeNode primitiveTypeNode = new PrimitiveTypeNode();
        primitiveTypeNode.setTypeCode(primitiveType.getPrimitiveTypeCode());
        if (primitiveTypeNode.isIntegerType()) {
            return new IntegerLiteralNode();
        } else if (primitiveTypeNode.isDoubleType()) {
            return new DoubleLiteralNode();
        } else if (primitiveTypeNode.getTypeCode().equals(PrimitiveType.CHAR)) {
            return new CharacterLiteralNode();
        } else if (primitiveTypeNode.getTypeCode().equals(PrimitiveType.BOOLEAN)) {
            return new BooleanLiteralNode();
        } else if (primitiveTypeNode.getTypeCode().equals(PrimitiveType.VOID)) {
            throw new RuntimeException("Void type??");
        } else {
            throw new RuntimeException("Type given is not primitive type!!!");
        }
    }

    public static LiteralNode[] changePrimitiveTypeToLiteralInitializationArray(PrimitiveType primitiveType, int capacityOfArray) {
        PrimitiveTypeNode primitiveTypeNode = new PrimitiveTypeNode();
        primitiveTypeNode.setTypeCode(primitiveType.getPrimitiveTypeCode());

        if (primitiveTypeNode.isIntegerType()) {

            return IntegerLiteralNode.createIntegerLiteralInitializationArray(capacityOfArray);

        } else if (primitiveTypeNode.isDoubleType()) {

            return DoubleLiteralNode.createDoubleLiteralInitializationArray(capacityOfArray);

        } else if (primitiveTypeNode.getTypeCode().equals(PrimitiveType.CHAR)) {

            return CharacterLiteralNode.createCharacterLiteralInitializationArray(capacityOfArray);

        } else if (primitiveTypeNode.getTypeCode().equals(PrimitiveType.BOOLEAN)) {

            return BooleanLiteralNode.createBooleanLiteralInitializationArray(capacityOfArray);

        } else if (primitiveTypeNode.getTypeCode().equals(PrimitiveType.VOID)) {
            throw new RuntimeException("Void type??");
        } else {
            throw new RuntimeException("Type given is not primitive type!!!");
        }
    }

    public boolean isNumberType() {
        return (typeCode.equals(PrimitiveType.INT) ||
                typeCode.equals(PrimitiveType.LONG) ||
                typeCode.equals(PrimitiveType.DOUBLE) ||
                typeCode.equals(PrimitiveType.FLOAT) ||
                typeCode.equals(PrimitiveType.SHORT) ||
                typeCode.equals(PrimitiveType.BYTE));
    }

    public boolean isIntegerType() {
        return (typeCode.equals(PrimitiveType.INT) ||
                typeCode.equals(PrimitiveType.LONG) ||
                typeCode.equals(PrimitiveType.SHORT) ||
                typeCode.equals(PrimitiveType.BYTE));
    }

    public boolean isDoubleType() {
        return (typeCode.equals(PrimitiveType.DOUBLE) ||
                typeCode.equals(PrimitiveType.FLOAT));
    }
}
