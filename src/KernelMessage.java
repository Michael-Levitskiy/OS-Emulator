public class KernelMessage {
    //////////////////////////////
    // Class Instance Variables //
    //////////////////////////////
    public int senderPID;
    public int targetPID;
    public int message;
    public byte[] data;


    //////////////////
    // Constructors //
    //////////////////
    /**
     * Null Constructor
     */
    public KernelMessage(){}

    /**
     * Copy Constructor
     * @param toCopy (KernelMessage) -
     */
    public KernelMessage(KernelMessage toCopy){
        this.senderPID = toCopy.senderPID;
        this.targetPID = toCopy.targetPID;
        this.message = toCopy.message;
        this.data = toCopy.data;
    }

    /**
     * Overridden toString method
     * @return a String
     */
    @Override
    public String toString() {
        return "From " + senderPID + " -> " + targetPID +
                ": (" + message + ")";
    }
}