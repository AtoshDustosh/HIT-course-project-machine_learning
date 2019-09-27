package defaultData;

/**
 * Some constants used in this program.
 */
public enum MyNumbers {

    num_chartPoints(100),  // number of points on original function
    index_originalFunc(0),  // index of original function

    num_trainPoints(100),    // number of training points
    index_trainPoints(1);   // index of training points

    private final double value; // value of the constant

    /**
     * Constructor.
     *
     * @param value
     */
    private MyNumbers(double value) {
        this.value = value;
    }

    /**
     * Get value of this enum constant.
     *
     * @return double value
     */
    public double getValue() {
        return this.value;
    }
}