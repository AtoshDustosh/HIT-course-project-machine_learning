package leastSquareMethod;

import Jama.Matrix;
import chart.MyChart;
import dataset.MyXYDataset;
import defaultData.MyNumbers;
import org.jfree.data.xy.XYSeries;

import java.util.Vector;

/**
 * A class realizing least square fitting of polynomial curve.
 */
public class LSFit {

    /*
     * To change regulator, change value of "public static final double defaultRegulator.
     */

    public static final double defaultRegulator = Math.pow(Math.E, -9);
    public static final int defaultDimM = 3;

    private MyXYDataset dataset = null;    // fitting function is stored last

    // parameters of functions
    private int dimM = 3;    // dimension of polynomial function

    private Vector<Double> vectX = null;
    private Vector<Double> vectY = null;
    private Vector<Double> vectW = new Vector<>();  // dimension of vector W is one dim higher than the polynomial function because of the existence of constant item

    private boolean vectWInitialized = false;

    // parameters of regularization
    private boolean regularized = false;
    private double regulator = 0;

    /**
     * Construct a LSFit class with input dataset and regulator.
     *
     * <p>
     * Input dataset must and only contain an original function XYSeries and a XYSeries created by adding noise to the original function.
     * </p>
     *
     * @param dimM         dimension of polynomial function to be fitted
     * @param regularized  whether regularize or not
     * @param regulator    regulator
     * @param inputDataset input MyXYDataset
     */
    public LSFit(int dimM, boolean regularized, double regulator, MyXYDataset inputDataset) {
        this.dimM = dimM;
        this.dataset = inputDataset;
        this.regularized = regularized;
        this.regulator = regulator;

        this.vectX = inputDataset.getVectX(inputDataset.seriesCount() - 1);
        this.vectY = inputDataset.getVectY(inputDataset.seriesCount() - 1);

        this.leastSquareFitting(regularized, regulator);

        this.createFitFuncDataset();
    }

    /*
     * Change i and maxDimM to change dimension of fitting function.
     */
    public static void main(String[] args) {
        MyXYDataset initialXYDataset = new MyXYDataset();

        int maxDimM = 10;
        for (int i = 0; i < maxDimM; i++) {
            int dimM = i + 1;
            System.out.println("Dim = " + dimM);

            String title = "Line Chart";
            MyChart chart = new MyChart(title, initialXYDataset.clone());

            // least square fit without regulator
            boolean regularized = false;
            double regulator = 0;
            LSFit lsf = new LSFit(dimM, regularized, regulator, initialXYDataset.clone());

            // debug(finished): chart got wrong because different parameters(initialXYDataset) transferred among functions point to the same object in memory dump.
            // add fit func.(without regulator) into XYDataset of MyChart
            MyXYDataset fitFuncDataset = lsf.getFitFuncXYDataset();
            chart.addSeries(fitFuncDataset);
            System.out.println("\tERMS(without regulator) - train: " + lsf.trainLoss() + ", test: " + lsf.testLoss());

            // least square fit with regulator
            regularized = true;
            regulator = LSFit.defaultRegulator;
            lsf = new LSFit(dimM, regularized, regulator, initialXYDataset.clone());
            // add fit func.(with regulator) into XYDataset of MyChart
            fitFuncDataset = lsf.getFitFuncXYDataset();
            chart.addSeries(fitFuncDataset);
            System.out.println("\tERMS(with regulator) - train: " + lsf.trainLoss() + ", test: " + lsf.testLoss());

            // initialize, build and display chart
            String subTitle = "Least Square Fitting (M = " + dimM + ")";
            chart.initializeChart(subTitle);
            chart.display();
            System.out.println();
        }

        // TODO add visualization of ERMS(without regulator) and ERMS(with regulator)
    }

    /**
     * Detailed least square fitting.
     *
     * @param regularized regularize or not
     * @param regulator   regulator
     */
    private void leastSquareFitting(boolean regularized, double regulator) {
        int n_row = 1 + dimM;   // MatX is a Matrix of (m+1)*(n+1)
        int n_column = 1 + (int) MyNumbers.num_trainPoints.getValue();
        double[][] arrayMatX = new double[n_row][n_column];

        // create Matrix X
        for (int column = 0; column < n_column; column++) {
            for (int row = 0; row < n_row; row++) {
                double value = Math.pow(vectX.get(column), row);
                arrayMatX[row][column] = value;
            }
        }
        Matrix matX = new Matrix(arrayMatX);

        n_row = n_column;   // MatY equals a vector - (n+1)*1
        n_column = 1;
        double[][] arrayMatY = new double[n_row][n_column];

        // create Matrix Y
        for (int row = 0; row < n_row; row++) {
            for (int column = 0; column < n_column; column++) {
                double value = vectY.get(row);
                arrayMatY[row][column] = value;
            }
        }
        Matrix matY = new Matrix(arrayMatY);

        // calculate matW
        Matrix matTemp = matX.times(matX.transpose());
        if (regularized) {
            int dim = matTemp.getColumnDimension();
            double[][] unitMat_array = new double[dim][dim];
            for (int i = 0; i < dim; i++) {
                unitMat_array[i][i] = this.regulator;
            }
            Matrix unitMat = new Matrix(unitMat_array);
            matTemp = matTemp.plus(unitMat);
        }
        Matrix matW = matTemp.inverse().times(matX.times(matY));

        n_row = matW.getRowDimension();
        n_column = matW.getColumnDimension();

        assert n_row == 1 + dimM : "row of matrix W not correct. ";
        assert n_column == 1 : "column of matrix W not correct. ";

        for (int i = 0; i < n_row; i++) {
            vectW.add(matW.get(i, 0));
        }
        this.vectWInitialized = true;
    }

    /**
     * Fitting function from least square fitting.
     *
     * @param x parameter x
     * @return calculating result responding to x
     */
    private double fitFunction(double x) {
        double result = 0;
        if (this.vectWInitialized == false) {
            //System.out.println("Cannot use this method before initializing vector W. ");
            return result;
        }
        for (int i = 0; i < this.vectW.size(); i++) {
            result += vectW.get(i) * Math.pow(x, i);
        }
        return result;
    }

    /**
     * Create XY data set of fit function.
     */
    private void createFitFuncDataset() {
        String fitFuncLabel = "fit func.(non-regularized)";
        if (this.regularized) {
            fitFuncLabel = "fit func.(regularized)";
        }
        XYSeries xySeries2 = new XYSeries(fitFuncLabel);

        // add "fit function" into XY data set
        boolean lineVisible = true;
        boolean shapeVisible = false;
        for (int i = 0; i <= MyNumbers.num_chartPoints.getValue(); i++) {
            double x = (1 / MyNumbers.num_chartPoints.getValue()) * i;
            double y = this.fitFunction(x);
            xySeries2.add(x, y);
        }

        this.dataset.addSeries(xySeries2, lineVisible, shapeVisible);
    }

    /**
     * Calculate testing fitting error using Mat.sqrt(2*E(w)/N).
     *
     * @return testing fitting error
     */
    public double testLoss() {
        double EW = 0;
        for (int i = 0; i < this.vectX.size(); i++) {
            double x = this.vectX.get(i);
            double y = this.dataset.originalFunc(x);
            double temp = y - this.fitFunction(x);
            temp = temp * temp;
            EW += temp;
        }
        EW = 0.5 * EW;

        return Math.sqrt(2 * EW / MyNumbers.num_trainPoints.getValue());
    }

    /**
     * Calculate training fitting error using Mat.sqrt(2*E(w)/N).
     *
     * @return training fitting error
     */
    public double trainLoss() {
        double EW = 0;
        for (int i = 0; i < this.vectX.size(); i++) {
            double x = this.vectX.get(i);
            double y = this.vectY.get(i);
            double temp = y - this.fitFunction(x);
            temp = temp * temp;
            EW += temp;
        }
        EW = 0.5 * EW;

        return Math.sqrt(2 * EW / MyNumbers.num_trainPoints.getValue());
    }

    /**
     * Get XY data set of fitting function.
     *
     * @return XY data set of fitting func.
     */
    public MyXYDataset getFitFuncXYDataset() {
        MyXYDataset myXYDataset = new MyXYDataset();
        myXYDataset.clear();

        int[] indexArraySelected = {this.dataset.seriesCount() - 1};
        myXYDataset = this.dataset.selectSeries(indexArraySelected);
        return myXYDataset;
    }

    /**
     * Get dimension of polynomial function.
     *
     * @return dimension of polynomial function
     */
    public int getDimM() {
        return this.dimM;
    }
}