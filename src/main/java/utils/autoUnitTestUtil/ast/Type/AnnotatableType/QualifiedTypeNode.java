package utils.autoUnitTestUtil.ast.Type.AnnotatableType;

import utils.autoUnitTestUtil.ast.Expression.Name.SimpleNameNode;
import utils.autoUnitTestUtil.ast.Type.TypeNode;

public class QualifiedTypeNode extends AnnotatableTypeNode {
    private TypeNode qualifier = null;
    private SimpleNameNode name = null;
}
