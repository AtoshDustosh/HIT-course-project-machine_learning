package gmm;

import Jama.Matrix;

import org.jfree.data.xy.XYSeries;

import java.util.Random;
import java.util.Vector;

import chart.GMMChart;
import dataset.GMMDataset;
import defaultargs.MyArg;
import kmeans.Kmeans;

public class GMM {

    // the dimension of data matPoints
    private static final int dataDim = 2;

    private GMMDataset dataset = null;

    // Representation for the number of matCenters/clusters
    private int centerNum = MyArg.valueK.value();

    // unclassified x-y coordinates of data matPoints - x_n
    private Vector<Matrix> matPoints = new Vector<>();

    // Given the n-th point, the possibility of it being assigned to the k-th cluster - \gama_nk
    // TODO This is not a proper sequence of accessing data in memory. The better one is \gama_kn
    //  rather than \gama_nk. If you want to improve the performance, you may change it and modify
    //  relevant functions.However, when we classify the data points, this is better.
    private Vector<Vector<Double>> gamas = new Vector<>();

    // Coefficients of #(centerNum) Gaussian distributions in likelihood equation
    private Vector<Double> coefficients = new Vector<>();

    // Vector of center matPoints - \miu_k
    private Vector<Matrix> matCenters = new Vector<>();

    // Vector of covariance matrices - \Sigma_k
    private Vector<Matrix> matCovariances = new Vector<>();

    /*
     If applied m-dimension data matPoints, the following variables should integrate into another
     m-dimension vector. But for this program and the convenience of imaging results, we just
     use 2 dimension to represent data matPoints.
     (I don't know how to draw high-dimension data matPoints in a coordinate system ...)
     */
    // x coordinates of classified data matPoints
    private Vector<Vector<Double>> classifiedXPoints = new Vector<>();
    // y coordinates of classified data matPoints
    private Vector<Vector<Double>> classifiedYPoints = new Vector<>();

    public GMM(GMMDataset dataset) {
        this.dataset = dataset;
        this.extract(this.dataset);
        this.initArgs();
    }

    public static void main(String[] args) {
        GMMDataset dataset = new GMMDataset(MyArg.setNum.value(), "K");
        dataset.integrate();
        GMM gmm = new GMM(dataset);
        String title = "JFreeChart - GMM";
        GMMChart chart = new GMMChart(title, gmm.getDatasetForImaging());
        chart.initializeChart("GMM");
        System.out.println(chart.seriesCount());
        chart.display();

        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(150);
                gmm.iterate(1);
                chart.setGMMDataset(gmm.getDatasetForImaging());
                chart.initializeChart("GMM");
                System.out.println("iteration " + i + " completed");
                System.out.println("Log likelihood: " + gmm.logLikelihood());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            chart.display();
        }

        chart = new GMMChart(title, dataset.getInitialDataset());
        chart.initializeChart("Original data");
        chart.display();
    }

    /**
     * Create and get a dataset that fits for imaging. And add the center matPoints to the dataset.
     *
     * @return a dataset fitting for imaging
     */
    public GMMDataset getDatasetForImaging() {
        GMMDataset dataset = new GMMDataset();

        // add classified data points to dataset
        int numberOfCluster = this.classifiedXPoints.size();
        for (int k = 0; k < numberOfCluster; k++) {
            Vector<Double> x_clusterK = this.classifiedXPoints.get(k);
            Vector<Double> y_clusterK = this.classifiedYPoints.get(k);
            XYSeries xySeries = new XYSeries("optimized(" + k + ")");
            for (int i = 0; i < x_clusterK.size(); i++) {
                xySeries.add(x_clusterK.get(i), y_clusterK.get(i));
            }
            dataset.addSeries(xySeries);
        }

        // add centers to dataset
        XYSeries centerSeries = new XYSeries("matCenters");
        for (int i = 0; i < this.centerNum; i++) {
            Matrix matCenter = this.matCenters.get(i);
            double x = matCenter.get(0, 0);
            double y = matCenter.get(1, 0);
            centerSeries.add(x, y);
        }
        dataset.addSeries(centerSeries);
        return dataset;
    }

    /**
     * Classify and recenter the GMM dataset for a specific step.
     *
     * @param steps steps taken to (E & M)
     */
    public void iterate(int steps) {
        // iterate #(steps) steps - E & M
        for (int i = 0; i < steps; i++) {
            evaluateResponsibilities();
            estimateModelParams();
        }

        // classify data points
        classify();
    }

    /**
     * Evaluate responsibilities - each possibility when x_n is classified to set of center_k.
     */
    private void evaluateResponsibilities() {
        // clear all gamas (previous calculated first)
        this.gamas.clear();

        // calculate gamas for point_xN_centerK
        int pointsCount = this.matPoints.size();
        for (int n = 0; n < pointsCount; n++) {
            Matrix matX_n = this.matPoints.get(n);
            Vector<Double> gamas_xn = new Vector<>();

            double priorPossibility = 0;

            // calculate priorPossibility
            for (int j = 0; j < this.centerNum; j++) {
                double coefficient_j = this.coefficients.get(j);
                Matrix matMean_j = this.matCenters.get(j);
                Matrix matCov_j = this.matCovariances.get(j);
                double gausDis_njj = this.gausDis(matX_n, matMean_j, matCov_j);

                priorPossibility += coefficient_j * gausDis_njj;
            }

            for (int k = 0; k < this.centerNum; k++) {
                double coefficient_k = this.coefficients.get(k);
                double gausDis_nkk = 0;

                // calculate gaussian distribution
                Matrix matMean_k = this.matCenters.get(k);
                Matrix matCov_k = this.matCovariances.get(k);
                gausDis_nkk = this.gausDis(matX_n, matMean_k, matCov_k);

                // calculate gama_nk and store it
                double gama_xnk = coefficient_k * gausDis_nkk / priorPossibility;
                gamas_xn.add(gama_xnk);

                // TODO Debug
                if (n < 0) {
                    System.out.println("gama_xnk(" + coefficient_k + "," + gausDis_nkk + "," + priorPossibility + ")");
                    System.out.println("matX_n_T - (" + matX_n.get(0, 0) + "," + matX_n.get(1, 0) + ")");
                    System.out.println("matMean_k_T - (" + matMean_k.get(0, 0) + "," + matMean_k.get(1, 0) + ")");
                    System.out.println("matCov_k - (" + matCov_k.get(0, 0) + "," + matCov_k.get(0, 1) + ")");
                    System.out.println("\t\t\t(" + matCov_k.get(1, 0) + "," + matCov_k.get(1, 1) + ")");
                    System.out.println(" (" + gama_xnk + ") ");
                    System.out.println("*****************************");
                }
            }
            this.gamas.add(gamas_xn);
        }
    }

    /**
     * Estimate model parameters - new centers, covariance matrices, and coefficients for components
     * of mixed gaussian distribution.
     */
    private void estimateModelParams() {
        // remove all previous model params first (except gamas)
        Vector<Matrix> newMatCenters = new Vector<>();
        Vector<Matrix> newMatCovariances = new Vector<>();
        Vector<Double> newCoefficients = new Vector<>();


        // calculate the k-th model params
        for (int k = 0; k < this.centerNum; k++) {
            double effectivePointsNum = 0; // 1 / N_k
            Matrix newMatCenter_k = new Matrix(dataDim, 1);    // \miu_k
            Matrix newMatCovariance_k = new Matrix(dataDim, dataDim);  // \Sigma_k
            double newCoefficient_k = 0;   // \pi_k

            Matrix matCenter_k = this.matCenters.get(k);
            // calculate the k-th center and its coefficient and the k-th covariance matrix
            for (int n = 0; n < this.matPoints.size(); n++) {
                Vector<Double> gamas_n = this.gamas.get(n);
                double gama_nk = gamas_n.get(k);
                Matrix matX_n = this.matPoints.get(n);

                // sum up responsibilities
                effectivePointsNum += gama_nk;

                // sum up for new centers' calculation
                newMatCenter_k = newMatCenter_k.plus(matX_n.times(gama_nk));

                // sum up for new covariance matrices' calculation
                Matrix matTemp = matX_n.minus(matCenter_k);
                matTemp = matTemp.times(matTemp.inverse());
                newMatCovariance_k = newMatCovariance_k.plus(matTemp.times(gama_nk));
            }
            newMatCenter_k = newMatCenter_k.times(1 / effectivePointsNum);
            newMatCovariance_k.times(1.0 / effectivePointsNum);
            newCoefficient_k = effectivePointsNum / this.matPoints.size();

            newMatCenters.add(newMatCenter_k);
            newMatCovariances.add(newMatCovariance_k);
            newCoefficients.add(newCoefficient_k);

            // TODO Debug
            if (k < 0) {
                System.out.println("matCenter_k - (" + newMatCenter_k.get(0, 0) + "," + newMatCenter_k.get(1, 0) + ")");
                System.out.println("coefficient_k - " + newCoefficient_k);
                System.out.println("matCovariance_k - (" + newMatCovariance_k.get(0, 0) + "," + newMatCovariance_k.get(0, 1) + ")");
                System.out.println("\t\t\t\t (" + newMatCovariance_k.get(1, 0) + "," + newMatCovariance_k.get(1, 1) + ")");
                System.out.println("*****************************");
            }
        }
        this.matCovariances = newMatCenters;
        this.matCovariances = newMatCovariances;
        this.coefficients = newCoefficients;

    }

    /**
     * Classify data points to clusters.
     */
    private void classify() {
        // clear the original classified data
        this.classifiedXPoints.clear();
        this.classifiedYPoints.clear();

        // add empty vectors and get ready for adding classified data
        for (int i = 0; i < this.centerNum; i++) {
            this.classifiedXPoints.add(new Vector<>());
            this.classifiedYPoints.add(new Vector<>());
        }

        // classify data points and put them into classified data set
        for (int n = 0; n < this.matPoints.size(); n++) {
            Vector<Double> gamas_n = this.gamas.get(n);

            // get the index of center that the point may be assigned to with biggest possibility
            double maxGama_nk = gamas_n.get(0);
            int maxGama_nk_index = 0;

            // TODO Debug
//            System.out.printf("(");
            for (int k = 0; k < gamas_n.size(); k++) {
                double gama_nk = gamas_n.get(k);
                if (gama_nk > maxGama_nk) {
                    maxGama_nk = gama_nk;
                    maxGama_nk_index = k;
                }

                // TODO Debug
//                System.out.printf(gama_nk + " ");
            }
            // TODO Debug
//            System.out.printf(")\n");


            Matrix matPoint = this.matPoints.get(n);
            double x = matPoint.get(0, 0);
            double y = matPoint.get(1, 0);

            // TODO Debug
//            System.out.println("point_" + i + " is assigned to cluster_" + maxGama_nk_index);

            // add the point to its cluster
            this.classifiedXPoints.get(maxGama_nk_index).add(x);
            this.classifiedYPoints.get(maxGama_nk_index).add(y);
        }
    }

    /**
     * Calculate the (log likelihood) of GMM.
     *
     * @return value of Log likelihood
     */
    public double logLikelihood() {
        double result = 0;

        for (int n = 0; n < this.matPoints.size(); n++) {
            double sumOfPsty = 0;

            Matrix matX_n = this.matPoints.get(n);
            for (int k = 0; k < this.centerNum; k++) {
                double coefficient_k = this.coefficients.get(k);
                Matrix matMean_k = this.matCenters.get(k);
                Matrix matCovariance_k = this.matCovariances.get(k);

                double gausDis_nkk = this.gausDis(matX_n, matMean_k, matCovariance_k);
                sumOfPsty = sumOfPsty + coefficient_k * gausDis_nkk;
            }

            result = result + Math.log(sumOfPsty);
        }

        return result;
    }

    /**
     * Extract information of data matPoints in dataset and store them.
     *
     * @param dataset GMMDataset
     * @implNote if need to realize higher dimension GMM, this should be modified.
     */
    public void extract(GMMDataset dataset) {
        // initialize data set
        this.classifiedXPoints.clear();
        this.classifiedYPoints.clear();
        this.classifiedXPoints.add(new Vector<>());
        this.classifiedYPoints.add(new Vector<>());
        this.matPoints.clear();
        this.matCenters.clear();
        this.matCovariances.clear();

        // get all XY series
        int seriesCount = dataset.seriesCount();
        for (int i = 0; i < seriesCount; i++) {
            XYSeries xySeries = dataset.getSeries(i);

            // get matrix of points in every XY series
            int itemsCount = xySeries.getItemCount();
            for (int j = 0; j < itemsCount; j++) {
                double x = xySeries.getX(j).doubleValue();
                double y = xySeries.getY(j).doubleValue();
                // different from K-means, GMM directly store the data point
                Matrix pointMatrix = new Matrix(dataDim, 1);    // create a point matrix
                pointMatrix.set(0, 0, x);
                pointMatrix.set(1, 0, y);
                this.matPoints.add(pointMatrix);
                this.classifiedXPoints.get(0).add(x);
                this.classifiedYPoints.get(0).add(y);
            }
        }
    }

    /**
     * Initialize arguments of GMM based on extracted dataset - \miu(matCenters), coefficients, and
     * \Sigma(covariance matrices).
     *
     * Before use this method, extract data first.
     *
     * @implNote if need to realize higher dimension GMM, this should be modified.
     * @see public void extract(GMMDataset dataset)
     */
    public void initArgs() {
        int pointsCount = this.matPoints.size();
        double defaultCoefficient = 1.0 / this.centerNum;
        Random rand = new Random();

        // TODO Debug
        System.out.println("Points count: " + pointsCount);

        for (int k = 0; k < this.centerNum; k++) {
            int border = MyArg.borderSize.value();

            // initialize matCenters for GMM
            // limit the coordinate value of center within (-border, border)
            double center1 = rand.nextDouble() * 2 * border - border;
            double center2 = rand.nextDouble() * 2 * border - border;
            Matrix matCenter_k = new Matrix(dataDim, 1);  // create a center matrix
            matCenter_k.set(0, 0, center1);  // set value of the first dimension
            matCenter_k.set(1, 0, center2);  // set value of the second dimension

            // TODO Another option of choosing the initial centers (below)
//            matCenter_k = this.matPoints.get(rand.nextInt(pointsCount)).times(1.1);
            // TODO Another option of choosing the initial centers (above)

            this.matCenters.add(matCenter_k);

            // TODO Debug
            System.out.println("center_" + k + "(" + center1 + "," + center2 + ")");

            // initialize coefficients of Gaussian distributions for GMM
            this.coefficients.add(defaultCoefficient);

            // initialize covariance matrices for GMM
            Matrix matCovariance_k = new Matrix(dataDim, dataDim);
            for (int j = 0; j < pointsCount; j++) {
                Matrix matPoint_n = this.matPoints.get(j);   // point matrix
                Matrix matTemp = matPoint_n.minus(matCenter_k);
                matTemp = matTemp.times(matTemp.transpose());
                matCovariance_k = matCovariance_k.plus(matTemp);
            }
            matCovariance_k.times(1.0 / pointsCount);
            this.matCovariances.add(matCovariance_k);

            // TODO Debug
            System.out.print("MatCovariance_" + k + " - " + matCovariance_k.getRowDimension()
                    + ", " + matCovariance_k.getColumnDimension());
            matCovariance_k.print(5, 5);
        }
    }

    ///////////////////////// functions for simplifying calculation /////////////////////////

    /**
     * Sum 2 vectors by every dimension.
     *
     * @param vect1 vector 1
     * @param vect2 vector 2
     * @return sum-vector
     */
    private Vector<Double> sumVector(Vector<Double> vect1, Vector<Double> vect2) {
        Vector<Double> vector = new Vector<>();
        if (vect1.size() != vect2.size()) {
            System.out.println("Error - summing vector failed. ");
            System.exit(-1);
        }
        int vectLength = vect1.size();
        for (int i = 0; i < vectLength; i++) {
            double sumi = vect1.get(i) + vect2.get(i);
            vector.add(sumi);
        }
        return vector;
    }

    /**
     * Calculate the gaussian distribution with specific input data.
     *
     * <note>
     * The original name "gaussianDistribution" is too long. Thus we just call it "gausDis".
     * </note>
     *
     * @param matX    matrix of point x - column vector D * 1
     * @param matMean matrix of mean - column vector D * 1
     * @param matCov  matrix of covariance - column vector D * D
     * @return gaussian distribution possibility for point x
     */
    private double gausDis(Matrix matX, Matrix matMean, Matrix matCov) {
        int dim = matX.getRowDimension();
        // check rows and columns of matrices
        if (matMean.getRowDimension() != dim || matCov.getColumnDimension() != dim
                || matMean.getColumnDimension() != 1 || matCov.getRowDimension() != dim
                || matX.getColumnDimension() != 1) {
            System.out.println("Gaussian distribution - format of args not match. ");
            System.exit(-1);
        }

        double pbty = 0;    // possibility
        double exponent = 0;    // parameter of "exp(...)"
        double determinant = matCov.det(); // determinant of matCov

        // matrices used for storing results temporarily
        Matrix matCov_inverse = matCov.inverse();
        Matrix mat1 = matX.minus(matMean); // (x-u)
        mat1 = (mat1.transpose().times(matCov_inverse).times(mat1)).times(-1.0 / 2.0);
        exponent = mat1.get(0, 0);

        pbty = 1 / (Math.pow(2 * Math.PI, dim / 2));
        pbty *= 1 / (Math.sqrt(determinant));
        pbty *= Math.exp(exponent);

        return pbty;
    }

    /**
     * Calculate the absolute value of a double value.
     *
     * @param x input value
     * @return absolute value of x
     */
    private static double abs(double x) {
        return Math.abs(x);
    }

    /**
     * Calculate the square root of a double value.
     *
     * @param x input value
     * @return square root of x
     */
    private static double sqrt(double x) {
        return Math.sqrt(x);
    }

    /**
     * Calculate the square of a double value.
     *
     * @param x input value
     * @return sqrt of x
     */
    private static double square(double x) {
        return x * x;
    }
}
