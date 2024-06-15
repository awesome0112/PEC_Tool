package utils.autoUnitTestUtil.testResult;

import java.util.ArrayList;
import java.util.List;

public class ConcolicTestResult {
    private List<ConcolicTestData> fullTestData = new ArrayList<>();
    private double fullCoverage = 0;

    public void addToFullTestData(ConcolicTestData testData) {
        fullTestData.add(testData);
    }

    public List<ConcolicTestData> getFullTestData() {
        return fullTestData;
    }

    public double getFullCoverage() {
        return fullCoverage;
    }

    public void setFullCoverage(double fullCoverage) {
        this.fullCoverage = (double) Math.round(fullCoverage * 100) / 100;
    }

    public List<List<Object>> getFullTestDataSet() {
        List<List<Object>> result = new ArrayList<>();
        for (ConcolicTestData testData : fullTestData) {
            result.add(testData.getTestDataSet());
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
//        for (int i = 0; i < fullTestData.size(); i++) {
//            result.append("Test no " + i + ": " + fullTestData.get(i));
//        }
        result.append(fullTestData);
        return result.toString();
    }
}
