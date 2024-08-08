package com.zimji.gateway.configuration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Configuration
public class LogbackConfig {

    static String FILE_NAME = "GATEWAY";
    static String CONSOLE_APPENDER_NAME = "CONSOLE";

    public LogbackConfig() {
        configure();
    }

    private void configure() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Console Appender for common log
        ConsoleAppender<ILoggingEvent> consoleAppender = createConsoleAppender(loggerContext);
        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(consoleAppender);

        // File Appender for common log
        RollingFileAppender<ILoggingEvent> fileAppender = createRollingFileAppender(loggerContext, FILE_NAME);
        loggerContext.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(fileAppender);

        // Root Logger
        Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(consoleAppender);
        rootLogger.addAppender(fileAppender);
    }

    private ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext loggerContext) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(loggerContext);
        consoleAppender.setName(CONSOLE_APPENDER_NAME);
        PatternLayoutEncoder consoleEncoder = createPatternLayoutEncoder(loggerContext);
        consoleAppender.setEncoder(consoleEncoder);
        consoleAppender.start();
        return consoleAppender;
    }

    private RollingFileAppender<ILoggingEvent> createRollingFileAppender(LoggerContext loggerContext, String fileName) {
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName(fileName + "_FILE");
        fileAppender.setRollingPolicy(createTimeBasedRollingPolicy(loggerContext, fileAppender, fileName));
        PatternLayoutEncoder fileEncoder = createPatternLayoutEncoder(loggerContext);
        fileAppender.setEncoder(fileEncoder);
        fileAppender.start();
        return fileAppender;
    }

    private PatternLayoutEncoder createPatternLayoutEncoder(LoggerContext loggerContext) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy/MM/dd - HH:mm:ss.SSS} [%thread] %-5level %logger{36} --- %msg%n");
        // encoder.setPattern("%d{yyyy/MM/dd - HH:mm:ss.SSS} [%thread] %highlight(%-5level) %cyan(%logger{36}) - %msg%n");
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();
        return encoder;
    }

    private <T> TimeBasedRollingPolicy<ILoggingEvent> createTimeBasedRollingPolicy(LoggerContext loggerContext,
                                                                                   RollingFileAppender<ILoggingEvent> fileAppender,
                                                                                   String fileName) {
        // Get package name
        String fileLog = "logs/%d{yyyy-MM-dd}/" + fileName.toLowerCase() + ".log";

        TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
        rollingPolicy.setFileNamePattern(fileLog);
        rollingPolicy.setMaxHistory(30); // Keep 30 days of history
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setTotalSizeCap(new FileSize(10L * FileSize.GB_COEFFICIENT)); // 10 GB
        rollingPolicy.start();
        return rollingPolicy;
    }

}