package utils.autoUnitTestUtil.testDriver;
import java.io.FileWriter;
public class TestDriver {
private static boolean mark(String statement, boolean isTrueCondition, boolean isFalseCondition) {
StringBuilder markResult = new StringBuilder();
markResult.append(statement).append("===");
markResult.append(isTrueCondition).append("===");
markResult.append(isFalseCondition).append("---end---");
writeDataToFile(markResult.toString(), "src/main/java/utils/autoUnitTestUtil/concreteExecuteResult.txt", true);
if (!isTrueCondition && !isFalseCondition) return true;
return !isFalseCondition;
}
private static void writeDataToFile(String data, String path, boolean append) {
try {
FileWriter writer = new FileWriter(path, append);
writer.write(data);
writer.close();
} catch(Exception e) {
e.printStackTrace();
}
}
public static double intPow(double number, int index)
{
mark("double result=1;\n", false, false);
double result=1;
if (((index == 0) && mark("index == 0", true, false)) || mark("index == 0", false, true))
{
mark("return 1;\n", false, false);
return 1;
}
else {
if (((index < 0) && mark("index < 0", true, false)) || mark("index < 0", false, true))
{
{
mark("int i=0", false, false);
for (int i=0; ((i < -index) && mark("i < -index", true, false)) || mark("i < -index", false, true); mark("i++", false, false),
i++) {
{
mark("result*=number;\n", false, false);
result*=number;
}
}
mark("return 1 / result;\n", false, false);
return 1 / result;
}
}
else {
{
mark("int i=0", false, false);
for (int i=0; ((i < index) && mark("i < index", true, false)) || mark("i < index", false, true); mark("i++", false, false),
i++) {
{
mark("result*=number;\n", false, false);
result*=number;
}
}
mark("return result;\n", false, false);
return result;
}
}
}
}

public static void main(String[] args) {
writeDataToFile("", "core-engine/cfg/src/main/java/data/testDriverData/runTestDriverData.txt", false);
long startRunTestTime = System.nanoTime();
Object output = intPow(1.0, 0);
long endRunTestTime = System.nanoTime();
double runTestDuration = (endRunTestTime - startRunTestTime) / 1000000.0;
writeDataToFile(runTestDuration + "===" + output, "src/main/java/utils/autoUnitTestUtil/concreteExecuteResult.txt", true);
}
}