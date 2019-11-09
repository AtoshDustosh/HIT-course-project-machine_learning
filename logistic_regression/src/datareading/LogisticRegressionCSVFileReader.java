package datareading;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import dataformat.LogisticRegressionDataset;

/**
 * A file(.csv) reader for logistic regression.
 */
public class LogisticRegressionCSVFileReader {

    private static final String DATAFILEPATH1 = "dataset/divorce_dualclass/divorce.csv";
    private static final String DATAFILEPATH2 = "dataset/breastCancer_dualClass/breastCancer.csv";

    /**
     * Test this class.
     * @param args arguments
     */
    public static void main(String[] args) {
        LogisticRegressionCSVFileReader csvReader = new LogisticRegressionCSVFileReader();
        LogisticRegressionDataset dataset = csvReader.readData(DATAFILEPATH2);
        dataset.display();
        System.out.println(dataset.getSamplesClassificationVector());
    }

    /**
     * Read csv file and generate a dataset of LogisticRegressionDataset format.
     *
     * @param filePath file path
     * @return dataset generated
     */
    public LogisticRegressionDataset readData(String filePath) {
        String[] lines = null;
        try {
            long start = System.currentTimeMillis();
            String data = new String(Files.readAllBytes(Paths.get(filePath)));

            lines = data.split("\n");

            /*for (int i = 0; i < lines.length; i++) {
                System.out.println("Line(" + (i + 1) + "): " + lines[i]);
            }*/
            long end = System.currentTimeMillis();
            System.out.println("readAllBytes time spent: " + (end - start) + "(ms)");
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogisticRegressionDataset dataset = this.datasetGenerate(lines);
        return dataset;
    }

    /**
     * Detailed dataset generation process.
     *
     * @param lines String array read from csv file
     * @return dataset generated
     */
    private LogisticRegressionDataset datasetGenerate(String[] lines) {
        LogisticRegressionDataset dataset = new LogisticRegressionDataset();

        // set names of arguments
        List<String> argNames = new ArrayList<>();
        String[] argLine = lines[0].split(";");
        for (int i = 0; i < argLine.length; i++) {
            String str = argLine[i];
            if(str.contains("\r")){
                str = str.replace("\r","");
            }
            argNames.add(str);
        }
        dataset.setArgNames(argNames);

        // set values of arguments and samples' classification result
        List<List<Double>> samples = new ArrayList<>();
        List<Double> samplesClassification = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String[] line = lines[i].split(";");
            // add values of arguments of a sample
            List<Double> argValues = new ArrayList<>();
            for (int j = 0; j < line.length - 1; j++) {
                argValues.add(Double.valueOf(line[j]));
            }
            samples.add(argValues);
//            System.out.print(argValues);

            // add sample's classification result
            double classification = 0;
            String str = line[line.length-1].replace("\r","");
            if (str.equals("1")) {
                classification = 1;
            }
            samplesClassification.add(classification);
        }
        dataset.setSamples(samples, samplesClassification);

        return dataset;
    }

}
