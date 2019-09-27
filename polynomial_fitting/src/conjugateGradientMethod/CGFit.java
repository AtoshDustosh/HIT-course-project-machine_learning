package conjugateGradientMethod;

import Jama.Matrix;
import chart.MyChart;
import dataset.MyXYDataset;
import defaultData.MyNumbers;
import org.jfree.data.xy.XYSeries;

import java.util.Vector;

/**
 * A class realizing conjugate gradient fitting of polynomial curve.
 * <p>
 * matA = matX * matX.transpose();
 * matC = matY.transpose() * matY;
 * vectW = a_0 * d_0 + ... + a_m * d_m;
 * <p>
 */
public class CGFit {

    /*
     * To change regulator, change value of "public static final double defaultRegulator".
     * To change standard of stopping iteration, change value of "private double epsilon".
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

    // parameters of conjugate gradient fitting
    private double epsilon = Math.pow(Math.E, -33);
    private Matrix matW = null;
    private Matrix matA = null;
    private Matrix matB = null;

    /**
     * Construct a CGFit class with input dataset and regulator.
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
    public CGFit(int dimM, boolean regularized, double regulator, MyXYDataset inputDataset) {
        this.dimM = dimM;
        this.dataset = inputDataset;
        this.regularized = regularized;
        this.regulator = regulator;

        this.vectX = inputDataset.getVectX(inputDataset.seriesCount() - 1);
        this.vectY = inputDataset.getVectY(inputDataset.seriesCount() - 1);

        this.initializeData(regularized, regulator);
        this.conjugateGradientFitting();

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
            CGFit cgf = new CGFit(dimM, regularized, regulator, initialXYDataset.clone());

            // debug(finished): chart got wrong because different parameters(initialXYDataset) transferred among functions point to the same object in memory dump.
            // add fit func.(without regulator) into XYDataset of MyChart
            MyXYDataset fitFuncDataset = cgf.getFitFuncXYDataset();
            chart.addSeries(fitFuncDataset);
            System.out.println("\tERMS(without regulator) - train: " + cgf.trainLoss() + ", test: " + cgf.testFittingError());

            // least square fit with regulator
            regularized = true;
            regulator = CGFit.defaultRegulator;
            cgf = new CGFit(dimM, regularized, regulator, initialXYDataset.clone());
            // add fit func.(with regulator) into XYDataset of MyChart
            fitFuncDataset = cgf.getFitFuncXYDataset();
            chart.addSeries(fitFuncDataset);
            System.out.println("\tERMS(with regulator) - train: " + cgf.trainLoss() + ", test: " + cgf.testFittingError());

            // initialize, build and display chart
            String subTitle = "Conjugate Gradient Fitting (M = " + dimM + ")";
            chart.initializeChart(subTitle);
            chart.display();
            System.out.println();
        }

        // TODO add visualization of ERMS(without regulator) and ERMS(with regulator)
    }

    /**
     * Initialize some data for conjugate gradient fitting.
     *
     * @param regularized regularize or not
     * @param regulator   regulator
     */
    private void initializeData(boolean regularized, double regulator) {

        // create Matrix X
        int n_row = 1 + dimM;   // MatX is a Matrix of (m+1)*(n+1)
        int n_column = 1 + (int) MyNumbers.num_trainPoints.getValue();
        double[][] arrayMatX = new double[n_row][n_column];

        for (int column = 0; column < n_column; column++) {
            for (int row = 0; row < n_row; row++) {
                double value = Math.pow(vectX.get(column), row);
                arrayMatX[row][column] = value;
            }
        }
        Matrix matX = new Matrix(arrayMatX);
//        System.out.println("Mat_X : " + matX.getRowDimension() + ", " + matX.getColumnDimension());

        // create Matrix Y
        n_row = n_column;   // MatY equals a vector - (n+1)*1
        n_column = 1;
        double[][] arrayMatY = new double[n_row][n_column];

        for (int row = 0; row < n_row; row++) {
            for (int column = 0; column < n_column; column++) {
                double value = vectY.get(row);
                arrayMatY[row][column] = value;
            }
        }
        Matrix matY = new Matrix(arrayMatY);
//        System.out.println("Mat_Y : " + matY.getRowDimension() + ", " + matY.getColumnDimension());

        // calculate parameters for conjugate gradient fitting
        this.matA = matX.times(matX.transpose());
        if (regularized) {
            n_row = this.dimM + 1;
            n_column = this.dimM + 1;
            double[][] unitMat_array = new double[n_row][n_column];
            for (int i = 0; i < n_row; i++) {
                unitMat_array[i][i] = this.regulator;
            }
            Matrix unitMat = new Matrix(unitMat_array);
            this.matA = this.matA.plus(unitMat);
        }

        this.matB = matX.times(matY);

        // initialize vector w and create matrix W
        n_row = this.dimM + 1;
        n_column = 1;
        double[][] matW_array = new double[n_row][n_column];
        for (int i = 0; i < n_row; i++) {
            this.vectW.add((double) 0);
        }
        this.matW = new Matrix(matW_array);

        this.vectWInitialized = true;
    }

    /**
     * Detailed steps of conjugate gradient fitting.
     */
    private void conjugateGradientFitting() {
//        System.out.println("Mat_A : " + matA.getRowDimension() + ", " + matA.getColumnDimension());
//        System.out.println("Mat_B : " + matB.getRowDimension() + ", " + matB.getColumnDimension());
//        System.out.println("Mat_W : " + matW.getRowDimension() + ", " + matW.getColumnDimension());

        // r_0 = b - A * w_0, also r_k
        Matrix mat_r = this.matB.minus(this.matA.times(this.matW));
//        System.out.println("mat_r : " + mat_r.getRowDimension() + ", " + mat_r.getColumnDimension());

        // d_0 = r_0, also d_k
        Matrix mat_d = (Matrix) mat_r.clone();
//        System.out.println("mat_d : " + mat_d.getRowDimension() + ", " + mat_d.getColumnDimension());

        double param_a = 0;
        double errorValue = this.trainLoss();
        int count = 0;

        while (errorValue > this.epsilon) {
            double[][] arrayW = this.matW.getArray();
            this.vectW.clear();
            for (int i = 0; i < arrayW.length; i++) {
                this.vectW.add(arrayW[i][0]);
            }
            double previousLoss = this.trainLoss();

            // a_i = b.transpose() * d_i / (d_i.transpose() * mat_A * d_i)
            Matrix mat_alpha = (mat_r.transpose().times(mat_r)).times((mat_d.transpose().times(this.matA).times(mat_d)).inverse());
//            System.out.println("mat_alpha : " + mat_alpha.getRowDimension() + ", " + mat_alpha.getColumnDimension());
            param_a = mat_alpha.get(0, 0);

            // w_k+1 = w_k + a_k * d_k
            this.matW = this.matW.plus(mat_d.times(param_a));
//            System.out.println("matW : " + matW.getRowDimension() + ", " + matW.getColumnDimension());

            // r_k+1 = r_k - a_k * A * d_k
            Matrix mat_r_plus = mat_r.minus(this.matA.times(mat_d).times(param_a));
//            System.out.println("mat_r_plus : " + mat_r_plus.getRowDimension() + ", " + mat_r_plus.getColumnDimension());

            // beita = (<r_k+1, r_k+1>/ <r_k, r_k>)
            Matrix mat_beita = mat_r_plus.transpose().times(mat_r_plus).times((mat_r.transpose().times(mat_r)).inverse());
            double beita = mat_beita.get(0, 0);

            // d_k+1 = r_k+1 + beita * d_k;
            Matrix mat_d_plus = mat_r_plus.plus(mat_d.times(beita));

            arrayW = this.matW.getArray();
            this.vectW.clear();
            for (int i = 0; i < arrayW.length; i++) {
                this.vectW.add(arrayW[i][0]);
            }
            double thisLoss = this.trainLoss();

            errorValue = Math.abs(thisLoss - previousLoss);
            mat_r = mat_r_plus;
            mat_d = mat_d_plus;
            count++;
        }

        System.out.println("\ttotal cycle count: " + count);

        vectW.clear();
        for (int i = 0; i < this.matW.getRowDimension(); i++) {
            this.vectW.add(this.matW.get(i, 0));
        }
//        System.out.println("vector_W: " + this.vectW);

    }

    /**
     * Measure the error value of the error matrix.
     *
     * @param mat_r error matrix
     * @return measurement of error matrix
     */
    private double errorMeasure(Matrix mat_r) {
        double error = 0;
        assert mat_r.getRowDimension() == this.dimM + 1 : "Row dimension of error matrix incorrect. ";
        assert mat_r.getColumnDimension() == 1 : "Column dimension of error matrix incorrect. ";

        for (int i = 0; i < this.dimM + 1; i++) {
            double value = mat_r.get(i, 0);
            error += value * value;
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
    public double testFittingError() {
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