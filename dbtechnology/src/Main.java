
public static void main(String[] args) throws IOException
{
    //User defines the dimensions (e.g., "lat", "lon")
    QueryDimensions queryDims = new QueryDimensions("lat", "lon", "postcode");

    //Loading data (e.g., "data.csv", "data2.csv")
    String filePath = "src/dbtechnology/data2.csv";
    List<Point> points = DataParser.parsePointsAutoDetect(filePath, queryDims);
    System.out.println("Total points loaded: " + points.size());

    //R*-Tree creation
    RStarTree tree = new RStarTree(queryDims.size());
    points.forEach(tree::insert);

    //User defines search scope (e.g., "lat", "postcode")
    Map<String, double[]> rangeFilters = new HashMap<>();
    rangeFilters.put("lat", new double[]{40.54, 40.55});
    rangeFilters.put("postcode", new double[]{57000, 57002});

    //R*-Tree search
    long startTimeTree = System.nanoTime();
    List<Point> treeResults = tree.rangeQuery(rangeFilters, queryDims);
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
    System.out.println("\n Performance Comparison ");
    System.out.printf("R*-Tree was %.1fx faster than serial search\n", serialTimeMs / treeTimeMs);
}