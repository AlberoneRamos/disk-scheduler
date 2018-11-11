package classes;

public class DiskRequest implements Comparable<DiskRequest> {
    private String requestName;
    private int arrivalTime;
    private int latency;
    private int seekTime;
    private int transferTime;

    DiskRequest(String requestName, int arrivalTime, int latency, int seekTime, int transferTime) {
        this.requestName = requestName;
        this.arrivalTime = arrivalTime;
        this.latency = latency;
        this.seekTime = seekTime;
        this.transferTime = transferTime;
    }

    DiskRequest(DiskRequest request) {
        this.requestName = request.getRequestName();
        this.arrivalTime = request.getArrivalTime();
        this.latency = request.getLatency();
        this.seekTime = request.getSeekTime();
        this.transferTime = request.getTransferTime();
    }


    String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    int getAccessTime(int lastRequestLatency) {
        return Math.abs(latency - lastRequestLatency) + seekTime + transferTime;
    }

    int getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(int arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    int getLatency() {
        return latency;
    }

    public void setLatency(int latency) {
        this.latency = latency;
    }

    int getSeekTime() {
        return seekTime;
    }

    public void setSeekTime(int seekTime) {
        this.seekTime = seekTime;
    }

    int getTransferTime() {
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
