package utils.autoUnitTestUtil.ast.Expression.Name;


import utils.autoUnitTestUtil.ast.Expression.ExpressionNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.QualifiedName;

public class QualifiedNameNode extends NameNode {
    private NameNode qualifier = null;
    private SimpleNameNode name = null;

    public static QualifiedNameNode executeQualifiedName(QualifiedName qualifiedName, MemoryModel memoryModel) {
//        QualifiedNameNode qualifiedNameNode = new QualifiedNameNode();
//        qualifiedNameNode.qualifier = NameNode.executeName(qualifiedName.getQualifier());
//        qualifiedNameNode.name = SimpleNameNode.executeSimpleName(qualifiedName.getName());
//        return qualifiedNameNode;

        /*????*/
        return null;
    }

    public static ExpressionNode executeQualifiedNameNode(QualifiedNameNode qualifiedNameNode, MemoryModel memoryModel) {
        /*????*/
        return null;
    }

    public static String getStringQualifiedName(QualifiedName qualifiedName) {
        /*????*/
        return null;
    }

    public static String getStringQualifiedNameNode(QualifiedNameNode qualifiedNameNode) {
        return null;
    }
}
