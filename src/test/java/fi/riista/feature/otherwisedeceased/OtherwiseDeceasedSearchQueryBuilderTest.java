package fi.riista.feature.otherwisedeceased;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.ValueGenerator;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;


public class OtherwiseDeceasedSearchQueryBuilderTest extends EmbeddedDatabaseTest {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    private EntitySupplier es;
    private OtherwiseDeceasedBuilder builder;

    @Before
    public void setUp() {
        es = getEntitySupplier();
        builder = OtherwiseDeceasedBuilder.create(es);
    }

    @Test
    public void testWithGameSpecies() {
        final OtherwiseDeceased expected = builder.build();
        builder.withSpecies(null).build();
        persistInNewTransaction();

        runInTransaction(() -> {
            final List<OtherwiseDeceased> actual = OtherwiseDeceasedSearchQueryBuilder.create(jpqlQueryFactory)
                    .withGameSpecies(expected.getSpecies().getOfficialCode())
                    .list();
            assertThat(actual, hasSize(1));
            assertThat(actual.get(0).getId(), equalTo(expected.getId()));
            assertThat(actual.get(0).getSpecies().getOfficialCode(), equalTo(expected.getSpecies().getOfficialCode()));
        });
    }

    @Test
    public void testWithBeginDate() {
        final OtherwiseDeceased expected = builder.build();
        builder.withPointOfTime(expected.getPointOfTime().minusDays(1)).build();
        persistInNewTransaction();

        runInTransaction(() -> {
            final List<OtherwiseDeceased> actual = OtherwiseDeceasedSearchQueryBuilder.create(jpqlQueryFactory)
                    .withBeginDate(expected.getPointOfTime().toLocalDate())
                    .list();
            assertThat(actual, hasSize(1));
            assertThat(actual.get(0).getId(), equalTo(expected.getId()));
            assertThat(actual.get(0).getPointOfTime(), equalTo(expected.getPointOfTime()));
        });

    }

    @Test
    public void testWithEndDate() {
        final OtherwiseDeceased expected = builder.build();
        builder.withPointOfTime(expected.getPointOfTime().plusDays(1)).build();
        persistInNewTransaction();

        runInTransaction(() -> {
            final List<OtherwiseDeceased> actual = OtherwiseDeceasedSearchQueryBuilder.create(jpqlQueryFactory)
                    .withEndDate(expected.getPointOfTime().toLocalDate())
                    .list();
            assertThat(actual, hasSize(1));
            assertThat(actual.get(0).getId(), equalTo(expected.getId()));
            assertThat(actual.get(0).getPointOfTime(), equalTo(expected.getPointOfTime()));
        });
    }

    @Test
    public void testWithRka() {
        final OtherwiseDeceased expected = builder.build();
        builder.withRka(null).build();
        persistInNewTransaction();

        runInTransaction(() -> {
            final List<OtherwiseDeceased> actual = OtherwiseDeceasedSearchQueryBuilder.create(jpqlQueryFactory)
                    .withRka(expected.getRka().getOfficialCode())
                    .list();
            assertThat(actual, hasSize(1));
            assertThat(actual.get(0).getId(), equalTo(expected.getId()));
            assertThat(actual.get(0).getRka(), equalTo(expected.getRka()));
        });
    }

    @Test
    public void testWithRhy() {
        final OtherwiseDeceased expected = builder.build();
        builder.withRhy(null).build();
        persistInNewTransaction();

        runInTransaction(() -> {
            final List<OtherwiseDeceased> actual = OtherwiseDeceasedSearchQueryBuilder.create(jpqlQueryFactory)
                    .withRhy(expected.getRhy().getOfficialCode())
                    .list();
            assertThat(actual, hasSize(1));
            assertThat(actual.get(0).getId(), equalTo(expected.getId()));
            assertThat(actual.get(0).getRhy(), equalTo(expected.getRhy()));
        });
    }

    @Test
    public void testWithCause() {
        final OtherwiseDeceased expected = builder.build();
        builder.withCause(someOtherThan(expected.getCause(), OtherwiseDeceasedCause.class)).build();
        persistInNewTransaction();

        runInTransaction(() -> {
            final List<OtherwiseDeceased> actual = OtherwiseDeceasedSearchQueryBuilder.create(jpqlQueryFactory)
                    .withCause(expected.getCause())
                    .list();
            assertThat(actual, hasSize(1));
            assertThat(actual.get(0).getId(), equalTo(expected.getId()));
            assertThat(actual.get(0).getCause(), equalTo(expected.getCause()));
        });
    }

    @Test
    public void testWithShowRejected() {
        final OtherwiseDeceased expected = builder.build();
        builder.withRejected(!expected.isRejected()).build();
        persistInNewTransaction();

        runInTransaction(() -> {
            final List<OtherwiseDeceased> actual = OtherwiseDeceasedSearchQueryBuilder.create(jpqlQueryFactory)
                    .withRejected(expected.isRejected())
                    .list();
            assertThat(actual, hasSize(1));
            assertThat(actual.get(0).getId(), equalTo(expected.getId()));
            assertThat(actual.get(0).isRejected(), equalTo(expected.isRejected()));
        });
    }

    @Test
    public void testWithFilter() {
        final OtherwiseDeceased expected = builder.build();
        // Generate other items that mismatch only by one attribute
        builder.clone().withPointOfTime(expected.getPointOfTime().minusDays(1)).build();
        builder.clone().withPointOfTime(expected.getPointOfTime().plusDays(1)).build();
        builder.clone().withSpecies(null).build();
        builder.clone().withRka(null).build();
        builder.clone().withRhy(null).build();
        builder.clone().withCause(someOtherThan(expected.getCause(), OtherwiseDeceasedCause.class)).build();
        builder.clone().withRejected(!expected.isRejected()).build();
        persistInNewTransaction();

        final OtherwiseDeceasedFilterDTO filter = new OtherwiseDeceasedFilterDTO();
        filter.setGameSpeciesCode(expected.getSpecies().getOfficialCode());
        filter.setBeginDate(expected.getPointOfTime().toLocalDate());
        filter.setEndDate(expected.getPointOfTime().toLocalDate());
        filter.setRkaOfficialCode(expected.getRka().getOfficialCode());
        filter.setRhyOfficialCode(expected.getRhy().getOfficialCode());
        filter.setCause(expected.getCause());
        filter.setShowRejected(expected.isRejected());

        runInTransaction(() -> {
            final List<OtherwiseDeceased> actual = OtherwiseDeceasedSearchQueryBuilder.create(jpqlQueryFactory)
                    .withFilter(filter)
                    .list();
            assertThat(actual, hasSize(1));
            assertThat(actual.get(0).getId(), equalTo(expected.getId()));
        });
    }

    // Helper builder

    public static class OtherwiseDeceasedBuilder {

        public static OtherwiseDeceasedBuilder create(final EntitySupplier es) {
            return new OtherwiseDeceasedBuilder(es);
        }

        private final EntitySupplier es;
        private DateTime pointOfTime;
        private OtherwiseDeceasedCause cause;
        private GameSpecies species;
        private Riistanhoitoyhdistys rhy;
        private RiistakeskuksenAlue rka;
        private boolean rejected;

        public OtherwiseDeceasedBuilder(final EntitySupplier es) {
            this.es = es;
        }

        public OtherwiseDeceasedBuilder withPointOfTime(final DateTime pointOfTime) {
            this.pointOfTime = pointOfTime;
            return this;
        }

        public OtherwiseDeceasedBuilder withCause(final OtherwiseDeceasedCause cause) {
            this.cause = cause;
            return this;
        }


        public OtherwiseDeceasedBuilder withSpecies(final GameSpecies species) {
            this.species = species;
            return this;
        }

        public OtherwiseDeceasedBuilder withRhy(final Riistanhoitoyhdistys rhy) {
            this.rhy = rhy;
            return this;
        }

        public OtherwiseDeceasedBuilder withRka(final RiistakeskuksenAlue rka) {
            this.rka = rka;
            return this;
        }

        public OtherwiseDeceasedBuilder withRejected(final boolean rejected) {
            this.rejected = rejected;
            return this;
        }

        @Override
        public OtherwiseDeceasedBuilder clone() {
            generateDataIfRequired();
            return OtherwiseDeceasedBuilder.create(es)
                    .withPointOfTime(pointOfTime)
                    .withCause(cause)
                    .withSpecies(species)
                    .withRhy(rhy)
                    .withRka(rka)
                    .withRejected(rejected);
        }

        public OtherwiseDeceased build() {
            generateDataIfRequired();
            return es.newOtherwiseDeceased(pointOfTime, cause, species, rhy, rka, rejected);
        }

        private void generateDataIfRequired() {
            if (pointOfTime == null) {
                pointOfTime = DateUtil.now();
            }

            if (cause == null) {
                cause = ValueGenerator.some(OtherwiseDeceasedCause.class, es.getNumberGenerator()); // some()
            }

            if (species == null) {
                species = es.newGameSpecies();
            }

            if (rka == null) {
                rka = es.newRiistakeskuksenAlue();
                rhy = null; // Ensure that rhy belongs to the new RKA
            }

            if (rhy == null) {
                rhy = es.newRiistanhoitoyhdistys(rka);
            }
        }

    }

}
