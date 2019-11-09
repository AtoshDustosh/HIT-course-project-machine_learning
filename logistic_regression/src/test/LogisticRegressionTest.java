package test;

import java.util.Vector;

import dataformat.LogisticRegressionDataset;
import datareading.LogisticRegressionCSVFileReader;
import logisticregression.LogisticRegression;

public class LogisticRegressionTest {

    /*
     * Because the arg values are very low, the importance of regulator in this sample is not very
     * significant. Recommended parameters: learningRate = exp(-13), regulator = exp(-6), maxIteration
     * = exp(9).
     */
    private static final String SAMPLEFILEPATH1 = "dataset/divorce_dualClass/divorce_Samples.csv";
    private static final String TESTFILEPATH1 = "dataset/divorce_dualClass/divorce.csv";

    /*
     * Regulator works a ve~ry little bit for reducing the value of 2-norms of vector W, but the
     * classification result is almost not changed.  Recommended parameters: learningRate = exp(-19),
     * regulator = exp(-14), maxIteration = exp(10).
     * Possibly this is due to samples we use.
     */
    private static final String SAMPLEFILEPATH2 = "dataset/breastCancer_dualClass/breastCancer_Samples.csv";
    private static final String TESTFILEPATH2 = "dataset/breastCancer_dualClass/breastCancer.csv";

    public static void main(String[] args) {
        LogisticRegressionCSVFileReader csvReader = new LogisticRegressionCSVFileReader();
        LogisticRegressionDataset testDataset = csvReader.readData(SAMPLEFILEPATH2);
        LogisticRegressionDataset sampleDataset = csvReader.readData(TESTFILEPATH2);

        sampleDataset.display();

        LogisticRegression LRProgram = null;
        Vector<Double> optimizedVectW = null;
        Vector<Double> classified = null;
        double recallRate = 0;

        // logistic regression - no regularization
        System.out.println("Logistic regression without regulator. ");
        LRProgram = new LogisticRegression(sampleDataset.getSamplesValueVector(),
                sampleDataset.getSamplesClassificationVector(), false);
        LRProgram.execute();
        optimizedVectW = LRProgram.getOptimizedVectW();
        classified = LRProgram.classify(testDataset.getSamplesValueVector());
        recallRate = LRProgram.recallRate(testDataset.getSamplesClassificationVector(), classified);
        System.out.println("Recall rate: " + recallRate);

        System.out.println("********************************");

        // logistic regression - with regularization
        System.out.println("Logistic regression with regulator: " + LogisticRegression.DEFAULTREGULATOR);
        LRProgram = new LogisticRegression(sampleDataset.getSamplesValueVector(),
                sampleDataset.getSamplesClassificationVector(), true);
        LRProgram.execute();
        ;
        optimizedVectW = LRProgram.getOptimizedVectW();
        classified = LRProgram.classify(testDataset.getSamplesValueVector());
        recallRate = LRProgram.recallRate(testDataset.getSamplesClassificationVector(), classified);
        System.out.println("Recall rate: " + recallRate);

    }
}
