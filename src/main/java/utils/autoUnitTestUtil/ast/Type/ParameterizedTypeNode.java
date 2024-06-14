package utils.autoUnitTestUtil.ast.Type;

import utils.autoUnitTestUtil.ast.AstNode;

import java.util.List;

public class ParameterizedTypeNode extends TypeNode {
    private TypeNode type = null;
    private List<AstNode> typeArguments;
}
