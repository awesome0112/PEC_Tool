package utils.autoUnitTestUtil.algorithms;

import com.microsoft.z3.*;
import utils.FilePath;
import utils.autoUnitTestUtil.ast.AstNode;
import utils.autoUnitTestUtil.ast.Expression.*;
import utils.autoUnitTestUtil.ast.Expression.OperationExpression.*;
import utils.autoUnitTestUtil.ast.additionalNodes.Node;
import utils.autoUnitTestUtil.cfg.CfgBoolExprNode;
import utils.autoUnitTestUtil.cfg.CfgNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import utils.autoUnitTestUtil.dataStructure.Path;
import utils.autoUnitTestUtil.variable.Variable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class SymbolicExecution {
    private MemoryModel memoryModel;
    private List<Expr> vars;
    private Model model;

    private Path testPath;
    private List<ASTNode> parameters;

    public SymbolicExecution(Path testPath, List<ASTNode> parameters) {
        this.testPath = testPath;
        this.parameters = parameters;
        execute();
    }

    private void execute() {
        memoryModel = new MemoryModel();
        vars = new ArrayList<>();

        HashMap<String, String> cfg = new HashMap();
        cfg.put("model", "true");
        Context ctx = new Context(cfg);

        for (ASTNode astNode : parameters) {
            AstNode.executeASTNode(astNode, memoryModel);
            createZ3ParameterVariable(astNode, ctx);
        }

        Node currentNode = testPath.getCurrentFirst();

        Expr finalZ3Expression = null;

        while (currentNode != null) {
            CfgNode currentCfgNode = currentNode.getData();

            ASTNode astNode = currentCfgNode.getAst();

            if (astNode != null) {
                AstNode executedAstNode = AstNode.executeASTNode(astNode, memoryModel);
                if (currentNode.getData() instanceof CfgBoolExprNode) {

                    // Kiểm tra xem path hiện tại chứa node bool phủ định
                    if (currentNode.getNext() != null && currentNode.getNext().getData().isFalseNode()) {
                        PrefixExpressionNode newAstNode = new PrefixExpressionNode();
                        newAstNode.setOperator(PrefixExpression.Operator.NOT);
                        newAstNode.setOperand((ExpressionNode) executedAstNode);

                        executedAstNode = PrefixExpressionNode.executePrefixExpressionNode(newAstNode, memoryModel);
                    }

                    BoolExpr constrain = (BoolExpr) OperationExpressionNode.createZ3Expression((ExpressionNode) executedAstNode, ctx, vars, memoryModel);

                    if (finalZ3Expression == null) finalZ3Expression = constrain;
                    else {
                        finalZ3Expression = ctx.mkAnd(finalZ3Expression, constrain);
                    }
                }
            }
            currentNode = currentNode.getNext();
        }

        model = createModel(ctx, (BoolExpr) finalZ3Expression);
        evaluateAndSaveTestDataCreated();
    }

    private void createZ3ParameterVariable(ASTNode parameter, Context ctx) {
        if (parameter instanceof SingleVariableDeclaration) {
            SingleVariableDeclaration declaration = (SingleVariableDeclaration) parameter;
            String name = declaration.getName().toString();

            Expr variable = Variable.createZ3Variable(memoryModel.getVariable(name), ctx);
            if(!haveDuplicateVariable(variable.toString())) {
                vars.add(variable);
            }
        } else {
            throw new RuntimeException("Invalid parameter");
        }
    }

    private boolean haveDuplicateVariable(String var) {
        for(int i = 0; i < vars.size(); i++) {
            if(var.equals(vars.get(i).toString())) {
                return true;
            }
        }
        return false;
    }

    private Model createModel(Context ctx, BoolExpr f) {
        Solver s = ctx.mkSolver();
        s.add(f);
        System.out.println(s);

        Status satisfaction = s.check();
        if (satisfaction != Status.SATISFIABLE) {
            System.out.println("Expression is UNSATISFIABLE");
            return null;
        } else {
            return s.getModel();
        }
    }

    private void evaluateAndSaveTestDataCreated() {
        if(model != null) {
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < vars.size(); i++) {
                Expr evaluateResult = model.evaluate(vars.get(i), false);

                if (evaluateResult instanceof IntNum) {
                    result.append(evaluateResult);
                } else if (evaluateResult instanceof IntExpr) {
                    result.append("1");
                } else if (evaluateResult instanceof RatNum) {
                    RatNum ratNum = (RatNum) evaluateResult;
                    double value = (ratNum.getNumerator().getInt() * 1.0) / ratNum.getDenominator().getInt();
                    result.append(value);
                } else if (evaluateResult instanceof RealExpr) {
                    result.append("1.0");
                } else if (evaluateResult instanceof BoolExpr) {
                    BoolExpr boolExpr = (BoolExpr) evaluateResult;
                    if (!boolExpr.toString().equals("false") && !boolExpr.toString().equals("true")) {
                        result.append("false");
                    } else {
                        result.append(boolExpr);
                    }
                }

                if(i != vars.size() - 1) {
                    result.append("\n");
                }
            }

            writeDataToFile(result.toString());
        }
    }

    private void writeDataToFile(String data) {
        try {
            FileWriter writer = new FileWriter(FilePath.generatedTestDataPath);
            writer.write(data + "\n");
            writer.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public Model getModel() {
        return model;
    }
}
