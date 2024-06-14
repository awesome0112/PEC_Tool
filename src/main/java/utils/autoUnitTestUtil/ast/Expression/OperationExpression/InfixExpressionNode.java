package utils.autoUnitTestUtil.ast.Expression.OperationExpression;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import utils.autoUnitTestUtil.ast.*;
import utils.autoUnitTestUtil.ast.Expression.ExpressionNode;
import utils.autoUnitTestUtil.ast.Expression.Literal.LiteralNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;

import java.util.ArrayList;
import java.util.List;

public class InfixExpressionNode extends OperationExpressionNode {

    private ExpressionNode leftOperand;
    private ExpressionNode rightOperand;
    private InfixExpression.Operator operator;
    private List<AstNode> extendedOperands;

    public static Expr createZ3Expression(InfixExpressionNode infixExpressionNode, Context ctx, List<Expr> vars, MemoryModel memoryModel) {
        ExpressionNode leftOperand = infixExpressionNode.leftOperand;
        ExpressionNode rightOperand = infixExpressionNode.rightOperand;
        InfixExpression.Operator operator = infixExpressionNode.operator;
        List<AstNode> extendedOperands = infixExpressionNode.extendedOperands;

        Expr Z3LeftOperand = OperationExpressionNode.createZ3Expression(leftOperand, ctx, vars, memoryModel);
        Expr Z3RightOperand = OperationExpressionNode.createZ3Expression(rightOperand, ctx, vars, memoryModel);

        Expr result = createInfixZ3Expression(ctx, Z3LeftOperand, operator, Z3RightOperand);

        for (int i = 0; i < extendedOperands.size(); i++) {
            Expr extendedOperand = OperationExpressionNode.createZ3Expression((ExpressionNode) extendedOperands.get(i), ctx, vars, memoryModel);
            result = createInfixZ3Expression(ctx, result, operator, extendedOperand);
        }

        return result;
    }

    private static Expr createInfixZ3Expression(Context ctx, Expr Z3LeftOperand, InfixExpression.Operator operator, Expr Z3RightOperand) {
        if (operator.equals(InfixExpression.Operator.PLUS)) {
            return ctx.mkAdd(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.MINUS)) {
            return ctx.mkSub(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.TIMES)) {
            return ctx.mkMul(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.DIVIDE)) {
            return ctx.mkDiv(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.REMAINDER)) {
            return ctx.mkRem(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.LESS)) {
            return ctx.mkLt(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.GREATER)) {
            return ctx.mkGt(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.LESS_EQUALS)) {
            return ctx.mkLe(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.GREATER_EQUALS)) {
            return ctx.mkGe(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.EQUALS)) {
            return ctx.mkEq(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.NOT_EQUALS)) {
            return ctx.mkDistinct(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.CONDITIONAL_AND)) {
            return ctx.mkAnd(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.CONDITIONAL_OR)) {
            return ctx.mkOr(Z3LeftOperand, Z3RightOperand);
        } else if (operator.equals(InfixExpression.Operator.LEFT_SHIFT)) {
            return ctx.mkMul(Z3LeftOperand, ctx.mkPower(ctx.mkInt(2), Z3RightOperand)); // Cần sửa
        } else if (operator.equals(InfixExpression.Operator.RIGHT_SHIFT_SIGNED)) {
            return ctx.mkDiv(Z3LeftOperand, ctx.mkPower(ctx.mkInt(2), Z3RightOperand)); // Cần sửa
        } else if (operator.equals(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED)) {
            return ctx.mkDiv(Z3LeftOperand, ctx.mkPower(ctx.mkInt(2), Z3RightOperand)); // Cần sửa
        } else {
            throw new RuntimeException("Invalid operator");
        }
    }

    public static ExpressionNode executeInfixExpression(InfixExpression infixExpression, MemoryModel memoryModel) {
        InfixExpressionNode infixExpressionNode = new InfixExpressionNode();
        infixExpressionNode.leftOperand = (ExpressionNode) ExpressionNode.executeExpression(infixExpression.getLeftOperand(), memoryModel);
        infixExpressionNode.rightOperand = (ExpressionNode) ExpressionNode.executeExpression(infixExpression.getRightOperand(), memoryModel);
        infixExpressionNode.operator = infixExpression.getOperator();

        List<AstNode> extendedOperands = new ArrayList<>();
        for (int i = 0; i < infixExpression.extendedOperands().size(); i++) {
            extendedOperands.add(AstNode.executeASTNode((ASTNode) infixExpression.extendedOperands().get(i), memoryModel));
        }
        infixExpressionNode.extendedOperands = extendedOperands;

        ExpressionNode expressionNode = executeInfixExpressionNode(infixExpressionNode, memoryModel);

        return expressionNode;
    }

    public static ExpressionNode executeInfixExpressionNode(InfixExpressionNode infixExpressionNode, MemoryModel memoryModel) {
        ExpressionNode leftOperand = infixExpressionNode.leftOperand;
        ExpressionNode rightOperand = infixExpressionNode.rightOperand;
        InfixExpression.Operator operator = infixExpressionNode.operator;
        List<AstNode> extendedOperands = infixExpressionNode.extendedOperands;

//        if(!extendedOperands.isEmpty()) {
//            List<AstNode> operands = new ArrayList<>();
//            operands.add(leftOperand);
//            operands.add(rightOperand);
//            operands.addAll(extendedOperands);
//
//            LiteralNode literalResult = null;
//
//            for(AstNode i : operands) {
//                if(i instanceof LiteralNode) {
//                    LiteralNode literalOperand = (LiteralNode) i;
//                    if(literalResult == null) {
//                        literalResult = literalOperand;
//                    } else {
//                        literalResult = LiteralNode.analyzeTwoInfixLiteral(literalResult, operator, literalOperand);
//
//                    }
//
//                }
//            }
//        }

        if (leftOperand.isLiteralNode() && rightOperand.isLiteralNode()) {
            LiteralNode literalResult = LiteralNode.analyzeTwoInfixLiteral((LiteralNode) leftOperand, operator, (LiteralNode) rightOperand);

            if (extendedOperands.isEmpty()) {
                return literalResult;
            } else {
                infixExpressionNode.leftOperand = literalResult;
                infixExpressionNode.rightOperand = (ExpressionNode) extendedOperands.remove(0);
                return executeInfixExpressionNode(infixExpressionNode, memoryModel);
            }
        } else {
            ExpressionNode oldLeftOperand = infixExpressionNode.leftOperand;
            ExpressionNode oldRightOperand = infixExpressionNode.rightOperand;

            if (!leftOperand.isLiteralNode()) {
                infixExpressionNode.leftOperand = OperationExpressionNode.executeOperandNode(leftOperand, memoryModel);
            }
            if (!rightOperand.isLiteralNode()) {
                infixExpressionNode.rightOperand = OperationExpressionNode.executeOperandNode(rightOperand, memoryModel);
            }

//            if (oldLeftOperand != infixExpressionNode.leftOperand || oldRightOperand != infixExpressionNode.rightOperand) {
//                return executeInfixExpressionNode(infixExpressionNode, memoryModel);
//            } else {
//                return infixExpressionNode;
//            }
            return infixExpressionNode;
        }
    }

    public static boolean isBitwiseOperator(InfixExpression.Operator operator) {
        return (operator.equals(InfixExpression.Operator.OR) ||
                operator.equals(InfixExpression.Operator.XOR) ||
                operator.equals(InfixExpression.Operator.AND) ||
                operator.equals(InfixExpression.Operator.LEFT_SHIFT) ||
                operator.equals(InfixExpression.Operator.RIGHT_SHIFT_SIGNED) ||
                operator.equals(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED));
    }

    public static boolean isBooleanBitwiseOperator(InfixExpression.Operator operator) {
        return (operator.equals(InfixExpression.Operator.OR) ||
                operator.equals(InfixExpression.Operator.XOR) ||
                operator.equals(InfixExpression.Operator.AND));
    }

    public static boolean isArithmeticOperator(InfixExpression.Operator operator) {
        return (operator.equals(InfixExpression.Operator.PLUS) ||
                operator.equals(InfixExpression.Operator.MINUS) ||
                operator.equals(InfixExpression.Operator.DIVIDE) ||
                operator.equals(InfixExpression.Operator.TIMES) ||
                operator.equals(InfixExpression.Operator.REMAINDER));
    }

    public static boolean isComparisonOperator(InfixExpression.Operator operator) {
        return (operator.equals(InfixExpression.Operator.EQUALS) ||
                operator.equals(InfixExpression.Operator.NOT_EQUALS) ||
                operator.equals(InfixExpression.Operator.LESS) ||
                operator.equals(InfixExpression.Operator.GREATER) ||
                operator.equals(InfixExpression.Operator.LESS_EQUALS) ||
                operator.equals(InfixExpression.Operator.GREATER_EQUALS));
    }

    public static boolean isConditionalOperator(InfixExpression.Operator operator) {
        return (operator.equals(InfixExpression.Operator.CONDITIONAL_OR) ||
                operator.equals(InfixExpression.Operator.CONDITIONAL_AND));
    }

    public static boolean isBooleanComparisonOperator(InfixExpression.Operator operator) {
        return (operator.equals(InfixExpression.Operator.EQUALS) ||
                operator.equals(InfixExpression.Operator.NOT_EQUALS));
    }

    public static boolean isStringComparisonOperator(InfixExpression.Operator operator) {
        return (operator.equals(InfixExpression.Operator.EQUALS) ||
                operator.equals(InfixExpression.Operator.NOT_EQUALS));
    }

    public static boolean isStringConcatenationOperator(InfixExpression.Operator operator) {
        return operator.equals(InfixExpression.Operator.PLUS);
    }

    public void setLeftOperand(ExpressionNode leftOperand) {
        this.leftOperand = leftOperand;
    }

    public void setRightOperand(ExpressionNode rightOperand) {
        this.rightOperand = rightOperand;
    }

    public void setOperator(InfixExpression.Operator operator) {
        this.operator = operator;
    }

    public ExpressionNode getLeftOperand() {
        return leftOperand;
    }

    public ExpressionNode getRightOperand() {
        return rightOperand;
    }

    public InfixExpression.Operator getOperator() {
        return operator;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("");
        result.append(leftOperand.toString());
        result.append(operator.toString());
        result.append(rightOperand.toString());

        for (AstNode extendedOperand : extendedOperands) {
            result.append(operator.toString() + extendedOperand.toString());
        }

        return result.toString();
    }
}
