package utils.autoUnitTestUtil.parser;

import utils.autoUnitTestUtil.cfg.CfgNode;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProjectParser {
    public static ArrayList<ASTNode> parseFile(String filePath) throws IOException
    {
        File file = new File(filePath);

        ArrayList<ASTNode> retFuncList = new ArrayList<>();

        if (file.isFile() && file.getName().endsWith(".java")) {
            String fileToString = FileService.readFileToString(file.getPath());

            retFuncList = CfgNode.parserToAstFuncList(fileToString);

            System.out.println("retFuncList.count = " + retFuncList.size());
        }

        return retFuncList;
    }

    public static List<MethodDeclaration> parseFileToConstructorList(String filePath) throws IOException {
        File file = new File(filePath);

        List<MethodDeclaration> constructorList = new ArrayList<>();

        if (file.isFile() && file.getName().endsWith(".java")) {
            String fileToString = FileService.readFileToString(file.getPath());

            constructorList = CfgNode.parserToConstructorList(fileToString);
        }

        return constructorList;
    }

    public static CompilationUnit parseFileToCompilationUnit(String filePath) throws IOException {
        File file = new File(filePath);

        CompilationUnit compilationUnit = null;

        if (file.isFile() && file.getName().endsWith(".java")) {
            String fileToString = FileService.readFileToString(file.getPath());
            compilationUnit = CfgNode.parserToCompilationUnit(fileToString);
        }
        return compilationUnit;
    }
}
