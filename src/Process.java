import java.util.concurrent.Semaphore;

public abstract class Process implements Runnable {

    /////////////////////
    // Class Variables //
    /////////////////////
    private final Thread thread;
    private final Semaphore semaphore;
    private Boolean expired_quantum = false;


    /**
     * Constructor
     * Creates new Thread and new Semaphore
     * starts Thread
     */
    public Process() {
        thread = new Thread(this);
        semaphore = new Semaphore(0);
        this.thread.start();
    }


    /**
     * Public Abstract Method
     * Will represent the main of our "Program"
     */
    public abstract void main();


    ////////////////////
    // Public Methods //
    ////////////////////

    /**
     * Sets the boolean indicating that this process' quantum has expired
     */
    public void requestStop() {
        this.expired_quantum = true;
    }

    /**
     * @return a boolean indicating whether the semaphore is 0
     */
    public boolean isStopped() {
        return (semaphore.availablePermits() == 0);
    }

    /**
     * @return true when the Java thread is not alive
     */
    public boolean isDone() {
        return !(thread.isAlive());
    }

    /**
     * releases (increments) the semaphore, allowing this thread to run
     */
    public void start() {
        semaphore.release();
    }

    /**
     * acquires (decrements) the semaphore, stopping this thread from running
     *
     * @throws InterruptedException because we are calling Semaphore.acquire()
     */
    public void stop() throws InterruptedException {
        semaphore.acquire();
    }

    /**
     * acquire the semaphore, then call main
     */
    public void run() {
        try {
            semaphore.acquire();
            this.main();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * if the boolean is true:
     * set the boolean to false and call OS.switchProcess()
     */
    public void cooperate() {
        if (this.expired_quantum) {
            this.expired_quantum = false;
            OS.SwitchProcess();
        }
    }
}