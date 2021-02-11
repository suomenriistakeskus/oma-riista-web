package fi.riista.util;

import fi.riista.feature.gamediary.GameDiaryEntitySpecVersion;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface VersionedTestExecutionSupport<T extends GameDiaryEntitySpecVersion> {

    List<T> getTestExecutionVersions();

    default void forEachVersion(final Consumer<T> execution) {
        forEachVersionMatchingCondition(t -> true, execution);
    }

    default void forEachVersionBefore(final T maxVersion, final Consumer<T> execution) {
        forEachVersionMatchingCondition(v -> v.lessThan(maxVersion), execution);
    }

    default void forEachVersionStartingFrom(final T minVersion, final Consumer<T> execution) {
        forEachVersionMatchingCondition(v -> v.greaterThanOrEqualTo(minVersion), execution);
    }

    default void forEachVersionExcept(final T version, final Consumer<T> execution) {
        forEachVersionMatchingCondition(v -> v != version, execution);
    }

    default void forEachVersionMatchingCondition(final Predicate<T> condition, final Consumer<T> execution) {
        getTestExecutionVersions().stream().filter(condition).sorted(GameDiaryEntitySpecVersion::compareTo).forEach(version -> {
            onBeforeVersionedTestExecution(version);
            runTest(version, execution);
            onAfterVersionedTestExecution(version);
        });
    }

    default void runTest(final T version, final Consumer<T> execution) {
        try {
            execution.accept(version);
        } catch (final AssertionError e) {
            final String versionStr = "[version: " + version + "]";
            final String newMessage = e.getMessage() == null ? versionStr : e.getMessage() + " " + versionStr;
            throw new AssertionError(newMessage, e);
        }
    }

    default void onBeforeVersionedTestExecution(@SuppressWarnings("unused") final T version) {
    }

    default void onAfterVersionedTestExecution(@SuppressWarnings("unused") final T version) {
    }
}
