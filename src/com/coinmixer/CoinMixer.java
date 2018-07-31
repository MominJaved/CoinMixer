package com.coinmixer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.logging.Level;

public class CoinMixer {

    public static void main(String[] args) throws Exception {

        // central synchronized map data structure for the application
        // Key: deposit address for the user
        // Value: comma separated user addresses
        HashMap<String, String> hmap= new HashMap<String, String>();
        Map map= Collections.synchronizedMap(hmap);

        // custom AppLogger used for logging
        AppLogger appLogger = new AppLogger();

        // InputReader started in separate thread
        // populates central map with application data
        InputReader inputReader = new InputReader(map, appLogger);
        Thread t = new Thread(inputReader);
        t.start();

        // Poller started
        Poller poller = new Poller(map, appLogger);

        //Poller to poll deposit addresses in central map every 7 seconds
        while (true) {
            appLogger.log(Level.INFO, "polling addresses in map");
            Set<String> depositAddressSet = map.keySet();
            synchronized(map){
                Iterator<String> i = depositAddressSet.iterator();
                while(i.hasNext()) {
                    String depositAddress = i.next();
                    poller.poll(depositAddress);
                }
            }
            Thread.sleep(7000);
        }
    }
}


