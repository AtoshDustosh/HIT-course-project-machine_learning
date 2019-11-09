package defaultargs;

/**
 * Some arguments/parameters for K-means and GMM algorithms.
 */
public enum MyArg {
    valueK(5),       // value K for K-means method
    setNum(5),  // number of sets generated in a GMMDataset
    borderSize(10),  // size of border for the x-y coordinates
    pointsPerSet(300),  // #(points) per set (cluster)
    ;

    // integer value of this arg
    private int value = 0;

    /**
     * Construct with an integer value.
     *
     * @param value value of arg/param
     */
    MyArg(int value){
        this.value = value;
    }

    /**
     * Get the value of this argument/parameter.
     *
     * @return value of arg/param
     */
    public int value(){
        return this.value;
    }
}
