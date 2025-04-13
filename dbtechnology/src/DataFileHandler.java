import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

/**
 * Class that is used for various tasks in the datafile and indexfile such as saving and updating
 * It also stores metadata information (file paths, block sizes and other)
 */
class DataFileHandler{
    private static final int BLOCK_SIZE= 32*1024; // Each Block is 32KB
    static final String PathToDatafile= "datafile.dat";
    private static final String PathToIndexfile= "indexfile.dat";
    private static final String PathToCSV= "data.csv";
    private static final String Delimiter= ","; // The delimiter for csv file
    private static int dataDimensions;
    private static int datafileBlockCounter;
    private static int indexfileBlockCounter;
    private static int rStarTreeHeight;

    static String getPathToCsv(){ return PathToCSV; }
    static String getDelimiter(){ return Delimiter; }
    static int getDataDimensions(){ return dataDimensions; }
    static int getDatafileBlockCounter() { return datafileBlockCounter; }
    static int getIndexfileBlockCounter() { return indexfileBlockCounter; }
    static int getRStarTreeHeight() { return rStarTreeHeight; }

    /**
     * It is used for serializing an Object to byte array
     * @param obj an Object
     * @return a byte array
     * @throws IOException
     */
    private static byte[] serialize(Object obj) throws IOException{
        ByteArrayOutputStream out= new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream= new ObjectOutputStream(out);
        objectOutputStream.writeObject(obj);
        return out.toByteArray();
    }

    /**
     * It is used to deserialize a byte array to an Object
     * @param data a byte array
     * @return an Object
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException{
        ByteArrayInputStream in= new ByteArrayInputStream(data);
        ObjectInputStream objectInputStream= new ObjectInputStream(in);
        return objectInputStream.readObject();
    }

    /**
     * Reads the metadata from the file path
     * @param pathToFile given file path
     * @return an ArrayList<Integer> that includes the values of the metadata file
     */
    private static ArrayList<Integer> readMetaDataBlock(String pathToFile){
        try {
            RandomAccessFile randomAccessFile= new RandomAccessFile(new File(pathToFile), "rw");
            FileInputStream fileInputStream= new FileInputStream(randomAccessFile.getFD());
            BufferedInputStream bufferedInputStream= new BufferedInputStream(fileInputStream);
            byte[] block= new byte[BLOCK_SIZE];
            if (bufferedInputStream.read(block,0,BLOCK_SIZE) != BLOCK_SIZE)
                throw new IllegalStateException("Wrong Block size read");
            byte[] dataSizeBytes= serialize(new Random().nextInt());
            System.arraycopy(block, 0, dataSizeBytes, 0, dataSizeBytes.length);
            byte[] dataInBlock= new byte[(Integer)deserialize(dataSizeBytes)];
            System.arraycopy(block, dataSizeBytes.length, dataInBlock, 0, dataInBlock.length);
            return (ArrayList<Integer>)deserialize(dataInBlock);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Updates the metadata
     * @param pathToFile
     */
    private static void updateMetaDataBlock(String pathToFile) {
        try {
            ArrayList<Integer> dataFileMetaData= new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(BLOCK_SIZE);
            if (pathToFile.equals(PathToDatafile))
                dataFileMetaData.add(++datafileBlockCounter);
            else if (pathToFile.equals(PathToIndexfile)){
                dataFileMetaData.add(++indexfileBlockCounter);
                dataFileMetaData.add(rStarTreeHeight);
            }
            byte[] metaDataInBytes= serialize(dataFileMetaData);
            byte[] dataSizeBytes= serialize(metaDataInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(dataSizeBytes, 0, block, 0, dataSizeBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, dataSizeBytes.length, metaDataInBytes.length);
            RandomAccessFile f = new RandomAccessFile(new File(pathToFile), "rw");
            f.write(block);
            f.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Creates or loads datafile
     * @param dataDimensions
     * @param makeNewDataFile
     */
    static void initializeDataFile(int dataDimensions, boolean makeNewDataFile){
        try{ //if datafile exists
            if (!makeNewDataFile && Files.exists(Paths.get(PathToDatafile))){
                ArrayList<Integer> dataFileMetaData = readMetaDataBlock(PathToDatafile);
                if (dataFileMetaData == null){
                    throw new IllegalStateException("Could not read datafile's Meta Data Block");
                }
                DataFileHandler.dataDimensions=dataFileMetaData.get(0);
                if (DataFileHandler.dataDimensions<= 0){
                    throw new IllegalStateException("The number of data dimensions must be a positive number");
                }
                if (dataFileMetaData.get(1) != BLOCK_SIZE){
                    throw new IllegalStateException("Wrong Βlock size read");
                }
                datafileBlockCounter = dataFileMetaData.get(2);
                if (datafileBlockCounter< 0){
                    throw new IllegalStateException("The total blocks of the datafile must be a positive number");
                }
            } else{ //if datafile does not exist
                Files.deleteIfExists(Paths.get(PathToDatafile));
                DataFileHandler.dataDimensions= dataDimensions;
                if (DataFileHandler.dataDimensions<= 0){
                    throw new IllegalStateException("Data dimensions must be a positive number");
                }
                updateMetaDataBlock(PathToDatafile);
                ArrayList<Point> blockPoints=new ArrayList<>();
                BufferedReader csvReader= (new BufferedReader(new FileReader(PathToCSV)));
                String stringPoint;
                int maxPointsInBlock= calculateMaxPointsInBlock();
                while ((stringPoint=csvReader.readLine()) != null){
                    if (blockPoints.size() == maxPointsInBlock){
                        writeDataFileBlock(blockPoints);
                        blockPoints=new ArrayList<>();
                    }
                    blockPoints.add(new Point(stringPoint));
                }
                csvReader.close();
                if (blockPoints.size()> 0)
                    writeDataFileBlock(blockPoints);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Finds the max number of records a block of Block size can have
     * @return
     */
    private static int calculateMaxPointsInBlock() {
        ArrayList<Point> blockPoints = new ArrayList<>();
        int i;
        for (i=0;i<Integer.MAX_VALUE; i++){
            ArrayList<Double> coordinateForEachDimension= new ArrayList<>();
            for (int d=0;d<DataFileHandler.dataDimensions; d++)
                coordinateForEachDimension.add(0.0);
            Point point=new Point(0, coordinateForEachDimension);
            blockPoints.add(point);
            byte[] pointInBytes= new byte[0];
            byte[] dataSizeBytes= new byte[0];
            try {
                pointInBytes= serialize(blockPoints);
                dataSizeBytes= serialize(pointInBytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (dataSizeBytes.length + pointInBytes.length > BLOCK_SIZE)
                break;
        }
        return i;
    }

    /**
     * Writes/saves the given array of point as a new block of bytes in datafile
     * @param points
     */
    private static void writeDataFileBlock(ArrayList<Point> points){
        try{
            byte[] pointInBytes = serialize(points);
            byte[] dataSizeBytes= serialize(pointInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(dataSizeBytes, 0, block, 0, dataSizeBytes.length);
            System.arraycopy(pointInBytes, 0, block, dataSizeBytes.length, pointInBytes.length);
            FileOutputStream fileOutputStream= new FileOutputStream(PathToDatafile,true);
            BufferedOutputStream bufferedOutputStream= new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(block);
            updateMetaDataBlock(PathToDatafile);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Reads a block from datafile based on the id
     * @param blockId
     * @return
     */
    static ArrayList<Point> readDataFileBlock(int blockId){
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(new File(PathToDatafile), "rw");
            FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            randomAccessFile.seek(blockId*BLOCK_SIZE);
            byte[] block = new byte[BLOCK_SIZE];
            if (bufferedInputStream.read(block,0,BLOCK_SIZE) != BLOCK_SIZE){
                throw new IllegalStateException("Wrong Block size read");
            }
            byte[] dataSizeBytes = serialize(new Random().nextInt());
            System.arraycopy(block, 0, dataSizeBytes, 0, dataSizeBytes.length);
            byte[] pointsInBlock = new byte[(Integer)deserialize(dataSizeBytes)];
            System.arraycopy(block, dataSizeBytes.length, pointsInBlock, 0, pointsInBlock.length);
            return (ArrayList<Point>)deserialize(pointsInBlock);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //Αυτή δεν την καταλαβαίνω γιατι πιο πανω εχουμε και αλλη που κανει το ιδιο
    /*
    static int calculateMaxEntriesInNode(){
        ArrayList<Entry> entries=new ArrayList<>();
        int i;
        for (i=0; i<Integer.MAX_VALUE; i++) {
            ArrayList<Bounds> boundsForEachDimension = new ArrayList<>();
            for (int d = 0; d < DataFileHandler.dataDimensions; d++)
                boundsForEachDimension.add(new Bounds(0.0, 0.0));
            Entry entry = new LeafEntry(new Random().nextLong(),new Random().nextLong(), boundsForEachDimension);
            entry.setChildNodeBlockId(new Random().nextLong());
            entries.add(entry);
            byte[] nodeInBytes = new byte[0];
            byte[] goodPutBytes = new byte[0];
            try {
                nodeInBytes=serialize(new Node(new Random().nextInt(), entries));
                goodPutBytes=serialize(nodeInBytes.length);
            } catch (IOException e){
                e.printStackTrace();
            }
            if (goodPutBytes.length + nodeInBytes.length>BLOCK_SIZE){
                break;
            }
        }
        return i;
    }
    */

    /**
     * Updates block 0 with any change that happened
     */
    private static void updateRStarTreeHeight(){
        try {
            ArrayList<Integer> dataFileMetaData= new ArrayList<>();
            dataFileMetaData.add(dataDimensions);
            dataFileMetaData.add(BLOCK_SIZE);
            dataFileMetaData.add(indexfileBlockCounter);
            dataFileMetaData.add(++rStarTreeHeight);
            byte[] metaDataInBytes = serialize(dataFileMetaData);
            byte[] dataSizeBytes = serialize(metaDataInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(dataSizeBytes, 0, block, 0, dataSizeBytes.length);
            System.arraycopy(metaDataInBytes, 0, block, dataSizeBytes.length, metaDataInBytes.length);
            RandomAccessFile file = new RandomAccessFile(new File(PathToIndexfile), "rw");
            file.write(block);
            file.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Calculates the total blocks in the indexFile and updates it with the data from the csv files
     * @param dataDimensions
     * @param makeNewDataFile
     */
    static void initializeIndexFile(int dataDimensions, boolean makeNewDataFile){
        try{
            //if indexfile exists
            if (!makeNewDataFile && Files.exists(Paths.get(PathToIndexfile))){
                ArrayList<Integer> indexFileMetaData= readMetaDataBlock(PathToIndexfile);
                if (indexFileMetaData == null) {
                    throw new IllegalStateException("Could not read datafile's Meta Data Block");
                }
                DataFileHandler.dataDimensions= indexFileMetaData.get(0);
                if (DataFileHandler.dataDimensions<= 0){
                    throw new IllegalStateException("Data dimensions must be a positive number");
                }
                if (indexFileMetaData.get(1) != BLOCK_SIZE){
                    throw new IllegalStateException("Wrong Block size read");
                }
                indexfileBlockCounter = indexFileMetaData.get(2);
                if (indexfileBlockCounter< 0){
                    throw new IllegalStateException("Total blocks in index file must be a positive number");
                }
                rStarTreeHeight = indexFileMetaData.get(3);
                if (rStarTreeHeight< 0){
                    throw new IllegalStateException("Index's tree height must be a positive number");
                }
            }else{ // if indexfile does not exist
                Files.deleteIfExists(Paths.get(PathToIndexfile));
                DataFileHandler.dataDimensions= dataDimensions;
                rStarTreeHeight = 1;
                if (DataFileHandler.dataDimensions<= 0)
                    throw new IllegalStateException("The number of data dimensions must be a positive integer");
                updateMetaDataBlock(PathToIndexfile);
            }
        }catch(Exception e){e.printStackTrace();}
    }

    /**
     * Writes and saves the given array of pooints and id as a new block of bytes in the indexFile
     * @param node
     */
    static void writeNewIndexFileBlock(Node node) {
        try {
            byte[] nodeInBytes = serialize(node);
            byte[] dataSizeBytes = serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(dataSizeBytes, 0, block, 0, dataSizeBytes.length);
            System.arraycopy(nodeInBytes, 0, block, dataSizeBytes.length, nodeInBytes.length);
            FileOutputStream fileOutputStream = new FileOutputStream(PathToIndexfile,true);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(block);
            updateMetaDataBlock(PathToIndexfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing block in indexfile with new data from a modified R*-tree Node.
     * @param node the new node
     * @param rStarTreeHeight
     */
    static void updateIndexFileBlock(Node node, int rStarTreeHeight) {
        try {
            byte[] nodeInBytes = serialize(node);
            byte[] dataSizeBytes = serialize(nodeInBytes.length);
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(dataSizeBytes, 0, block, 0, dataSizeBytes.length);
            System.arraycopy(nodeInBytes, 0, block, dataSizeBytes.length, nodeInBytes.length);
            RandomAccessFile file = new RandomAccessFile(new File(PathToIndexfile), "rw");
            file.seek(node.getBlockId()*BLOCK_SIZE);
            file.write(block);
            file.close();
            if (node.getBlockId() == RStarTree.getRootNodeBlockId() && DataFileHandler.rStarTreeHeight != rStarTreeHeight){
                updateRStarTreeHeight();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Reads a node from R* tree based on the blockId
     * @param blockId which block a node is from
     * @return the node or null if it doesn't exist
     */
    static Node readIndexFileBlock(long blockId){
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(new File(PathToIndexfile), "rw");
            FileInputStream fileInputStream = new FileInputStream(randomAccessFile.getFD());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            randomAccessFile.seek(blockId*BLOCK_SIZE);
            byte[] block = new byte[BLOCK_SIZE];
            if (bufferedInputStream.read(block,0,BLOCK_SIZE) != BLOCK_SIZE) {
                throw new IllegalStateException("Wrong Block size read");
            }
            byte[] dataSizeBytes= serialize(new Random().nextInt());
            System.arraycopy(block, 0, dataSizeBytes, 0, dataSizeBytes.length);
            byte[] nodeInBytes = new byte[(Integer)deserialize(dataSizeBytes)];
            System.arraycopy(block, dataSizeBytes.length, nodeInBytes, 0, nodeInBytes.length);
            return (Node)deserialize(nodeInBytes);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}