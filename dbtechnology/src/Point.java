class Point {
    private int dimensions;
    private final double[] coordinates;

    public Point(int dimensions, double[] coordinates){
        this.dimensions = dimensions;
        this.coordinates = coordinates.clone();
    }

    public static int getSize(){
        dimensions = 2;
        return (Integer.SIZE / 8)  + dimensions * (Double.SIZE / 8);
    }
}