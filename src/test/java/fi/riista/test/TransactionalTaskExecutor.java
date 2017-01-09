package fi.riista.test;

import org.springframework.core.task.TaskExecutor;

import javax.annotation.Nonnull;

import java.util.concurrent.Callable;

public interface TransactionalTaskExecutor extends TaskExecutor {

    <T> T execute(@Nonnull Callable<T> callable);

}
