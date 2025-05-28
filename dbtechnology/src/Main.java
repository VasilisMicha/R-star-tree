public static void main(String[] args) throws IOException
{
    //User defines the dimensions (e.g., "lat", "lon")
    QueryDimensions queryDims = new QueryDimensions("lat", "lon", "postcode");

    //Loading data (e.g., "data.csv", "data2.csv")
    String filePath = "src/dbtechnology/data2.csv";
    List<Point> points = DataParser.parsePointsAutoDetect(filePath, queryDims);
    System.out.println("Total points loaded: " + points.size());

    //R*-Tree creation // Αρχικοποίηση του index file
    IndexFileHandler.initializeIndexFile(queryDims.size(), true);
    RStarTree tree = new RStarTree();
    for (Point p : points) { tree.insert(p);}

    //User defines search scope (e.g., "lat", "postcode")
    Map<String, double[]> rangeFilters = new HashMap<>();
    rangeFilters.put("lat", new double[]{40.54, 40.55});
    rangeFilters.put("postcode", new double[]{57000, 57002});

//Range query
    //R*-Tree search
    long startTimeTree = System.nanoTime();
    RStarTreeRangeQuery rangeQuery = new RStarTreeRangeQuery(tree);
    List<Point> treeResults = rangeQuery.execute(rangeFilters, queryDims);
    long endTimeTree = System.nanoTime();
    double treeTimeMs = (endTimeTree - startTimeTree) / 1_000_000.0;

    System.out.println("\n R*-Tree Results ");
    System.out.printf("Found %d points in %.3f ms\n", treeResults.size(), treeTimeMs);
    treeResults.forEach(point -> System.out.println("  - " + point));

    //Serial search
    long startTimeSerial = System.nanoTime();
    List<Point> serialResults = RangeQuery.execute(points, rangeFilters, queryDims);
    long endTimeSerial = System.nanoTime();
    double serialTimeMs = (endTimeSerial - startTimeSerial) / 1_000_000.0;

    System.out.println("\n Serial Search Results ");
    System.out.printf("Found %d points in %.3f ms\n", serialResults.size(), serialTimeMs);
    serialResults.forEach(point -> System.out.println("  - " + point));

    //Performance comparison
  //  System.out.println("\n Performance Comparison ");
 //   System.out.printf("R*-Tree was %.1fx faster than serial search\n", serialTimeMs / treeTimeMs);


//Skyline query
    //R*-Tree search (BBS algorithm)
    long startBBSSkyline = System.nanoTime();
    List<Point> bbsSkyline = Skyline_BBS_Query.computeSkyline(tree);
    long endBBSSkyline = System.nanoTime();
    double timeBBSSkyline = (endBBSSkyline - startBBSSkyline) / 1_000_000.0;
    System.out.println("\n BBS R*-Tree Skyline Results");
    System.out.printf("Found %d skyline points in %.3f ms\n", bbsSkyline.size(), timeBBSSkyline);


    //Serial skyline search (BNL algorithm)
    long startSerialSkyline = System.nanoTime();
    List<Point> serialSkyline = Skyline_BNL_Query.computeSkyline(points);
    long endSerialSkyline = System.nanoTime();
    double timeSerialSkyline = (endSerialSkyline - startSerialSkyline) / 1_000_000.0;
    System.out.println("\n Serial Skyline Results");
    System.out.printf("Found %d skyline points in %.3f ms\n", serialSkyline.size(), timeSerialSkyline);


    //Performance comparison
   // System.out.println("\n Skyline Performance Comparison");
  //  System.out.printf("BBS was %.2fx faster than Serial\n", timeSerialSkyline / timeBBSSkyline);
}