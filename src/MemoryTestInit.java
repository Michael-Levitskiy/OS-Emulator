public class MemoryTestInit extends UserlandProcess{

    @Override
    public void main() {
        OS.CreateProcess(new MemoryTestInit());
        for (int i = 0; i < 1; i++){
            OS.CreateProcess(new MemoryTestProcess1());
        }
        OS.Exit();
    }
}