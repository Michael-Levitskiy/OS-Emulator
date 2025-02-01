public class VirtualToPhysicalMapping {
    //////////////////////////////
    // Class Instance Variables //
    //////////////////////////////
    public int physicalPageNumber;
    public int onDiskPageNumber;

    /**
     * Null Constructor
     */
    public VirtualToPhysicalMapping(){
        this.physicalPageNumber = -1;
        this.onDiskPageNumber = -1;
    }
}