/**
 * This Userland Process is used so the Simulation doesn't crash and fail
 */
public class IdleProcess extends UserlandProcess {

    @Override
    public void main() {
        while (true) {
            this.cooperate();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println("Idle Process");
        }
    }
}
