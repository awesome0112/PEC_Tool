package utils.autoUnitTestUtil.ast.Expression.Literal;

import org.eclipse.jdt.core.dom.BooleanLiteral;

public class BooleanLiteralNode extends LiteralNode {
    private boolean value = false;

    public static BooleanLiteralNode executeBooleanLiteral(BooleanLiteral booleanLiteral) {
        BooleanLiteralNode booleanLiteralNode = new BooleanLiteralNode();
        booleanLiteralNode.setValue(booleanLiteral.booleanValue());
        return booleanLiteralNode;
    }

    public static BooleanLiteralNode createBooleanLiteral(boolean value) {
        BooleanLiteralNode literalNode = new BooleanLiteralNode();
        literalNode.setValue(value);
        return literalNode;
    }

    public static BooleanLiteralNode[] createBooleanLiteralInitializationArray(int capacity) {
        BooleanLiteralNode[] array = new BooleanLiteralNode[capacity];
        for(int i = 0; i < capacity; i++) {
            array[i] = new BooleanLiteralNode();
        }
        return array;
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
