package com.example.catalog.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class InitializerTest {

    @Mock
    private ApplicationProperties applicationProperties;

    private Initializer initializer;

    private ListAppender<ILoggingEvent> logWatcher;

    @BeforeEach
    void setUp() {
        initializer = new Initializer(applicationProperties);
        Logger logger = (Logger) LoggerFactory.getLogger(Initializer.class);
        logWatcher = new ListAppender<>();
        logWatcher.start();
        logger.addAppender(logWatcher);
    }

    @Test
    void shouldLogOnStartup() throws Exception {
        initializer.run();

        assertThat(logWatcher.list)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains("Running Initializer.....");
    }
}
