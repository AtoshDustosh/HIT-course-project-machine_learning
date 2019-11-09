package debug;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class csvDataReadingTest {

    private static final String TESTPATH = "src/debug/fileReadingData.csv";
    private static final String DATAPATH = "dataset/divorce.dualclass/divorce.csv";

    private String[] lines = null;  // lines read from data file of csv format

    public static void main(String args[]) {
        csvDataReadingTest test = new csvDataReadingTest();
        test.readFile(TESTPATH);
        test.stringOperationsTest();

    }


    /**
     * Read data file of csv format.
     *
     * @param inputFile input data file of csv format
     */
    public void readFile(String inputFile) {
        String str = new String();
        try {
            long start = System.currentTimeMillis();
            str = new String(Files.readAllBytes(Paths.get(inputFile)));

            this.lines = str.split("\n");

            for (int i = 0; i < lines.length; i++) {
                System.out.println("Line(" + (i + 1) + "): " + lines[i]);
            }
            long end = System.currentTimeMillis();
            System.out.println("readAllBytes time spent: " + (end - start) + "(ms)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test some operations on String type.
     */
    public void stringOperationsTest() {
        String line = this.lines[0];
        String[] lineArray = line.split(";");

        for (int i = 0; i < lineArray.length; i++) {
            System.out.printf("%s", lineArray[i]);
            if ((i + 1) % 9 == 0) System.out.println();
        }
    }


}
