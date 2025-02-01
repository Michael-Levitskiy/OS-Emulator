/**
 * Contains the Java's main method
 * Uses TestInit.java to create processes to test sleeping and promotion of processes
 */
public class TestMain {
    public static void main(String[] args) {

        OS.Startup(new TestInit());
    }
}