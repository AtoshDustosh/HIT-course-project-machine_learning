package pca;

import Jama.Matrix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

public class Dataset {
    private static final String testImage1 = "src/testdata/pcaImage3.jpg";
    private static final String testImage2 = "src/testdata/imageOp2.jpg";

    private String filePath = null;

    private Vector<Vector<Integer>> imagePixelsVec = new Vector<>();

    public Dataset() {

    }

    public Dataset(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        Dataset dataset = new Dataset();
        dataset.readGrayImage(testImage1);
        dataset.displayData();

        Matrix mat = dataset.getPixelMatrix();
        mat.print(4, 1);
        System.out.println("row: " + mat.getRowDimension() + ", column: " + mat.getColumnDimension());
    }

    /**
     * Generate a dataset manually.
     *
     * @param n number of data records
     * @param m dimension of data
     */
    public void generateDefaultDataset(int n, int m) {
        Random rand = new Random();
        this.imagePixelsVec.clear();
        for (int i = 0; i < n; i++) {
            Vector<Integer> vector = new Vector<>();
            for (int j = 0; j < m; j++) {
                int value = rand.nextInt(10);
                vector.add(value);
            }
            this.imagePixelsVec.add(vector);
        }
    }

    /**
     * Read r/g/b data of an image.
     *
     * @param imageFilePath file path of the input image
     */
    public void readRGBImage(String imageFilePath) {
        this.filePath = imageFilePath;
        this.imagePixelsVec.clear();

        File file = new File(imageFilePath);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {

            e.printStackTrace();
        }

        int width = bi.getWidth();
        int height = bi.getHeight();
        int minX = bi.getMinX();
        int minY = bi.getMinY();
        for (int y = minY; y < height; y++) {
            System.out.print("line(" + y + "): ");
            for (int x = minX; x < width; x++) {
                Vector<Integer> rgbVec = new Vector<>();
                int pixel = bi.getRGB(x, y);
                // get r/g/b values of the pixel
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                rgbVec.add(r); //r
                rgbVec.add(g); //g
                rgbVec.add(b); //b
                System.out.print("(" + r + "," + g + "," + b + ")");
//                printf("(" + x + ")");
                this.imagePixelsVec.add(rgbVec);
            }
            System.out.println();
        }
    }

    /**
     * Read data of a gray image.
     *
     * @param imageFilePath file path of the input image
     */
    public void readGrayImage(String imageFilePath) {
        this.filePath = imageFilePath;
        this.imagePixelsVec.clear();

        File file = new File(imageFilePath);
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {

            e.printStackTrace();
        }

        int width = bi.getWidth();
        int height = bi.getHeight();
        int minX = bi.getMinX();
        int minY = bi.getMinY();
        for (int y = minY; y < height; y++) {
//            printf("line(" + y + "): ");
            Vector<Integer> rgbVec = new Vector<>();
            for (int x = minX; x < width; x++) {
                int pixel = bi.getRGB(x, y);
                // get r/g/b values of the pixel
                int r = (pixel & 0xff0000) >> 16;
                int g = (pixel & 0xff00) >> 8;
                int b = (pixel & 0xff);
                int grayValue = (int) (0.30f * r + 0.59f * g + 0.11f * b);
//                System.out.print("(" + r + "," + g + "," + b + ")");
//                System.out.print("(" + grayValue + ")");
                rgbVec.add(grayValue);
            }

            this.imagePixelsVec.add(rgbVec);
//            System.out.println();
        }
    }

    /**
     * Display the pixels' vectors.
     */
    public void displayData() {
        int length = this.imagePixelsVec.size();
        for (int i = 0; i < length; i++) {
            System.out.println("pixel(" + (i + 1) + "): " + this.imagePixelsVec.get(i));
        }
    }

    /**
     * Get the pixel matrix.
     *
     * @return matrix of pixels
     */
    public Matrix getPixelMatrix() {
        int n = this.imagePixelsVec.size();
        int m = this.imagePixelsVec.get(0).size();
        double[][] array = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                array[i][j] = this.imagePixelsVec.get(i).get(j);
            }
        }
        return new Matrix(array);
    }

    /**
     * Transform the image pixel matrix into one-column matrix.
     *
     * @return one-column matrix of image pixel matrix
     */
    public Matrix getOneColumnPixelMatrix() {
        int n = this.imagePixelsVec.size();
        int m = this.imagePixelsVec.get(0).size();
        double[][] array = new double[n * m][1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                array[i * n + j][0] = this.imagePixelsVec.get(i).get(j);
            }
        }
        return new Matrix(array);
    }

}
