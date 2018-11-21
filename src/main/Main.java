package main;

import classes.DiskScheduler;

public class Main {

    public static void main(String[] args) {
        DiskScheduler scheduler = new DiskScheduler();
        for (int i = 1; i <= 4; i++) {
            scheduler.changeInputData("./input/Arquivo " + i + ".in");
            scheduler.executeAllMethods("Arquivo " + i + ".out");
        }
    }
}
