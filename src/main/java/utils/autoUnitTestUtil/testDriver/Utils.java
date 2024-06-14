package utils.autoUnitTestUtil.testDriver;

import org.eclipse.jdt.core.dom.*;
import utils.FilePath;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public final class Utils {
    private Utils() {
        throw new AssertionError("Utility class should not be instantiated.");
    }

    /**
     * Get parameter's classes list from AST Node List
     *
     * @param parameters (get from MethodDeclaration)
     * @return parameter's classes list
     */
    public static Class<?>[] getParameterClasses(List<ASTNode> parameters) {
        Class<?>[] types = new Class[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            ASTNode param = parameters.get(i);
            if (param instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration declaration = (SingleVariableDeclaration) param;
                Type type = declaration.getType();
                types[i] = getTypeClass(type);
            } else if (param instanceof VariableDeclarationFragment) {
                VariableDeclarationFragment declaration = (VariableDeclarationFragment) param;
                Type type = (Type) declaration.resolveBinding().getType();
                types[i] = getTypeClass(type);
            } else {
                throw new RuntimeException("Unsupported parameter: " + param.getClass());
            }
        }

        return types;
    }

    public static List<String> getParameterNames(List<ASTNode> parameters) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            ASTNode param = parameters.get(i);
            if (param instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration declaration = (SingleVariableDeclaration) param;
                names.add(declaration.getName().getIdentifier());
            } else if (param instanceof VariableDeclarationFragment) {
                VariableDeclarationFragment declaration = (VariableDeclarationFragment) param;
                names.add(declaration.getName().getIdentifier());
            } else {
                throw new RuntimeException("Unsupported parameter: " + param.getClass());
            }
        }
        return names;
    }

    /**
     * Get class base on the type (can be PrimitiveType, ArrayType)
     *
     * @param type
     * @return the class of the type
     */
    private static Class<?> getTypeClass(Type type) {
        if (type instanceof PrimitiveType) {
            PrimitiveType.Code primitiveTypeCode = (((PrimitiveType) type).getPrimitiveTypeCode());
            return getPrimitiveClass(primitiveTypeCode);
        } else if (type instanceof ArrayType) {
            ArrayType arrayType = (ArrayType) type;
            Type componentType = arrayType.getElementType();
            Class<?> componentClass = getTypeClass(componentType); // get clas of the component of array like int, double, ...
            int dimension = arrayType.getDimensions();
            return Array.newInstance(componentClass, new int[dimension]).getClass();
        } else if (type instanceof SimpleType) {
            SimpleType simpleType = (SimpleType) type;
            return getClassForName(simpleType.getName().getFullyQualifiedName());
        } else {
            throw new RuntimeException("Unsupported parameter type: " + type.getClass());
        }
    }

    /**
     * Get primitive class by its type code
     *
     * @param primitiveTypeCode
     * @return class of the code
     */
    private static Class<?> getPrimitiveClass(PrimitiveType.Code primitiveTypeCode) {
        String primitiveTypeStr = primitiveTypeCode.toString();
        switch (primitiveTypeStr) {
            case "int":
                return int.class;
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "char":
                return char.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "void":
                return void.class;
            default:
                throw new RuntimeException("Unsupported primitive type: " + primitiveTypeStr);
        }
    }

    /**
     * Get class of SimpleType by is name
     *
     * @param className
     * @return class
     */
    private static Class<?> getClassForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found: " + className, e);
        }
    }

    public static Object[] getParameterValue(Class<?>[] parameterClasses) {
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

//    public static void createCloneMethod(MethodDeclaration method, CompilationUnit compilationUnit) {
//        StringBuilder cloneMethod = new StringBuilder();
//        cloneMethod.append("package data;\n");
//        for(ASTNode iImport : (List<ASTNode>) compilationUnit.imports()) {
//            cloneMethod.append(iImport);
//        }
//        cloneMethod.append("import static core.dataStructure.MarkedPath.markOneStatement;\n");
//        cloneMethod.append("public class CloneFile {\n");
//        cloneMethod.append("public static ").append(method.getReturnType2()).append(" ").append(method.getName()).append("(");
//        List<ASTNode> parameters = method.parameters();
//        for (int i = 0; i < parameters.size(); i++) {
//            cloneMethod.append(parameters.get(i));
//            if (i != parameters.size() - 1) cloneMethod.append(", ");
//        }
//        cloneMethod.append(")\n");
//
//        cloneMethod.append(generateCodeForBlock(method.getBody()));
//
//        cloneMethod.append("}");
//
//        writeDataToFile(cloneMethod.toString(), "core-engine/cfg/src/main/java/data/CloneFile.java");
//
//        reCompileClonedFile();
//    }
//
//    private static void reCompileClonedFile() {
//        // Re-compile cloned file
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        int compilationResult = compiler.run(null, null, null, "core-engine\\cfg\\src\\main\\java\\data\\CloneFile.java");
//        if (compilationResult == 0) {
//            System.out.println("Compilation is successful.");
//        } else {
//            System.out.println("Compilation failed.");
//        }
//
//        // Move .class file of clone file to target folder
//        String sourceFilePath = "core-engine\\cfg\\src\\main\\java\\data\\CloneFile.class";
//        String destinationDirectoryPath = "core-engine\\cfg\\target\\classes\\data\\";
//        try {
//            File sourceFile = new File(sourceFilePath);
//            File destinationDirectory = new File(destinationDirectoryPath);
//
//            if (sourceFile.exists()) {
//                if (destinationDirectory.exists() && destinationDirectory.isDirectory()) {
//
//                    // Move file
//                    java.nio.file.Path sourcePath = sourceFile.toPath();
//                    java.nio.file.Path destinationPath = new File(destinationDirectory, sourceFile.getName()).toPath();
//                    Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e.getMessage());
//        }
//    }
//
//    private static String generateCodeForOneStatement(ASTNode statement, String markMethodSeparator) {
//        if (statement == null) {
//            return "";
//        }
//
//        if (statement instanceof Block) {
//            return generateCodeForBlock((Block) statement);
//        } else if (statement instanceof IfStatement) {
//            return generateCodeForIfStatement((IfStatement) statement);
//        } else if (statement instanceof ForStatement) {
//            return generateCodeForForStatement((ForStatement) statement);
//        } else if (statement instanceof WhileStatement) {
//            return generateCodeForWhileStatement((WhileStatement) statement);
//        } else if (statement instanceof DoStatement) {
//            return generateCodeForDoStatement((DoStatement) statement);
//        } else {
//            return generateCodeForNormalStatement(statement, markMethodSeparator);
//        }
//
//    }
//
//    private static String generateCodeForBlock(Block block) {
//        StringBuilder result = new StringBuilder();
//        List<ASTNode> statements = block.statements();
//
//        result.append("{\n");
//        for (int i = 0; i < statements.size(); i++) {
//            result.append(generateCodeForOneStatement(statements.get(i), ";"));
//        }
//        result.append("}\n");
//
//        return result.toString();
//    }
//
//    private static String generateCodeForIfStatement(IfStatement ifStatement) {
//        StringBuilder result = new StringBuilder();
//
//        result.append("if (").append(generateCodeForCondition(ifStatement.getExpression())).append(")\n");
//        result.append("{\n");
//        result.append(generateCodeForOneStatement(ifStatement.getThenStatement(), ";"));
//        result.append("}\n");
//
//
//        String elseCode = generateCodeForOneStatement(ifStatement.getElseStatement(), ";");
//        if (!elseCode.equals("")) {
//            result.append("else {\n").append(elseCode).append("}\n");
//        }
//
//        return result.toString();
//    }
//
//    private static String generateCodeForForStatement(ForStatement forStatement) {
//        StringBuilder result = new StringBuilder();
//
//        // Initializers
//        List<ASTNode> initializers = forStatement.initializers();
//        for (ASTNode initializer : initializers) {
//            result.append(generateCodeForMarkMethod(initializer, ";"));
//        }
//        result.append("for (");
//        for (int i = 0; i < initializers.size(); i++) {
//            result.append(initializers.get(i));
//            if (i != initializers.size() - 1) result.append(", ");
//        }
//
//        // Condition
//        result.append("; ");
//        result.append(generateCodeForCondition(forStatement.getExpression()));
//
//        // Updaters
//        result.append("; ");
//        List<ASTNode> updaters = forStatement.updaters();
//        for (int i = 0; i < updaters.size(); i++) {
//            result.append(generateCodeForOneStatement(updaters.get(i), ","));
//            if (i != updaters.size() - 1) result.append(", ");
//        }
//
//        // Body
//        result.append(") {\n");
//        result.append(generateCodeForOneStatement(forStatement.getBody(), ";"));
//        result.append("}\n");
//
//        return result.toString();
//    }
//
//    private static String generateCodeForWhileStatement(WhileStatement whileStatement) {
//        StringBuilder result = new StringBuilder();
//
//        // Condition
//        result.append("while (");
//        result.append(generateCodeForCondition(whileStatement.getExpression()));
//        result.append(") {\n");
//
//        result.append(generateCodeForOneStatement(whileStatement.getBody(), ";"));
//        result.append("}\n");
//
//        return result.toString();
//    }
//
//    private static String generateCodeForDoStatement(DoStatement doStatement) {
//        StringBuilder result = new StringBuilder();
//
//        // Do body
//        result.append("do {");
//        result.append(generateCodeForOneStatement(doStatement.getBody(), ";"));
//        result.append("}\n");
//
//        // Condition
//        result.append("while (");
//        result.append(generateCodeForCondition(doStatement.getExpression()));
//        result.append(");\n");
//
//        return result.toString();
//    }
//
//    private static String generateCodeForNormalStatement(ASTNode statement, String markMethodSeparator) {
//        StringBuilder result = new StringBuilder();
//
//        result.append(generateCodeForMarkMethod(statement, markMethodSeparator));
//        result.append(statement);
//
//        return result.toString();
//    }
//
//    private static String generateCodeForMarkMethod(ASTNode statement, String markMethodSeparator) {
//        StringBuilder result = new StringBuilder();
//
//        String stringStatement = statement.toString();
//        StringBuilder newStatement = new StringBuilder();
//
//        // Rewrite Statement for mark method
//        for (int i = 0; i < stringStatement.length(); i++) {
//            char charAt = stringStatement.charAt(i);
//
//            if (charAt == '\n') {
//                newStatement.append("\\n");
//                continue;
//            } else if (charAt == '"') {
//                newStatement.append("\\").append('"');
//                continue;
//            } else if (i != stringStatement.length() - 1 && charAt == '\\' && stringStatement.charAt(i + 1) == 'n') {
//                newStatement.append("\" + \"").append("\\n").append("\" + \"");
//                i++;
//                continue;
//            }
//
//            newStatement.append(charAt);
//        }
//
//        result.append("markOneStatement(\"").append(newStatement).append("\", false, false)").append(markMethodSeparator).append("\n");
//
//        return result.toString();
//    }
//
//    private static String generateCodeForCondition(Expression condition) {
//        StringBuilder result = new StringBuilder();
//
//        if (condition instanceof InfixExpression && isSeparableOperator(((InfixExpression) condition).getOperator())) {
//            InfixExpression infixCondition = (InfixExpression) condition;
//
//            result.append("(").append(generateCodeForCondition(infixCondition.getLeftOperand())).append(") ").append(infixCondition.getOperator()).append(" (");
//            result.append(generateCodeForCondition(infixCondition.getRightOperand())).append(")");
//
//            List<ASTNode> extendedOperands = infixCondition.extendedOperands();
//            for (ASTNode operand : extendedOperands) {
//                result.append(" ").append(infixCondition.getOperator()).append(" ");
//                result.append("(").append(generateCodeForCondition((Expression) operand)).append(")");
//            }
//        } else {
//            result.append("((").append(condition).append(") && markOneStatement(\"").append(condition).append("\", true, false))");
//            result.append(" || markOneStatement(\"").append(condition).append("\", false, true)");
//        }
//
//        return result.toString();
//    }
//
//    private static boolean isSeparableOperator(InfixExpression.Operator operator) {
//        return operator.equals(InfixExpression.Operator.CONDITIONAL_OR) ||
//                operator.equals(InfixExpression.Operator.OR) ||
//                operator.equals(InfixExpression.Operator.CONDITIONAL_AND) ||
//                operator.equals(InfixExpression.Operator.AND);
//    }

    public static void writeDataToFile(String data, String path) {
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(data + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
