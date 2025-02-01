/**
 * This Userland Process runs the OS.Sleep() method, preventing process demotion
 */
public class TestSleepProcess extends UserlandProcess {

    @Override
    public void main() {
        Scheduler.Priority priority = OS.getPriority();

        while(true){
            Scheduler.Priority currentPriority = OS.getPriority();

            System.out.println("Sleep Process originally set as " + priority + " is now " + currentPriority);
            OS.Sleep(1000);
            this.cooperate();
        }
    }
}