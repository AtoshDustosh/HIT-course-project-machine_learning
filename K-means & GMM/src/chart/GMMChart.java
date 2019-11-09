package chart;

import dataset.GMMDataset;
import defaultargs.MyArg;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;

import java.awt.*;

public class GMMChart extends ApplicationFrame {

    private static final int setNum = MyArg.setNum.value();  // recommended value - setNum = 5

    // preferred size of the panel
    private static final int preferredWidth = 1440;
    private static final int preferredHeight = 960;

    private GMMDataset dataset;
    private JPanel jPanel = null;

    /**
     * Constructor - construct with a title.
     *
     * @param title title for chart
     */
    public GMMChart(String title) {
        super(title);
        this.dataset = new GMMDataset(setNum, "$");
    }

    /**
     * Constructor - construct with title and dataset.
     *
     * @param title   title for chart
     * @param dataset dataset from outside input
     */
    public GMMChart(String title, GMMDataset dataset) {
        super(title);
        this.dataset = dataset;
    }

    public static void main(String[] args) {
        GMMChart chart = new GMMChart("JFreeChart - Line Chart.java");
        chart.initializeChart("Line Chart");
        chart.display();
        System.out.println(chart.seriesCount());

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
                chart.addAllSeries(new GMMDataset(1, "" + (char)((int)('A') + i)));
                chart.initializeChart("Line Chart ");
                System.out.println(chart.seriesCount());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            chart.display();
        }
    }

    /**
     * Initialize the chart with its XY dataset and set the subtitle.
     *
     * @param subtitle subtitle of the chart
     */
    public void initializeChart(String subtitle) {
        JFreeChart jfreechart = ChartFactory.createXYLineChart(
                subtitle, "X", "Y", this.dataset.getXYDataset(),
                PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer();
        xylineandshaperenderer.setBaseLinesVisible(true);

        // System.out.println("date set series count: " + xydataset.getSeriesCount());
        // set visible or not shapes of series
//        System.out.printf("sets in the dataset: ");     // TODO delete this "print" if necessary
        for (int i = 0; i < this.dataset.seriesCount(); i++) {
//            System.out.printf(this.dataset.getSeries(i).getKey() + " ");
            xylineandshaperenderer.setSeriesLinesVisible(i, false);
            xylineandshaperenderer.setSeriesShapesVisible(i, this.dataset.shapeVisible(i));
        }
        xylineandshaperenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        xyplot.setRenderer(xylineandshaperenderer);
        NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        this.jPanel = new ChartPanel(jfreechart);
    }

    /**
     * Display the chart.
     */
    public void display() {
        this.jPanel.setPreferredSize(new Dimension(this.preferredWidth, this.preferredHeight));
        setContentPane(this.jPanel);
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    /**
     * Set the GMM dataset of this GMM chart.
     *
     * @param dataset new dataset
     */
    public void setGMMDataset(GMMDataset dataset){
        this.dataset = dataset;
    }

    /**
     * Add series and relevant values of another dataset into the dataset of this chart.
     *
     * @param newDataset dataset to be added
     */
    public void addAllSeries(GMMDataset newDataset) {
        int seriesCount = newDataset.seriesCount();
        for (int i = 0; i < seriesCount; i++) {
            XYSeries xySeries = newDataset.getSeries(i);
            boolean shapeVisible = newDataset.shapeVisible(i);
            this.dataset.addSeries(xySeries, shapeVisible);
        }
    }

    /**
     * Add all series and relevant values of this dataset to another dataset. And then make that
     * dataset the dataset of this GMM chart.
     *
     * @param newDataset new dataset
     */
    public void addAllSeriesTo(GMMDataset newDataset){
        int seriesCount = this.dataset.seriesCount();
        for (int i = 0; i < seriesCount; i++) {
            XYSeries xySeries = this.dataset.getSeries(i);
            boolean shapeVisible = this.dataset.shapeVisible(i);
            newDataset.addSeries(xySeries, shapeVisible);
        }
        this.dataset = newDataset;
    }

    /**
     * Count of series in the dataset of this chart.
     *
     * @return series count
     */
    public int seriesCount() {
        return this.dataset.seriesCount();
    }
}
