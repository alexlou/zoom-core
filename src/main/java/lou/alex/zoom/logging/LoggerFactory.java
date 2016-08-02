package lou.alex.zoom.logging;

import org.slf4j.Logger;

public final class LoggerFactory {

    private LoggerFactory() {}

    public static Logger contextLogger() {
        final StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
        // 0 - current line
        // 1 - current invoked method
        // 2 - caller method
        final StackTraceElement staticMethodCaller = stackTraces[2];
        final String callerClassName = staticMethodCaller.getClassName();
        return org.slf4j.LoggerFactory.getLogger(callerClassName);
    }

}
