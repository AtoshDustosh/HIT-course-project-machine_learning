package debug;

import java.util.Vector;

public class VectorClassTest {
    public static void main(String[] args){
        VectorClassTest test = new VectorClassTest();
        test.addTest();
    }

    public void addTest(){
        Vector<Double> vect = new Vector<>();
        vect.add(3.0);
        vect.add(4.0);
        System.out.println(vect);
        vect.add(0,1.0);
        System.out.println(vect);
        vect.add(1,2.0);
        System.out.println(vect);
    }
}
