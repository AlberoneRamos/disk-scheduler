package classes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class DiskScheduler {
    private int numSectors;
    private int numTracks;
    private int startPosition;
    private List<DiskRequest> diskRequests;

    public DiskScheduler(String filePath) {
        this();
        this.initializeData(filePath);
    }

    public DiskScheduler() {
        this.numSectors = 0;
        this.numTracks = 0;
        this.startPosition = 0;
        this.diskRequests = new ArrayList<>();
        File directory = new File("output/");
        File[] files = directory.listFiles();
        if (files != null)
            for (File file : files)
                if (!file.delete()) System.out.println("Failed to delete " + file);
    }

    private void initializeData(String filePath) {
        File file = new File(filePath);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String currentLine;
            for (int i = 0; (currentLine = reader.readLine()) != null; i++) {
                switch (i) {
                    case 0:
                        numSectors = Integer.valueOf(currentLine.replaceAll("[^0-9]", "")) - 1;
                        break;
                    case 1:
                        numTracks = Integer.valueOf(currentLine.replaceAll("[^0-9]", ""));
                        break;
                    case 2:
                        startPosition = Integer.valueOf(currentLine.replaceAll("[^0-9]", ""));
                        break;
                    default:
                        int[] fileValues = Stream.of(currentLine.substring(3).split(",")).mapToInt(Integer::parseInt).toArray();
                        diskRequests.add(new DiskRequest(currentLine.substring(0, 2), fileValues[0], fileValues[1], fileValues[2], fileValues[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<DiskRequest> cloneRequestQueue() {
        List<DiskRequest> clone = new ArrayList<>();
        clone.addAll(this.diskRequests);
        return clone;
    }

    public void firstComeFirstServed(String fileName) {
        List<DiskRequest> diskRequests = cloneRequestQueue();
        int elapsedTime = 0;
        float totalAccessTime = 0;
        float totalWaitingTime = 0;
        int lastLatency = startPosition;
        int numRequests = diskRequests.size();
        while (!diskRequests.isEmpty()) {
            for (int i = 0; i < diskRequests.size(); i++) {
                int currentAccessTime = diskRequests.get(i).getAccessTime(lastLatency);
                if (elapsedTime >= diskRequests.get(i).getArrivalTime()) {
//                    System.out.println(diskRequests.get(i) + " - " + currentAccessTime + " - " + totalAccessTime + " - " + Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime()));
                    elapsedTime += currentAccessTime;
                    totalWaitingTime = totalWaitingTime + Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime());
                    totalAccessTime += currentAccessTime;
                    lastLatency = diskRequests.get(i).getLatency();
                    diskRequests.remove(i);
                    i--;
                }
            }
        }
        this.writeData(fileName, "FCFS", totalAccessTime / numRequests, totalWaitingTime / numRequests);
    }

    public void scan(String fileName) {
        List<DiskRequest> diskRequests = cloneRequestQueue();
        int elapsedTime = 0;
        float totalAccessTime = 0;
        float totalWaitingTime = 0;
        int lastLatency = startPosition;
        int numRequests = diskRequests.size();
        diskRequests.sort(Comparator.naturalOrder());
        boolean isReturning = false;
        int numOfRequests;
        while (!diskRequests.isEmpty()) {
            int i;
            int currentAccessTime = 0;
            numOfRequests = diskRequests.size();
            for (i = 0; i < diskRequests.size(); i++) {
                if (elapsedTime >= diskRequests.get(i).getArrivalTime() && diskRequests.get(i).getLatency() >= lastLatency) {
                    if (isReturning) {
                        currentAccessTime = diskRequests.get(i).getSeekTime() + diskRequests.get(i).getTransferTime() + lastLatency + diskRequests.get(i).getLatency();
                        isReturning = false;
                    } else
                        currentAccessTime = diskRequests.get(i).getAccessTime(lastLatency);
//                    System.out.println(diskRequests.get(i) + " - " + currentAccessTime + " - " + totalAccessTime + " - " + Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime()));
                    elapsedTime += currentAccessTime;
                    totalWaitingTime += Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime());
                    totalAccessTime += currentAccessTime;
                    lastLatency = diskRequests.get(i).getLatency();
                    diskRequests.remove(i);
                    i--;
                }
            }
            i--;
            if (!diskRequests.isEmpty()) isReturning = true;
            for (; i > -1; i--) {
                if (elapsedTime >= diskRequests.get(i).getArrivalTime() && diskRequests.get(i).getLatency() <= lastLatency) {
                    if (isReturning) {
                        currentAccessTime = ((this.numSectors - lastLatency) + (this.numSectors - diskRequests.get(i).getLatency())) + diskRequests.get(i).getSeekTime() + diskRequests.get(i).getTransferTime();
                        isReturning = false;
                    } else
                        currentAccessTime = diskRequests.get(i).getAccessTime(lastLatency);
//                    System.out.println(diskRequests.get(i) + " - " + currentAccessTime + " - " + totalAccessTime + " - " + Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime()));
                    elapsedTime += currentAccessTime;
                    totalWaitingTime = totalWaitingTime + Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime());
                    totalAccessTime += currentAccessTime;
                    lastLatency = diskRequests.get(i).getLatency();
                    diskRequests.remove(i);
                    i++;
                }
            }
            i++;
            if (!diskRequests.isEmpty()) isReturning = true;
            if (numOfRequests == diskRequests.size())
                elapsedTime++;
        }
        this.writeData(fileName, "SCAN", totalAccessTime / numRequests, totalWaitingTime / numRequests);
    }

    public void cScan(String fileName) {
        List<DiskRequest> diskRequests = cloneRequestQueue();
        int elapsedTime = 0;
        float totalAccessTime = 0;
        float totalWaitingTime = 0;
        int lastLatency = startPosition;
        int numRequests = diskRequests.size();
        diskRequests.sort(Comparator.naturalOrder());
        boolean isReturning = false;
        int currentAccessTime = 0;
        while (!diskRequests.isEmpty()) {
            int i;
            for (i = 0; i < diskRequests.size(); i++) {
                if (elapsedTime >= diskRequests.get(i).getArrivalTime() && diskRequests.get(i).getLatency() >= lastLatency) {
                    if (isReturning) {
                        currentAccessTime += (diskRequests.get(i).getSeekTime() + diskRequests.get(i).getTransferTime() + diskRequests.get(i).getLatency());
                        isReturning = false;
                    } else {
                        currentAccessTime = diskRequests.get(i).getAccessTime(lastLatency);
                    }
                    elapsedTime += currentAccessTime;
                    totalWaitingTime += Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime());
                    totalAccessTime += currentAccessTime;
                    lastLatency = diskRequests.get(i).getLatency();
//                    System.out.println(diskRequests.get(i) + " - " + currentAccessTime + " - " + totalAccessTime + " - " + totalWaitingTime);
                    diskRequests.remove(i);
                    i--;
                }
            }
            if (!diskRequests.isEmpty()) {
                currentAccessTime = ((this.numSectors - lastLatency) + this.numSectors);
                isReturning = true;
                lastLatency = 0;
            }
        }
        this.writeData(fileName, "C-SCAN", totalAccessTime / numRequests, totalWaitingTime / numRequests);
    }

    public void cLook(String fileName) {
        List<DiskRequest> diskRequests = cloneRequestQueue();
        int elapsedTime = 0;
        float totalAccessTime = 0;
        float totalWaitingTime = 0;
        int lastLatency = startPosition;
        int numRequests = diskRequests.size();
        diskRequests.sort(Comparator.naturalOrder());
        boolean isReturning = false;
        int currentAccessTime = 0;
        while (!diskRequests.isEmpty()) {
            int i;
            for (i = 0; i < diskRequests.size(); i++) {
                if (elapsedTime >= diskRequests.get(i).getArrivalTime() && diskRequests.get(i).getLatency() >= lastLatency) {
                    currentAccessTime = diskRequests.get(i).getAccessTime(lastLatency);
                    elapsedTime += currentAccessTime;
                    totalWaitingTime += Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime());
                    totalAccessTime += currentAccessTime;
                    lastLatency = diskRequests.get(i).getLatency();
//                    System.out.println(diskRequests.get(i) + " - " + currentAccessTime + " - " + totalAccessTime + " - " + totalWaitingTime);
                    diskRequests.remove(i);
                    i--;
                }
            }
            if (!diskRequests.isEmpty()) {
                int j = 0;
                while (diskRequests.get(j).getArrivalTime() > elapsedTime && j < diskRequests.size())
                    j++;
                currentAccessTime = diskRequests.get(j).getAccessTime(lastLatency);
                elapsedTime += currentAccessTime;
                totalWaitingTime += Math.abs(totalAccessTime - diskRequests.get(j).getArrivalTime());
                totalAccessTime += currentAccessTime;
                lastLatency = diskRequests.get(j).getLatency();
//                System.out.println(diskRequests.get(j) + " - " + currentAccessTime + " - " + totalAccessTime + " - " + totalWaitingTime);
                diskRequests.remove(j);
                i--;
            }
        }
        this.writeData(fileName, "C-LOOK", totalAccessTime / numRequests, totalWaitingTime / numRequests);
    }

    public void shortestSeekTimeFirst(String fileName) {
        List<DiskRequest> diskRequests = cloneRequestQueue();
        int elapsedTime = 0;
        float totalAccessTime = 0;
        float totalWaitingTime = 0;
        int numRequests = diskRequests.size();
        int lastLatency = startPosition;
        while (!diskRequests.isEmpty()) {
            int i = 0;
            i = getNearestPos(lastLatency, elapsedTime, diskRequests);
            int currentAccessTime = diskRequests.get(i).getAccessTime(lastLatency);
            if (elapsedTime >= diskRequests.get(i).getArrivalTime()) {
//                System.out.println(diskRequests.get(i) + " - " + currentAccessTime + " - " + totalAccessTime + " - " + Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime()));
                elapsedTime += currentAccessTime;
                totalWaitingTime = totalWaitingTime + Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime());
                totalAccessTime += currentAccessTime;
                lastLatency = diskRequests.get(i).getLatency();
                diskRequests.remove(i);
            }
        }
        this.writeData(fileName, "SSTF", totalAccessTime / numRequests, totalWaitingTime / numRequests);
        diskRequests.forEach(System.out::println);
    }

    private void writeData(String fileName, String schedulingMethodName, float avgAccessTime, float avgNumRequests) {
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
        formatSymbols.setDecimalSeparator('.');
        formatSymbols.setGroupingSeparator(',');
        DecimalFormat decimal = new DecimalFormat("#.00", formatSymbols);
        decimal.setMaximumFractionDigits(2);
        Path filePath = Paths.get("output/" + fileName);
        try {
            // Make sure the directories exist
            Files.createDirectories(filePath.getParent());  // No need for your null check, so I removed it; based on `fileName`, it will always have a parent
            System.out.println(schedulingMethodName + System.lineSeparator()
                    + "-AccessTime=" + decimal.format(avgAccessTime) + System.lineSeparator()
                    + "-WaitingTime=" + decimal.format(avgNumRequests));
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writer.write(schedulingMethodName + System.lineSeparator());
                writer.write("-AccessTime=" + decimal.format(avgAccessTime) + System.lineSeparator());
                writer.write("-WaitingTime=" + decimal.format(avgNumRequests) + System.lineSeparator());
                System.out.println("Escrito com sucesso em " + Paths.get(fileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int getNearestPos(int position, int elapsedTime, List<DiskRequest> diskRequests) {
        int closestRequest = 0;
        int closestPosition = Integer.MAX_VALUE;
        for (int i = 0; i < diskRequests.size(); i++) {
            if (diskRequests.get(i).getArrivalTime() < elapsedTime) {
                if (Math.abs(diskRequests.get(i).getLatency() - position) < closestPosition) {
                    closestRequest = i;
                    closestPosition = Math.abs(diskRequests.get(i).getLatency() - position);
                } else if (Math.abs(diskRequests.get(i).getLatency() - position) == closestPosition) {
                    if (diskRequests.get(i).getLatency() > diskRequests.get(closestRequest).getLatency())
                        closestRequest = i;
                }
            }
        }
        return closestRequest;
    }

    public void lastComeFirstServed(String fileName) {
        List<DiskRequest> diskRequests = cloneRequestQueue();
        int elapsedTime = 0;
        float totalAccessTime = 0;
        float totalWaitingTime = 0;
        DecimalFormat decimal = new DecimalFormat("#.##");
        int lastLatency = startPosition;
        int numRequests = diskRequests.size();
        while (!diskRequests.isEmpty()) {
            for (int i = diskRequests.size() - 1; i >= 0; i--) {
                int currentAccessTime = diskRequests.get(i).getAccessTime(lastLatency);
                if (elapsedTime >= diskRequests.get(i).getArrivalTime()) {
//                    System.out.println(diskRequests.get(i) + " - " + currentAccessTime + " - " + totalAccessTime + " - " + Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime()));
                    elapsedTime += currentAccessTime;
                    totalWaitingTime = totalWaitingTime + Math.abs(totalAccessTime - diskRequests.get(i).getArrivalTime());
                    totalAccessTime += currentAccessTime;
                    lastLatency = diskRequests.get(i).getLatency();
                    diskRequests.remove(i);
                }
            }
        }
        this.writeData(fileName, "MY", totalAccessTime / numRequests, totalWaitingTime / numRequests);
    }

    public void changeInputData(String filePath) {
        this.diskRequests = new ArrayList<DiskRequest>();
        this.initializeData(filePath);
    }

    public void executeAllMethods(String fileName) {
        this.firstComeFirstServed(fileName);
        this.shortestSeekTimeFirst(fileName);
        this.scan(fileName);
        this.cScan(fileName);
        this.cLook(fileName);
        this.lastComeFirstServed(fileName);
    }
}
