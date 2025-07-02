/**
 * Class that is used for linear search.
 */
import java.util.ArrayList;
abstract class Query{
    //Returns the ids of the query's records
    abstract ArrayList<Long> getQueryRecordIds(Node node);

    //Returns the ids of the query's records
    abstract ArrayList<Long> getQueryRecordIds();
}