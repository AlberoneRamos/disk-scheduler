package classes;

import java.util.ArrayList;

public class RequestQueue<E extends DiskRequest> extends ArrayList<E> {

    public int getRequestAccessTime(int index) {
        int lastLatency = index == 0 ? 0 : this.get(index - 1).getLatency();
        return this.get(index).getAccessTime(lastLatency);
    }

//    public float getAverageAccessTime(){
//        float average = 0;
//        for(int i = 0 ; i < this.size() ; i++)
//            average += this.getRequestAccessTime(i);
//        return average/this.size();
//    }

    public int getWaitingTime(int index) {
        if (index == 0) return 0;
        int waitingTime = 0;
        for (int i = index == 0 ? 0 : index - 1; i > -1; i--)
            waitingTime += this.getRequestAccessTime(i);
        waitingTime -= this.get(index).getArrivalTime();
        return waitingTime;
    }

//    public float getAverageWaitingTime(){
//        float average = 0;
//        for(int i = 0 ; i < this.size() ; i++)
//            average += this.getWaitingTime(i);
//        return average/this.size();
//    }
}
