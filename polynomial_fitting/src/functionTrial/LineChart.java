package functionTrial;

import dataset.MyXYDataset;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.*;

public class LineChart extends ApplicationFrame {
    private MyXYDataset dataset = new MyXYDataset();

    /**
     * Construct a line chart with title.
     *
     * @param title title of chart
     */
    private LineChart(String title) {
        super(title);
        JPanel jpanel = createPanel();
        jpanel.setPreferredSize(new Dimension(800, 400));
        setContentPane(jpanel);
    }

    /**
     * Construct a line chart with title and XYDataset.
     *
     * @param title title of chart
     * @param xyDataset XY dataset
     */
    public LineChart(String title, MyXYDataset xyDataset) {
        super(title);
        this.dataset = xyDataset;
        JPanel jpanel = createPanel();
        jpanel.setPreferredSize(new Dimension(800, 400));
        setContentPane(jpanel);
    }

    public static void main(String[] args) {
        LineChart chart = new LineChart("JFreeChart - Line Chart.java");
        chart.pack();
        RefineryUtilities.centerFrameOnScreen(chart);
        chart.setVisible(true);
    }

    private JPanel createPanel() {
        JFreeChart jfreechart = createChart();
        return new ChartPanel(jfreechart);
    }

    /**
     * Create a jfreechart.
     *
     * @return chart int the form of JFreeChart
     */
    private JFreeChart createChart() {
        JFreeChart jfreechart = ChartFactory.createXYLineChart("Line Chart", "X", "Y", dataset.getXYDataset(), PlotOrientation.VERTICAL, true, true, false);
        XYPlot xyplot = (XYPlot) jfreechart.getPlot();
        XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer();
        // System.out.println("date set series count: " + xydataset.getSeriesCount());
        // set visible or not for lines and shapes of series
        for (int i = 0; i < dataset.seriesCount(); i++) {
            xylineandshaperenderer.setSeriesLinesVisible(i, dataset.lineVisible(i));
            xylineandshaperenderer.setSeriesShapesVisible(i, dataset.shapeVisible(i));
        }
        xylineandshaperenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        xyplot.setRenderer(xylineandshaperenderer);
        NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        return jfreechart;
    }
}
