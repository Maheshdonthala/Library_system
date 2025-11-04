package com.yourdomain.librarysystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StartupLogger {

    private static final Logger logger = LoggerFactory.getLogger(StartupLogger.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (mongoUri != null) {
            // redact credentials if present by replacing userinfo between :// and @ with REDACTED
            String redacted = mongoUri;
            int schemeEnd = redacted.indexOf("://");
            int atIndex = redacted.indexOf('@');
            if (schemeEnd != -1 && atIndex != -1 && atIndex > schemeEnd) {
                redacted = redacted.substring(0, schemeEnd + 3) + "REDACTED@" + redacted.substring(atIndex + 1);
            }
            logger.info("Effective spring.data.mongodb.uri (redacted): {}", redacted);
        } else {
            logger.info("spring.data.mongodb.uri is not set; using default");
        }
        logger.info("Library System started successfully with MongoDB Atlas");
    }
}
