package main;

import classes.DiskScheduler;

public class Main {

    public static void main(String[] args) {
        DiskScheduler scheduler = new DiskScheduler("./input/Arquivo 1.in");
        scheduler.executeAllMethods("Arquivo 1.out");
    }
}
