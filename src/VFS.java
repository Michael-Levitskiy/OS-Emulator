/**
 * Virtual File System
 */
public class VFS implements Device{
    //////////////////////////////
    // Class Instance Variables //
    //////////////////////////////
    Device[] devices = new Device[30];
    int[] IDs = new int[30];


    ///////////////////////////////
    // Overridden Public Methods //
    ///////////////////////////////
    /**
     * Reads the given string and determines the device to create
     * @param s (String) - containing the device to create and the rest of the input to that device
     * @return the id of the new device
     */
    @Override
    public int Open(String s) {
        // iterate to find an open location in the array
        for (int i = 0; i < 30; i++){
            if (devices[i] == null){
                // split the input string to determine what type of device to open
                String[] array = s.split(" ", 2);
                switch (array[0]){
                    case "random":
                        devices[i] = new RandomDevice();
                        break;
                    case "file":
                        devices[i] = new FakeFileSystem();
                        break;
                    default:
                        System.err.println("Invalid Device to Open");
                        System.exit(0);
                }
                IDs[i] = devices[i].Open(array[1]); // add the devices internal ID to array
                return i;                           // return the devices ID as it's index in the devices array
            }
        }
        return -1;
    }

    /**
     * Closes a device and nulls it in the array
     * @param id (int) - the ID (index) of the device to close
     */
    @Override
    public void Close(int id) {
        if (devices[id] != null){
            devices[id].Close(IDs[id]);
            devices[id] = null;
        }
    }

    /**
     * Checks that the device exists and calls Read on it
     * @param id (int) - ID of the device
     * @param size (int) - the amount of bytes to read
     * @return a byte array
     */
    @Override
    public byte[] Read(int id, int size) {
        if (devices[id] != null){
            return devices[id].Read(IDs[id], size);
        }
        return null;
    }

    /**
     * Checks that the device exists and calls Seek on it
     * @param id (int) - ID of the device
     * @param to (int) - the amount to offset to
     */
    @Override
    public void Seek(int id, int to) {
        if (devices[id] != null){
            devices[id].Seek(IDs[id], to);
        }
    }

    /**
     * Checks that the device exists and calls Write on it
     * @param id (int) - ID of the device
     * @param data (byte[]) - the data to write
     * @return an int
     */
    @Override
    public int Write(int id, byte[] data) {
        if (devices[id] != null){
            return devices[id].Write(IDs[id], data);
        }
        return -1;
    }
}