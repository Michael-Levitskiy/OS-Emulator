import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * This Class is a Device
 * All of the required methods are Implemented
 * Processes can create a Fake File System Device
 */
public class FakeFileSystem implements Device {
    /////////////////////////////
    // Class Instance Variable //
    /////////////////////////////
    RandomAccessFile[] randomAccessFiles = new RandomAccessFile[10];


    ///////////////////////////////
    // Overridden Public Methods //
    ///////////////////////////////
    /**
     * Creates new random access file
     * @param s (String) - name for our random access file
     * @return the id of the random access file
     */
    @Override
    public int Open(String s) {
        // verify that the string provided is not empty
        if (s.isEmpty()){
            try {
                throw new Exception("No file name provided");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        for (int i = 0; i < 10; i++){
            if (randomAccessFiles[i] == null){
                try {
                    randomAccessFiles[i] = new RandomAccessFile(s, "rw");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * CLoses the file system and nulls the location in the array
     * @param id (int) - the ID of the random access file
     */
    @Override
    public void Close(int id) {
        try {
            randomAccessFiles[id].close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        randomAccessFiles[id] = null;
    }

    /**
     * Reads bytes from a file and returns an array
     * @param id (int) - the ID of the file to read
     * @param size (int) - the size of the byte[] to return
     * @return a byte array containing the read information
     */
    @Override
    public byte[] Read(int id, int size) {
        byte[] toReturn = new byte[size];
        try {
            randomAccessFiles[id].read(toReturn);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return toReturn;
    }

    /**
     * Sets the file-pointer offset of the given file
     * @param id (int) - id of the file
     * @param to (int) - the amount to offset to
     */
    @Override
    public void Seek(int id, int to) {
        try {
            randomAccessFiles[id].seek(to);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the given data to the correct file
     * @param id (int) - id of the file
     * @param data (byte[]) - the data to write to the file
     * @return 0, indicating a successful operation
     */
    @Override
    public int Write(int id, byte[] data) {
        try {
            randomAccessFiles[id].write(data);
            return 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}