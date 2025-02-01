import java.time.*;
import java.util.*;

public class Scheduler {

    public enum Priority{realTime, interactive, background}

    //////////////////////////////
    // Class Instance Variables //
    //////////////////////////////
    private final LinkedList<PCB> realTimeProcesses = new LinkedList<>();
    private final LinkedList<PCB> interactiveProcesses = new LinkedList<>();
    private final LinkedList<PCB> backgroundProcesses = new LinkedList<>();
    private final LinkedList<PCB> waitList = new LinkedList<>();
    private final HashMap<Integer, PCB> waitForMessageList = new HashMap<>();
    private final Clock clock = Clock.systemDefaultZone();
    private PCB currentlyRunning;



    /**
     * Constructor
     * Set the timer to execute requestStop() every 250ms
     */
    public Scheduler(){
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                currentlyRunning.up.requestStop();
                currentlyRunning.demotionCount++;
            }
            
        };
        Timer timer = new Timer();
        timer.schedule(task, 250, 250);
    }


    ////////////////////
    // Public Methods //
    ////////////////////
    /**
     * Add the userland process it to the list of interactive processes as default
     * if nothing else is running, call SwitchProcess() to get it started
     * @param pcb (PCB) - PCB to add to list
     * @return the pcb's pid
     */
    public int CreateProcess(PCB pcb){
        this.interactiveProcesses.add(pcb); // add UserlandProcess to the list
        
        if (currentlyRunning == null){      // if nothing else is running
            this.SwitchProcess();           // calls SwitchProcess() to get started
        }
        return pcb.pid;
    }

    /**
     * Add the PCB to the list corresponding to its priority
     * If nothing else is running, call SwitchProcess() to get it started
     * @param pcb (PCB) - PCB to add to list
     * @param priority (Scheduler.Priority) - priority of the PCB
     * @return the pcb's pid
     */
    public int CreateProcess(PCB pcb, Priority priority){
        switch (priority){
            case realTime:
                this.realTimeProcesses.add(pcb);
                break;
            case interactive:
                this.interactiveProcesses.add(pcb);
                break;
            case background:
                this.backgroundProcesses.add(pcb);
                break;
        }
        if (currentlyRunning == null){  // if nothing else is running
            this.SwitchProcess();       // calls SwitchProcess() to get started
        }
        return pcb.pid;
    }

    /**
     * take the currently running process and put it at the end of the list
     * It then takes the head of the list and runs it
     */
    public int SwitchProcess() {
        // iterates through the wait list and reschedule processes that are done waiting
        for (PCB waiting : waitList){
            Instant current = clock.instant();
            if (waiting.timeToWake.isBefore(current)){
                addToList(waiting);
            }
        }

        // check that the currentlyRunning process is not null
        if (this.currentlyRunning != null){
            // check if I need to demote
            if (currentlyRunning.demotionCount > 5){
                // switch case to demote the process properly
                switch(currentlyRunning.priority){
                    case realTime:
                        currentlyRunning.priority = Priority.interactive;
                        break;
                    case interactive:
                        currentlyRunning.priority = Priority.background;
                        break;
                    default:
                        break;
                }
                currentlyRunning.demotionCount = 0;
            }

            // check if it is done
            if (this.currentlyRunning.isDone()){
                // close all of it's processes
                for (int id : currentlyRunning.deviceIDs){
                    if (id != -1){
                        Kernel.vfs.Close(id);
                    }
                }
            }
            else{
                // call helper method to place the currentlyRunning into the correct list
                addToList(this.currentlyRunning);
            }
        }
        // Clear the TLB
        Hardware.TLB[0][0] = -1;
        Hardware.TLB[0][1] = -1;
        Hardware.TLB[1][0] = -1;
        Hardware.TLB[1][1] = -1;

        this.currentlyRunning = this.choosePCB();   // helper method to select the next process to run

        return this.currentlyRunning.pid;           // return the pid
    }

    /**
     * Put the currentlyRunning process to Sleep for the given amount of time
     * Adds the given milliseconds to store the time for when to wake up the process
     * stop this process and run a new process
     *
     * @param milliseconds (int) - the amount of time in milliseconds to sleep for
     */
    public void Sleep(int milliseconds){
        // Use Instant and Duration to get a new Instant value for when to wake
        Instant instant = clock.instant();
        Duration durationToAdd = Duration.ofMillis(milliseconds);
        this.currentlyRunning.timeToWake = instant.plus(durationToAdd);

        this.currentlyRunning.demotionCount = 0;    // reset the demotion count to 0
        this.currentlyRunning.up.requestStop();     // stop the currentlyRunning process
        waitList.add(this.currentlyRunning);        // add currentlyRunning to the waitList
        this.currentlyRunning = this.choosePCB();   // select a new process to run
    }

    /**
     * Stop the currently running process without rescheduling the process
     * And select a new process as the currently running
     */
    public void Exit(){
        this.currentlyRunning.up.requestStop();
        this.currentlyRunning = this.choosePCB();
    }

    /**
     * @return the current process' PID
     */
    public int GetPid(){
        return this.currentlyRunning.pid;
    }

    /**
     * @param name (String) - name of a process
     * @return the PID of a process with that name
     */
    public int GetPidByName(String name){
        // check the currently running process for the name
        if (this.currentlyRunning.name.equals(name)){
            return this.currentlyRunning.pid;
        }
        // check the realTimeProcesses list for the name
        for (PCB pcb : realTimeProcesses){
            if (pcb.name.equals(name)){
                return pcb.pid;
            }
        }
        // check the interactiveProcesses list for the name
        for (PCB pcb : interactiveProcesses){
            if (pcb.name.equals(name)){
                return pcb.pid;
            }
        }
        // check the backgroundProcesses list for the name
        for (PCB pcb : backgroundProcesses){
            if (pcb.name.equals(name)){
                return pcb.pid;
            }
        }
        // check the waitList for the name
        for (PCB pcb : waitList){
            if (pcb.name.equals(name)){
                return pcb.pid;
            }
        }
        // in no process with that name
        return -1;
    }

    /**
     * Method in which a process can send a KernelMessage to another process
     * @param km (KernelMessage) - the message to be sent
     */
    public void SendMessage(KernelMessage km){
        KernelMessage copyKM = new KernelMessage(km);   // use copy constructor to copy the original message
        copyKM.senderPID = this.currentlyRunning.pid;   // populate the sender's pid as currently running's PID
        PCB target = this.findPCB(copyKM.targetPID);    // find target's PCB
        // check that we find the target
        if (target != null){
            target.messages.add(copyKM);    // add message to the message list

            // check if the target is waiting for a message
            if (waitForMessageList.containsValue(target)){
                waitForMessageList.remove(target.pid);  // remove from wait list
                this.addToList(target);                 // add to runnable queues
            }
        }
    }

    /**
     * Method in which this process receives a message or waits to receive a message
     * @return the KernelMessage
     */
    public KernelMessage WaitForMessage(){
        // check if the messages list is empty
        if (this.currentlyRunning.messages.isEmpty()){
            this.currentlyRunning.demotionCount = 0;        // reset the demotion count to 0
            this.currentlyRunning.up.requestStop();         // stop the currentlyRunning process

            // add currentlyRunning to the waitForMessage list
            waitForMessageList.put(this.currentlyRunning.pid, this.currentlyRunning);
            this.currentlyRunning = this.choosePCB();       // select a new process to run

            return null;
        }
        else{
            return this.currentlyRunning.messages.pop();
        }
    }

    /**
     * Method in which we update the TLB in Hardware
     * to contain the mapping for the given virtual page number
     * @param virtualPageNumber (int) - the virtual page number to find the physical address for
     */
    public void GetMapping(int virtualPageNumber){
        // find the physical address within the PCB array
        VirtualToPhysicalMapping vtpm = currentlyRunning.pageTable[virtualPageNumber];
        int physicalPageNumber = vtpm.physicalPageNumber;
        int onDiskPageNumber = vtpm.onDiskPageNumber;

        if (physicalPageNumber == -1){
            // find a physical page in the 'in use' array
            boolean foundPhysicalPage = false;
            for (int i = 0; i < 1024; i++){
                if (!Hardware.inUseMemory[i]){
                    foundPhysicalPage = true;
                    physicalPageNumber = i;
                    break;
                }
            }
            // if we found a physical page, assign it and update the TLB
            if (!foundPhysicalPage){
                // Perform a page swap to free a page
                boolean foundVictimPage = false;            // boolean to run while loop
                PCB victimProcess;                          // variable to hold selected process
                VirtualToPhysicalMapping victimPage = null; // variable to hold the selected page

                while (!foundVictimPage) {
                    victimProcess = this.getRandomProcess();
                    for (VirtualToPhysicalMapping pages : victimProcess.pageTable) {
                        if (pages != null && pages.physicalPageNumber != -1) {
                            foundVictimPage = true;
                            victimPage = pages;
                            break;
                        }
                    }
                }
                // write the victim page to disk
                physicalPageNumber = victimPage.physicalPageNumber;
                byte[] page = new byte[1024];
                System.arraycopy(Hardware.memory, physicalPageNumber * 1024, page, 0, 1024);
                OS.Write(OS.SwapFileID, page);

                // change victim's page values
                victimPage.onDiskPageNumber = OS.onDiskPageCounter++;
                victimPage.physicalPageNumber = -1;

                // check if onDiskPageCounter is not -1, to determine how to change memory
                if (onDiskPageNumber != -1){
                    OS.Seek(OS.SwapFileID, onDiskPageNumber*1024);
                    page = OS.Read(OS.SwapFileID, 1024);
                    System.arraycopy(page, 0, Hardware.memory, physicalPageNumber * 1024, 1024);
                }
                else{
                    for (int i = 0; i < 1024; i++){
                        Hardware.memory[(physicalPageNumber*1024)+i] = 0;
                    }
                }
            }
            vtpm.onDiskPageNumber = -1;
            vtpm.physicalPageNumber = physicalPageNumber;
        }
        this.assignAndUpdateTLB(virtualPageNumber, physicalPageNumber);
    }

    /**
     * @return a random PCB
     */
    public PCB getRandomProcess(){
        // add all processes to a list
        ArrayList<PCB> processes = new ArrayList<>();
        processes.addAll(realTimeProcesses);
        processes.addAll(interactiveProcesses);
        processes.addAll(backgroundProcesses);
        processes.addAll(waitList);

        Random rand = new Random(); // random variable

        // generate a random int bounded by the size of the arrayList and return the corresponding process
        return processes.get(rand.nextInt(processes.size()));
    }

    public void assignAndUpdateTLB(int virtualPageNumber, int physicalPageNumber){
        Random rand = new Random();
        int TLBIndex = ((rand.nextInt() % 2) + 1 ) % 2;
        Hardware.TLB[TLBIndex][0] = virtualPageNumber;
        Hardware.TLB[TLBIndex][1] = physicalPageNumber;
    }


    ///////////////
    // Accessors //
    ///////////////
    /**
     * @return the currently running process
     */
    public PCB getCurrentlyRunning(){
        return this.currentlyRunning;
    }

    /**
     * THIS METHOD IS FOR TESTING PURPOSES ONLY!!!
     *
     * @return a Scheduler.Priority of the currentlyRunning process
     */
    public Priority getPriority(){
        return this.currentlyRunning.priority;
    }


    ///////////////////////////
    // Private Helper Method //
    ///////////////////////////
    /**
     * Private helper method to add a given PCB to the correct scheduler list
     * Uses a switch case to determine the correct list
     *
     * @param toAdd (PCB) - the PCB we need to add to a list
     */
    private void addToList(PCB toAdd){
        switch (toAdd.priority) {
            case realTime:
                realTimeProcesses.add(toAdd);
                break;
            case interactive:
                interactiveProcesses.add(toAdd);
                break;
            case background:
                backgroundProcesses.add(toAdd);
                break;
        }
    }

    /**
     * Private helper method to return the next process to run
     * If realTimeProcesses exist
     *      - run realTime 6/10 times, interactive 3/10 times, and background 1/10 times
     * If interactiveProcesses exist
     *      - run interactive 3/4 times and background 1/4 times
     * Otherwise run background process
     *
     * @return a PCB as the next process to run
     */
    private PCB choosePCB(){
        Random rand = new Random();

        // if there are realtime processes
        if (!realTimeProcesses.isEmpty()){
            // get a random number from 0-9
            int randInt = rand.nextInt(10);

            // if 9 selected and background list isn't empty, return background
            if (randInt == 9 && !backgroundProcesses.isEmpty()){
                return backgroundProcesses.pop();
            }
            // if 6-9 selected and interactive list isn't empty, return interactive
            else if (randInt >= 6 && !interactiveProcesses.isEmpty()){
                return interactiveProcesses.pop();
            }
            // otherwise, return realtime
            else{
                return realTimeProcesses.pop();
            }
        }
        // if there are interactive processes
        else if(!interactiveProcesses.isEmpty()){
            // get a random number from 0-3
            int randInt = rand.nextInt(4);

            // if 3 selected and background list isn't empty, return background
            if (randInt == 3 && !backgroundProcesses.isEmpty()){
                return backgroundProcesses.pop();
            }
            // otherwise, return interactive
            else{
                return interactiveProcesses.pop();
            }
        }
        // otherwise, return a background process
        else{
            return backgroundProcesses.pop();
        }
    }

    /**
     * Private helper method made to iterate through all the lists
     * Looking for the PCB with the targetPID
     * @param targetPID (int) - the PID we are looking for
     * @return the PCB with the PID we're looking for
     */
    private PCB findPCB(int targetPID){
        // check the waitForMessage list for the targetPID
        if (waitForMessageList.containsKey(targetPID)){
            return waitForMessageList.get(targetPID);
        }
        // check the realTimeProcesses list for the targetPID
        for (PCB pcb : realTimeProcesses){
            if (pcb.pid == targetPID){
                return pcb;
            }
        }
        // check the interactiveProcesses list for the targetPID
        for (PCB pcb : interactiveProcesses){
            if (pcb.pid == targetPID){
                return pcb;
            }
        }
        // check the backgroundProcesses list for the targetPID
        for (PCB pcb : backgroundProcesses){
            if (pcb.pid == targetPID){
                return pcb;
            }
        }
        // check the waitList for the targetPID
        for (PCB pcb : waitList){
            if (pcb.pid == targetPID){
                return pcb;
            }
        }
        return null;
    }
}