/**
 * This Userland Process prints "Hello World" forever
 */
public class HelloWorld extends UserlandProcess {

    @Override
    public void main() {
        while (true) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
                System.err.println(e);
            }

            System.out.println("Hello World");
            this.cooperate();
        }
    }
}