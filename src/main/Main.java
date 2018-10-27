package main;

import classes.DiskScheduler;

public class Main {

    public static void main(String[] args) {
        DiskScheduler scheduler = new DiskScheduler();
        scheduler.SCAN("./diskRequests.txt");
    }
}
