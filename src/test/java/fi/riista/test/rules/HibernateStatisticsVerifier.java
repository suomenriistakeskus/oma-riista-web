package fi.riista.test.rules;

import fi.riista.test.HibernateStatisticsHelper;
import org.hibernate.stat.Statistics;
import org.junit.rules.Verifier;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public abstract class HibernateStatisticsVerifier extends Verifier {

    private HibernateStatisticsAssertions assertions;

    protected abstract Statistics getStatistics();

    @Override
    public Statement apply(final Statement base, final Description description) {
        assertions = description.getAnnotation(HibernateStatisticsAssertions.class);

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                base.evaluate();

                if (assertions != null) {
                    verify();
                }
            }
        };
    }

    @Override
    public void verify() {
        checkAssertions(getStatistics());
    }

    protected void checkAssertions(final Statistics stats) {
        if (assertions.maxQueries() >= 0) {
            HibernateStatisticsHelper.assertCurrentQueryCountAtMost(stats, assertions.maxQueries());
        }
        if (assertions.queryCount() >= 0) {
            HibernateStatisticsHelper.assertCurrentQueryCount(stats, assertions.queryCount());
        }
    }
}
