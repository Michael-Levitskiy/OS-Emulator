/**
 * This Userland Process is initialized by 'Main.java'
 */
public class Init extends UserlandProcess {

    @Override
    public void main() {
        OS.CreateProcess(new HelloWorld());
        OS.CreateProcess(new GoodbyeWorld());
        OS.Exit();
    }
}