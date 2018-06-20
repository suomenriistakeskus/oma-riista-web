package fi.riista.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

import java.util.Objects;
import java.util.concurrent.Callable;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@Component
public class TransactionalTaskExecutorImpl implements TransactionalTaskExecutor {

    @Override
    public void execute(@Nonnull final Runnable task) {
        Objects.requireNonNull(task).run();
    }

    @Override
    public <T> T execute(@Nonnull final Callable<T> callable) {
        try {
            return callable.call();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
