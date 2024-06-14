package utils.uploadUtil;

import org.eclipse.jdt.core.dom.*;
import utils.cloneProjectUtil.Parser;
import utils.cloneProjectUtil.projectTreeObjects.Folder;
import utils.cloneProjectUtil.projectTreeObjects.JavaFile;
import utils.cloneProjectUtil.projectTreeObjects.Unit;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ConcolicUploadUtil {

    public static final Map<String, Integer> totalStatementsInJavaFile = new HashMap<>();
    public static final Map<String, Integer> totalStatementsInUnits = new HashMap<>();
    public static final Map<String, Integer> totalBranchesInUnits = new HashMap<>();


    public static String javaUnzipFile(String Filepath, String DestinationFolderPath) {
        try {
            File destDir = new File(DestinationFolderPath);
            FileInputStream fin = new FileInputStream(Filepath);
            BufferedInputStream bin = new BufferedInputStream(fin);
            ZipInputStream zis = new ZipInputStream(bin);

            String unzipDestinationFolder = zis.getNextEntry().getName();

            ZipEntry zipEntry = null;
            int count = 0;
            while ((zipEntry = zis.getNextEntry()) != null) {
                // ...
                count++;
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    BufferedOutputStream bufout = new BufferedOutputStream(fos);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = zis.read(buffer)) != -1) {
                        bufout.write(buffer, 0, len);
                    }
                    zis.closeEntry();
                    bufout.close();
                    fos.close();
                }
            }
            System.out.println(count);
            zis.close();
            return unzipDestinationFolder;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private static int totalFunctionStatements;
    private static int totalClassStatements;
    private static int totalFunctionBranches;

    private enum CoverageType {
        STATEMENT,
        BRANCH
    }

    public static Folder createProjectTree (String dirPath) throws IOException {
        Folder rootFolder = new Folder("java");
        iCreateProjectTree(dirPath, rootFolder);
        return rootFolder;
    }

    private static void iCreateProjectTree (String dirPath, Folder folder) throws IOException {

        File[] files = getFilesInDirectory(dirPath);

        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                Folder newFolder = new Folder(dirName);
                iCreateProjectTree(dirPath + "\\" + dirName, newFolder);
                folder.addChild(newFolder);
            } else if (file.isFile() && file.getName().endsWith("java")) {
                totalClassStatements = 0;
                String fileName = file.getName();
                JavaFile javaFile = new JavaFile(fileName.replace(".java", ""));
                CompilationUnit compilationUnit = Parser.parseFileToCompilationUnit(dirPath + "\\" + fileName);
                createJavaFileTreeObject(compilationUnit, dirPath + "\\" + fileName, javaFile);
                folder.addChild(javaFile);
            }
        }
    }

    private static File[] getFilesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (!directory.isDirectory()) {
            throw new RuntimeException("Invalid Dir: " + directory.getPath());
        }

        return directory.listFiles();
    }

    private static void createJavaFileTreeObject(CompilationUnit compilationUnit, String filePath, JavaFile javaFile) {
        String key = (compilationUnit.getPackage() != null ? compilationUnit.getPackage().getName().toString() : "") + javaFile.getName() + "totalStatement";

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
            totalFunctionStatements = 0;
            totalFunctionBranches = 0;
            MethodDeclaration methodDeclaration = (MethodDeclaration) astNode;
            createUnitTreeObject(methodDeclaration);
            createTotalFunctionCoverageVariable(methodDeclaration, key, totalFunctionStatements, CoverageType.STATEMENT);
            createTotalFunctionCoverageVariable(methodDeclaration, key, totalFunctionBranches, CoverageType.BRANCH);
            String methodName = methodDeclaration.getName().toString();
            Unit unit = new Unit(methodName, filePath, methodName, javaFile.getName() + ".java");
            javaFile.addUnit(unit);
        }

        createTotalClassStatementVariable(key);
    }

    private static void createTotalFunctionCoverageVariable(MethodDeclaration methodDeclaration, String classKey, int totalStatement, CoverageType coverageType) {
        StringBuilder result = new StringBuilder(classKey);
        result.append(methodDeclaration.getReturnType2());
        result.append(methodDeclaration.getName());
        for (int i = 0; i < methodDeclaration.parameters().size(); i++) {
            result.append(methodDeclaration.parameters().get(i));
        }
        if(coverageType == CoverageType.STATEMENT) {
            result.append("TotalStatement");
            totalStatementsInUnits.put(reformatVariableName(result.toString()), totalStatement);
        } else if (coverageType == CoverageType.BRANCH){
            result.append("TotalBranch");
            totalBranchesInUnits.put(reformatVariableName(result.toString()), totalStatement);
        } else {
            throw new RuntimeException("Invalid Coverage");
        }

    }

    private static String reformatVariableName(String name) {
        return name.replace(" ", "").replace(".", "")
                .replace("[", "").replace("]", "")
                .replace("<", "").replace(">", "")
                .replace(",", "");
    }

    private static void createTotalClassStatementVariable(String key) {
        totalStatementsInJavaFile.put(key, totalClassStatements);
    }

    private static void createUnitTreeObject(MethodDeclaration method) {
        generateForBlock(method.getBody());
    }

    private static void generateForOneStatement(ASTNode statement, String markMethodSeparator) {
        if (statement == null) {
            return;
        }

        if (statement instanceof Block) {
            generateForBlock((Block) statement);
        } else if (statement instanceof IfStatement) {
            generateForIfStatement((IfStatement) statement);
        } else if (statement instanceof ForStatement) {
            generateForForStatement((ForStatement) statement);
        } else if (statement instanceof WhileStatement) {
            generateForWhileStatement((WhileStatement) statement);
        } else if (statement instanceof DoStatement) {
            generateForDoStatement((DoStatement) statement);
        } else {
            generateForNormalStatement(statement, markMethodSeparator);
        }

    }

    private static void generateForBlock(Block block) {
        if(block != null) {
            List<ASTNode> statements = block.statements();
            for (int i = 0; i < statements.size(); i++) {
                generateForOneStatement(statements.get(i), ";");
            }
        }
    }

    private static void generateForIfStatement(IfStatement ifStatement) {
        generateForCondition(ifStatement.getExpression());
        generateForOneStatement(ifStatement.getThenStatement(), ";");
        generateForOneStatement(ifStatement.getElseStatement(), ";");
    }

    private static void generateForForStatement(ForStatement forStatement) {

        // Initializers
        List<ASTNode> initializers = forStatement.initializers();
        for (ASTNode initializer : initializers) {
            updateTotalStatments(initializer, ";");
        }

        generateForCondition(forStatement.getExpression());

        List<ASTNode> updaters = forStatement.updaters();
        for (int i = 0; i < updaters.size(); i++) {
            generateForOneStatement(updaters.get(i), ",");
        }

        generateForOneStatement(forStatement.getBody(), ";");
    }

    private static void generateForWhileStatement(WhileStatement whileStatement) {
        generateForCondition(whileStatement.getExpression());
        generateForOneStatement(whileStatement.getBody(), ";");
    }

    private static void generateForDoStatement(DoStatement doStatement) {
        generateForOneStatement(doStatement.getBody(), ";");
        generateForCondition(doStatement.getExpression());
    }

    private static void generateForNormalStatement(ASTNode statement, String markMethodSeparator) {
        updateTotalStatments(statement, markMethodSeparator);
    }

    private static void updateTotalStatments(ASTNode statement, String markMethodSeparator) {
        totalFunctionStatements++;
        totalClassStatements++;
    }

    private static void generateForCondition(Expression condition) {
        totalFunctionStatements++;
        totalClassStatements++;
        totalFunctionBranches += 2;
    }
}
