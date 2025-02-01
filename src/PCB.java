import java.time.*;
import java.util.LinkedList;

/**
 * Process Control Block
 */
public class PCB {
    //////////////////////////////
    // Class Instance Variables //
    //////////////////////////////
    public static int nextPID = 1;
    public final int pid;
    public final String name;
    public UserlandProcess up;
    public Scheduler.Priority priority =  Scheduler.Priority.interactive;
    public int demotionCount = 0;
    public Instant timeToWake;
    public int[] deviceIDs;
    public LinkedList<KernelMessage> messages = new LinkedList<>();
    public VirtualToPhysicalMapping[] pageTable;


    //////////////////
    // Constructors //
    //////////////////
    /**
     * Constructor
     * Stores Userland Process, sets pid, and increments nextPID
     *
     * @param up (UserlandProcess)
     */
    public PCB(UserlandProcess up) {
        this.up = up;
        this.name = up.getClass().getSimpleName();
        this.pid = PCB.nextPID;
        PCB.nextPID++;
        deviceIDs = new int[10];
        for (int i = 0; i < 10; i++){
            deviceIDs[i] = -1;
        }
        pageTable = new VirtualToPhysicalMapping[100];
    }

    /**
     * Constructor
     * Sets Priority, stores UserlandProcess, sets pid, and increments nextPID
     *
     * @param up (UserlandProcess)
     * @param priority (Scheduler.Priority)
     */
    public PCB(UserlandProcess up, Scheduler.Priority priority){
        this.priority = priority;
        this.up = up;
        this.name = up.getClass().getSimpleName();
        this.pid = PCB.nextPID;
        PCB.nextPID++;
        this.deviceIDs = new int[10];
        for (int i = 0; i < 10; i++){
            this.deviceIDs[i] = -1;
        }
        pageTable = new VirtualToPhysicalMapping[100];
    }


    ////////////////////
    // Public Methods //
    ////////////////////
    /**
     * Calls UserlandProcess' stop.
     * Loops with Thread.sleep() until up.isStopped() is true
     */
    public void stop() {
        try {
            this.up.stop();
            while (!up.isStopped()) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * calls UserlandProcess' isDone()
     *
     * @return a boolean indicating whether the process is done
     */
    public boolean isDone() {
        return this.up.isDone();
    }

    /**
     * calls UserlandProcess' start()
     */
    public void start() {
        this.up.start();
    }


    /**
     * Helper method to find an open block of memory of a certain number of pages
     * @param numPages (int) - the number of pages for the block
     * @return and int of the index in the array
     */
    public int findBlock(int numPages){
        for (int i = 0; i < 100; i++){
            boolean availableBlock = true;
            for (int j = 0; j < numPages; j++){
                if (pageTable[i+j] != null){
                    availableBlock = false;
                    break;
                }
            }
            if (availableBlock){
                return i;
            }
        }
        return -1;
    }


    /**
     * Overridden toString() method to use for printing and testing
     * @return a String showing the PID and Userland Process
     */
    @Override
    public String toString(){
        return "PCB: (" + this.pid + ") " + this.up;
    }
}