package utils.autoUnitTestUtil.testResult;

public class PECResult {

    private PECDataSetResult DS1;
    private PECDataSetResult DS2;
    private PECDataSetResult DS3;
    private PECDataSetResult DS4;

    public PECResult(PECDataSetResult DS1, PECDataSetResult DS2, PECDataSetResult DS3, PECDataSetResult DS4) {
        this.DS1 = DS1;
        this.DS2 = DS2;
        this.DS3 = DS3;
        this.DS4 = DS4;
    }

    public PECDataSetResult getDS1() {
        return DS1;
    }

    public PECDataSetResult getDS2() {
        return DS2;
    }

    public PECDataSetResult getDS3() {
        return DS3;
    }

    public PECDataSetResult getDS4() {
        return DS4;
    }
}
