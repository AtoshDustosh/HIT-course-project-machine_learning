package dataset;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Random;
import java.util.Vector;

import defaultargs.MyArg;

/**
 * For the convenience of imaging results, we choose 2 dimensions' data points in this dataset. And
 * the absolute values of borders for data points are all the same, making the whole graph look like
 * a square.
 */
public class GMMDataset {

    public static final double BorderSize = MyArg.borderSize.value();

    // borders of the graph
    private final double XUpperBorder = BorderSize;
    private final double XLowerBorder = -BorderSize;
    private final double YUpperBorder = BorderSize;
    private final double YLowerBorder = -BorderSize;

    // points per set of gaussian distribution
    private final int pointsPerSet = MyArg.pointsPerSet.value();

    // the border of deviation for gaussian distribution
    private double deviationBorder = BorderSize / 5;

    // a collection for data sets of data points
    private XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

    // whether shapes of XYPoints are visible
    private Vector<Boolean> shapesVisible = new Vector<>();

    // the initial (first created) dataset.
    private GMMDataset initialDataset;

    /**
     * Construct an empty GMM dataset.
     */
    public GMMDataset() {
        this(0, "");
    }

    /**
     * Construct a GMMDataset that contains #(setNum) set of data points with different gaussian
     * distribution.
     *
     * @param setNum    number of sets of data points
     * @param seriesKey key/name of the series in the dataset
     */
    public GMMDataset(int setNum, String seriesKey) {
        String xySeriesKey;
        XYSeries xySeries;

        // create #(setNum) set of data points
        for (int i = 0; i < setNum; i++) {
            xySeriesKey = seriesKey + "(" + i + ")";   // name/label of a XY series
            xySeries = new XYSeries(xySeriesKey);
            Random rand = new Random();
            double meanX
                    = rand.nextDouble() * (this.XUpperBorder - this.XLowerBorder) + this.XLowerBorder;
            double meanY
                    = rand.nextDouble() * (this.YUpperBorder - this.YLowerBorder) + this.YLowerBorder;
            // create data points for each set
            int pointsCount = rand.nextInt(this.pointsPerSet) + this.pointsPerSet;
            for (int j = 0; j < pointsCount; j++) {
                double x = deviationBorder * rand.nextGaussian() + meanX;
                double y = deviationBorder * rand.nextGaussian() + meanY;
                xySeries.add(x, y);
            }

            this.xySeriesCollection.addSeries(xySeries);
            this.shapesVisible.add(true);
        }

        // store the initial dataset
        this.initialDataset = this.clone();
    }

    /**
     * Construct a GMM dataset with specific xySeriesCollection and shapesVisible.
     *
     * @param xySeriesCollection XY series collection - data points set
     * @param shapesVisible      whether sets of data points are visible or not
     */
    public GMMDataset(XYSeriesCollection xySeriesCollection, Vector<Boolean> shapesVisible) {
        this.xySeriesCollection = xySeriesCollection;
        this.shapesVisible = shapesVisible;
    }

    /**
     * Integrate all sets of data points together.
     */
    public void integrate() {
        XYSeries xySeries;
        // integrate all sets of data points together
        XYSeries integratedXYSeries = new XYSeries("integration");
        for (int i = 0; i < this.xySeriesCollection.getSeriesCount(); i++) {
            xySeries = this.xySeriesCollection.getSeries(i);
            int itemsCount = xySeries.getItemCount();
            for (int j = 0; j < itemsCount; j++) {
                double x = xySeries.getX(j).doubleValue();
                double y = xySeries.getY(j).doubleValue();
                integratedXYSeries.add(x, y);
            }
        }
        this.xySeriesCollection.removeAllSeries();
        this.shapesVisible.clear();
        this.xySeriesCollection.addSeries(integratedXYSeries);
        this.shapesVisible.add(true);
    }

    /**
     * Get the initial dataset (firstly created and before integration).
     *
     * @return dataset that is firstly created and before integration
     */
    public GMMDataset getInitialDataset() {
        return this.initialDataset;
    }

    /**
     * Remove all series and related values from this dataset.
     */
    public void clear() {
        this.xySeriesCollection = new XYSeriesCollection();
        this.shapesVisible = new Vector<>();
    }

    /**
     * Whether the shapes of data points in a series of this dataset is visible.
     *
     * @param index index of the series
     * @return true if visible; false otherwise
     */
    public boolean shapeVisible(int index) {
        return this.shapesVisible.get(index);
    }

    /**
     * Add a number of empty XY series.
     *
     * @param seriesNum     number of empty series to add
     * @param seriesBaseKey base key for series
     */
    public void addSeries(int seriesNum, String seriesBaseKey) {
        for (int i = 0; i < seriesNum; i++) {
            String seriesKey = seriesBaseKey + i;
            XYSeries xySeries = new XYSeries(seriesKey);
            this.addSeries(xySeries, true);
        }
    }

    /**
     * Add a new XYSeries into this dataset. (default visibility: true)
     *
     * @param newSeries new XY series
     */
    public void addSeries(XYSeries newSeries) {
        this.addSeries(newSeries, true);
    }

    /**
     * Add a new XY series into this dataset with designated visibility.
     *
     * @param newSeries    new XY series
     * @param shapeVisible whether shape is visible for this XY series
     */
    public void addSeries(XYSeries newSeries, boolean shapeVisible) {
        this.xySeriesCollection.addSeries(newSeries);
        this.shapesVisible.add(shapeVisible);
    }

    /**
     * Get the required XY series from this dataset.
     *
     * @param index index of XY series
     * @return XY series
     */
    public XYSeries getSeries(int index) {
        return this.xySeriesCollection.getSeries(index);
    }

    /**
     * Get data set from this dataset.
     *
     * @return XY data set
     */
    public XYDataset getXYDataset() {
        return this.xySeriesCollection;
    }

    /**
     * Remove a series from this dataset.
     *
     * @param index index of XY series
     */
    public void removeSeries(int index) {
        this.xySeriesCollection.removeSeries(index);
    }

    /**
     * Get the count of series in this dataset.
     *
     * @return count of series
     */
    public int seriesCount() {
        return this.xySeriesCollection.getSeriesCount();
    }

    /**
     * Returns a clone of this GMM dataset. The copy will contain a reference to a clone of the
     * internal dataset, not a reference to the original internal dataset of this GMMDataset
     * object.
     *
     * @return a clone of this GMM dataset
     */
    public GMMDataset clone() {
        int count = this.xySeriesCollection.getSeriesCount();

        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();
        Vector<Boolean> shapesVisible = new Vector<>();

        for (int i = 0; i < count; i++) {
            xySeriesCollection.addSeries(this.xySeriesCollection.getSeries(i));
            shapesVisible.add(this.shapesVisible.get(i));
        }

        return new GMMDataset(xySeriesCollection, shapesVisible);
    }


}
