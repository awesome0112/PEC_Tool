package utils.autoUnitTestUtil.ast.Expression.OperationExpression;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import utils.autoUnitTestUtil.ast.AstNode;
import utils.autoUnitTestUtil.ast.Expression.ExpressionNode;
import utils.autoUnitTestUtil.ast.Expression.Literal.LiteralNode;
import utils.autoUnitTestUtil.ast.Expression.Name.NameNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import org.eclipse.jdt.core.dom.PostfixExpression;

import java.util.List;

public class PostfixExpressionNode extends OperationExpressionNode {
    private ExpressionNode operand;
    private PostfixExpression.Operator operator;

    public static Expr createZ3Expression(PostfixExpressionNode postfixExpressionNode, Context ctx, List<Expr> vars, MemoryModel memoryModel) {
        ExpressionNode operand = postfixExpressionNode.operand;
        PostfixExpression.Operator operator = postfixExpressionNode.operator;

        Expr Z3Operand = OperationExpressionNode.createZ3Expression(operand, ctx, vars, memoryModel);
        return Z3Operand;
    }

    public static ExpressionNode executePostfixExpression(PostfixExpression postfixExpression, MemoryModel memoryModel) {
        PostfixExpressionNode postfixExpressionNode = new PostfixExpressionNode();
        postfixExpressionNode.operand = (ExpressionNode) ExpressionNode.executeExpression(postfixExpression.getOperand(), memoryModel);
        postfixExpressionNode.operator = postfixExpression.getOperator();

        ExpressionNode expressionNode = executePostfixExpressionNode(postfixExpressionNode, memoryModel);
        return expressionNode;
    }

    public static ExpressionNode executePostfixExpressionNode(PostfixExpressionNode postfixExpressionNode, MemoryModel memoryModel) {
        ExpressionNode operand = postfixExpressionNode.operand;
        PostfixExpression.Operator operator = postfixExpressionNode.operator;

        ExpressionNode oldOperand = postfixExpressionNode.operand;

        if(!operand.isLiteralNode()) {
            postfixExpressionNode.operand = OperationExpressionNode.executeOperandNode(operand, memoryModel);
        } else {
            return operand;
        }
        // PAUSE executing

        // RE-ASSIGN
        if(operand instanceof NameNode) {
            String key = NameNode.getStringNameNode((NameNode) operand);
            AstNode value = memoryModel.getValue(key);

            if(value instanceof LiteralNode) {
                memoryModel.assignVariable(key, LiteralNode.analyzeOnePostfixLiteral((LiteralNode) value, operator));
            } else if (value instanceof OperationExpressionNode) {
                PostfixExpressionNode newValue = new PostfixExpressionNode();
                newValue.operator = operator;
                newValue.operand = (OperationExpressionNode) value;
                memoryModel.assignVariable(key, newValue);
            }
        }

        // CONTINUE executing
//        if(oldOperand != postfixExpressionNode.operand) {
//            return executePostfixExpressionNode(postfixExpressionNode, memoryModel);
//        } else {
//            return postfixExpressionNode;
//        }
        return postfixExpressionNode;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("");
        result.append(operand.toString());
        result.append(operator.toString());

        return result.toString();
    }
}
