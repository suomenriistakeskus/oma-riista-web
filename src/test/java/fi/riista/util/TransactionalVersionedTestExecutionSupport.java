package fi.riista.util;

import fi.riista.feature.gamediary.GameDiaryEntitySpecVersion;
import java.util.function.Consumer;

public interface TransactionalVersionedTestExecutionSupport<T extends GameDiaryEntitySpecVersion>
        extends VersionedTestExecutionSupport<T> {

    TransactionalTaskExecutor getTransactionalExecutor();

    // Do resetting between transaction-wrapped test executions.
    void reset();

    @Override
    default void runTest(final T version, final Consumer<T> execution) {
        getTransactionalExecutor().execute(() -> {
            onBeforeTestExecutionWithinSameTransaction();

            VersionedTestExecutionSupport.super.runTest(version, execution);

            onAfterTestExecutionWithinSameTransaction();
        });
    }

    @Override
    default void onAfterVersionedTestExecution(final T version) {
        reset();
    }

    default void onBeforeTestExecutionWithinSameTransaction() {
    }

    default void onAfterTestExecutionWithinSameTransaction() {
    }

}
