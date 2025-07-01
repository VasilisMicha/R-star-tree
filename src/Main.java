import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        DataFileHandler.initializeDataFile(2, true);
        long maxId = DataFileHandler.getMaxBlockId();
        for (int i=0; i<maxId; i++){
            ArrayList<Point> points = DataFileHandler.readDataFileBlock(i+1);
            for (Point point : points){
                System.out.println(point.getBlockId()+" ."+point.getName());
            }

        }
    }
}
