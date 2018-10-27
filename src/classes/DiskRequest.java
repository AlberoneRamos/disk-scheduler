package classes;

public class DiskRequest implements Comparable<DiskRequest> {
    private String requestName;
    private int arrivalTime;
    private int latency;
    private int seekTime;
    private int transferTime;

    public DiskRequest(String requestName, int arrivalTime, int latency, int seekTime, int transferTime) {
        this.requestName = requestName;
        this.arrivalTime = arrivalTime;
        this.latency = latency;
        this.seekTime = seekTime;
        this.transferTime = transferTime;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public int getAccessTime(int lastRequestLatency) {
        return Math.abs(latency - lastRequestLatency) + seekTime + transferTime;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    public int getSeekTime() {
        return seekTime;
    }

    public void setSeekTime(int seekTime) {
        this.seekTime = seekTime;
    }

    public int getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(int transferTime) {
        this.transferTime = transferTime;
    }

    public String toString() {
        return requestName + "=" + arrivalTime + "," + latency + "," + seekTime + "," + transferTime;
    }

    @Override
    public int compareTo(DiskRequest req) {
        if (this.latency > req.getLatency()) return 1;
        else if (this.latency < req.getLatency()) return -1;
        return 0;
    }
}
