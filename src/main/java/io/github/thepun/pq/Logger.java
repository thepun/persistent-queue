package io.github.thepun.pq;

final class Logger {

    static void info(String message, Object ... params) {
        System.out.println("INFO: " + fillPlaceholders(message, params));
    }

    static void warn(String message, Object ... params) {
        System.err.println("WARN: " + fillPlaceholders(message, params));
    }

    static void error(String message, Object ... params) {
        System.err.println("ERROR: " + fillPlaceholders(message, params));
    }

    static void error(Throwable e, String message, Object ... params) {
        System.err.println("ERROR: " + fillPlaceholders(message, params));
        e.printStackTrace(System.err);
    }

    static String fillPlaceholders(String message, Object ... params) {
        for (Object param : params) {
            message = message.replaceFirst("\\{\\}", param.toString());
        }
        return message;
    }
}
