public class MemoryTestProcess2 extends UserlandProcess{

    @Override
    public void main() {
        int pointer = OS.AllocateMemory(2048);
        if (pointer != -1){
            System.out.println("(Process2) properly allocated memory");
            byte value = (byte) 123;
            Hardware.Write(pointer, value);
            Hardware.Write(pointer+1800, value);

            this.cooperate();

            System.out.println("(Process2) Written to address: " + pointer + ", was int: 123");
            System.out.println("(Process2) Written to address: " + pointer+1800 + ", was int: 123");

            int int1 = (int) Hardware.Read(pointer);
            int int2 = (int) Hardware.Read(pointer+1800);

            this.cooperate();

            System.out.println("(Process2) Read from address: " + pointer + ", was int: " + (int) int1);
            System.out.println("(Process2) Read from address: " + pointer+1800 + ", was int: " + (int) int2);

            this.cooperate();

            boolean F = OS.FreeMemory(pointer, 1025);
            boolean T = OS.FreeMemory(pointer, 1024);

            System.out.println("(Process2) when trying to free memory of incorrect size, I was given the note " + F);
            System.out.println("(Process2) when trying to free memory of the correct size, I was given the note " + T);

            OS.Exit();
        }
    }
}