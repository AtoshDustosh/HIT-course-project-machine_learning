package trial;

import java.util.Random;

public class RandomTest {

    public static void main(String[] args) {
        RandomTest test = new RandomTest();
        test.testRandom();
    }

    public void testRandom() {
        Random rand = new Random();

        System.out.println("Test random - rand.nextInt(border): ");
        for (int i = 0; i < 10; i++) {
            System.out.printf("" + rand.nextInt(3));
        }
        System.out.println();
        for (int i = 0; i < 10; i++) {
            System.out.printf("" + rand.nextInt(10));
        }

        System.out.println("//////////////////");
        System.out.println("Test random - rand.nextDouble(border): ");
        for (int i = 0; i < 10; i++) {
            System.out.println("" + rand.nextDouble() * 3);
        }
        System.out.println("******************");
        for (int i = 0; i < 10; i++) {
            System.out.println("" + rand.nextDouble() * 10);
        }
        System.out.println();
    }
}
