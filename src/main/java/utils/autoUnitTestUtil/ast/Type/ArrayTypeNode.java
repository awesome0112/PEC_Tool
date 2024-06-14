package utils.autoUnitTestUtil.ast.Type;

import utils.autoUnitTestUtil.ast.AstNode;

import java.util.List;

public class ArrayTypeNode extends TypeNode {
    private TypeNode type = null;
    private List<AstNode> dimensions = null;

    public TypeNode getType() {
        return type;
    }

    public void setType(TypeNode type) {
        this.type = type;
    }

//    public static ArrayTypeNode executeArrayType(ArrayType arrayType) {
//        ArrayTypeNode typeNode = new ArrayTypeNode();
//
//    }
}
