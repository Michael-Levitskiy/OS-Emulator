public class MemoryTestProcess1 extends UserlandProcess{

    @Override
    public void main() {
        int pid = OS.GetPid();

        if (pid > 50) {OS.Exit();}

        int pointer = OS.AllocateMemory(1024*100);
        if (pointer != -1){
            System.out.println("(Process: " + pid + ") properly allocated memory");
            byte value = (byte) 'a';

            for (int i = 0; i < 100; i++){
                int address = pointer + (1024 * i);
                Hardware.Write(address, value);
                System.out.println("(Process: " + pid + ") Written to address: " + address + ", was char: a");

            }
            this.cooperate();

            for (int i = 0; i < 100; i++){
                int address = pointer + (1024 * i);
                value = Hardware.Read(address);
                System.out.println("(Process: " + pid + ") Read from address: " + address + ", was char: " + (char) value);
                this.cooperate();
            }

            this.cooperate();

            boolean F = OS.FreeMemory(pointer, 1025);
            boolean T = OS.FreeMemory(pointer, 1024);

            System.out.println("(Process1) when trying to free memory of incorrect size, I was given the note " + F);
            System.out.println("(Process1) when trying to free memory of the correct size, I was given the note " + T);

            OS.Exit();
        }
    }
}