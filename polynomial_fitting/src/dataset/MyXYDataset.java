package dataset;

import defaultData.MyNumbers;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Random;
import java.util.Vector;

public class MyXYDataset {

    private double noiseRatio = 0.3;

    private XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

    private Vector<Boolean> linesVisible = new Vector<>();  // whether lines of the series are visible (according to indexes)
    private Vector<Boolean> shapesVisible = new Vector<>(); // whether shapes of the series are visible (according to indexes)

    /**
     * Construct initial(default) dataset in XYSeriesCollection.
     */
    public MyXYDataset() {
        String xySeries0_key = "sin(2πx)";
        XYSeries xySeries0 = new XYSeries(xySeries0_key);

        // add "sin(2πx)" into XY data set
        this.linesVisible.add(true);
        this.shapesVisible.add(false);
        for (int i = 0; i <= MyNumbers.num_chartPoints.getValue(); i++) {
            double x = (1 / MyNumbers.num_chartPoints.getValue()) * i;
            double y = Math.sin(2 * Math.PI * x);
            xySeries0.add(x, y);
        }

        String xySeries1_key = "random noise added";
        XYSeries xySeries1 = new XYSeries(xySeries1_key);

        // add "random noise added" into XY data set
        this.linesVisible.add(false);
        this.shapesVisible.add(true);
        for (int i = 0; i <= MyNumbers.num_trainPoints.getValue(); i++) {
            Random random = new Random();
            double x = (1 / MyNumbers.num_trainPoints.getValue()) * i;
            double y = Math.sin(2 * Math.PI * x) + this.noiseRatio * random.nextGaussian();
            xySeries1.add(x, y);
        }
        this.xySeriesCollection.addSeries(xySeries0);
        this.xySeriesCollection.addSeries(xySeries1);
    }

    /**
     * Construct dataset using outside input data.
     *
     * @param xySeriesCollection XYSeriesCollection
     * @param linesVisible       vector of whether lines of the series are visible
     * @param shapesVisible      vector of whether shapes of the series are visible
     */
    public MyXYDataset(XYSeriesCollection xySeriesCollection, Vector<Boolean> linesVisible, Vector<Boolean> shapesVisible) {
        this.xySeriesCollection = xySeriesCollection;
        this.linesVisible = linesVisible;
        this.shapesVisible = shapesVisible;
    }

    /**
     * Whether line of a series in XYSeriesCollection is visible.
     *
     * @param index index of the series
     * @return true if visible; false otherwise
     */
    public boolean lineVisible(int index) {
        return this.linesVisible.get(index);
    }

    /**
     * Whether shape of a series in XYSeriesCollection is visible.
     *
     * @param index index of the series
     * @return true if visible; false otherwise
     */
    public boolean shapeVisible(int index) {
        return this.shapesVisible.get(index);
    }

    /**
     * Add a new XY series into XYSeriesCollection.
     *
     * @param newSeries    new XY series
     * @param lineVisible  whether line is visible for this XY series
     * @param shapeVisible whether shape is visible for this XY series
     */
    public void addSeries(XYSeries newSeries, boolean lineVisible, boolean shapeVisible) {
        this.xySeriesCollection.addSeries(newSeries);
        this.linesVisible.add(lineVisible);
        this.shapesVisible.add(shapeVisible);
    }

    /**
     * Get the required XY series from XYSeriesCollection.
     *
     * @param index index to XY series
     * @return XY series
     */
    public XYSeries getSeries(int index) {
        return this.xySeriesCollection.getSeries(index);
    }

    /**
     * Get the vector for X of a series in XYSeriesCollection.
     *
     * @param seriesIndex index of XYSeries
     * @return corresponding vector of X coordinates
     */
    public Vector<Double> getVectX(int seriesIndex) {
        XYSeries xySeries = this.xySeriesCollection.getSeries(seriesIndex);
        int dimX = xySeries.getItemCount();
        Vector<Double> vectX = new Vector<>();

        for (int i = 0; i < dimX; i++) {
            Double xi = (Double) xySeries.getX(i);
            vectX.add(xi);
        }

        return vectX;
    }

    /**
     * Get the vector for Y of a series in XYSeriesCollection.
     *
     * @param seriesIndex index of XYSeries
     * @return corresponding vector of Y coordinates
     */
    public Vector<Double> getVectY(int seriesIndex) {
        XYSeries xySeries = this.xySeriesCollection.getSeries(seriesIndex);
        int dimY = xySeries.getItemCount();
        Vector<Double> vectY = new Vector<>();

        for (int i = 0; i < dimY; i++) {
            Double xi = (Double) xySeries.getY(i);
            vectY.add(xi);
        }

        return vectY;
    }

    /**
     * Get data set from XYSeriesCollection.
     *
     * @return XY data set
     */
    public XYDataset getXYDataset() {
        return this.xySeriesCollection;
    }

    /**
     * Select  a group of series and retain them from series collection.
     *
     * @param selection array of index for selected series
     * @return selected series packed in MYyXYDataset
     */
    public MyXYDataset selectSeries(int[] selection) {
        MyXYDataset mydataset = new MyXYDataset();
        int originalSeriesCount = mydataset.seriesCount();
        // clean the newly created data set
        for (int i = 0; i < originalSeriesCount; i++) {
            mydataset.removeSeries(originalSeriesCount - (i + 1));
        }
        // add selected series into the data set
        for (int i = 0; i < selection.length; i++) {
            int index = selection[i];
            mydataset.addSeries(this.xySeriesCollection.getSeries(index), this.linesVisible.get(index), this.shapesVisible.get(index));
        }

        return mydataset;
    }

    /**
     * Remove a series from XYSeriesCollection.
     *
     * @param index index of data set
     */
    public void removeSeries(int index) {
        this.xySeriesCollection.removeSeries(index);
        this.linesVisible.remove(index);
        this.shapesVisible.remove(index);
    }

    /**
     * Remove all series and related values from XYDataset.
     */
    public void clear() {
        this.xySeriesCollection = new XYSeriesCollection();
        this.shapesVisible.clear();
        this.linesVisible.clear();
    }

    /**
     * Get the count of series in series collection.
     *
     * @return count of series
     */
    public int seriesCount() {
        return this.xySeriesCollection.getSeriesCount();
    }

    /**
     * Result of original function with input x.
     *
     * @param x input
     * @return result
     */
    public double originalFunc(double x) {
        return Math.sin(2 * Math.PI * x);
    }

    /**
     * Returns a clone of this XYDataset.
     * The copy will contain a reference to a clone of the internal dataset, not a reference to the original internal dataset of this MyXYDataset object.
     *
     * @return a clone of this XYDataset
     */
    public MyXYDataset clone() {
        int count = this.xySeriesCollection.getSeriesCount();

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        Vector<Boolean> linesVisible = new Vector<>();
        Vector<Boolean> shapesVisible = new Vector<>();

        for (int i = 0; i < count; i++) {
            xySeriesCollection.addSeries(this.xySeriesCollection.getSeries(i));
            linesVisible.add(this.linesVisible.get(i));
            shapesVisible.add(this.shapesVisible.get(i));
        }

        MyXYDataset cloneDataset = new MyXYDataset(xySeriesCollection, linesVisible, shapesVisible);
        return cloneDataset;
    }
}
