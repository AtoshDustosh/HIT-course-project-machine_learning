package pca;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;

public class PCAMethod {

    private static final int DefaultPCADim = 3;

    private static final String imageFormat = ".png";

    private static final String inputImage = "src/testdata/pcaImageB2" + imageFormat;
    private static final String inputImageSet = "src/testdata/pcaImageB2_";
    private static final String generatedImage = "src/testdata/generation0" + imageFormat;
    private static final String generatedImageSet = "src/testdata/generation";

    private int pcaDim = 0;
    private Matrix dimReductedMat = null;  // the dim-reducted data matrix
    private Matrix unifiedOriginalMat = null; // unified original data matrix
    private Matrix columnMat = null;   // acquired from unifiedOriginalMat

    private Vector<Integer> meanVec = null;
    private int rowDim = 0;
    private int columnDim = 0;
    private Matrix covarianceMat = null;

    private Vector<Matrix> eigenVectors = new Vector<>();   // store vectors as column matrix
    private Vector<Double> eigenValues = new Vector<>();    // stored in ascending order
    private Matrix pcaEigenMat = null;   // matrix formed by selected #(pcaDim) eigenvectors

    public PCAMethod(Matrix mat, int pcaDim) {
        this.unifiedOriginalMat = mat;
        this.rowDim = mat.getRowDimension();
        this.columnDim = mat.getColumnDimension();
        this.meanVec = this.unifyByRow(this.unifiedOriginalMat);
        this.changeUnifiedMatIntoRowMat();
        this.covarianceMat = this.calculateCovarianceMat(this.columnMat);
        this.pcaDim = pcaDim;
    }

    /**
     * Select eigenvectors corresponding to the #(dim) largest eigenvalues and form a eigenMat.
     *
     * @param dim dimension of PCA matrix
     * @return eigenMat
     */
    private Matrix selectPCAEigenMat(int dim) {
        int vecNum = this.eigenVectors.size();
        Matrix pcaMat = new Matrix(dim, this.eigenValues.size());

        // start from the eigenvector corresponding to the largest eigenvalue
        for (int i = vecNum - 1; i > vecNum - 1 - dim; i--) {
            Matrix eigenVector = this.eigenVectors.get(i);
            System.out.println("... select vector of eigenvalue: " + this.eigenValues.get(i));
            for (int j = 0; j < eigenVector.getRowDimension(); j++) {
                int row = vecNum - 1 - i;
                pcaMat.set(row, j, eigenVector.get(j, 0));
            }
        }
        return pcaMat;
    }

    /**
     * Process the covariance matrix and get corresponding eigenvalues and eigen vectors.
     *
     * @param covarianceMat covariance matrix
     */
    private void eigProcess(Matrix covarianceMat) {
        EigenvalueDecomposition eigVD = covarianceMat.eig();
        Matrix matD = eigVD.getD(); // matrix of eigenvalues
        Matrix matV = eigVD.getV(); // matrix of eigenvectors

        if (matD.getRowDimension() != matD.getColumnDimension() ||
                matV.getRowDimension() != matV.getColumnDimension()) {
            System.out.println("! error: covariance matrix not symmetric. ");
            System.exit(-1);
        }
        // get the eigenvalues and eigenvectors
        for (int i = 0; i < matD.getRowDimension(); i++) {
            double eigenValue = matD.get(i, i);
            double norm = 0;
            Matrix eigenVector = new Matrix(matV.getRowDimension(), 1);
            for (int j = 0; j < matV.getRowDimension(); j++) {
                eigenVector.set(j, 0, matV.get(j, i));
                norm += matV.get(j, i) * matV.get(j, i);
            }
            // unify the eigen vector to make its norm as "1"
            norm = Math.sqrt(norm);
            eigenVector.times(1 / norm);

            this.eigenValues.add(eigenValue);
            this.eigenVectors.add(eigenVector);
        }

//        System.out.println("eigenVector<0>: ");
//        this.eigenVectors.get(0).print(4, 2);

        // print eigenvalues - they are already sorted by ascending order
        System.out.println("Eigenvalues: ");
        for (int i = 0; i < this.eigenValues.size(); i++) {
            System.out.println("(" + i + "):\t" + this.eigenValues.get(i));
        }
    }

    /**
     * Calculate the covariance matrix using input matrix.
     *
     * @param mat input matrix
     * @return covariance matrix of input matrix
     */
    private Matrix calculateCovarianceMat(Matrix mat) {
        double m = mat.getColumnDimension();
        return mat.times(mat.transpose()).times(1 / m);
    }

    /**
     * Unify the matrix by row and get the means of the image's rows.
     *
     * @param mat input matrix
     * @return vector of means
     */
    private Vector<Integer> unifyByRow(Matrix mat) {
        int n = mat.getRowDimension();
        int m = mat.getColumnDimension();
        Vector<Integer> meanVec = new Vector<>();

        for (int i = 0; i < n; i++) {
            int sum = 0;
            for (int j = 0; j < m; j++) {
                sum = sum + (int) mat.get(i, j);
            }
            int mean = sum / m;
            for (int j = 0; j < m; j++) {
                mat.set(i, j, mat.get(i, j) - mean);
            }
            meanVec.add(mean);
//            System.out.println("mean(" + i + "):\t" + mean);
        }

        return meanVec;
    }

    /**
     * Change unified original matrix into a one-column matrix.
     */
    private void changeUnifiedMatIntoRowMat() {
        this.columnMat = new Matrix(this.rowDim * this.columnDim, 1);
        for (int i = 0; i < this.rowDim; i++) {
            for (int j = 0; j < this.columnDim; j++) {
                this.columnMat.set(i * this.columnDim + j, 0, this.unifiedOriginalMat.get(i, j));
            }
        }
    }

    public static void main(String[] args) {
        Dataset dataset = new Dataset();
        PCAMethod pca = null;
        Matrix originalDataMat = null;
        Matrix reconstructedMat = null;
        double PSNR = 0;

        // initialize dataset and PCA method
        dataset.readGrayImage(inputImage);
        /**
         * @Optimal the following line is used for testing PCA with simple data matrix
         */
//        dataset.generateDefaultDataset(7, 2);

        dataset.displayData();

        originalDataMat = dataset.getPixelMatrix();
        pca = new PCAMethod(originalDataMat, DefaultPCADim);

        // execute the pca process
        pca.pcaExecute();

//        pca.getPcaEigenMat().print(4, 2);

        /**
         * If you want to see the effect of rebuild images, de-annotate the following code and
         * annotate the code line that is marked as "@Optimal" above.
         */

        // reconstruct the original image
        originalDataMat = pca.getOriginalMat();
        reconstructedMat = pca.getReconstructedMat();
//        reconstructedMat.print(5, 2);
        pca.outputGrayImage(generatedImage, reconstructedMat);

        PSNR = pca.calculatePSNR(originalDataMat, reconstructedMat);
        System.out.println("PSNR: " + PSNR);

        // use the extracted PCA params to reconstruct the image set generated from this same image
        String inImageSet = inputImageSet;
        String geImageSet = generatedImageSet;
        for(int i = 1; i <= 4; i++){
            inImageSet = inputImageSet + i + imageFormat;
            geImageSet = generatedImageSet + i + imageFormat;

            // read another image and reconstruct it with previous PCA params
            System.out.println("read " + inImageSet + " and reconstruct as " + geImageSet);
            dataset.readGrayImage(inImageSet);
            originalDataMat = dataset.getPixelMatrix();
            reconstructedMat = pca.reconstructAnotherMatrix(originalDataMat);
            pca.outputGrayImage(geImageSet, reconstructedMat);

            PSNR = pca.calculatePSNR(originalDataMat, reconstructedMat);
            System.out.println("PSNR: " + PSNR);
        }
    }

    /**
     * Reconstruct another matrix using the previous extracted PCA.
     *
     * @param mat anthoer matrix
     * @return reconstructed matrix
     */
    public Matrix reconstructAnotherMatrix(Matrix mat) {
        Matrix reconstructedMatrix = null;
        this.unifiedOriginalMat = mat;
        this.rowDim = mat.getRowDimension();
        this.columnDim = mat.getColumnDimension();
        this.meanVec = this.unifyByRow(this.unifiedOriginalMat);
        this.changeUnifiedMatIntoRowMat();

        this.dimReductedMat = this.pcaEigenMat.times(this.columnMat);
        return this.getReconstructedMat();
    }

    /**
     * Get the original pixel matrix of this PCA method.
     *
     * @return original pixel matrix
     */
    public Matrix getOriginalMat() {
        Matrix pixelMat = this.unifiedOriginalMat.copy();
        for (int i = 0; i < pixelMat.getRowDimension(); i++) {
            int meanValue = this.meanVec.get(i);
            for (int j = 0; j < pixelMat.getColumnDimension(); j++) {
                pixelMat.set(i, j, this.unifiedOriginalMat.get(i, j) + meanValue);
            }
        }
        return pixelMat;
    }

    /**
     * Get a copy of pca eigen matrix in this PCA method.
     *
     * @return a copy of pca eigen matrix
     */
    public Matrix getPcaEigenMat(){
        return this.pcaEigenMat.copy();
    }

    /**
     * Get a copy of dim reducted pixel matrix in this PCA method.
     *
     * @return a copy of dim reducted pixel matrix
     */
    public Matrix getDimReductedMat() {
        return this.dimReductedMat.copy();
    }

    /**
     * Get a copy of covariance matrix in this PCA method.
     *
     * @return a copy of covariance matrix
     */
    public Matrix getCovarianceMat() {
        return this.covarianceMat.copy();
    }

    /**
     * Execute the pca process and generate dim-reducted pixel matrix.
     */
    public void pcaExecute() {
        eigProcess(this.covarianceMat);
        this.pcaEigenMat = this.selectPCAEigenMat(this.pcaDim);
        System.out.println("pcaEigenMat row: " + this.pcaEigenMat.getRowDimension()
                + ", pcaEigenMat column: " + this.pcaEigenMat.getColumnDimension());
        System.out.println("unifiedOriginalMat row: " + this.unifiedOriginalMat.getRowDimension()
                + ", unifiedOriginalMat column: " + this.unifiedOriginalMat.getColumnDimension());

        this.dimReductedMat = this.pcaEigenMat.times(this.columnMat);
        System.out.println("Dim reducted mat row: " + this.dimReductedMat.getRowDimension()
                + ", reducted column: " + this.dimReductedMat.getColumnDimension());
    }

    /**
     * Reconstruct the original pixel matrix using dim-reducted pixel matrix.
     *
     * @return reconstructed pixel matrix
     */
    public Matrix getReconstructedMat() {
        Matrix reconstructedRowMat = this.pcaEigenMat.transpose().times(this.dimReductedMat);
        Matrix reconstructedMat = new Matrix(this.rowDim, this.columnDim);

        for (int i = 0; i < this.rowDim; i++) {
            int meanValue = this.meanVec.get(i);
            for (int j = 0; j < this.columnDim; j++) {
                double matValue = reconstructedRowMat.get(i * this.columnDim + j, 0) + meanValue;
                reconstructedMat.set(i, j, matValue);
            }
        }
        return reconstructedMat;
    }

    /**
     * Calculate the PSNR between the original image and the reconstructed image.
     *
     * @param originalImagePixelMatrix pixel matrix of original image
     * @param reconstructedImageMatrix pixel matrix of reconstructed image
     * @return value of PSNR
     */
    public double calculatePSNR(Matrix originalImagePixelMatrix, Matrix reconstructedImageMatrix) {
        Matrix MSEMat = originalImagePixelMatrix.minus(reconstructedImageMatrix);
        int n = MSEMat.getRowDimension();
        int m = MSEMat.getColumnDimension();
        double MSE = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double difference = MSEMat.get(i, j);
                MSE = MSE + difference * difference;
            }
        }
        MSE = MSE / (n * m);
        return Math.log10(255 * 255 / MSE);
    }

    /**
     * Output a pixel matrix of image to a specific path.
     *
     * @param outputImagePath output path
     * @param pixelMat        pixel matrix of an image
     */
    public void outputGrayImage(String outputImagePath, Matrix pixelMat) {
        int height = pixelMat.getRowDimension();
        int width = pixelMat.getColumnDimension();
        File file = new File(outputImagePath);

        System.out.println("Image height: " + height + ", width: " + width);
        System.out.println("#(mean vector): " + this.meanVec.size());

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                int pixel = 0;
                int grayValue = (int) pixelMat.get(x, y);
                pixel = ((grayValue << 16) & 0x00ff0000) | (pixel & 0xff00ffff);
                pixel = ((grayValue << 8) & 0x0000ff00) | (pixel & 0xffff00ff);
                pixel = ((grayValue) & 0x000000ff) | (pixel & 0xffffff00);
//                System.out.println("pixel(" + x + ", " + y + "): " + pixel);
                image.setRGB(y, x, pixel);
            }
        }

        try {
            ImageIO.write(image, "jpg", new File(outputImagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
