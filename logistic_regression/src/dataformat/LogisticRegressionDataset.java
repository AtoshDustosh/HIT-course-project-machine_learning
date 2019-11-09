package dataformat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * A customized logistic regression dataset. Classification result is dual, and all sample arguments
 * should be Double.(You can also use Double to represent different types, such as "1" for "type A",
 * "2" for "type B")
 * <p>
 * CSV file requirement: fist line should be names of arguments; all arguments should be Double;
 * classification is dual. Refer to the standardFile.csv file for details.
 * </p>
 */
public class LogisticRegressionDataset {

    private final List<String> argNames = new ArrayList<String>();
    // a list of names for all arguments
    private final List<List<Double>> samples = new ArrayList<>();
    // a list of samples which contain values for all arguments
    private final List<Double> samplesClassification = new ArrayList<>();
    // a list of classification result of samples

    private boolean argSettingFinished = false;
    // check if argNames are set; otherwise samples cannot be created.

    /**
     * Test this class.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        LogisticRegressionDataset dataset = new LogisticRegressionDataset();
        List<String> argNames = Arrays.asList("arg1", "arg2", "arg3", "Class");
        List<Double> sample1 = Arrays.asList(11.0, 12.0, 13.0);
        List<Double> sample2 = Arrays.asList(21.0, 22.0, 23.0);
        List<Double> sample3 = Arrays.asList(31.0, 32.0, 33.0);
        List<Double> sample4 = Arrays.asList(41.0, 42.0, 43.0);
        List<List<Double>> samples = Arrays.asList(sample1, sample2, sample3, sample4);
        List<Double> samplesClassification = Arrays.asList(1.0, 0.0, 0.0, 1.0);

        dataset.setArgNames(argNames);
        dataset.setSamples(samples, samplesClassification);
        dataset.display();
    }

    /**
     * Set arg names and clean all sample data. You need to reset all data after using this method.
     *
     * @param argNames String array of names for args
     */
    public void setArgNames(List<String> argNames) {
        this.argNames.clear();
        this.samples.clear();
        this.samplesClassification.clear();
        this.argNames.addAll(argNames);
        this.argSettingFinished = true;
    }

    /**
     * Check input data to make all data match each other and then clean all sample data and reset
     * data. Cannot use this before using "void setArgNames(String[] argNames)".
     *
     * @param samples               list of arrays that each stores all args for a sample
     * @param samplesClassification list of results of samples' classification
     * @return true if data valid{ (number of samples == number of classification results) &&
     * (number of input data arg num == number of arg names) } && number; otherwise false
     */
    public boolean setSamples(List<List<Double>> samples, List<Double> samplesClassification) {
        // check whether input data matches this.argNames
        int samplesSize = samples.size();
        int samplesClassificationSize = samplesClassification.size();
        if (samplesSize != samplesClassificationSize) {
            System.out.println("Error: samples size is unequal to samples classification size. ");
            return false;
        }
        int sampleArgNum = samples.get(0).size();
        if (sampleArgNum != (this.argNames.size() - 1)) {
            System.out.println("Error: samples' arg num is unequal to arg num. ");
            return false;
        }

        this.samples.clear();
        this.samplesClassification.clear();
        this.samples.addAll(samples);
        this.samplesClassification.addAll(samplesClassification);
        return true;
    }

    /**
     * Clear all data and reset the dataset.
     * <p>
     * We recommend you use this method to rebuild the whole dataset instead of set data
     * respectively.
     * </p>
     */
    public void clear() {
        this.argNames.clear();
        this.samples.clear();
        this.samplesClassification.clear();
        this.argSettingFinished = false;
    }

    /**
     * Get the vector of samples' value.
     *
     * @return samples' value
     */
    public Vector<Vector<Double>> getSamplesValueVector() {
        Vector<Vector<Double>> vectX = new Vector<>();
        for (int i = 0; i < this.samples.size(); i++) {
            Vector<Double> vectXi = new Vector<>();
            for (int j = 0; j < this.samples.get(i).size(); j++) {
                vectXi.add(this.samples.get(i).get(j));
            }
            vectX.add(vectXi);
        }
        return vectX;
    }

    /**
     * Get the vector of samples' classification results.
     *
     * @return vector of samples' classification results
     */
    public Vector<Double> getSamplesClassificationVector() {
        Vector<Double> vectY = new Vector<>();
        for (int i = 0; i < this.samplesClassification.size(); i++) {
            vectY.add(this.samplesClassification.get(i));
        }
        return vectY;
    }

    /**
     * Display the content of this data set.
     */
    public void display() {
        System.out.println("ArgNum = " + this.argNames.size() + ", sampleNum = " + this.samples.size());
        System.out.printf("\t\t\t");
        for (int i = 0; i < this.argNames.size(); i++) {
            System.out.printf("%s\t", argNames.get(i));
        }
        System.out.println();
        for (int i = 0; i < this.samples.size(); i++) {
            System.out.printf("Sample " + (i + 1) + "\t");
            for (int j = 0; j < this.samples.get(i).size(); j++) {
                System.out.printf("%6.4f\t", this.samples.get(i).get(j));
            }
            System.out.printf(this.samplesClassification.get(i).toString());
            System.out.println();
        }
    }


}
