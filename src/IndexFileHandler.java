import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class IndexFileHandler {

    private static final int BLOCK_SIZE = 32 * 1024; // 32 KB = 32 * 1024 bytes
    private static final String PATH_TO_FILE = "indexfile.dat";

    public static Node readNode(long blockId) {
        try {
            RandomAccessFile raf = new RandomAccessFile(new File(PATH_TO_FILE), "rw");
            FileInputStream fis = new FileInputStream(raf.getFD());
            BufferedInputStream bis = new BufferedInputStream(fis);

            byte[] node = new byte[BLOCK_SIZE];
            raf.seek(blockId * BLOCK_SIZE);

            int totalRead = 0;
            while (totalRead < BLOCK_SIZE) {
                int bytesRead = bis.read(node, totalRead, BLOCK_SIZE - totalRead);
                if (bytesRead == -1)
                    throw new EOFException("Unexpected end of file while reading Node.");
                totalRead += bytesRead;
            }

            byte[] sizeOfData = DataFileHandler.serialize(new Random().nextInt()); // the amount of data in bytes stored in the node
            System.arraycopy(node, 0, sizeOfData, 0, sizeOfData.length);
            byte[] nodeInBytes = new byte[(Integer)DataFileHandler.deserialize(sizeOfData)];
            System.arraycopy(node, sizeOfData.length, nodeInBytes, 0, nodeInBytes.length);

            return (Node)DataFileHandler.deserialize(nodeInBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
