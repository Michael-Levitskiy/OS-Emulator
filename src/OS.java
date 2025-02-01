import java.util.ArrayList;

/**
 * Operating System
 */
public class OS {

    public enum CallType {CreateProcess, SwitchProcess, Sleep, Exit, Open, Close, Read, Seek, Write,
                            GetPid, GetPidByName, SendMessage, WaitForMessage, GetMapping, AllocateMemory, FreeMemory}

    //////////////////////////////
    // Class Instance Variables //
    //////////////////////////////
    public static CallType currentCall;
    public static ArrayList<Object> parameters = new ArrayList<>();
    public static Object returnValue;
    private static Kernel kernel;
    public static int SwapFileID;
    public static int onDiskPageCounter;


    ///////////////////////////
    // Public Static Methods //
    ///////////////////////////
    /**
     * Create the Kernel()
     * Calls CreateProcess twice - once for "init" and once for the idle process
     *
     * @param init (UserlandProcess) - the first process to create
     */
    public static void Startup(UserlandProcess init) {
        OS.kernel = new Kernel();                   // initialize the kernel

        FakeFileSystem ffs = new FakeFileSystem();  // create FakeFileSystem
        OS.SwapFileID = ffs.Open("SwapFile");   // Open the Swap File and store the ID
        OS.onDiskPageCounter = -1;                  // Set page number as the next page to write out

        OS.CreateProcess(init);                     // create the provided init process
        OS.CreateProcess(new IdleProcess());        // create an idle process
    }

    /**
     * Communicate with the Kernel to perform CreateProcess()
     *
     * @param up (UserlandProcess) - the UserlandProcess to create
     * @return the pid of the created process
     */
    public static int CreateProcess(UserlandProcess up) {
        OS.parameters.clear();                              // reset the parameters
        OS.parameters.add(up);                              // add new parameters to the parameter list
        OS.currentCall = CallType.CreateProcess;            // set the currentCall
        kernelCall();
        return (int) OS.returnValue;                        // Cast and return the return value
    }

    /**
     * Communicate with the Kernel to perform CreateProcess()
     *
     * @param up       (UserlandProcess) - the UserlandProcess to create
     * @param priority (Scheduler.Priority) - priority to give the process
     * @return the pid of the created process
     */
    public static int CreateProcess(UserlandProcess up, Scheduler.Priority priority) {
        OS.parameters.clear();                      // reset the parameters
        OS.parameters.add(up);                      // add new parameters to the parameter list
        OS.parameters.add(priority);
        OS.currentCall = CallType.CreateProcess;    // set the currentCall
        kernelCall();
        return (int) OS.returnValue;                // Cast and return the return value
    }

    /**
     * Communicate with the Kernel to perform SwitchProcess()
     *
     * @return the pid of the process we switched to
     */
    public static int SwitchProcess() {
        OS.parameters.clear();                      // reset the parameters
        // no new parameters needed to be added to the parameter list
        OS.currentCall = CallType.SwitchProcess;    // set the currentCall
        kernelCall();
        if (OS.returnValue instanceof Integer) {return (int) OS.returnValue;}// Cast and return the return value
        return OS.GetPid();
    }

    /**
     * Communicate with the Kernel to perform Sleep()
     *
     * @param milliseconds (int) - the amount of time in milliseconds to sleep for
     */
    public static void Sleep(int milliseconds) {
        OS.parameters.clear();              // reset the parameters
        OS.parameters.add(milliseconds);    // add milliseconds to the parameters list
        OS.currentCall = CallType.Sleep;    // set the currentCall to Sleep
        kernelCall();                       // call helper method
    }

    /**
     * Communicate with the Kernel to perform Exit()
     */
    public static void Exit() {
        OS.parameters.clear();              // reset the parameters
        // no parameters needed to be added
        OS.currentCall = CallType.Exit;     // set the currentCall to exit
        kernelCall();                       // call helper method
    }

    /**
     * Communicate with the kernel to perform Open(String)
     * @param s (String) - to be passed as a parameter
     * @return an int representing the device ID
     */
    public static int Open(String s) {
        OS.parameters.clear();              // reset the parameters
        OS.parameters.add(s);               // add the string to the parameters list
        OS.currentCall = CallType.Open;     // set the currentCall to Open
        kernelCall();                       // call helper method
        return (int) OS.returnValue;        // Cast and return the value
    }

    /**
     * Communicates with the kernel to perform Close(int)
     * @param id (int) - id of a device
     */
    public static void Close(int id) {
        OS.parameters.clear();              // reset the parameters
        OS.parameters.add(id);              // add the ID to the parameters list
        OS.currentCall = CallType.Close;    // set the currentCall to Close
        kernelCall();                       // call helper method
    }

    /**
     * Communicates with the kernel to perform Read(int, int)
     * @param id (int) - ID of a device
     * @param size (int) - size of the array to return
     * @return a byte[] of the read values
     */
    public static byte[] Read(int id, int size) {
        OS.parameters.clear();              // reset the parameters
        OS.parameters.add(id);              // add the parameters
        OS.parameters.add(size);
        OS.currentCall = CallType.Read;     // set the currentCall to Read
        kernelCall();                       // call helper method
        return (byte[]) OS.returnValue;     // cast and return the value
    }

    /**
     * Communicates with the kernel to perform Seek(int, int)
     * @param id (int) - ID of the device
     * @param to (int) - value to seek to
     */
    public static void Seek(int id, int to) {
        OS.parameters.clear();              // reset the parameters
        OS.parameters.add(id);              // add the parameters
        OS.parameters.add(to);
        OS.currentCall = CallType.Seek;     // set the currentCall to Seek
        kernelCall();                       // call helper method
    }

    /**
     * Communicates with the kernel to perform Write(int, byte[])
     * @param id (int) - ID of the device
     * @param data (byte[]) - data to be written
     * @return an int
     */
    public static int Write(int id, byte[] data) {
        OS.parameters.clear();              // reset the parameters
        OS.parameters.add(id);              // add the parameters
        OS.parameters.add(data);
        OS.currentCall = CallType.Write;    // set the currentCall to Write
        kernelCall();                       // call helper method
        return (int) OS.returnValue;        // cast and return the value
    }

    /**
     * Communicates with the kernel to perform GetPid()
     * @return the PID of the current process
     */
    public static int GetPid() {
        OS.parameters.clear();              // reset the parameters
        // no parameters needed to be added
        OS.currentCall = CallType.GetPid;   // set the currentCall to GetPid
        kernelCall();                       // call helper method
        return (int) OS.returnValue;        // cast and return the value
    }

    /**
     * Communicates with the kernel to perform GetPidByName(String)
     * @param name (String) - name of a process
     * @return the PID of the process with that name
     */
    public static int GetPidByName(String name) {
        OS.parameters.clear();                  // reset the parameters
        OS.parameters.add(name);                // add the parameters
        OS.currentCall = CallType.GetPidByName; // set the currentCall to GetPidByName
        kernelCall();                           // call helper method
        return (int) OS.returnValue;            // cast and return the value
    }

    /**
     * Communicates with the kernel to perform SendMessage(KernelMessage)
     * @param km (KernelMessage) - message that needs to be sent
     */
    public static void SendMessage(KernelMessage km){
        OS.parameters.clear();                  // reset the parameters
        OS.parameters.add(km);                  // add the parameters
        OS.currentCall = CallType.SendMessage;  // set the currentCall SendMessage
        kernelCall();                           // call helper method
    }

    /**
     * Communicates with the kernel to perform WaitForMessage()
     * @return the KernelMessage to be received
     */
    public static KernelMessage WaitForMessage(){
        OS.parameters.clear();                      // reset the parameters
        // no parameters needed to be added
        OS.currentCall = CallType.WaitForMessage;   // set the currentCall to WaitForMessage
        kernelCall();                               // call helper method
        if (OS.returnValue.getClass() != KernelMessage.class) {return null;}
        else{
            return (KernelMessage) OS.returnValue;}      // cast and return the value
    }

    /**
     * Communicates with kernel to perform GetMapping(int)
     * @param virtualPageNumber (int) - page number we need to map for
     */
    public static void GetMapping(int virtualPageNumber){
        OS.parameters.clear();                  // reset the parameters
        OS.parameters.add(virtualPageNumber);   // add the parameters
        OS.currentCall = CallType.GetMapping;   // set the currentCall SendMessage
        kernelCall();                           // call helper method
    }

    /**
     * Communicates with the kernel to perform AllocateMemory(int)
     * @param size (int) - the size of the memory to allocate
     * @return the start virtual address
     */
    public static int AllocateMemory(int size){
        OS.parameters.clear();                      // reset the parameters
        OS.parameters.add(size);                    // add the parameters
        OS.currentCall = CallType.AllocateMemory;   // set the currentCall to AllocateMemory
        kernelCall();                               // call helper method
        return (int) OS.returnValue;                // cast and return the value
    }

    /**
     * Communicates with the kernel to perform FreeMemory(int, int)
     * @param pointer (int) - the starting address to free
     * @param size (int) - amount of memory to free
     * @return if the operation was successful
     */
    public static boolean FreeMemory(int pointer, int size){
        OS.parameters.clear();                  // reset the parameters
        OS.parameters.add(pointer);             // add the parameters
        OS.parameters.add(size);
        OS.currentCall = CallType.FreeMemory;   // set the currentCall to FreeMemory
        kernelCall();                           // call helper method
        return (boolean) OS.returnValue;        // cast and return the value
    }



    /**
     * THIS METHOD IS FOR TESTING PURPOSES ONLY!!!
     * The only process allowed to call this method is Test Processes
     *
     * @return the priority of the currently running process
     */
    public static Scheduler.Priority getPriority(){
        if (kernel.getScheduler().getCurrentlyRunning().up.getClass() == TestDemotionProcess.class
        || kernel.getScheduler().getCurrentlyRunning().up.getClass() == TestSleepProcess.class){
            return kernel.getScheduler().getPriority();
        }
        else{
            System.err.println("Invalid Userland Process attempted to access Kernel Land data");
            System.exit(0);
            return null;
        }
    }



    /////////////////////
    // Private Methods //
    /////////////////////
    /**
     * Private helper method to start the kernel and stop the current process
     */
    private static void kernelCall() {
        Scheduler scheduler = kernel.getScheduler();    // get the scheduler from the kernel
        kernel.start();                                 // switch to the kernel

        // while loop to wait until a process is running before stopping it
        while (scheduler.getCurrentlyRunning() == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        scheduler.getCurrentlyRunning().stop();
    }
}