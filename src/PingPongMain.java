/**
 * Contains the Java's main method
 * Uses PingPongInit.java to create PingProcess and PongProcess
 */
public class PingPongMain {
    public static void main(String[] args) {

        OS.Startup(new PingPongInit());
    }
}