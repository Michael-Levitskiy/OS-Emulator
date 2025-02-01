/**
 * Contains the Java's main method
 * Uses MemoryTestInit.java to create processes that test the validity of the implemented Memory Methods
 */
public class MemoryTestMain {
    public static void main(String[] args) {

        OS.Startup(new MemoryTestInit());
    }
}