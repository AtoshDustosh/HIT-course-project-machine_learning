package logisticregression;

import Jama.Matrix;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import dataformat.LogisticRegressionDataset;

/**
 * Logistic regression class.
 */
public class LogisticRegression {

    public static final double DEFAULTREGULATOR = Math.exp(-6);

    /*
     * some restriction fields
     */
    private boolean constructedCorrectly = false;   // whether this type has been constructed correctly

    /*
     * arguments used for calculation got from the csv data file
     * note: vectX and vectW need adjustments to fit for logistic regression
     */
    private Vector<Vector<Double>> vectX = new Vector<>();  // n*(m+1) vector (n: number of samples, m: number of sample's arguments)
    private Vector<Double> vectY = new Vector<>();  // n*1 vector (n: number of samples)
    private Vector<Double> vectW = new Vector<>();  // (m+1)*1 vector (m: number of sample's arguments)

    private boolean regularized = false;    // whether regularize or not
    private double regulator = LogisticRegression.DEFAULTREGULATOR;   // parameter of regularization

    /*
     * parameters used for gradient descent method
     */
    private final double learningRate = Math.exp(-17);   // learning rate of gradient descent
    private final double maxIteration = Math.exp(14);   // maximum number of iteration
    private final double epsilon = Math.exp(-6);   // stop when gradient 2-norm is below this value

    /*
     * store the final result after execution
     */
    private Vector<Double> optimizedVectW = new Vector<>();

    /**
     * Construct a LogisticRegression type for executing logsitic regression.
     *
     * @param vectX       values of all args of all samples
     * @param vectY       classification results of all samples
     * @param regularized whether to add regularization item into loss function
     */
    public LogisticRegression(Vector<Vector<Double>> vectX, Vector<Double> vectY, boolean regularized) {
        /*
         * check arguments
         */
        int N = vectX.size();   // number of samples
        if (N == 0) {
            throw new IllegalArgumentException("Error: empty sample. ");
        }
        int M = vectX.get(0).size();    // number of sample's arguments
        if (M == 0) {
            throw new IllegalArgumentException("Error: empty sample arguments. ");
        }
        if (vectX.size() != vectY.size()) {
            throw new IllegalArgumentException("Error: invalid sample parameters. ");
        }

        for (int i = 0; i < M + 1; i++) { // note: M + 1 dimension (vectW)
            vectW.add((double) 0);
        }
        this.vectX = vectX;
        for (int i = 0; i < N; i++) { // add the intercept item into vectX
            this.vectX.get(i).add(0, 1.0);
        }
        this.vectY = vectY;
        this.regularized = regularized;
        this.constructedCorrectly = true;
    }

    /**
     * Test this class.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        LogisticRegressionDataset dataset = new LogisticRegressionDataset();
        List<String> argNames = Arrays.asList("arg1", "arg2", "arg3", "arg4", "Class");
        List<Double> sample1 = Arrays.asList(6.0, 30.0, 5.0, 30.0);
        List<Double> sample2 = Arrays.asList(1.0, 5.0, 2.0, 100.0);
        List<Double> sample3 = Arrays.asList(9.0, 10.0, 1.0, 50.0);
        List<Double> sample4 = Arrays.asList(9.0, 1.0, 9.0, 40.0);
        List<Double> sample5 = Arrays.asList(9.0, 1.0, 9.0, 90.0);
        List<List<Double>> samples = Arrays.asList(sample1, sample2, sample3, sample4, sample5);
        List<Double> samplesClassification = Arrays.asList(1.0, 0.0, 0.0, 1.0, 1.0);

        dataset.setArgNames(argNames);
        dataset.setSamples(samples, samplesClassification);
        dataset.display();

        Vector<Vector<Double>> vectX = new Vector<>();
        Vector<Double> vectY = new Vector<>();
        for (int i = 0; i < samples.size(); i++) {
            Vector<Double> vectXi = new Vector<>();
            for (int j = 0; j < samples.get(i).size(); j++) {
                vectXi.add(samples.get(i).get(j));
            }
            vectX.add(vectXi);
            vectY.add(samplesClassification.get(i));
        }

        LogisticRegression LGProgram = new LogisticRegression(vectX, vectY, false);
        LGProgram.execute();
        // System.out.println(LGProgram.getOptimizedVectW());
    }

    /**
     * Get a copy of the optimized vector W.
     *
     * @return optimized vector W
     */
    public Vector<Double> getOptimizedVectW() {
        return (Vector<Double>) this.optimizedVectW.clone();
    }

    /**
     * Execute gradient descent to get the optimized result.
     */
    public void execute() {
        if (!this.constructedCorrectly) {
            System.out.println("This object is not correctly constructed. ");
            return;
        }
        /*
         * transforming the input vectors into matrices for the convenience of calculation
         */
        int dimVectW = this.vectW.size();
        int sampleNum = this.vectY.size();
        double[][] arrayW = new double[dimVectW][1];
        double[][] arrayX = new double[sampleNum][dimVectW];
        double[][] arrayY = new double[sampleNum][1];
        Matrix matW = new Matrix(arrayW);
        for (int i = 0; i < sampleNum; i++) {
            Vector<Double> vectXi = this.vectX.get(i);
            arrayY[i][0] = this.vectY.get(i);
            if (vectXi.size() != dimVectW) {
                System.out.println("Error: vector of X invalid. ");
                return;
            }
            for (int j = 0; j < dimVectW; j++) {
                arrayX[i][j] = vectXi.get(j);
            }
        }
        Matrix matX = new Matrix(arrayX);
        Matrix matY = new Matrix(arrayY);

//        System.out.println("Matrix Samples(matX): ");
//        matX.print(4, 2);
//        System.out.println("Matrix Classification(matY): ");
//        matY.print(4, 2);

        // the iteration loop
        int loopCount = 0;
        boolean convergenceGradient2Norm = true;    // not recommended stopping condition
        boolean convergenceMaximumLoop = true;

        while (convergenceMaximumLoop) {
//            System.out.println("Loop " + (loopCount + 1) + "-th iteration");

            Matrix gradient = this.lossGradient(matW, matX, matY);
//            System.out.println("gradient(" + (loopCount + 1) + ")");
//            gradient.print(4, 2);
            matW = matW.plus(gradient.times(this.learningRate));

            Vector<Double> classification = this.classifySamples(matW, matX);
            double recallRate = this.recallRate(this.vectY, classification);
            loopCount++;

            Vector<Double> vectGradient = new Vector<>();
            for (int i = 0; i < dimVectW; i++) {
                vectGradient.add(gradient.get(i, 0));
            }
            double gradient2Norm = this.getVectNorm(vectGradient);
            convergenceGradient2Norm = (Math.abs(gradient2Norm) >= this.epsilon);
            convergenceMaximumLoop = loopCount <= this.maxIteration;

            if (loopCount % 1000 == 0 || (loopCount - 1) % 1000 ==0) {
                System.out.println("recallRate: " + recallRate + ", loopCount: "
                        + loopCount + ", gradient: " + gradient2Norm + "\\\\");
                /*Vector<Double> tempVectW = new Vector<>();
                for(int i =0; i < dimVectW; i++){
                    tempVectW.add(matW.get(i,0));
                }
                System.out.println(tempVectW);*/
            }
        }
        System.out.println("Total iteration times: " + loopCount);

        // store the optimized vector W
        for (int i = 0; i < dimVectW; i++) {
            this.optimizedVectW.add(matW.get(i, 0));
        }

        System.out.println("Optimized vector W: ");
        System.out.println(this.optimizedVectW);
        System.out.println(" vector W 2-norm: " + this.getVectNorm(this.optimizedVectW));
    }

    /**
     * Get the norm of a vector.
     *
     * @param vect vector
     * @return norm of a vector
     */
    public double getVectNorm(Vector<Double> vect) {
        double norm = 0;
        for (int i = 0; i < vect.size(); i++) {
            double value = vect.get(i);
            norm += value*value;
        }
        return Math.sqrt(norm);
    }

    /**
     * Classify samples according to vector W calculated.
     *
     * <p>
     * M is number of args for classification. N is number of samples.
     * </p>
     *
     * @param matW matrix W (M+1)*1 - parameters of decision function (optimized)
     * @param matX matrix X N*(M+1) - values of samples
     * @return vector of classification results
     */
    private Vector<Double> classifySamples(Matrix matW, Matrix matX) {
        Vector<Double> vectClassify = new Vector<>();
        Matrix matClassify = matX.times(matW);  // n * 1 matrix
        for (int i = 0; i < matClassify.getRowDimension(); i++) {
            double possibility2BeZero = matClassify.get(i, 0);
            if (possibility2BeZero <= 0.5) {
                vectClassify.add(0.0);
            } else {
                vectClassify.add(1.0);
            }
        }
        return vectClassify;
    }

    /**
     * Classify an input set.
     *
     * @param vectX vector of samples waited to be classified
     * @return vector of classify results
     */
    public Vector<Double> classify(Vector<Vector<Double>> vectX) {
        int N = vectX.size();   // number of samples
        if (N == 0) {
            throw new IllegalArgumentException("Error: empty sample. ");
        }
        int M = vectX.get(0).size();    // number of sample's arguments
        if (M == 0) {
            throw new IllegalArgumentException("Error: invalid sample arguments. ");
        }

        System.out.println("Required arg num: " + (this.optimizedVectW.size() - 1));
        System.out.println("Test data sample num: " + N + ", arg num: " + M);

        // transform the input into matrix format
        int dimVectW = this.optimizedVectW.size();
        int sampleNum = vectX.size();
        double[][] arrayW = new double[dimVectW][1];
        double[][] arrayX = new double[sampleNum][dimVectW];
        for (int i = 0; i < sampleNum; i++) {
            Vector<Double> vectXi = vectX.get(i);
            if (vectXi.size() != (dimVectW - 1)) {
                throw new IllegalArgumentException("Error: vector of X invalid. ");
            }
            // set intercept and args' value for arrayX
            arrayX[i][0] = 1;
            for (int j = 1; j < dimVectW; j++) {
                arrayX[i][j] = vectXi.get(j - 1);
            }
        }
        for (int i = 0; i < dimVectW; i++) {
            arrayW[i][0] = this.optimizedVectW.get(i);
        }
        Matrix matX = new Matrix(arrayX);   // get matrix X
        Matrix optimizedMaxW = new Matrix(arrayW);  // get matrix W

        // calculate the classification possibility
        Vector<Double> vectClassify = new Vector<>();
        Matrix matClassify = matX.times(optimizedMaxW);  // n * 1 matrix
        for (int i = 0; i < matClassify.getRowDimension(); i++) {
            double possibility2BeZero = matClassify.get(i, 0);
            if (possibility2BeZero <= 0.5) {
                vectClassify.add(0.0);
            } else {
                vectClassify.add(1.0);
            }
        }

        System.out.println("... classified. ");
        return vectClassify;
    }

    /**
     * Calculate the recall rate of classification.
     *
     * @param sampleClass   classes of samples
     * @param classifyClass classes of classification results
     * @return recall rate
     */
    public double recallRate(Vector<Double> sampleClass, Vector<Double> classifyClass) {
        double recallRate = 0;
        int sampleNum = sampleClass.size();

//        System.out.println("Sample num: " + sampleNum + ", classified num: " + classifyClass.size());

        for (int i = 0; i < sampleNum; i++) {
            if (sampleClass.get(i).intValue() == classifyClass.get(i).intValue()) {
                recallRate++;
            }
        }
        return recallRate / sampleNum;
    }

    /**
     * Loss function(W, X, Y) = Sum[(yi-1)*lnSumExp(0, alpha)-yi*lnSumExp(0,-alpha)] -
     * (lambda/2)matW_T*matW, alpha = matW_T * matX
     *
     * <p>
     * M is number of args for classification. N is number of samples.
     * </p>
     *
     * @param matW matrix W (M+1)*1 - parameters of decision function
     * @param matX matrix X N*(M+1) - values of samples
     * @param matY matrix Y N*1 - classification result of samples
     * @return result of loss function
     */
    public double loss(Matrix matW, Matrix matX, Matrix matY) {
        /*
         * check formats of parameters
         */
        try {
            if (matX.getColumnDimension() != matW.getRowDimension() || matX.getRowDimension() != matY.getRowDimension()) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        double loss = 0;
        int dimVectW = matW.getRowDimension();
        int sampleNum = matY.getRowDimension();
        // calculate the regulation item
        double regulationItem = 0;
        if (this.regularized) {
            regulationItem = matW.transpose().times(matW).get(0, 0) * regulator / 2;
        }
        loss = loss - regulationItem;
        // calculate the loss item
        for (int i = 0; i < sampleNum; i++) {
            double yi = matY.get(i, 0);  // classification result of the i-th sample
            Matrix matXi = matX.getMatrix(i, i, 0, dimVectW - 1);   // args of the i-th sample (1*(M+1) matrix)
            double alpha = matXi.times(matW).get(0, 0);
            Vector<Double> vectArg1 = new Vector<>();
            Vector<Double> vectArg2 = new Vector<>();
            vectArg1.add((double) 0);
            vectArg1.add(-alpha);
            vectArg2.add((double) 0);
            vectArg2.add(alpha);
            double addend = (yi - 1) * this.lnSumExp(vectArg1) - yi * this.lnSumExp(vectArg2);
            loss += addend;
        }
        return loss;
    }

    /**
     * Gradient of loss function. (derivation on matW)
     *
     * <p>
     * M is number of args for classification. N is number of samples.
     * </p>
     *
     * @param matW matrix W (M+1)*1 - parameters of decision function
     * @param matX matrix X N*(M+1) - values of samples
     * @param matY matrix Y N*1 - classification result of samples
     * @return result of loss function
     */
    public Matrix lossGradient(Matrix matW, Matrix matX, Matrix matY) {
        /*
         * check formats of parameters
         */
        try {
            if (matX.getColumnDimension() != matW.getRowDimension() || matX.getRowDimension() != matY.getRowDimension()) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        int dimVectW = matW.getRowDimension();
        int sampleNum = matY.getRowDimension();
        Matrix gradient = new Matrix(dimVectW, 1);
//        System.out.println(".matW_k");
//        matW.print(4, 2);
        // calculate the regulation item
        Matrix regulationItem = new Matrix(dimVectW, 1);
        if (this.regularized) {
            regulationItem = matW.times(this.regulator);
        }
        gradient = gradient.minus(regulationItem);
        // calculate the loss item
        for (int i = 0; i < sampleNum; i++) {
//            System.out.println(".gradient of loss - " + (i + 1) + "-th sample");
            double yi = matY.get(i, 0);  // classification result of the i-th sample
            Matrix matXi = matX.getMatrix(i, i, 0, dimVectW - 1);   // args of the i-th sample (1*M matrix)
//            System.out.println("..matX" + (i + 1) + ": ");
//            matXi.print(4, 2);
            double alpha = matXi.times(matW).get(0, 0);
//            System.out.println("..alpha" + (i + 1) + ": " + alpha);
            Matrix addend = matXi.transpose().times(yi - 1 / this.logit(-alpha));
            gradient = gradient.plus(addend);
//            gradient.print(4,2);
        }
        return gradient;
    }

    /**
     * logit(a) = 1 + exp(a)
     *
     * @param arg arg of the function
     * @return result of the function
     */
    private double logit(double arg) {
        return 1 + Math.exp(arg);
    }

    /**
     * Ln(Sum of exp(xi)), i = 1, 2, ..., n.
     *
     * @param vectX vector X
     * @return result of the function
     */
    private double lnSumExp(Vector<Double> vectX) {
        // find the max value in vector X
        double maxX = vectX.get(0);
        for (int i = 0; i < vectX.size(); i++) {
            if (maxX < vectX.get(i)) {
                maxX = vectX.get(i);
            }
        }

        double argLn = 0;   // arg of ln(...)
        for (int i = 0; i < vectX.size(); i++) {
            argLn += Math.exp(vectX.get(i) - maxX);
        }

        double result = maxX + Math.log(argLn);
        return result;
    }

}
