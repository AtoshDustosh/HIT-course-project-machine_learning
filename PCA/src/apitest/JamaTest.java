package apitest;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class JamaTest {

    private Matrix A;
    private Matrix B;

    public JamaTest(Matrix A, Matrix B) {
        this.A = A;
        this.B = B;
    }

    public static void main(String[] args) {
        double[][] array1 = {
                {-1, 1, 0},
                {-4, 3, 0},
                {1, 0, 2}};
        double[][] array2 = {
                {1, 1, 0},
                {1, 3, 0}};
        Matrix A = new Matrix(array1);
        Matrix B = new Matrix(array2);

        System.out.println("A: " + A.getRowDimension() + ", B: " + B.getRowDimension());

        JamaTest jama = new JamaTest(A, B);
//        jama.testPlus(A, B);
//        jama.testMinus(A, B);
//        jama.testTimes(A, B.transpose());
//        jama.testTranspose(A);
//        jama.testInverse(A);
        jama.testEig(A);

    }

    public void testEig(Matrix matA){
        EigenvalueDecomposition eigVD = matA.eig();
        Matrix matD = eigVD.getD();
        Matrix matV = eigVD.getV();
        matD.print(4,3);
        matV.print(4,3);
    }

    public void testPlus(Matrix A, Matrix B) {
        System.out.println("Plus test: ");
        Matrix C = A.plus(B);
        C.print(3, 2);
    }

    public void testMinus(Matrix A, Matrix B) {
        System.out.println("Minus test: ");
        Matrix C = A.minus(B);
        C.print(3, 2);
    }

    public void testTimes(Matrix A, Matrix B) {
        System.out.println("Times test: ");
        Matrix C = A.times(B);
        C.print(4, 2);
    }

    public void testTranspose(Matrix A) {
        System.out.println("Transpose test: ");
        Matrix C = A.transpose();
        C.print(3, 2);
    }

    public void testInverse(Matrix A) {
        System.out.println("Inverse test: ");
        Matrix C = A.inverse();
        C.print(3, 2);
    }

}
