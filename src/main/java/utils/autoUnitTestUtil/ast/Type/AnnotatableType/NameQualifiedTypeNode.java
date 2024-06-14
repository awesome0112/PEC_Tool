package utils.autoUnitTestUtil.ast.Type.AnnotatableType;

import utils.autoUnitTestUtil.ast.Expression.Name.NameNode;
import utils.autoUnitTestUtil.ast.Expression.Name.SimpleNameNode;

public class NameQualifiedTypeNode extends AnnotatableTypeNode {
    private NameNode qualifier = null;
    private SimpleNameNode name = null;
}
