import java.util.Random;

public class RandomDevice implements Device{
    //////////////////////////////
    // Class Instance Variables //
    //////////////////////////////
    Random[] randomDevices = new Random[10];


    ///////////////////////////////
    // Overridden Public Methods //
    ///////////////////////////////
    /**
     * Creates a new Random device and puts it in an empty slot
     * @param s (String) - if not null or empty
     *          assume it is the seed for the Random class
     * @return an int - the id of the device
     */
    @Override
    public int Open(String s) {
        // check that there is an open space in the array
        for (int i = 0; i < 10; i++){
            if (randomDevices[i] == null){
                if (s.isEmpty()){
                    randomDevices[i] = new Random();
                }
                else{
                    randomDevices[i] = new Random(Integer.parseInt(s));
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * Nulls the device entry
     * @param id (int) of the device to close (nullify)
     */
    @Override
    public void Close(int id) {
        randomDevices[id] = null;
    }

    /**
     * Creates/fills an array with random values
     * @param id (int) - index in the array
     * @param size (int) - size of the array to return
     * @return an array of the random values
     */
    @Override
    public byte[] Read(int id, int size) {
        byte[] array = new byte[size];          // create new array of the appropriate size
        Random device = randomDevices[id];      // select the correct random device
        for (int i = 0; i < size; i++){         // for loop to get a number for every index of the new array
            array[i] = (byte) device.nextInt(); // get and place a new int inside the array
        }
        return array;
    }

    /**
     * Reads random bytes, but does not return them
     * @param id (int) - the id of the device to seek
     * @param to (int) - the amount to seek
     */
    @Override
    public void Seek(int id, int to) {
        for (int i = 0; i < to; i++){
            randomDevices[id].nextInt();
        }
    }

    /**
     * @return 0 length and do nothing
     */
    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }
}