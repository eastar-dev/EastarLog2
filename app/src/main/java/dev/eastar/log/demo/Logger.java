package dev.eastar.log.demo;

import android.log.Log;

public class Logger {
    public static int e(String formatter, Object... args) {
        return Log.e(Logger.class.getSimpleName(), String.format(formatter, args));
    }
}
