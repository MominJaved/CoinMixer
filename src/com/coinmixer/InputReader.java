package com.coinmixer;

import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

/**
 * InputReader keeps reading from stdin on a separate thread and processes
 * user input to add to the central map.
 */

public class InputReader implements Runnable {

    private Map map;
    private AppLogger appLogger;
    private Scanner inputScanner = new Scanner(System.in);

    // InputReader takes in the central map and appLogger
    public InputReader (Map map, AppLogger appLogger) {
        this.map = map;
        this.appLogger = appLogger;
    }

    // when thread is run, InputReader keeps reading from stdin for user data
    public void run() {
        while(true) {
            // user addresses stored in a string
            System.out.println("Please enter your addresses separated by commas (,) :");
            String userAdrsList = inputScanner.nextLine().trim();

            // random deposit address generated
            UUID uuid = UUID.randomUUID();
            String depositAdrs = uuid.toString().replace("-", "");

            // deposit and user addresses added to map
            addToMap(depositAdrs, userAdrsList);

            System.out.println("Transfer your Jobcoins to: " + depositAdrs);
        }
    }

    public void addToMap(String key, String value) {
        // synchronization for thread safety
        synchronized (map) {
            if (!map.containsKey(key)) {
                map.put(key, value);
                appLogger.log(Level.INFO, "ADDED TO MAP [depositAddress: " + key + "] [userAddresses: " + value + "]");
            }
        }
    }
}