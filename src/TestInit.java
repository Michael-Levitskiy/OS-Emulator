/**
 * This Userland Process is initialized by 'TestMain.java'
 */
public class TestInit extends UserlandProcess{
    @Override
    public void main() {
        OS.CreateProcess(new TestDemotionProcess(), Scheduler.Priority.realTime);
        OS.CreateProcess(new TestSleepProcess(), Scheduler.Priority.realTime);
        OS.Exit();
    }
}
