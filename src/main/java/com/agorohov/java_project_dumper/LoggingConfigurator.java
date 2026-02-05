package com.agorohov.java_project_dumper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingConfigurator {

    private LoggingConfigurator() {}

    public static void configure(boolean debug) {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        root.setLevel(debug ? Level.DEBUG : Level.INFO);
    }
}
