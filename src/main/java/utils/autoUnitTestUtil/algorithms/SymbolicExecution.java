package utils.autoUnitTestUtil.algorithms;

import com.microsoft.z3.*;
import utils.FilePath;
import utils.autoUnitTestUtil.Z3Vars.Z3VariableWrapper;
import utils.autoUnitTestUtil.ast.AstNode;
import utils.autoUnitTestUtil.ast.Expression.*;
import utils.autoUnitTestUtil.ast.Expression.OperationExpression.*;
import utils.autoUnitTestUtil.ast.additionalNodes.Node;
import utils.autoUnitTestUtil.cfg.CfgBoolExprNode;
import utils.autoUnitTestUtil.cfg.CfgNode;
import utils.autoUnitTestUtil.dataStructure.MemoryModel;
import utils.autoUnitTestUtil.dataStructure.Path;
import utils.autoUnitTestUtil.variable.ArrayTypeVariable;
import utils.autoUnitTestUtil.variable.PrimitiveTypeVariable;
import utils.autoUnitTestUtil.variable.Variable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public final class SymbolicExecution {
    private MemoryModel memoryModel;
    private List<Z3VariableWrapper> Z3Vars;
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
        Z3Vars = new ArrayList<>();

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

                    BoolExpr constrain = (BoolExpr) OperationExpressionNode.createZ3Expression((ExpressionNode) executedAstNode, ctx, Z3Vars, memoryModel);

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

            Variable variable = memoryModel.getVariable(name);

            if (variable instanceof PrimitiveTypeVariable) {
                Expr z3Variable = Variable.createZ3Variable(variable, ctx);
                if (z3Variable != null) {
                    Z3VariableWrapper z3VariableWrapper = new Z3VariableWrapper(z3Variable);
                    if (!haveDuplicateVariable(z3VariableWrapper)) {
                        Z3Vars.add(z3VariableWrapper);
                    }
                }
            } else if (variable instanceof ArrayTypeVariable) {
                ArrayTypeVariable arrayTypeVariable = (ArrayTypeVariable) variable;
                Z3VariableWrapper z3VariableWrapper = new Z3VariableWrapper(arrayTypeVariable);
                if (!haveDuplicateVariable(z3VariableWrapper)) {
                    Z3Vars.add(z3VariableWrapper);
                }
            } else {
                throw new RuntimeException("Invalid type variable");
            }
        } else {
            throw new RuntimeException("Invalid parameter");
        }
    }

    private boolean haveDuplicateVariable(Z3VariableWrapper z3Variable) {
        for (Z3VariableWrapper i : Z3Vars) {
            if (i.equals(z3Variable)) {
                return true;
            }
        }
        return false;
    }

    private Model createModel(Context ctx, BoolExpr f) {
        Solver s = ctx.mkSolver();
        s.add(f);
//        System.out.println(s);

        Status satisfaction = s.check();
        if (satisfaction != Status.SATISFIABLE) {
            System.out.println("Expression is UNSATISFIABLE");
            return null;
        } else {
            return s.getModel();
        }
    }

    private void evaluateAndSaveTestDataCreated() {
        if (model != null) {
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < Z3Vars.size(); i++) {
                Z3VariableWrapper z3VariableWrapper = Z3Vars.get(i);

                if (z3VariableWrapper.getPrimitiveVar() != null) {
                    Expr primitiveVar = z3VariableWrapper.getPrimitiveVar();

                    Expr evaluateResult = model.evaluate(primitiveVar, false);

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
                } else {
                    ArrayTypeVariable arrayTypeVariable = z3VariableWrapper.getArrayVar();
                    result.append(arrayTypeVariable.getConstraints());
                }

                if (i != Z3Vars.size() - 1) {
                    result.append("\n");
                }
            }

            writeDataToFile(result.toString());
        }
    }

    public static Object[] getEvaluatedTestData(Class<?>[] parameterClasses) {
        Object[] result = new Object[parameterClasses.length];
        Scanner scanner;
        try {
            scanner = new Scanner(new File(FilePath.generatedTestDataPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < parameterClasses.length; i++) {
            if (!scanner.hasNext()) {
                result[i] = createRandomVariableData(parameterClasses[i]);
                continue;
            }

            Class<?> parameterClass = parameterClasses[i];

            if (parameterClass.isPrimitive()) {

                String className = parameterClasses[i].getName();

                if ("int".equals(className)) {
                    result[i] = scanner.nextInt();
                } else if ("boolean".equals(className)) {
                    result[i] = scanner.nextBoolean();
                } else if ("byte".equals(className)) {
                    result[i] = scanner.nextByte();
                } else if ("short".equals(className)) {
                    result[i] = scanner.nextShort();
                } else if ("char".equals(className)) {
                    result[i] = (char) scanner.nextInt();
                } else if ("long".equals(className)) {
                    result[i] = scanner.nextLong();
                } else if ("float".equals(className)) {
                    result[i] = scanner.nextFloat();
                } else if ("double".equals(className)) {
                    result[i] = scanner.nextDouble();
                } else if ("void".equals(className)) {
                    result[i] = null;
                } else {
                    throw new RuntimeException("Unsupported type: " + className);
                }
            } else if (parameterClass.isArray()) {
                String constraint = scanner.nextLine();
                String[] constraints = constraint.split(" ");
                int numberOfDimensions = Integer.parseInt(constraints[0]);
                int[] dimensions = new int[numberOfDimensions];
                for (int j = 0; j < numberOfDimensions; j++) {
                    dimensions[j] = Integer.parseInt(constraints[j + 1]);
                }
                result[i] = Array.newInstance(parameterClass.getComponentType(), dimensions);

                // Specific element constraints!!!
            }
        }

        return result;
    }

    public static Object[] createRandomTestData(Class<?>[] parameterClasses) {
        Object[] result = new Object[parameterClasses.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = createRandomVariableData(parameterClasses[i]);
        }

        return result;
    }

    private static Object createRandomVariableData(Class<?> parameterClass) {
        if (parameterClass.isPrimitive()) {
            return createRandomPrimitiveVariableData(parameterClass);
        } else if (parameterClass.isArray()) {
            return createRandomArrayVariableData(parameterClass);
        }
        throw new RuntimeException("Unsupported type: " + parameterClass.getName());
    }

    private static Object createRandomArrayVariableData(Class<?> parameterClass) {
        int totalDimentsions = 1;
        for (Class<?> componentType = parameterClass.getComponentType(); ; ) {
            if (componentType.isArray()) {
                totalDimentsions++;
                componentType = componentType.getComponentType();
            } else {
                int[] dimensions = new int[totalDimentsions];
                Arrays.fill(dimensions, 10);
                return Array.newInstance(componentType, dimensions);
            }
        }
    }

    private static Object createRandomPrimitiveVariableData(Class<?> parameterClass) {
        String className = parameterClass.getName();
        Random random = new Random();

        if ("int".equals(className)) {
//            return random.nextInt();
            return 8;
        } else if ("boolean".equals(className)) {
            return random.nextInt() % 2 == 0;
        } else if ("byte".equals(className)) {
            return (byte) ((Math.random() * (127 - (-128)) + (-128)));
        } else if ("short".equals(className)) {
            return (short) ((Math.random() * (32767 - (-32768)) + (-32768)));
        } else if ("char".equals(className)) {
//            return (char) random.nextInt();
            return 'X';
        } else if ("long".equals(className)) {
//            return random.nextLong();
            return 16;
        } else if ("float".equals(className)) {
//            return random.nextFloat();
            return 8.0;
        } else if ("double".equals(className)) {
//            return random.nextDouble();
            return 8.0;
        } else if ("void".equals(className)) {
            return null;
        }
        throw new RuntimeException("Unsupported type: " + className);
    }

    private void writeDataToFile(String data) {
        try {
            FileWriter writer = new FileWriter(FilePath.generatedTestDataPath);
            writer.write(data + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Model getModel() {
        return model;
    }
}
