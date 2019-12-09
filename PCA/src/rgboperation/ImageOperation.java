package rgboperation;

import Jama.Matrix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageOperation {

    private static final String inputImagePath = "src/testdata/pcaImage3.jpg";
    private static final String outputImagePath = "src/testdata/pcaImage3.jpg";

    public static void main(String[] args) {
        ImageOperation test = new ImageOperation();
//        test.getImagePixel(inputImagePath4);
        test.transformToGray(inputImagePath, outputImagePath);
//        test.getImagePixel(outputImagePath4);
//        test.rotateImage(inputImagePath4, outputImagePath4, 90);
    }

    /**
     * Read r/g/b values of an image.
     *
     * @param inputImagePath file path of the image
     */
    public void readPrintPixel(String inputImagePath) {

        int[] rgb = new int[3];
        File file = new File(inputImagePath);
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
            printf("line(" + y + "): ");
            for (int x = minX; x < width; x++) {
                int pixel = bi.getRGB(x, y);
                // get r/g/b values of the pixel
                rgb[0] = (pixel & 0xff0000) >> 16; //r
                rgb[1] = (pixel & 0xff00) >> 8; //g
                rgb[2] = (pixel & 0xff); //b
                System.out.print("(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")");
//                printf("(" + x + ")");
            }
            System.out.println();
        }

    }

    /**
     * Transform the image to a gray image.
     *
     * @param inputImagePath  path of input image
     * @param outputImagePath path of output image
     */
    public void transformToGray(String inputImagePath, String outputImagePath) {
        BufferedImage image;
        try {
            image = ImageIO.read(new File(inputImagePath));
            for (int y = image.getMinY(); y < image.getHeight(); y++) {
                for (int x = image.getMinX(); x < image.getWidth(); x++) {
                    int pixel = image.getRGB(x, y);
                    int r = (pixel >> 16) & 0xff;
                    int g = (pixel >> 8) & 0xff;
                    int b = pixel & 0xff;

                    int grayValue = (int) (0.30f * r + 0.59f * g + 0.11f * b);

                    pixel = ((grayValue << 16) & 0x00ff0000) | (pixel & 0xff00ffff);
                    pixel = ((grayValue << 8) & 0x0000ff00) | (pixel & 0xffff00ff);
                    pixel = ((grayValue) & 0x000000ff) | (pixel & 0xffffff00);
                    image.setRGB(x, y, pixel);
                }
            }
            ImageIO.write(image, "jpg", new File(outputImagePath));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Rotate the image to a specific angle, anticlockwise.
     *
     * <p>note that this method is based on mathematical theories.</p>
     *
     * @param inputImagePath  path of input image
     * @param outputImagePath path of output image
     * @param angle           angle to rotate
     */
    public void rotateImage(String inputImagePath, String outputImagePath, double angle) {
        String outputImagePathR = "src/testdata/ofr1.jpg";
        String outputImagePathG = "src/testdata/ofg1.jpg";
        String outputImagePathB = "src/testdata/ofb1.jpg";


        File file = new File(inputImagePath);
        BufferedImage bi = null;
        BufferedImage boR = null;
        BufferedImage boG = null;
        BufferedImage boB = null;
        try {
            bi = ImageIO.read(file);
            boR = ImageIO.read(file);
            boG = ImageIO.read(file);
            boB = ImageIO.read(file);
        } catch (IOException e) {

            e.printStackTrace();
        }

        // basic information of the image
        int width = bi.getWidth();
        int height = bi.getHeight();
        int minX = bi.getMinX();
        int minY = bi.getMinY();
        Matrix matR = new Matrix(width - minX, height - minY);
        Matrix matG = new Matrix(width - minX, height - minY);
        Matrix matB = new Matrix(width - minX, height - minY);

        for (int y = minY; y < height; y++) {
            for (int x = minX; x < width; x++) {
                int pixel = bi.getRGB(x, y);
                // get r/g/b values of the pixel
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;
                matR.set(x, y, r); //r
                matG.set(x, y, g); //g
                matB.set(x, y, b); //b

                boR.setRGB(x, y, r << 16);
                boG.setRGB(x, y, g << 8);
                boB.setRGB(x, y, b << 0);
            }
        }
        try {
            ImageIO.write(boR, "jpg", new File(outputImagePathR));
            ImageIO.write(boG, "jpg", new File(outputImagePathG));
            ImageIO.write(boB, "jpg", new File(outputImagePathB));
        } catch (IOException e) {
            e.printStackTrace();
        }
        printf("Image r/g/b separated. \n");

        // TODO rotate the image according to the formulas you got
    }

    private static void printf(String message) {
        System.out.print(message);
    }
}
