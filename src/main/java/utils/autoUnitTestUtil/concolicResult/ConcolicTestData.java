package utils.autoUnitTestUtil.concolicResult;

import java.util.ArrayList;
import java.util.List;

public class ConcolicTestData {
    private int testCaseID;
    private List<CoveredStatement> coveredStatements = new ArrayList<>();
    private List<ConcolicParameterData> parameterDataList = new ArrayList<>();
    private Object output;
    private double executeTime;
    private double sourceCodeCoverage;
    private double functionCoverage;
    private double requiredCoverage;
    private String status;

    public ConcolicTestData(List<String> names, Class<?>[] types, Object[] values, List<CoveredStatement> coveredStatements,
                            Object output, double executeTime, double requiredCoverage, double functionCoverage, double sourceCodeCoverage, int testCaseID) {
        if(names.size() != types.length || types.length != values.length) {
            throw new RuntimeException("Invalid");
        }

        for(int i = 0; i < names.size(); i++) {
            this.addToParameterDataList(new ConcolicParameterData(names.get(i), types[i].toString(), values[i].toString()));
        }

        this.coveredStatements = coveredStatements;
        this.output = output;
        this.executeTime = round(executeTime);
        this.sourceCodeCoverage = round(sourceCodeCoverage);
        this.functionCoverage = round(functionCoverage);
        this.requiredCoverage = round(requiredCoverage);
        this.status = "PASS";
        this.testCaseID = testCaseID;
    }

    private double round(double number) {
        return (double) Math.round(number * 100) / 100;
    }

    public void addToCoveredStatements(CoveredStatement statement) {
        coveredStatements.add(statement);
    }

    public void addToParameterDataList(ConcolicParameterData parameterData) {
        parameterDataList.add(parameterData);
    }

    public List<CoveredStatement> getCoveredStatements() {
        return coveredStatements;
    }

    public List<ConcolicParameterData> getParameterDataList() {
        return parameterDataList;
    }

    public Object getOutput() {
        return output;
    }

    public double getExecuteTime() {
        return executeTime;
    }

    public double getSourceCodeCoverage() {
        return sourceCodeCoverage;
    }

    public double getFunctionCoverage() {
        return functionCoverage;
    }

    public double getRequiredCoverage() {
        return requiredCoverage;
    }

    public String getStatus() {
        return status;
    }

    public int getTestCaseID() {
        return testCaseID;
    }

    @Override
    public String toString() {
        return "Test case " + testCaseID + ": " + status + " - Required coverage: " + requiredCoverage + "%";
    }
}
