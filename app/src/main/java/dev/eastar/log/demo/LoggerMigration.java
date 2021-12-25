package dev.eastar.log.demo;

import android.log.Log;

public class LoggerMigration {
    public static int e(String formatter, Object... args) {
        return Log.e(LoggerMigration.class.getSimpleName(), String.format(formatter, args));
    }
}
