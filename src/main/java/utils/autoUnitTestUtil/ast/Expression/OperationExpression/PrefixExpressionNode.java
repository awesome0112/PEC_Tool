package utils.autoUnitTestUtil.ast.Expression.OperationExpression;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import utils.autoUnitTestUtil.ast.AstNode;
import utils.autoUnitTestUtil.ast.Expression.ExpressionNode;
import utils.autoUnitTestUtil.ast.Expression.Literal.LiteralNode;
import utils.autoUnitTestUtil.ast.Expression.Name.NameNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.PrefixExpression;

import java.util.List;

public class PrefixExpressionNode extends OperationExpressionNode {
    private ExpressionNode operand;
    private PrefixExpression.Operator operator;

    public static Expr createZ3Expression(PrefixExpressionNode prefixExpressionNode, Context ctx, List<Expr> vars, MemoryModel memoryModel) {
        ExpressionNode operand = prefixExpressionNode.operand;
        PrefixExpression.Operator operator = prefixExpressionNode.operator;

        Expr Z3Operand = OperationExpressionNode.createZ3Expression(operand, ctx, vars, memoryModel);

        if(operator.equals(PrefixExpression.Operator.INCREMENT)) {
            return ctx.mkAdd(Z3Operand, ctx.mkInt(1));
        } else if (operator.equals(PrefixExpression.Operator.DECREMENT)) {
            return ctx.mkSub(Z3Operand, ctx.mkInt(1));
        } else if (operator.equals(PrefixExpression.Operator.PLUS)) {
            return ctx.mkMul(Z3Operand, ctx.mkInt(1));
        } else if (operator.equals(PrefixExpression.Operator.MINUS)) {
            return ctx.mkMul(Z3Operand, ctx.mkInt(-1));
        } else if (operator.equals(PrefixExpression.Operator.NOT)) {
            return ctx.mkNot(Z3Operand);
        } else if (operator.equals(PrefixExpression.Operator.COMPLEMENT)) {
            return ctx.mkMul(ctx.mkAdd(Z3Operand, ctx.mkInt(1)), ctx.mkInt(-1));
        } else {
            throw new RuntimeException("Invalid operator");
        }
    }

    public static ExpressionNode executePrefixExpression(PrefixExpression prefixExpression, MemoryModel memoryModel) {
        PrefixExpressionNode prefixExpressionNode = new PrefixExpressionNode();
        prefixExpressionNode.operand = (ExpressionNode) ExpressionNode.executeExpression(prefixExpression.getOperand(), memoryModel);
        prefixExpressionNode.operator = prefixExpression.getOperator();

        ExpressionNode expressionNode = executePrefixExpressionNode(prefixExpressionNode, memoryModel);
        return expressionNode;
    }

    public static ExpressionNode executePrefixExpressionNode(PrefixExpressionNode prefixExpressionNode, MemoryModel memoryModel) {
        ExpressionNode operand = prefixExpressionNode.operand;
        PrefixExpression.Operator operator = prefixExpressionNode.operator;
        if(operand.isLiteralNode()) {
            LiteralNode literalResult = LiteralNode.analyzeOnePrefixLiteral(operator, (LiteralNode) operand);
            return literalResult;
        } else {
            ExpressionNode oldOperand = prefixExpressionNode.operand;

            prefixExpressionNode.operand = OperationExpressionNode.executeOperandNode(operand, memoryModel);
            // PAUSE executing

            // RE_ASSIGN
            if(operand instanceof NameNode && (operator.equals(PrefixExpression.Operator.INCREMENT)
                    || operator.equals(PrefixExpression.Operator.DECREMENT))) {
                String key = NameNode.getStringNameNode((NameNode) operand);
                AstNode value = memoryModel.getValue(key);

                if(value instanceof LiteralNode) {
                    memoryModel.assignVariable(key, LiteralNode.analyzeOnePrefixLiteral(operator, (LiteralNode) value));
                } else if (value instanceof OperationExpressionNode) {
                    PrefixExpressionNode newValue = new PrefixExpressionNode();
                    newValue.operator = operator;
                    newValue.operand = (OperationExpressionNode) value;
                    memoryModel.assignVariable(key, newValue);
                }
            }
            // END RE-ASSIGN

            // CONTINUE executing
//            if(oldOperand != prefixExpressionNode.operand) {
//                return PrefixExpressionNode.executePrefixExpressionNode(prefixExpressionNode, memoryModel);
//            } else {
//                return prefixExpressionNode;
//            }
            return prefixExpressionNode;
        }
    }

    public ExpressionNode getOperand() {
        return operand;
    }

    public void setOperand(ExpressionNode operand) {
        this.operand = operand;
    }

    public PrefixExpression.Operator getOperator() {
        return operator;
    }

    public void setOperator(PrefixExpression.Operator operator) {
        this.operator = operator;
    }

    public static boolean isBitwiseOperator(PrefixExpression.Operator operator) {
        if(operator.equals(PrefixExpression.Operator.COMPLEMENT)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("");
        result.append(operator.toString());
        result.append(operand.toString());

        return result.toString();
    }
}