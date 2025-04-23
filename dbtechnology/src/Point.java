import java.io.Serializable;
import java.util.ArrayList;

/**
 * Class that represents a point in a n-dimensional space that has a unique id (record)
 */
class Point implements Serializable {
    private long id;
    private ArrayList<Double> coords;
    private String name;

    /**
     * Constructor for Point class
     * @param id1
     * @param coords1
     * @param name1
     */
    public Point(long id1, ArrayList<Double> coords1,String name1){
        id=id1;
        coords=coords1;
        name=name1;
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
        this.name=stringArray[1];
        this.coords=new ArrayList<>();
        for (int i=2; i<len;i++)
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
     * Getter for name
     * @return name
     */
    String getName(){
        return name;
    }

    /**
     * method that creates a string based on a record
     * @return the string
     */
    @Override
    public String toString(){
        StringBuilder pointToString= new StringBuilder(id+","+name+","+coords.get(0));
        for(int i=2; i<coords.size(); i++){
            pointToString.append(",").append(coords.get(i));
        }
        return String.valueOf(pointToString);
    }
}