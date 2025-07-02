import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.io.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        // 1. Φόρτωση δεδομένων από CSV
        List<Point> points = loadDataFromCSV("src/data2.csv");
        if (points.isEmpty()) {
            System.err.println("Δεν βρέθηκαν δεδομένα!");
            return;
        }

        // 2. Αρχικοποίηση αρχείων
        DataFileHandler.initializeDataFile(2, true);  // 2D (lat, lon)
        IndexFileHandler.initializeIndexFile(2, true);

        // 3. Κατασκευή δέντρων (2 τρόποι)
        System.out.println("\n=== Κατασκευή R*-tree ===");

        // Κατασκευή με εισαγωγές ένα-προς-ένα
        long startTime = System.nanoTime();
        RStarTree insertTree = new RStarTree();
        points.forEach(insertTree::insert);
        long insertTime = System.nanoTime() - startTime;
        System.out.printf("Διαδοχικές εισαγωγές: %.3f ms\n", insertTime/1e6);

        // Κατασκευή με bulk loading
        startTime = System.nanoTime();
        RStarTree bulkTree = new RStarTree();
        bulkTree.bulkLoad(points);
        long bulkTime = System.nanoTime() - startTime;
        System.out.printf("Μαζική φόρτωση: %.3f ms\n", bulkTime/1e6);

        // 4. Εκτέλεση ερωτημάτων
        executeRangeQueries(bulkTree, points);
        executeKNNQueries(bulkTree, points);
        executeSkylineQueries(bulkTree, points);

        // 5. Διαγραφή και επαλήθευση
        testDeletion(bulkTree, points);
    }

    private static List<Point> loadDataFromCSV(String filePath) {
        try {
            return DataParser.parsePointsAutoDetect(
                    filePath,
                    new QueryDimensions("lat", "lon")
            );
        } catch (IOException e) {
            System.err.println("Σφάλμα ανάγνωσης CSV: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void executeRangeQueries(RStarTree tree, List<Point> points) {
        System.out.println("\n=== Range Queries ===");

        // Ορισμός περιοχών για ερωτήματα
        Rectangle[] testAreas = {
                new Rectangle(new double[]{40.50, 22.90}, new double[]{40.65, 23.10}),  // Μεγάλη περιοχή
                new Rectangle(new double[]{40.54, 23.01}, new double[]{40.56, 23.03}),  // Μικρή περιοχή
                new Rectangle(new double[]{40.52, 22.95}, new double[]{40.60, 23.05})   // Μεσαία περιοχή
        };

        for (Rectangle area : testAreas) {
            // R*-tree
            long start = System.nanoTime();
            List<Point> rtreeResults = new RStarTreeRangeQuery(tree).execute(area);
            long rtreeTime = System.nanoTime() - start;

            // Linear search
            start = System.nanoTime();
            List<Point> linearResults = RangeQuery.execute(points,
                    Map.of(
                            "lat", new double[]{area.getMin()[0], area.getMax()[0]},
                            "lon", new double[]{area.getMin()[1], area.getMax()[1]}
                    ),
                    new QueryDimensions("lat", "lon"));
            long linearTime = System.nanoTime() - start;

            System.out.printf("\nΠεριοχή: %s\n", area);
            System.out.printf("R*-tree: %d σημεία (%.3f ms)\n", rtreeResults.size(), rtreeTime/1e6);
            System.out.printf("Linear: %d σημεία (%.3f ms)\n", linearResults.size(), linearTime/1e6);
        }
    }

    private static void executeKNNQueries(RStarTree tree, List<Point> points) {
        System.out.println("\n=== k-NN Queries ===");
        Point queryPoint = new Point(new double[]{40.55, 23.02});  // Κέντρο Θέρμης

        for (int k : new int[]{1, 5, 10, 20, 50}) {
            // R*-tree (θα πρέπει να υλοποιήσετε αυτό το query)
            long start = System.nanoTime();
            List<Point> knnResults = new NearestNeighboursQuery(queryPoint, k).getQueryRecordIds()
                    .stream()
                    .map(id -> points.stream().filter(p -> p.getId() == id).findFirst().orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            long rtreeTime = System.nanoTime() - start;

            // Linear search
            start = System.nanoTime();
            List<Point> linearResults = new LinearSearchNearestNeighbours(points)
                    .findKNearestNeighbors(queryPoint, k);
            long linearTime = System.nanoTime() - start;

            System.out.printf("\nk=%d:\n", k);
            System.out.printf("R*-tree: %.3f ms | ", rtreeTime/1e6);
            knnResults.forEach(p -> System.out.printf("%s (%.3f km) ",
                    p.getName(), p.distanceTo(queryPoint)));

            System.out.printf("\nLinear: %.3f ms | ", linearTime/1e6);
            linearResults.forEach(p -> System.out.printf("%s (%.3f km) ",
                    p.getName(), p.distanceTo(queryPoint)));
            System.out.println();
        }
    }

    private static void executeSkylineQueries(RStarTree tree, List<Point> points) {
        System.out.println("\n=== Skyline Queries ===");

        // BBS Algorithm (R*-tree)
        long start = System.nanoTime();
        List<Point> bbsSkyline = Skyline_BBS_Query.computeSkyline(tree);
        long bbsTime = System.nanoTime() - start;

        // BNL Algorithm (Linear)
        start = System.nanoTime();
        List<Point> bnlSkyline = Skyline_BNL_Query.computeSkyline(points);
        long bnlTime = System.nanoTime() - start;

        System.out.printf("BBS (R*-tree): %d σημεία (%.3f ms)\n", bbsSkyline.size(), bbsTime/1e6);
        System.out.printf("BNL (Linear): %d σημεία (%.3f ms)\n", bnlSkyline.size(), bnlTime/1e6);

        System.out.println("\nΣημεία κορυφογραμμής:");
        bbsSkyline.forEach(p -> System.out.printf("- %s %s\n",
                p.getName(), Arrays.toString(p.getCoordinates())));
    }

    private static void testDeletion(RStarTree tree, List<Point> points) {
        System.out.println("\n=== Διαγραφή Σημείου ===");
        Point toDelete = points.get(10);  // Διαγραφή του 11ου σημείου
        System.out.println("Διαγραφή: " + toDelete.getName());

        tree.delete(toDelete);

        // Επαλήθευση
        List<Point> results = new RStarTreeRangeQuery(tree).execute(
                new Rectangle(toDelete.getCoordinates(), toDelete.getCoordinates()));

        if (results.isEmpty()) {
            System.out.println("Επιτυχής διαγραφή!");
        } else {
            System.out.println("Αποτυχία διαγραφής. Βρέθηκαν " + results.size() + " σημεία.");
        }
    }
}