package chart;

import dataset.MyXYDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;

public class MyChart extends ApplicationFrame {

    private static final int preferredWidth = 800;
    private static final int preferredHeight = 400;
    private String chartTitle = null;
    private MyXYDataset dataset = null;
    private JPanel jPanel = null;

    /**
     * Constructor - construct with a title.
     *
     * @param title title for chart
     */
    public MyChart(String title) {
        super(title);
        this.chartTitle = title;
        this.dataset = new MyXYDataset();
    }

    /**
     * Constructor - construct with title and dataset.
     *
     * @param title   title for chart
     * @param dataset dataset from outside input
     */
    public MyChart(String title, MyXYDataset dataset) {
        super(title);
        this.chartTitle = title;
        this.dataset = dataset;
    }

    public static void main(String[] args) {
        MyChart chart = new MyChart("JFreeChart - Line Chart.java");
        chart.initializeChart("Line Chart");
        chart.display();
    }

    /**
     * Initialize the chart with its XY dataset and set the subtitle.
     *
     * @param subTitle subtitle of the chart
     */
    public void initializeChart(String subTitle) {
        JFreeChart jFreeChart = initialization(subTitle);
        this.jPanel = new ChartPanel(jFreeChart);
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
     * Add series and relevant values of another dataset into this dataset.
     *
     * @param newDataset dataset to be added
     */
    public void addSeries(MyXYDataset newDataset) {
        int seriesCount = newDataset.seriesCount();
        for (int i = 0; i < seriesCount; i++) {
            XYSeries xySeries = newDataset.getSeries(i);
            boolean lineVisible = newDataset.lineVisible(i);
            boolean shapeVisible = newDataset.shapeVisible(i);
            this.dataset.addSeries(xySeries, lineVisible, shapeVisible);
        }
    }

    /**
     * Count of series in the dataset of this chart.
     *
     * @return series count
     */
    public int seriesCount(){
        return this.dataset.seriesCount();
    }

    /**
     * @see public void initializeChart(String subTitle)
     */
    private JFreeChart initialization(String subtitle) {
        JFreeChart jfreechart = ChartFactory.createXYLineChart(subtitle, "X", "Y", this.dataset.getXYDataset(), PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer();
        // System.out.println("date set series count: " + xydataset.getSeriesCount());
        // set visible or not for lines and shapes of series
        for (int i = 0; i < this.dataset.seriesCount(); i++) {
            xylineandshaperenderer.setSeriesLinesVisible(i, this.dataset.lineVisible(i));
            xylineandshaperenderer.setSeriesShapesVisible(i, this.dataset.shapeVisible(i));
        }
        xylineandshaperenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        xyplot.setRenderer(xylineandshaperenderer);
        NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return jfreechart;
    }
}
