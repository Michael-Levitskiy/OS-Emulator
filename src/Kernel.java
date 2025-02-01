public class Kernel extends Process implements Device{

    /////////////////////////////
    // Class Instance Variable //
    /////////////////////////////
    private final Scheduler scheduler = new Scheduler();
    public static VFS vfs = new VFS();


    /**
     * Switch on OS.CurrentCall - for each of these, call the function that implements them
     * call start() on scheduler.CurrentlyRunning
     * call stop() on myself, so that only one process is running
     */
    @Override
    public void main() {
        while (true) {
            // switch case for the CallType
            switch (OS.currentCall) {
                case CreateProcess:
                    PCB pcb;

                    // determine which CreateProcess() to use by the number of parameters
                    switch (OS.parameters.size()) {
                        case 1:
                            pcb = new PCB((UserlandProcess) OS.parameters.get(0));
                            OS.returnValue = scheduler.CreateProcess(pcb);
                            break;
                        case 2:
                            pcb = new PCB((UserlandProcess) OS.parameters.get(0), (Scheduler.Priority) OS.parameters.get(1));
                            OS.returnValue = scheduler.CreateProcess(pcb, (Scheduler.Priority) OS.parameters.get(1));
                            break;
                    }
                    break;
                case SwitchProcess:
                    OS.returnValue = scheduler.SwitchProcess();
                    break;
                case Sleep:
                    scheduler.Sleep((int) OS.parameters.get(0));
                    break;
                case Exit:
                    this.Exit();
                    break;
                case Open:
                    OS.returnValue = this.Open((String) OS.parameters.get(0));
                    break;
                case Close:
                    this.Close((int) OS.parameters.get(0));
                    break;
                case Read:
                    OS.returnValue = this.Read((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    break;
                case Seek:
                    this.Seek((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    break;
                case Write:
                    OS.returnValue = this.Write((int) OS.parameters.get(0), (byte[]) OS.parameters.get(1));
                    break;
                case GetPid:
                    OS.returnValue = scheduler.GetPid();
                    break;
                case GetPidByName:
                    OS.returnValue = scheduler.GetPidByName((String) OS.parameters.get(0));
                    break;
                case SendMessage:
                    scheduler.SendMessage((KernelMessage) OS.parameters.get(0));
                    break;
                case WaitForMessage:
                    OS.returnValue = scheduler.WaitForMessage();
                    break;
                case GetMapping:
                    scheduler.GetMapping((int) OS.parameters.get(0));
                    break;
                case AllocateMemory:
                    OS.returnValue = this.AllocateMemory((int) OS.parameters.get(0));
                    break;
                case FreeMemory:
                    OS.returnValue = this.FreeMemory((int) OS.parameters.get(0), (int) OS.parameters.get(1));
                    break;
                default:
                    System.err.println("Invalid Kernel Call");
                    System.exit(0);
            }
            this.scheduler.getCurrentlyRunning().start();

            try {
                this.stop();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     *
     * @param s (String) to pass into the device Open method
     * @return the id of the device
     */
    @Override
    public int Open(String s) {
        PCB currentlyRunning = scheduler.getCurrentlyRunning();
        // iterate through the currently running device
        for (int i = 0; i < 10; i++){
            // if we find an open slot
            if (currentlyRunning.deviceIDs[i] == -1){
                int id = vfs.Open(s);   // call vfs.open
                if (id == -1){          // if result is -1, fail
                    return -1;
                }
                // set the id in the PCB's array and return the id
                currentlyRunning.deviceIDs[i] = id;
                return id;
            }
        }
        return -1;
    }

    /**
     * Calls the vfs method Close as well as
     * @param id (int) - the ID of the device to close
     */
    @Override
    public void Close(int id) {
        vfs.Close(id);
        scheduler.getCurrentlyRunning().deviceIDs[id] = -1;
    }

    /**
     * Calls the VFS method Read
     * @param id (int) - id of the device
     * @param size (int) - number of bytes to return
     * @return what the VFS method returns
     */
    @Override
    public byte[] Read(int id, int size) {
        return vfs.Read(id, size);
    }

    /**
     * Calls the VFS method Seek
     * @param id (int) - id of the device
     * @param to (int) - value to seek to
     */
    @Override
    public void Seek(int id, int to) {
        vfs.Seek(id, to);
    }

    /**
     * Calls the VFS method Write
     * @param id (int) - id of the device
     * @param data (byte[]) - data to write
     * @return what the vfs method returns
     */
    @Override
    public int Write(int id, byte[] data) {
        return vfs.Write(id, data);
    }


    /**
     * Method for the OS.Exit call
     * Before calling the scheduler method, free memory space in the inUseMemory array
     */
    public void Exit(){
        PCB currentProcess = scheduler.getCurrentlyRunning();
        // free memory used by process
        for (int i = 0; i < 100; i++){
            VirtualToPhysicalMapping vpm = currentProcess.pageTable[i];
            if (vpm != null && vpm.physicalPageNumber != -1){
                Hardware.inUseMemory[vpm.physicalPageNumber] = false;
            }
        }
        scheduler.Exit();
    }

    /**
     * Segments off the correct amount of pages to be used for this process
     * @param size (int) - the number of bytes to allocate for
     * @return the start virtual address
     */
    private int AllocateMemory(int size){
        // check that size is a multiple of 1024
        if (size % 1024 != 0 || size > 1024*1024) {return -1;}

        int numPages = size / 1024;     // get the number of pages we need to allocate for

        // get the currentlyRunning PCB and starting pointer within the PCB
        PCB currentProcess = this.scheduler.getCurrentlyRunning();
        int beginningPointer = currentProcess.findBlock(numPages);

        // loop to initialize the VirtualToPhysicalMapping variables
        for (int i = 0; i < numPages; i++){
            VirtualToPhysicalMapping vtpm = new VirtualToPhysicalMapping(); // initialize variable
            currentProcess.pageTable[beginningPointer + i] = vtpm;          // place vtpm in the page table
        }
        return beginningPointer * 1024;
    }

    /**
     * Frees pages from memory from being used by a specific process
     * @param pointer (int) - beginning pointer of memory to free
     * @param size (int) - to determine the number of pages to free
     * @return a boolean to show if the operation was successful
     */
    private boolean FreeMemory(int pointer, int size){
        // check that pointer and size are multiples of 1024 and that they don't exceed the physical memory
        if (pointer % 1024 != 0 || size % 1024 != 0 || size > 1024*1024 || pointer > 1024*1024 || size < 0 || pointer < 0){
            return false;
        }

        int index = pointer/1024;
        int numPages = size/1024;
        PCB currentProcess = this.scheduler.getCurrentlyRunning();

        // loop for the pages of memory to free
        for (int i = 0; i < numPages; i++){
            VirtualToPhysicalMapping vtpm = currentProcess.pageTable[index+i];

            // check if the vtpm has claimed a space in physical memory
            if (vtpm.physicalPageNumber != -1) {
                Hardware.inUseMemory[vtpm.physicalPageNumber] = false;
            }
            currentProcess.pageTable[index+i] = null;
        }
        return true;
    }

    /**
     * Accessor
     *
     * @return the Scheduler
     */
    public Scheduler getScheduler() { return scheduler; }
}