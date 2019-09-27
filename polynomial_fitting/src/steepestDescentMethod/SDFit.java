package steepestDescentMethod;

import Jama.Matrix;
import chart.MyChart;
import dataset.MyXYDataset;
import defaultData.MyNumbers;
import org.jfree.data.xy.XYSeries;

import java.util.Random;
import java.util.Vector;

/**
 * A class realizing steepest descent fitting of polynomial curve.
 */
public class SDFit {

    /*
     * To change regulator, change value of "public static final double defaultRegulator".
     * To change learning rate, change value of "public static final double defaultLearningRate".
     * To change standard of stopping iteration, change value of "private double epsilon".
     */

    public static final double defaultRegulator = Math.pow(Math.E, -7);
    public static final double defaultLearningRate = Math.pow(Math.E, -8);
    public static final int defaultDimM = 3;

    private MyXYDataset dataset = null;    // fitting function is stored last

    // parameters of functions
    private int dimM = 3;    // dimension of polynomial function

    private Vector<Double> vectX = null;
    private Vector<Double> vectY = null;
    private Vector<Double> vectW = new Vector<>();  // dimension of vector W is one dimension higher than the polynomial function because of the existence of constant item

    private boolean vectWInitialized = false;

    // parameters of regularization
    private boolean regularized = false;
    private double regulator = 0;

    // parameters of steepest descent
    private double learningRate = Math.pow(Math.E, -2);
    private double epsilon = Math.pow(Math.E, -20);

    // parameters used to calculate gradient
    private Matrix matrixW = null;
    private Matrix matrixParam1 = null;
    private Matrix matrixParam2 = null;

    /**
     * Construct a SDFit class with input dataset and regulator.
     *
     * <p>
     * Input dataset must and only contain an original function XYSeries and a XYSeries created by adding noise to the original function.
     * </p>
     *
     * @param dimM         dimension of polynomial function to be fitted
     * @param regularized  whether regularize or not
     * @param regulator    regulator
     * @param learningRate learning rate
     * @param inputDataset input MyXYDataset
     */
    public SDFit(int dimM, boolean regularized, double regulator, double learningRate, MyXYDataset inputDataset) {
        this.dimM = dimM;
        this.dataset = inputDataset;
        this.regularized = regularized;
        this.regulator = regulator;
        this.learningRate = learningRate;

        this.vectX = inputDataset.getVectX(inputDataset.seriesCount() - 1);
        this.vectY = inputDataset.getVectY(inputDataset.seriesCount() - 1);

        // initialize initial matrixW
        double[][] arrayW = new double[this.dimM + 1][1];
        for (int i = 0; i < arrayW.length; i++) {
            Random random = new Random();
            arrayW[i][0] = random.nextGaussian();
        }
        this.matrixW = new Matrix(arrayW);

        this.qualifyGradientParams(regularized, regulator);
        boolean fittingSucceedOrNot = this.steepestDescentFitting();

        int n_row = this.matrixW.getRowDimension();
        int n_column = this.matrixW.getColumnDimension();
        assert n_row == this.dimM + 1 : " Row dimension of matrixW incorrect. ";
        assert n_column == 1 : "Column dimension of matrixW incorrect. ";

        arrayW = this.matrixW.getArray();
        this.vectW.clear();
        for (int i = 0; i < n_row; i++) {
            this.vectW.add(arrayW[i][0]);
        }
        this.vectWInitialized = true;
        // System.out.println(this.vectW);

        this.createFitFuncDataset();
    }

    /*
     * Change i and maxDimM to change dimension of fitting function.
     */
    public static void main(String[] args) {
        MyXYDataset initialXYDataset = new MyXYDataset();

        int maxDimM = 10;
        double learningRate = SDFit.defaultLearningRate;
        for (int i = 0; i < maxDimM; i++) {
            int dimM = i + 1;
            System.out.println("Dim = " + dimM);

            String title = "Line Chart";
            MyChart chart = new MyChart(title, initialXYDataset.clone());

            // steepest descent fit without regulator
            boolean regularized = false;
            double regulator = 0;
            SDFit sdf = new SDFit(dimM, regularized, regulator, learningRate, initialXYDataset.clone());

            // add fit func.(without regulator) into XYDataset of MyChart
            MyXYDataset fitFuncDataset = sdf.getFitFuncXYDataset();
            chart.addSeries(fitFuncDataset);
            System.out.println("\tERMS(without regulator) - train: " + sdf.trainLoss() + ", test: " + sdf.testLoss());

            // steepest descent fit with regulator
            regularized = true;
            regulator = SDFit.defaultRegulator;
            sdf = new SDFit(dimM, regularized, regulator, learningRate, initialXYDataset.clone());

            // add fit func.(with regulator) into XYDataset of MyChart
            fitFuncDataset = sdf.getFitFuncXYDataset();
            chart.addSeries(fitFuncDataset);
            System.out.println("\tERMS(with regulator) - train: " + sdf.trainLoss() + ", test: " + sdf.testLoss());

            // initialize, build and display chart
            String subTitle = "Steepest Descent Fitting (M = " + dimM + ")";
            chart.initializeChart(subTitle);
            chart.display();
            System.out.println();
        }

    }

    /**
     * Steepest descent fitting.
     * End iteration when gradient difference is under a certain value.
     *
     * @return true if succeeds; false otherwise
     */
    private boolean steepestDescentFitting() {
        /*
         * matrixW represent matW_0
         * this.matrixW represent matW_1
         */

        // initialize matW_0 and initial error value
        Matrix matrixW = this.matrixW;
        double error = calcEpsilon(this.gradient(matrixW));

        int count = 0;

        while (error >= this.epsilon) {
            // calculate last train loss
            double[][] arrayW = this.matrixW.getArray();
            this.vectW.clear();
            for (int i = 0; i < arrayW.length; i++) {
                this.vectW.add(arrayW[i][0]);
            }
            double previousLoss = this.trainLoss();

            // calculate matW_k+1
            this.matrixW = matrixW.minus(gradient(matrixW).times(this.learningRate));

            // calculate this train loss
            arrayW = this.matrixW.getArray();
            this.vectW.clear();
            for (int i = 0; i < arrayW.length; i++) {
                this.vectW.add(arrayW[i][0]);
            }
            double thisLoss = this.trainLoss();

            // update matW_k and error value
            matrixW = this.matrixW;
            error = Math.abs(thisLoss - previousLoss);
            this.learningRate = Math.sin(this.learningRate) * Math.cos(this.learningRate);
            count++;
            if (count % 1E6 == 0) {
                System.out.println("cycle count: " + count);
            }
        }

        System.out.println("\ttotal cycle count: " + count);
        return true;
    }

    /**
     * Qualify values of parameters used for steepest descent fitting.
     *
     * @param regularized regularize or not
     * @param regulator   regulator
     */
    private void qualifyGradientParams(boolean regularized, double regulator) {
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

        // calculate the parameters used for steepest descent fitting
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
        this.matrixParam1 = matTemp;    // matX * matX.transpose + regulator * unitMatrix
        this.matrixParam2 = matX.times(-1).times(matY); // - matX * matY

    }

    /**
     * Calculate gradient with parameter matrixW.
     *
     * <p>
     * gradient = (matX * matX.transpose() + regulator * unitMatrix) * matW - matX * matY
     * </p>
     *
     * @param matrixW matrix W
     * @return matrix of gradient
     */
    private Matrix gradient(Matrix matrixW) {
        return this.matrixParam1.times(matrixW).plus(matrixParam2);
    }

    /**
     * A standard to measure the value of gradient difference
     *
     * <p>
     * epsilon = Math.sqrt( sum(Math.pow(gradient.get(i), 2)) )
     * gradient is a (dimM + 1) * 1 matrix
     * </p>
     *
     * @param gradientDifference gradient difference
     * @return epsilon - double value of gradient
     */
    private double calcEpsilon(Matrix gradientDifference) {
        assert gradientDifference.getRowDimension() == this.dimM + 1 : "Row dimension of gradient incorrect. ";
        assert gradientDifference.getColumnDimension() == 1 : "Column dimension of gradient incorrect. ";

        int dimGradient = gradientDifference.getRowDimension();
        double[][] array = gradientDifference.getArray();
        double error = 0;
        for (int i = 0; i < dimGradient; i++) {
            error += array[i][0] * array[i][0];
        }
        error = Math.sqrt(error);
        return error;
    }

    /**
     * Fitting function from least square fitting.
     *
     * @param x parameter x
     * @return calculating result responding to x
     */
    private double fitFunction(double x) {
        double result = 0;
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
