import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class that represents a point in a n-dimensional space that has a unique id (record)
 */
class Point implements Serializable {
    private long id;
    private ArrayList<Double> coords;

    /**
     * Constructor for Point class
     * @param id1
     * @param coords1
     */
    public Point(long id1, ArrayList<Double> coords1){
        id=id1;
        coords=coords1;
    }

    /**
     * Given string will be split by the delimiter provided
     * @param pointInString
     */
    public Point(String pointInString){
        String[] stringArray;
        stringArray=pointInString.split(DataFileHandler.getDelimiter());
        int len= stringArray.length;
        if (len != DataFileHandler.getDataDimensions()+1){
            throw new IllegalArgumentException("Wrong dimensions");
        }
        this.id=Long.parseLong(stringArray[0]);
        this.coords=new ArrayList<>();
        for (int i=1; i<len;i++)
            coords.add(Double.parseDouble(stringArray[i]));
    }

    /**
     * Getter for id
     * @return id
     */
    long getId(){
        return id;
    }

    /**
     * Returns the coordinate based on the dimension given as parameter
     * @param dimensions
     * @return the coordinates
     */
    double getCoords(int dimensions){
        return coords.get(dimensions);
    }

    /**
     * method that creates a string based on a record
     * @return the string
     */
    @Override
    public String toString(){
        StringBuilder pointToString= new StringBuilder(id+","+coords.get(0));
        for(int i=1; i<coords.size(); i++){
            pointToString.append(",").append(coords.get(i));
        }
        return String.valueOf(pointToString);
    }
}