public class Hardware {
    //////////////////////////////
    // Class Instance Variables //
    //////////////////////////////
    public static byte[] memory = new byte[1024*1024];  // 1024*1024 = 1,048,576
    public static int[][] TLB = new int[2][2];
    public static boolean[] inUseMemory = new boolean[1024];
    private static final int pageSize = 1024;           // 1024 = 1KB


    ////////////////////
    // Public Methods //
    ////////////////////
    /**
     * Hardware Read method is like the LOAD instruction in assembly
     * @param address (int) - virtual address to read from
     * @return the byte at the address
     */
    public static byte Read(int address){
        int virtualPageNumber = address / pageSize; // page number is address divided by the page size
        int pageOffset = address % pageSize;        // page offset is modular division between address and page size
        int physicalPageNumber = findPageNumber(virtualPageNumber);
        int physicalAddress = (physicalPageNumber * pageSize) + pageOffset;
        return Hardware.memory[physicalAddress];
    }


    /**
     * Hardware Write method is like the STORE instruction in assembly
     * @param address
     * @param value
     */
    public static void Write(int address, byte value){
        int virtualPageNumber = address / pageSize;     // page number is address divided by the page size (1KB)
        int pageOffset = address % pageSize;            // page offset is modular division between address and page size
        int physicalPageNumber = findPageNumber(virtualPageNumber);
        int physicalAddress = (physicalPageNumber * pageSize) + pageOffset;
        Hardware.memory[physicalAddress] = value;
    }


    /////////////////////
    // Private Methods //
    /////////////////////
    /**
     * Private helper method to find the physical page number
     * @param virtualPageNumber (int) - given virtual address
     * @return an int of the physical address
     */
    private static int findPageNumber(int virtualPageNumber){
        // check if the first entry matches the virtualPageNumber
        if(TLB[0][0] == virtualPageNumber){
            return TLB[0][1];
        }
        // check if the second entry matches the virtualPageNumber
        else if(TLB[1][0] == virtualPageNumber){
            return TLB[1][1];
        }
        // otherwise
        else{
            // Call OS.GetMapping to place virtualPageNumber mapping in TLB
            OS.GetMapping(virtualPageNumber);

            // Find the mapping and return the value
            if(TLB[0][0] == virtualPageNumber){
                return TLB[0][1];
            }
            else if (TLB[1][0] == virtualPageNumber){
                return TLB[1][1];
            }
            else{
                System.err.println("Get Mapping Failed");
                return -1;
            }
        }
    }
}