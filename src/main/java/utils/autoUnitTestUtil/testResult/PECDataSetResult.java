package utils.autoUnitTestUtil.testResult;

import java.util.List;

public class PECDataSetResult {

    private double generateTime;
    private float memoryUsed;
    private double coverage;
    private int testCaseNum;

    private List<List<Object>> testDataList;

    public PECDataSetResult(double generateTime, float memoryUsed, double coverage, int testCaseNum, List<List<Object>> testDataList) {
        this.generateTime = round(generateTime);
        this.memoryUsed = (float) round(memoryUsed);
        this.coverage = round(coverage);
        this.testCaseNum = testCaseNum;
        this.testDataList = testDataList;
    }

    private static double round(double num) {
        return Math.round(num * 100.0) / 100.0;
    }

    public double getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(double generateTime) {
        this.generateTime = generateTime;
    }

    public float getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(float memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public double getCoverage() {
        return coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    public int getTestCaseNum() {
        return testCaseNum;
    }

    public void setTestCaseNum(int testCaseNum) {
        this.testCaseNum = testCaseNum;
    }

    public List<List<Object>> getTestDataList() {
        return testDataList;
    }

    public void setTestDataList(List<List<Object>> testDataList) {
        this.testDataList = testDataList;
    }
}
