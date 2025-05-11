public class Point {
    private final double[] coordinates;

    public Point(double[] coordinates) {
        this.coordinates = coordinates.clone();
    }

    /**
     * Converts this Point to a Rectangle with the same coordinates, forming a zero-area hyperrectangle.
     */
    public Rectangle toRectangle() {
        return new Rectangle(coordinates.clone(), coordinates.clone());
    }

    public double[] getCoordinates() {
        return coordinates.clone();
    }
}
