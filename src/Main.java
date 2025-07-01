import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        DataFileHandler.initializeDataFile(2, true);
        System.out.println(DataFileHandler.calculateMaxPointsInBlock());
//        ArrayList<Point> points = DataFileHandler.readDataFileBlock(6);
//        for (Point point : points){
//            System.out.println(point.getBlockId()+". name: "+point.getName());
//        }


        long lastBlockId = DataFileHandler.getMaxBlockId();
        System.out.println(lastBlockId);
        ArrayList<Point> points = DataFileHandler.readDataFileBlock(1);
        for (Point point : points){
            System.out.println(point.getBlockId()+". name: "+point.getName());
        }
    }
}