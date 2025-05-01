/**
 * Class that represents the dimensions of interest for spatial queries (e.g., x, y, z).
 * Used to determine which CSV columns constitute the spatial coordinates.
 * @author athinaanv
**/

import java.util.List;

public class QueryDimensions {
    private final List<String> dimensions;

    public QueryDimensions(String...dimensions) {
        this.dimensions = List.of(dimensions);
    }

    public boolean contains(String dimension) {
        return dimensions.contains(dimension);
    }

    public List<String> getDimensions() {
        return dimensions;
    }

    public int size() {
        return dimensions.size();
    }
}