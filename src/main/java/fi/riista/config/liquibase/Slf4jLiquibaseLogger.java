package fi.riista.config.liquibase;

import liquibase.logging.core.AbstractLogger;
import org.slf4j.LoggerFactory;

public class Slf4jLiquibaseLogger extends AbstractLogger {

    private org.slf4j.Logger LOG;

    @Override
    public void setName(String name) {
        this.LOG = LoggerFactory.getLogger(name);
    }

    @Override
    public void setLogLevel(String logLevel, String logFile) {
        super.setLogLevel(logLevel);
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public void severe(String message) {
        if (this.LOG.isErrorEnabled()) {
            this.LOG.error(buildMessage(message));
        }
    }

    @Override
    public void severe(String message, Throwable e) {
        if (this.LOG.isErrorEnabled()) {
            this.LOG.error(buildMessage(message), e);
        }
    }

    @Override
    public void warning(String message) {
        if (this.LOG.isWarnEnabled()) {
            this.LOG.warn(buildMessage(message));
        }
    }

    @Override
    public void warning(String message, Throwable e) {
        if (this.LOG.isWarnEnabled()) {
            this.LOG.warn(buildMessage(message), e);
        }
    }

    @Override
    public void info(String message) {
        if (this.LOG.isInfoEnabled()) {
            this.LOG.info(buildMessage(message));
        }
    }

    @Override
    public void info(String message, Throwable e) {
        if (this.LOG.isInfoEnabled()) {
            this.LOG.info(buildMessage(message), e);
        }
    }

    @Override
    public void debug(String message) {
        if (this.LOG.isDebugEnabled()) {
            this.LOG.debug(buildMessage(message));
        }
    }

    @Override
    public void debug(String message, Throwable e) {
        if (this.LOG.isDebugEnabled()) {
            this.LOG.debug(buildMessage(message), e);
        }
    }
}
