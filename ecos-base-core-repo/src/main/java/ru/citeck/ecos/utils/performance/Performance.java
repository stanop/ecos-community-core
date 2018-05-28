package ru.citeck.ecos.utils.performance;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class Performance {

    private static final Log logger = LogFactory.getLog(Performance.class);

    private static final String LOG_MSG = "{%d} [%d] %s %s";

    private static long loggingTimeThreshold = 3000;
    private static boolean checkingEnabled = false;

    protected Object instance;

    private long startTime;

    Performance(Object instance) {
        this.instance = instance;
        restart();
    }

    public void restart() {
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        if (!checkingEnabled || instance == null) {
            return;
        }
        long delta = System.currentTimeMillis() - startTime;
        if (delta >= loggingTimeThreshold) {
            String toStringResult;
            try {
                toStringResult = toString();
            } catch (Exception e) {
                logger.error("Error!", e);
                toStringResult = null;
            }
            logger.warn(String.format(LOG_MSG,
                                      delta, System.identityHashCode(instance), instance.getClass(), toStringResult));
        }
    }

    public static void setCheckingEnabled(boolean value) {
        checkingEnabled = value;
    }

    public static void setLoggingTimeThreshold(long time) {
        loggingTimeThreshold = time;
    }
}
