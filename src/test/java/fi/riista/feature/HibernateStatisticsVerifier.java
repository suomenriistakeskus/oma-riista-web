package fi.riista.feature;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Joiner;

import fi.riista.util.jpa.HibernateStatisticsAssertions;

import javaslang.Tuple;

import org.apache.commons.lang.StringUtils;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;
import org.junit.rules.Verifier;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;
import java.util.stream.Stream;

public abstract class HibernateStatisticsVerifier extends Verifier {

    private HibernateStatisticsAssertions assertions;

    @Override
    public Statement apply(final Statement base, final Description description) {
        assertions = description.getAnnotation(HibernateStatisticsAssertions.class);

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (assertions != null) {
                    getStatistics().clear();
                }

                base.evaluate();

                if (assertions != null) {
                    verify();
                }
            }
        };
    }

    protected abstract Statistics getStatistics();

    @Override
    public void verify() {
        verifyQueryCount(getStatistics());
    }

    public void verifyAndClear() {
        final Statistics stats = getStatistics();
        verifyQueryCount(stats);
        stats.clear();
    }

    protected void verifyQueryCount(final Statistics stats) {
        final long totalQueryCount = Math.max(stats.getQueryExecutionCount(), stats.getPrepareStatementCount());

        if (assertions.maxQueries() >= 0 && totalQueryCount > assertions.maxQueries()) {
            final StringBuilder msgBuf = new StringBuilder();

            msgBuf.append("Statements prepared: ").append(stats.getPrepareStatementCount());

            // Create list of queries for debugging purposes
            final List<String> queryLines = Stream.of(stats.getQueries())
                    .map(query -> {
                        final QueryStatistics qStats = stats.getQueryStatistics(query);
                        return Tuple.of(qStats.getExecutionCount(), query);
                    })
                    .sorted(reverseOrder())
                    .map(pair -> String.format("%s: %s", StringUtils.leftPad(pair._1.toString(), 3, ' '), pair._2))
                    .collect(toList());

            if (!queryLines.isEmpty()) {
                msgBuf.append("\n  Queries (ordered by execution count): ")
                        .append(stats.getQueryExecutionCount())
                        .append("\n  ")
                        .append(Joiner.on("\n  ").join(queryLines));
            }

            throw new MaximumQueryCountExceededException(String.format("%s\n  %s\n",
                    MaximumQueryCountExceededException.getErrorMessage(assertions.maxQueries(), totalQueryCount),
                    msgBuf.toString()));
        }

        if (assertions.queryCount() >= 0 && totalQueryCount != assertions.queryCount()) {
            throw new QueryCountAssertionException(assertions.queryCount(), totalQueryCount);
        }
    }

    public static class MaximumQueryCountExceededException extends RuntimeException {
        public MaximumQueryCountExceededException(final long max, final long actual) {
            super(getErrorMessage(max, actual));
        }

        public MaximumQueryCountExceededException(final String msg) {
            super(msg);
        }

        private static String getErrorMessage(final long max, final long actual) {
            return String.format("Defined maximum of %d exceeded: %d", max, actual);
        }
    }

    public static class QueryCountAssertionException extends RuntimeException {
        public QueryCountAssertionException(final long expected, final long actual) {
            super(String.format("Expected %d but was: %d", expected, actual));
        }
    }

}
