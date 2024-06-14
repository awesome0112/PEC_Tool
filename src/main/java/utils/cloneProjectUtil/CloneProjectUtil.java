package utils.cloneProjectUtil;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.*;
import utils.FilePath;
import utils.cloneProjectUtil.projectTreeObjects.Folder;
import utils.cloneProjectUtil.projectTreeObjects.JavaFile;
import utils.cloneProjectUtil.projectTreeObjects.ProjectTreeObject;
import utils.cloneProjectUtil.projectTreeObjects.Unit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CloneProjectUtil {

    private static int totalFunctionStatement;
    private static int totalClassStatement;
    private static int totalFunctionBranch;
    private enum CoverageType {
        STATEMENT,
        BRANCH
    }
    private static StringBuilder command;

    public static String getJavaDirPath(String originDir) {
        File dir = new File(originDir);

        if (!dir.isDirectory()) {
            throw new RuntimeException("Invalid Dir");
        }
        for(File file : Objects.requireNonNull(dir.listFiles())) {
            if(file.isDirectory()) {
                if(file.getName().equals("java")) {
                    return file.getPath();
                }
                else {
                    String dirPath = getJavaDirPath(file.getPath());
                    if(dirPath.endsWith("java")) return dirPath;
                }
            }
        }
        return "";
    }

    public static Folder cloneProject(String originalDirPath, String destinationDirPath) throws IOException, InterruptedException {
        command = new StringBuilder("javac -d " + FilePath.targetClassesFolderPath + " ");
        Folder rootFolder = new Folder("java");
        iCloneProject(originalDirPath, destinationDirPath, rootFolder);
        System.out.println(command);

        Process p = Runtime.getRuntime().exec(command.toString());
        System.out.println(p.waitFor());

        if(p.waitFor() != 0) {
            System.out.println("Can't compile project");
            throw new RuntimeException("Can't compile project");
        }

        return rootFolder;
    }

    private static void iCloneProject(String originalDirPath, String destinationDirPath, Folder folder) throws IOException {
        deleteFilesInDirectory(destinationDirPath);
        boolean existJavaFile = false;

        File[] files = getFilesInDirectory(originalDirPath);

        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                createCloneDirectory(destinationDirPath, dirName);
                Folder newFolder = new Folder(dirName);
                iCloneProject(originalDirPath + "\\" + dirName, destinationDirPath + "\\" + dirName, newFolder);
                folder.addChild(newFolder);
            } else if (file.isFile() && file.getName().endsWith("java")) {
                existJavaFile = true;
                totalClassStatement = 0;
                String fileName = file.getName();
                JavaFile javaFile = new JavaFile(fileName.replace(".java", ""));
                createCloneFile(destinationDirPath, fileName);
                CompilationUnit compilationUnit = Parser.parseFileToCompilationUnit(originalDirPath + "\\" + fileName);
                String sourceCode = createCloneSourceCode(compilationUnit, originalDirPath + "\\" + fileName, javaFile);
                writeDataToFile(sourceCode, destinationDirPath + "\\" + fileName);
                folder.addChild(javaFile);
            }
        }

        if (existJavaFile) {
            command.append(destinationDirPath).append("\\*.java ");
        }
    }


    private static File[] getFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.isDirectory()) {
            throw new RuntimeException("Invalid Dir: " + directory.getPath());
        }

        return directory.listFiles();
    }

    public static void deleteFilesInDirectory(String directoryPath) throws IOException {
//        File[] files = getFilesInDirectory(directoryPath);
//        for (File file : files) {
//            if (file.isDirectory()) {
//                deleteFilesInDirectory(file.getPath());
//            }
//            file.delete();
//        }
        if(Files.exists(Path.of(directoryPath))) {
            FileUtils.cleanDirectory(new File(directoryPath));
        } else {
            FileUtils.forceMkdir(new File(directoryPath));
        }
    }

    public static void createCloneDirectory(String parent, String child) {
        File newDirectory = new File(parent, child);

        boolean created = newDirectory.mkdir();

        if (!created) {
            System.out.println("Existed Dir");
//            deleteFilesInDirectory(newDirectory.getPath());
        }
    }

    private static void createCloneFile(String directoryPath, String fileName) {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            throw new RuntimeException("Invalid dir");
        }

        File newFile = new File(directory, fileName);

        try {
            boolean created = newFile.createNewFile();

            if (!created) {
                System.out.println("Existed file");
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't create file");
        }
    }

    private static String createCloneSourceCode(CompilationUnit compilationUnit, String filePath, JavaFile javaFile) {
        StringBuilder result = new StringBuilder();

        //Packet
        if (compilationUnit.getPackage() != null) {
            result.append("package clonedProject.").append(compilationUnit.getPackage().getName().toString()).append(";\n");
        } else {
            result.append("package clonedProject;\n");
        }

        //Imports
        for (ASTNode iImport : (List<ASTNode>) compilationUnit.imports()) {
            result.append(iImport);
        }
        result.append("import java.io.FileWriter;\n");

//        final ClassData[] classDataArr = {new ClassData()};
        List<ClassData> classDataArr = new ArrayList<>();
        ASTVisitor classVisitor = new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
//                classDataArr[0] = new ClassData(node);
                classDataArr.add(new ClassData(node));
                return true;
            }
        };
        compilationUnit.accept(classVisitor);

        // Class type (interface/class) and class name
        ClassData classData = classDataArr.get(0);

        result.append("public ").append(classData.getTypeOfClass()).append(" ").append(classData.getClassName());

        //Extensions
        if (classData.getSuperClassName() != null) {
            result.append(" extends ").append(classData.getSuperClassName());
        }

        //implementations
        if (classData.getSuperInterfaceName() != null) {
            result.append(" implements ");
            List<String> interfaceList = classData.getSuperInterfaceName();
            for (int i = 0; i < interfaceList.size(); i++) {
                result.append(interfaceList.get(i));
                if (i != interfaceList.size() - 1) {
                    result.append(", ");
                }
            }
        }

        result.append(" {\n");

        result.append(classData.getFields());

        result.append("private static void writeDataToFile(String data, String path, boolean append) {\n" +
                "try {\n" +
                "FileWriter writer = new FileWriter(path, append);\n" +
                "writer.write(data);\n" +
                "writer.close();\n" +
                "} catch (Exception e) {\n" +
                "e.printStackTrace();\n" +
                "}\n" +
                "}\n" +
                "private static boolean mark(String statement, boolean isTrueCondition, boolean isFalseCondition) {\n" +
                "StringBuilder markResult = new StringBuilder();\n" +
                "markResult.append(statement).append(\"===\");\n" +
                "markResult.append(isTrueCondition).append(\"===\");\n" +
                "markResult.append(isFalseCondition).append(\"---end---\");\n" +
                "writeDataToFile(markResult.toString(), \"" + "src/main/java/utils/autoUnitTestUtil/concreteExecuteResult.txt" + "\", true);\n" +
                "if (!isTrueCondition && !isFalseCondition) return true;\n" +
                "return !isFalseCondition;\n" +
                "}\n");

        List<ASTNode> methods = new ArrayList<>();
        ASTVisitor methodsVisitor = new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                for (MethodDeclaration method : node.getMethods()) {
//                    if (!method.isConstructor()) {
                        methods.add(method);
//                    }
                }
                return true;
            }
        };
        compilationUnit.accept(methodsVisitor);

        for (ASTNode astNode : methods) {
            totalFunctionStatement = 0;
            totalFunctionBranch = 0;
            MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;
            result.append(createCloneMethod(methodDeclaration));
            result.append(createTotalFunctionCoverageVariable(methodDeclaration, totalFunctionStatement, CoverageType.STATEMENT));
            result.append(createTotalFunctionCoverageVariable(methodDeclaration, totalFunctionBranch, CoverageType.BRANCH));
            String methodName = methodDeclaration.getName().toString();
            Unit unit = new Unit(methodName, filePath, methodName, javaFile.getName() + ".java");
            javaFile.addUnit(unit);
        }

        result.append(createTotalClassStatementVariable(classData));

        result.append("}");

        return result.toString();
    }

    private static String createTotalFunctionCoverageVariable(MethodDeclaration methodDeclaration, int totalStatement, CoverageType coverageType) {
        StringBuilder result = new StringBuilder();
        result.append(methodDeclaration.getReturnType2());
        result.append(methodDeclaration.getName());
        for (int i = 0; i < methodDeclaration.parameters().size(); i++) {
            result.append(methodDeclaration.parameters().get(i));
        }
        if(coverageType == CoverageType.STATEMENT) {
            result.append("TotalStatement");
        } else if (coverageType == CoverageType.BRANCH){
            result.append("TotalBranch");
        } else {
            throw new RuntimeException("Invalid Coverage");
        }
        return "public static final int ".concat(reformatVariableName(result.toString())).concat(" = " + totalStatement + ";\n");
    }

    private static String reformatVariableName(String name) {
        return name.replace(" ", "").replace(".", "")
                .replace("[", "").replace("]", "")
                .replace("<", "").replace(">", "")
                .replace(",", "");
    }

    private static String createTotalClassStatementVariable(ClassData classData) {
        StringBuilder result = new StringBuilder();
        result.append(classData.getClassName()).append("TotalStatement");
        return "public static final int ".concat(reformatVariableName(result.toString())).concat(" = " + totalClassStatement + ";\n");
    }

    private static String createCloneMethod(MethodDeclaration method) {
        StringBuilder cloneMethod = new StringBuilder();

        List<ASTNode> modifiers = method.modifiers();
        for(ASTNode modifier : modifiers) {
            cloneMethod.append(modifier).append(" ");
        }

        cloneMethod.append(method.getReturnType2() != null ? method.getReturnType2() : "").append(" ").append(method.getName()).append("(");
        List<ASTNode> parameters = method.parameters();
        for (int i = 0; i < parameters.size(); i++) {
            cloneMethod.append(parameters.get(i));
            if (i != parameters.size() - 1) cloneMethod.append(", ");
        }
        cloneMethod.append(")\n");

        cloneMethod.append(generateCodeForBlock(method.getBody())).append("\n");

        return cloneMethod.toString();
    }

    private static String generateCodeForOneStatement(ASTNode statement, String markMethodSeparator) {
        if (statement == null) {
            return "";
        }

        if (statement instanceof Block) {
            return generateCodeForBlock((Block) statement);
        } else if (statement instanceof IfStatement) {
            return generateCodeForIfStatement((IfStatement) statement);
        } else if (statement instanceof ForStatement) {
            return generateCodeForForStatement((ForStatement) statement);
        } else if (statement instanceof WhileStatement) {
            return generateCodeForWhileStatement((WhileStatement) statement);
        } else if (statement instanceof DoStatement) {
            return generateCodeForDoStatement((DoStatement) statement);
        } else {
            return generateCodeForNormalStatement(statement, markMethodSeparator);
        }

    }

    private static String generateCodeForBlock(Block block) {
        StringBuilder result = new StringBuilder();

        result.append("{\n");
        if(block != null) {
            List<ASTNode> statements = block.statements();
            for (int i = 0; i < statements.size(); i++) {
                result.append(generateCodeForOneStatement(statements.get(i), ";"));
            }
        }
        result.append("}\n");

        return result.toString();
    }

    private static String generateCodeForIfStatement(IfStatement ifStatement) {
        StringBuilder result = new StringBuilder();

        result.append("if (").append(generateCodeForCondition(ifStatement.getExpression())).append(")\n");
        result.append("{\n");
        result.append(generateCodeForOneStatement(ifStatement.getThenStatement(), ";"));
        result.append("}\n");


        String elseCode = generateCodeForOneStatement(ifStatement.getElseStatement(), ";");
        if (!elseCode.equals("")) {
            result.append("else {\n").append(elseCode).append("}\n");
        }

        return result.toString();
    }

    private static String generateCodeForForStatement(ForStatement forStatement) {
        StringBuilder result = new StringBuilder();

        // Initializers
        List<ASTNode> initializers = forStatement.initializers();
        for (ASTNode initializer : initializers) {
            result.append(generateCodeForMarkMethod(initializer, ";"));
        }
        result.append("for (");
        for (int i = 0; i < initializers.size(); i++) {
            result.append(initializers.get(i));
            if (i != initializers.size() - 1) result.append(", ");
        }

        // Condition
        result.append("; ");
        result.append(generateCodeForCondition(forStatement.getExpression()));

        // Updaters
        result.append("; ");
        List<ASTNode> updaters = forStatement.updaters();
        for (int i = 0; i < updaters.size(); i++) {
            result.append(generateCodeForOneStatement(updaters.get(i), ","));
            if (i != updaters.size() - 1) result.append(", ");
        }

        // Body
        result.append(") {\n");
        result.append(generateCodeForOneStatement(forStatement.getBody(), ";"));
        result.append("}\n");

        return result.toString();
    }

    private static String generateCodeForWhileStatement(WhileStatement whileStatement) {
        StringBuilder result = new StringBuilder();

        // Condition
        result.append("while (");
        result.append(generateCodeForCondition(whileStatement.getExpression()));
        result.append(") {\n");

        result.append(generateCodeForOneStatement(whileStatement.getBody(), ";"));
        result.append("}\n");

        return result.toString();
    }

    private static String generateCodeForDoStatement(DoStatement doStatement) {
        StringBuilder result = new StringBuilder();

        // Do body
        result.append("do {");
        result.append(generateCodeForOneStatement(doStatement.getBody(), ";"));
        result.append("}\n");

        // Condition
        result.append("while (");
        result.append(generateCodeForCondition(doStatement.getExpression()));
        result.append(");\n");

        return result.toString();
    }

    private static String generateCodeForNormalStatement(ASTNode statement, String markMethodSeparator) {
        StringBuilder result = new StringBuilder();

        result.append(generateCodeForMarkMethod(statement, markMethodSeparator));
        result.append(statement);

        return result.toString();
    }

    private static String generateCodeForMarkMethod(ASTNode statement, String markMethodSeparator) {
        StringBuilder result = new StringBuilder();

        String stringStatement = statement.toString();
        StringBuilder newStatement = new StringBuilder();

        // Rewrite Statement for mark method
        for (int i = 0; i < stringStatement.length(); i++) {
            char charAt = stringStatement.charAt(i);

            if (charAt == '\n') {
                newStatement.append("\\n");
                continue;
            } else if (charAt == '"') {
                newStatement.append("\\").append('"');
                continue;
            } else if (i != stringStatement.length() - 1 && charAt == '\\' && stringStatement.charAt(i + 1) == 'n') {
                newStatement.append("\" + \"").append("\\n").append("\" + \"");
                i++;
                continue;
            }

            newStatement.append(charAt);
        }

        result.append("mark(\"").append(newStatement).append("\", false, false)").append(markMethodSeparator).append("\n");
        totalFunctionStatement++;
        totalClassStatement++;

        return result.toString();
    }

    private static String generateCodeForCondition(Expression condition) {
        totalFunctionStatement++;
        totalClassStatement++;
        totalFunctionBranch += 2;
        return "((" + condition + ") && mark(\"" + condition + "\", true, false))" +
                " || mark(\"" + condition + "\", false, true)";
    }

    private static void writeDataToFile(String data, String path) {
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(data + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
