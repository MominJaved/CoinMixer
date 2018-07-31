package com.coinmixer;

import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class AppLogger {

    /**
     * Custom configured and formatted AppLogger for the application.
     */

    private Logger appLogger = Logger.getLogger("AppLogger");

    public AppLogger() throws IOException {
        appLogger.setLevel(Level.ALL);
        appLogger.setUseParentHandlers(false);
        FileHandler handler = new FileHandler("coinmixer.log");

        // configuring logging format for the FileHandler
        handler.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format,
                        new Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                );
            }
        });
        appLogger.addHandler(handler);
    }

    public void log(Level level, String msg) {
        appLogger.log(level, msg);
    }

}
