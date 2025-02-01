/**
 * This Userland Process is initialized by 'PingPongMain.java'
 */
public class PingPongInit extends UserlandProcess{

    @Override
    public void main() {
        OS.CreateProcess(new PingProcess());
        OS.CreateProcess(new PongProcess());
        OS.Exit();
    }
}