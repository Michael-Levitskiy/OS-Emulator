/**
 * This Userland Process always reaches its quantum
 * Meaning that it will be demoted
 */
public class TestDemotionProcess extends UserlandProcess{

    @Override
    public void main() {
        Scheduler.Priority priority = OS.getPriority();

        while(true){
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                System.err.println(e);
            }
            Scheduler.Priority currentPriority = OS.getPriority();

            System.out.println("Demotion Process originally set as " + priority + " is now " + currentPriority);
            this.cooperate();
        }
    }
}