import chart.GMMChart;
import dataset.GMMDataset;
import defaultargs.MyArg;
import gmm.GMM;
import kmeans.Kmeans;

/**
 * Integrated test - to make a comparison betweeen K-means and GMM.
 */
public class Main {
    public static void main(String[] args){
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
                Thread.sleep(80);
                gmm.iterate(1);
                chart.setGMMDataset(gmm.getDatasetForImaging());
                chart.initializeChart("GMM - iteration " + i);
                System.out.println("iteration " + i + " completed");
                System.out.println("Log likelihood: " + gmm.logLikelihood());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            chart.display();
        }

        Kmeans kmeans = new Kmeans(dataset);
        title = "JFreeChart - K-means";
        chart = new GMMChart(title, kmeans.getDatasetForImaging());
        chart.initializeChart("K-means");
        System.out.println(chart.seriesCount());
        chart.display();

        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(80);
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
}
