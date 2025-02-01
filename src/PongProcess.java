/**
 * This Process sends and receives messages with PingProcess
 */
public class PongProcess extends UserlandProcess{

    @Override
    public void main() {
        System.out.println("I am Pong, pid = " + OS.GetPid());
        int targetPID = OS.GetPidByName("PingProcess");
        int message = 0;
        while (true){
            this.cooperate();
            KernelMessage toSend = new KernelMessage();
            toSend.targetPID = targetPID;
            toSend.message = message++;
            OS.SendMessage(toSend);
            KernelMessage receivedMessage = OS.WaitForMessage();
            while (receivedMessage == null){
                receivedMessage = OS.WaitForMessage();
            }
            System.out.println("Pong: " + receivedMessage);
        }
    }
}