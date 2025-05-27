import java.util.ArrayList;

/**
 * Class that is used for linear search
 */
abstract class Query{
    // Returns the ids of the query's records
    abstract ArrayList<Long> getResultIds();

    // Returns the ids of the query's records
    abstract ArrayList<Long> getResultIds(Node node);
}