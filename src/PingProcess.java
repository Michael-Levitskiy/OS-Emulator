/**
 * This Process sends and receives messages with PongProcess
 */
public class PingProcess extends UserlandProcess{

    @Override
    public void main() {
        System.out.println("I am Ping, pid = " + OS.GetPid());
        int targetPID = OS.GetPidByName("PongProcess");
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
            System.out.println("Ping: " + receivedMessage);
        }
    }
}