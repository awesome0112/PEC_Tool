package utils.autoUnitTestUtil.ast.Expression;


import utils.autoUnitTestUtil.ast.AstNode;
import utils.autoUnitTestUtil.ast.Expression.Literal.LiteralNode;
import utils.autoUnitTestUtil.ast.Expression.Literal.NumberLiteral.IntegerLiteralNode;
import utils.autoUnitTestUtil.ast.Expression.Name.NameNode;
import utils.autoUnitTestUtil.ast.Expression.OperationExpression.InfixExpressionNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.*;

public class AssignmentNode extends ExpressionNode {

    private Assignment.Operator operator;
    private ExpressionNode leftHandSide;
    private ExpressionNode rightHandSide;

    public static void executeAssignment(Assignment assignment, MemoryModel memoryModel) {
        AssignmentNode assignmentNode = new AssignmentNode();
        assignmentNode.operator = assignment.getOperator();
        assignmentNode.rightHandSide = (ExpressionNode) ExpressionNode.executeExpression(assignment.getRightHandSide(), memoryModel);

        ExpressionNode assignValue = analyzeAssignValue(assignmentNode.rightHandSide, assignmentNode.operator);

        Expression leftHandSide = assignment.getLeftHandSide();

        if(leftHandSide instanceof Name) {
            String key = NameNode.getStringName((Name) leftHandSide);
            memoryModel.assignVariable(key, assignValue);
        } else if(leftHandSide instanceof ArrayAccess){
            ArrayAccess arrayAccess = (ArrayAccess) leftHandSide;

            int index;
            ExpressionNode arrayIndex = (ExpressionNode) AstNode.executeASTNode(arrayAccess.getIndex(), memoryModel);
            if(arrayIndex instanceof LiteralNode) {
                index = LiteralNode.changeLiteralNodeToInteger((LiteralNode) arrayIndex);
            } else {
                throw new RuntimeException("Can't execute Index");
            }

            Expression arrayExpression = arrayAccess.getArray();
            ArrayNode arrayNode;
            if(arrayExpression instanceof ArrayAccess) {
                arrayNode = (ArrayNode) ArrayAccessNode.executeArrayAccessNode((ArrayAccess) arrayExpression, memoryModel);
            } else if(arrayExpression instanceof Name){
                String name = NameNode.getStringName((Name) arrayExpression);
                arrayNode = (ArrayNode) memoryModel.getValue(name);
            } else {
                throw new RuntimeException("Can't execute ArrayAccess");
            }
            arrayNode.set(assignValue, index);
        }
    }

    private static ExpressionNode analyzeAssignValue(ExpressionNode initialValue, Assignment.Operator assignmentOperator) {
        InfixExpressionNode assignValue = new InfixExpressionNode();
        assignValue.setLeftOperand(initialValue);
        assignValue.setRightOperand(new IntegerLiteralNode(1));

        if (assignmentOperator.equals(Assignment.Operator.ASSIGN)) {
            return initialValue;
        } else if(assignmentOperator.equals(Assignment.Operator.PLUS_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.PLUS);
        } else if (assignmentOperator.equals(Assignment.Operator.MINUS_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.MINUS);
        } else if (assignmentOperator.equals(Assignment.Operator.DIVIDE_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.DIVIDE);
        } else if (assignmentOperator.equals(Assignment.Operator.TIMES_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.TIMES);
        } else if (assignmentOperator.equals(Assignment.Operator.REMAINDER_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.REMAINDER);
        } else if (assignmentOperator.equals(Assignment.Operator.BIT_OR_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.OR);
        } else if (assignmentOperator.equals(Assignment.Operator.BIT_AND_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.AND);
        } else if (assignmentOperator.equals(Assignment.Operator.BIT_XOR_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.XOR);
        } else if (assignmentOperator.equals(Assignment.Operator.LEFT_SHIFT_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.LEFT_SHIFT);
        } else if (assignmentOperator.equals(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED);
        } else if (assignmentOperator.equals(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN)) {
            assignValue.setOperator(InfixExpression.Operator.RIGHT_SHIFT_SIGNED);
        } else {
            throw new RuntimeException("Invalid operator");
        }

        if(initialValue instanceof LiteralNode) {
            return LiteralNode.analyzeTwoInfixLiteral((LiteralNode) initialValue, assignValue.getOperator(), (LiteralNode) assignValue.getRightOperand());
        } else {
            return assignValue;
        }
    }
}
