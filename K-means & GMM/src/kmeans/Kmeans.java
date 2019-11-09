package kmeans;

import org.jfree.data.xy.XYSeries;

import java.util.Random;
import java.util.Vector;

import chart.GMMChart;
import dataset.GMMDataset;
import defaultargs.MyArg;

public class Kmeans {

    // which represents the value of "K" in "K-means"
    private int centerNum = MyArg.valueK.value();

    private GMMDataset dataset = null;

    // vector of center points - \miu
    private Vector<Double> xCenters = new Vector<>();
    private Vector<Double> yCenters = new Vector<>();

    /*
     If applied m-dimension data points, the following variables should integrate into another
     m-dimension vector. But for this program and the convenience of imaging results, we just
     use 2 dimension to represent data points.
     (I don't know how to draw high-dimension data points in a coordinate system ...)
     And if applied m-dimension, loops in functions of this class may add another layer, and we
     must use another dataset.
     */
    // Vector of xySeries vector of xyPoints (there are several clusters in the following
    // 2 vectors). The first dimension is cluster type. The second is data point index.
    private Vector<Vector<Double>> xPoints = new Vector<>();   // x coordinates of data points
    private Vector<Vector<Double>> yPoints = new Vector<>();   // y coordinates of data points

    public Kmeans(GMMDataset dataset) {
        this.dataset = dataset;
        this.extract(this.dataset);
        this.initCenter();
    }

    public static void main(String[] args) {
        GMMDataset dataset = new GMMDataset(MyArg.setNum.value(), "K");
        dataset.integrate();
        Kmeans kmeans = new Kmeans(dataset);
        String title = "JFreeChart - K-means";
        GMMChart chart = new GMMChart(title, kmeans.getDatasetForImaging());
        chart.initializeChart("K-means");
        System.out.println(chart.seriesCount());
        chart.display();

        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(300);
                kmeans.iterate(1);
                chart.setGMMDataset(kmeans.getDatasetForImaging());
                chart.initializeChart("K-means - iteration " + i);
                System.out.println("iteration " + i + " completed");
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
     * Create and get a dataset that fits for imaging.
     *
     * @return a dataset fitting for imaging
     */
    public GMMDataset getDatasetForImaging() {
        GMMDataset dataset = this.dataset.clone();
        XYSeries centerSeries = new XYSeries("centers");
        for (int i = 0; i < this.centerNum; i++) {
            double x = this.xCenters.get(i);
            double y = this.yCenters.get(i);
            centerSeries.add(x, y);
        }
        dataset.addSeries(centerSeries);
        return dataset;
    }

    /**
     * Classify and recenter the GMM dataset for a specific step.
     *
     * @param steps steps taken to (classify & recenter)
     */
    public void iterate(int steps) {
        GMMDataset newDataset = new GMMDataset();
        // add #(centerNum) empty series to the dataset for optimized data points
        newDataset.addSeries(this.centerNum, "optimized");

        // iterate #(steps) steps - classify & recenter
        for (int i = 0; i < steps; i++) {
            classify(newDataset);
            recenter(newDataset);
        }
    }

    /**
     * Classify all data points in the original dataset and put the classified data points into a
     * new dataset.
     *
     * @param newDataset new dataset
     */
    private void classify(GMMDataset newDataset) {
        // get the data points of the j-th XY series
        int seriesCount = this.dataset.seriesCount();
        for (int j = 0; j < seriesCount; j++) {
            XYSeries xySeries = this.dataset.getSeries(j);
            // get the data point of the k-th XY series in j-th XY series
            int itemsCount = xySeries.getItemCount();
            for (int k = 0; k < itemsCount; k++) {
                double x = xySeries.getX(k).doubleValue();
                double y = xySeries.getY(k).doubleValue();
                // search all centers to find the nearest
                double xCenter = this.xCenters.get(0);
                double yCenter = this.yCenters.get(0);
                int centerSelected = 0;
                double minDistance = this.euclideanDistance(x, y, xCenter, yCenter);
                for (int l = 1; l < this.centerNum; l++) {
                    xCenter = this.xCenters.get(l);
                    yCenter = this.yCenters.get(l);
                    double temp = this.euclideanDistance(x, y, xCenter, yCenter);
                    if (temp < minDistance) {
                        minDistance = temp;
                        centerSelected = l;
                    }
                }
                newDataset.getSeries(centerSelected).add(x, y);
            }
        }
        this.dataset = newDataset;
    }

    /**
     * Re-center the centers in the original dataset and store the new centers.
     *
     * @param dataset new dataset
     */
    private void recenter(GMMDataset dataset) {
        for (int i = 0; i < this.centerNum; i++) {
            XYSeries xySeries = dataset.getSeries(i);
            int itemsCount = xySeries.getItemCount();
            double xSum = 0;
            double ySum = 0;
            for (int j = 0; j < itemsCount; j++) {
                xSum += xySeries.getX(j).doubleValue();
                ySum += xySeries.getY(j).doubleValue();
            }
            double xMean = xSum / itemsCount;
            double yMean = ySum / itemsCount;
            this.xCenters.set(i, xMean);
            this.yCenters.set(i, yMean);
        }
    }

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
     * Calculate the euclidean distance of 2 points in 2-dimension space. (x-y coordinate system)
     *
     * @param x1 x coordinate of point 1
     * @param y1 y coordinate of point 1
     * @param x2 x coordinate of point 2
     * @param y2 y coordinate of point 2
     */
    private double euclideanDistance(double x1, double y1, double x2, double y2) {
        return sqrt(square(x1 - x2) + square(y1 - y2));
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

    /**
     * Initialize the center points of K-means.
     */
    public void initCenter() {
        Random rand = new Random();
        for (int i = 0; i < this.centerNum; i++) {
            int border = MyArg.borderSize.value();
            // limit the coordinate value of center within (-border, border)
            double xCenter;
            double yCenter;
            int seriesCount;
            int pointsCountSeries;

            // select a random series
            seriesCount = this.xPoints.size();
            Vector<Double> xSeries = this.xPoints.get(rand.nextInt(seriesCount));
            Vector<Double> ySeries = this.yPoints.get(rand.nextInt(seriesCount));

            // select a random data point from the series selected previously
            pointsCountSeries = xSeries.size();
            xCenter = xSeries.get(rand.nextInt(pointsCountSeries));
            yCenter = ySeries.get(rand.nextInt(pointsCountSeries));

            this.xCenters.add(xCenter);
            this.yCenters.add(yCenter);
        }
    }

    /**
     * Extract information of data points in dataset and store them.
     *
     * @param dataset GMMDataset
     */
    public void extract(GMMDataset dataset) {
        // get all XY series
        int seriesCount = dataset.seriesCount();
        for (int i = 0; i < seriesCount; i++) {
            XYSeries xySeries = dataset.getSeries(i);

            // get data points of every XY series
            int itemsCount = xySeries.getItemCount();
            Vector<Double> vecX = new Vector<>();
            Vector<Double> vecY = new Vector<>();
            for (int j = 0; j < itemsCount; j++) {
                double x = xySeries.getX(j).doubleValue();
                double y = xySeries.getY(j).doubleValue();
                vecX.add(x);
                vecY.add(y);
            }
            this.xPoints.add(vecX);
            this.yPoints.add(vecY);
        }
    }
}
