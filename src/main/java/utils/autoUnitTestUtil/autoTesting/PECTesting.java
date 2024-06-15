package utils.autoUnitTestUtil.autoTesting;

import controller.PEC_ToolController;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import utils.FilePath;
import utils.autoUnitTestUtil.algorithms.FindPath;
import utils.autoUnitTestUtil.algorithms.SymbolicExecution;
import utils.autoUnitTestUtil.autoTesting.PairwiseTesting.*;
import utils.autoUnitTestUtil.cfg.CfgBlockNode;
import utils.autoUnitTestUtil.cfg.CfgEndBlockNode;
import utils.autoUnitTestUtil.cfg.CfgNode;
import utils.autoUnitTestUtil.testResult.*;
import utils.autoUnitTestUtil.dataStructure.MarkedPath;
import utils.autoUnitTestUtil.dataStructure.MarkedStatement;
import utils.autoUnitTestUtil.dataStructure.Path;
import utils.autoUnitTestUtil.parser.ASTHelper;
import utils.autoUnitTestUtil.parser.ProjectParser;
import utils.autoUnitTestUtil.utils.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PECTesting {

    private static CompilationUnit compilationUnit;
    private static String simpleClassName;
    private static String fullyClonedClassName;
    private static ArrayList<ASTNode> funcAstNodeList;
    private static CfgNode cfgBeginNode;
    private static CfgEndBlockNode cfgEndNode;
    private static List<ASTNode> parameters;
    private static Class<?>[] parameterClasses;
    private static List<String> parameterNames;
    private static Method method;
    private static ASTNode testFunc;

    private PECTesting() {
    }

    private static long totalUsedMem = 0;
    private static long tickCount = 0;

    public static PECResult runFullPEC(String path, String methodName, String className, PEC_ToolController.Coverage coverage) throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchFieldException, InterruptedException {

        setup(path, className, methodName);
        setupCfgTree(coverage);
        setupParameters(methodName);

        totalUsedMem = 0;
        tickCount = 0;
        Timer T = new Timer(true);

        TimerTask memoryTask = new TimerTask() {
            @Override
            public void run() {
                totalUsedMem += (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                tickCount += 1;
            }
        };

        T.scheduleAtFixedRate(memoryTask, 0, 1); //0 delay and 5 ms tick

        ValueDictionary valueDictionary1 = new ValueDictionary(parameterNames.size());
        ValueDictionary valueDictionary2 = new ValueDictionary(parameterNames.size());
        ValueDictionary valueDictionary3 = new ValueDictionary(parameterNames.size());

        long startRunTestTime = System.nanoTime();

        ConcolicTestResult result = startGenerating(coverage);

        long endRunTestTime = System.nanoTime();
        double runTestDuration = (endRunTestTime - startRunTestTime) / 1000000.0;
        float usedMem = ((float) totalUsedMem) / tickCount / 1024 / 1024;
        System.out.println("DS1: " + runTestDuration + " ms, " + usedMem + " MB");
//        System.out.println(result);
        System.out.println(result.getFullTestData().size());

        PECDataSetResult DS1 = new PECDataSetResult(runTestDuration, usedMem, result.getFullCoverage(), result.getFullTestData().size(), result.getFullTestDataSet());

        List<Parameter> parameterList1 = PairwiseTestingUtils.convertTestResultToParameterValues(result);
        generatePairWiseTestdata(valueDictionary1, parameterList1);
        valueDictionary1.addDistinctToTestDataSet(result.getFullTestDataSet());

        endRunTestTime = System.nanoTime();
        runTestDuration = (endRunTestTime - startRunTestTime) / 1000000.0;
        usedMem = ((float) totalUsedMem) / tickCount / 1024 / 1024;
        System.out.println("DS2: " + runTestDuration + " ms, " + usedMem + " MB");
//        System.out.println(valueDictionary1);
        System.out.println(valueDictionary1.getTestDataSetSize());

        PECDataSetResult DS2 = new PECDataSetResult(runTestDuration, usedMem, result.getFullCoverage(), valueDictionary1.getTestDataSetSize(), valueDictionary1.getTestDataSet());

        List<Parameter> parameterList2 = GetParameterList.getFromAnalyzeSimpleConditions(cfgBeginNode, parameterClasses, parameterNames);
        parameterList2 = PairwiseTestingUtils.merge2ParameterList(parameterList1, parameterList2);
        generatePairWiseTestdata(valueDictionary2, parameterList2);
        valueDictionary2.addDistinctToTestDataSet(result.getFullTestDataSet());

        endRunTestTime = System.nanoTime();
        runTestDuration = (endRunTestTime - startRunTestTime) / 1000000.0;
        usedMem = ((float) totalUsedMem) / tickCount / 1024 / 1024;
        System.out.println("DS3: " + runTestDuration + " ms, " + usedMem + " MB");
//        System.out.println(valueDictionary2);
        System.out.println(valueDictionary2.getTestDataSetSize());

        PECDataSetResult DS3 = new PECDataSetResult(runTestDuration, usedMem, result.getFullCoverage(), valueDictionary2.getTestDataSetSize(), valueDictionary2.getTestDataSet());

        List<Parameter> parameterList3 = GetParameterList.getFromSymbolicExecuteAllPaths(cfgBeginNode, parameters, parameterClasses, parameterNames);
        parameterList3 = PairwiseTestingUtils.merge2ParameterList(parameterList2, parameterList3);
        generatePairWiseTestdata(valueDictionary3, parameterList3);
        valueDictionary3.addDistinctToTestDataSet(result.getFullTestDataSet());

        endRunTestTime = System.nanoTime();
        runTestDuration = (endRunTestTime - startRunTestTime) / 1000000.0;
        usedMem = ((float) totalUsedMem) / tickCount / 1024 / 1024;
        System.out.println("DS4: " + runTestDuration + " ms, " + usedMem + " MB");
//        System.out.println(valueDictionary3);
        System.out.println(valueDictionary3.getTestDataSetSize());

        PECDataSetResult DS4 = new PECDataSetResult(runTestDuration, usedMem, result.getFullCoverage(), valueDictionary3.getTestDataSetSize(), valueDictionary3.getTestDataSet());

        return new PECResult(DS1, DS2, DS3, DS4);
    }

    private static void generatePairWiseTestdata(ValueDictionary valueDictionary, List<Parameter> parameterList) {
        PairwiseTestingUtils.generateFirstTwoPairWiseTestData(valueDictionary, parameterList);

        for (int i = 2; i < parameterList.size(); i++) {
            List<Object> visitingParameterValues = parameterList.get(i).getValues();

            List<Pair> pairs = PairwiseTestingUtils.createPairsBetween2Paras(visitingParameterValues, i, parameterList);

            PairwiseTestingUtils.IPO_H(valueDictionary, i, parameterList, pairs);
            PairwiseTestingUtils.IPO_V(valueDictionary, i, parameterList, pairs);
        }
    }

    private static ConcolicTestResult startGenerating(PEC_ToolController.Coverage coverage) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchFieldException {
        ConcolicTestResult testResult = new ConcolicTestResult();
        int testCaseID = 1;
        Object[] evaluatedValues = utils.autoUnitTestUtil.testDriver.Utils.createRandomTestData(parameterClasses);

        writeDataToFile("", FilePath.concreteExecuteResultPath, false);

        long startRunTestTime = System.nanoTime();
        Object output = method.invoke(parameterClasses, evaluatedValues);
        long endRunTestTime = System.nanoTime();
        double runTestDuration = (endRunTestTime - startRunTestTime) / 1000000.0;

        List<MarkedStatement> markedStatements = getMarkedStatement();
        MarkedPath.markPathToCFGV2(cfgBeginNode, markedStatements);

        List<CoveredStatement> coveredStatements = CoveredStatement.switchToCoveredStatementList(markedStatements);

        testResult.addToFullTestData(new ConcolicTestData(parameterNames, parameterClasses, evaluatedValues, coveredStatements,
                output, runTestDuration, calculateRequiredCoverage(coverage), calculateFunctionCoverage(), calculateSourceCodeCoverage()));

        boolean isTestedSuccessfully = true;
        int i = 5;

        for (CfgNode uncoveredNode = findUncoverNode(cfgBeginNode, coverage); uncoveredNode != null; ) {

            Path newPath = (new FindPath(cfgBeginNode, uncoveredNode, cfgEndNode)).getPath();

            SymbolicExecution solution = new SymbolicExecution(newPath, parameters);

            if (solution.getModel() == null) {
                isTestedSuccessfully = false;
                break;
            }

            evaluatedValues = utils.autoUnitTestUtil.testDriver.Utils.getParameterValue(parameterClasses);

            writeDataToFile("", FilePath.concreteExecuteResultPath, false);

            startRunTestTime = System.nanoTime();
            output = method.invoke(parameterClasses, evaluatedValues);
            endRunTestTime = System.nanoTime();
            runTestDuration = (endRunTestTime - startRunTestTime) / 1000000.0;

            markedStatements = getMarkedStatement();
            MarkedPath.markPathToCFGV2(cfgBeginNode, markedStatements);

            coveredStatements = CoveredStatement.switchToCoveredStatementList(markedStatements);

            testResult.addToFullTestData(new ConcolicTestData(parameterNames, parameterClasses, evaluatedValues, coveredStatements, output, runTestDuration, calculateRequiredCoverage(coverage), calculateFunctionCoverage(), calculateSourceCodeCoverage()));

            uncoveredNode = findUncoverNode(cfgBeginNode, coverage);
            System.out.println("Uncovered Node: " + uncoveredNode);
        }

        if (isTestedSuccessfully) System.out.println("Tested successfully with 100% coverage");
        else System.out.println("Test fail due to UNSATISFIABLE constraint");

        testResult.setFullCoverage(calculateFullTestSuiteCoverage());

        return testResult;
    }

    private static List<MarkedStatement> getMarkedStatement() {
        List<MarkedStatement> result = new ArrayList<>();

        String markedData = getDataFromFile(FilePath.concreteExecuteResultPath);
        String[] markedStatements = markedData.split("---end---");
        for (int i = 0; i < markedStatements.length; i++) {
            String[] markedStatementData = markedStatements[i].split("===");
            String statement = markedStatementData[0];
            boolean isTrueConditionalStatement = Boolean.parseBoolean(markedStatementData[1]);
            boolean isFalseConditionalStatement = Boolean.parseBoolean(markedStatementData[2]);
            result.add(new MarkedStatement(statement, isTrueConditionalStatement, isFalseConditionalStatement));
        }
        return result;
    }

    private static void writeDataToFile(String data, String path, boolean append) {
        try {
            FileWriter writer = new FileWriter(path, append);
            writer.write(data);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getDataFromFile(String path) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            if ((line = br.readLine()) != null) {
                result.append(line);
            }
            while ((line = br.readLine()) != null) {
                result.append("\n").append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private static CfgNode findUncoverNode(CfgNode cfgNode, PEC_ToolController.Coverage coverage) {
        switch (coverage) {
            case STATEMENT:
                return MarkedPath.findUncoveredStatement(cfgNode);
            case BRANCH:
                return MarkedPath.findUncoveredBranch(cfgNode);
            default:
                throw new RuntimeException("Invalid coverage type");
        }
    }

    private static void setup(String path, String className, String methodName) throws IOException {
        funcAstNodeList = ProjectParser.parseFile(path);
        compilationUnit = ProjectParser.parseFileToCompilationUnit(path);
        setupFullyClonedClassName(className);
        setUpTestFunc(methodName);
        MarkedPath.resetFullTestSuiteCoveredStatements();
    }

    private static double calculateFullTestSuiteCoverage() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        int totalFunctionStatement = (int) Class.forName(fullyClonedClassName).getField(getTotalFunctionCoverageVariableName((MethodDeclaration) testFunc, PEC_ToolController.Coverage.STATEMENT)).get(null);
        int totalCovered = MarkedPath.getFullTestSuiteTotalCoveredStatements();
        return (totalCovered * 100.0) / totalFunctionStatement;
    }

    private static double calculateRequiredCoverage(PEC_ToolController.Coverage coverage) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        int totalFunctionCoverage = (int) Class.forName(fullyClonedClassName).getField(getTotalFunctionCoverageVariableName((MethodDeclaration) testFunc, coverage)).get(null);
        int totalCovered = 0;
        if (coverage == PEC_ToolController.Coverage.STATEMENT) {
            totalCovered = MarkedPath.getTotalCoveredStatement();
        } else if (coverage == PEC_ToolController.Coverage.BRANCH) {
            totalCovered = MarkedPath.getTotalCoveredBranch();
            System.out.println(totalCovered);
        }
        return (totalCovered * 100.0) / totalFunctionCoverage;
    }

    private static double calculateFunctionCoverage() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        int totalFunctionStatement = (int) Class.forName(fullyClonedClassName).getField(getTotalFunctionCoverageVariableName((MethodDeclaration) testFunc, PEC_ToolController.Coverage.STATEMENT)).get(null);
        int totalCoveredStatement = MarkedPath.getTotalCoveredStatement();
        return (totalCoveredStatement * 100.0) / (totalFunctionStatement * 1.0);
    }

    private static double calculateSourceCodeCoverage() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        int totalClassStatement = (int) Class.forName(fullyClonedClassName).getField(getTotalClassCoverageVariableName()).get(null);
        int totalCoveredStatement = MarkedPath.getTotalCoveredStatement();
        return (totalCoveredStatement * 100.0) / (totalClassStatement * 1.0);
    }

    private static void setUpTestFunc(String methodName) {
        for (ASTNode func : funcAstNodeList) {
            if (((MethodDeclaration) func).getName().getIdentifier().equals(methodName)) {
                testFunc = func;
            }
        }
    }

    private static void setupParameters(String methodName) throws ClassNotFoundException, NoSuchMethodException {
        parameters = ((MethodDeclaration) testFunc).parameters();
        parameterClasses = utils.autoUnitTestUtil.testDriver.Utils.getParameterClasses(parameters);
        parameterNames = utils.autoUnitTestUtil.testDriver.Utils.getParameterNames(parameters);
        method = Class.forName(fullyClonedClassName).getDeclaredMethod(methodName, parameterClasses);
    }

    private static void setupFullyClonedClassName(String className) {
        className = className.replace(".java", "");
        simpleClassName = className;
        String packetName = "";
        if (compilationUnit.getPackage() != null) {
            packetName = compilationUnit.getPackage().getName() + ".";
        }
        fullyClonedClassName = "clonedProject." + packetName + className;
    }

    private static void setupCfgTree(PEC_ToolController.Coverage coverage) {
        Block functionBlock = Utils.getFunctionBlock(testFunc);

        cfgBeginNode = new CfgNode();
        cfgBeginNode.setIsBeginCfgNode(true);

        cfgEndNode = new CfgEndBlockNode();
        cfgEndNode.setIsEndCfgNode(true);

        CfgNode block = new CfgBlockNode();
        block.setAst(functionBlock);

        int firstLine = compilationUnit.getLineNumber(functionBlock.getStartPosition());
        block.setLineNumber(1);

        block.setBeforeStatementNode(cfgBeginNode);
        block.setAfterStatementNode(cfgEndNode);

        ASTHelper.generateCFG(block, compilationUnit, firstLine, getCoverageType(coverage));
    }

    private static ASTHelper.Coverage getCoverageType(PEC_ToolController.Coverage coverage) {
        switch (coverage) {
            case STATEMENT:
                return ASTHelper.Coverage.STATEMENT;
            case BRANCH:
                return ASTHelper.Coverage.BRANCH;
            default:
                throw new RuntimeException("Invalid coverage");
        }
    }

    private static String getTotalFunctionCoverageVariableName(MethodDeclaration methodDeclaration, PEC_ToolController.Coverage coverage) {
        StringBuilder result = new StringBuilder();
        result.append(methodDeclaration.getReturnType2());
        result.append(methodDeclaration.getName());
        for (int i = 0; i < methodDeclaration.parameters().size(); i++) {
            result.append(methodDeclaration.parameters().get(i));
        }
        if (coverage == PEC_ToolController.Coverage.STATEMENT) {
            result.append("TotalStatement");
        } else if (coverage == PEC_ToolController.Coverage.BRANCH) {
            result.append("TotalBranch");
        } else {
            throw new RuntimeException("Invalid Coverage");
        }

        return reformatVariableName(result.toString());
    }

    private static String reformatVariableName(String name) {
        return name.replace(" ", "").replace(".", "")
                .replace("[", "").replace("]", "")
                .replace("<", "").replace(">", "")
                .replace(",", "");
    }

    private static String getTotalClassCoverageVariableName() {
        StringBuilder result = new StringBuilder();
        result.append(simpleClassName).append("TotalStatement");
        return result.toString().replace(" ", "").replace(".", "");
    }
}
