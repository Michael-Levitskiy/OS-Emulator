/**
 * This Userland Process prints "Goodbye World" forever
 */
public class GoodbyeWorld extends UserlandProcess {

    @Override
    public void main() {
        while (true) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Goodbye World");
            this.cooperate();
        }
    }
}