package fi.riista.feature.otherwisedeceased;

import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.integration.common.export.otherwisedeceased.ODA_DeceasedAnimal;
import fi.riista.integration.common.export.otherwisedeceased.ODA_DeceasedAnimals;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Optional;

import static fi.riista.feature.otherwisedeceased.OtherwiseDeceasedCause.OTHER;
import static fi.riista.feature.otherwisedeceased.OtherwiseDeceasedCause.UNDER_INVESTIGATION;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.currentYear;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assume.assumeTrue;

@RunWith(Theories.class)
public class OtherwiseDeceasedExportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private OtherwiseDeceasedExportFeature exportFeature;

    private SystemUser privilegedApiUser;

    @Before
    public void setUp() {
        privilegedApiUser = createNewApiUser(SystemUserPrivilege.EXPORT_OTHERWISE_DECEASED);
    }

    @Test(expected = AccessDeniedException.class)
    public void export_failsWithoutPrivilege() {
        onSavedAndAuthenticated(createNewApiUser(), () -> exportFeature.export(2021));
    }

    @Test
    public void export_emptyIfNoItemsFound() {
        onSavedAndAuthenticated(privilegedApiUser, () -> {
            final ODA_DeceasedAnimals result = exportFeature.export(2021);
            assertThat(result.getDeceasedAnimal(), hasSize(0));
        });
    }

    @Test
    public void export_returnsOnlyRequestedYearItems() {
        final EntitySupplier es = getEntitySupplier();
        final DateTime beginOfYear = new DateTime(2021, 1, 1, 0, 0, Constants.DEFAULT_TIMEZONE);
        final DateTime endOfLastYear = new DateTime(2020, 12, 31, 23, 59, 59, 999, Constants.DEFAULT_TIMEZONE);
        final DateTime beginOfNextYear = new DateTime(2022, 1, 1, 0, 0, Constants.DEFAULT_TIMEZONE);

        entityBuilder(es).withPointOfTime(beginOfYear).isIncluded().build();
        entityBuilder(es).withPointOfTime(endOfLastYear).isIncluded().build();
        entityBuilder(es).withPointOfTime(beginOfNextYear).isIncluded().build();

        onSavedAndAuthenticated(privilegedApiUser, () -> {
            final ODA_DeceasedAnimals result = exportFeature.export(2021);
            assertThat(result.getDeceasedAnimal(), hasSize(1));
            assertThat(result.getDeceasedAnimal().get(0).getPointOfTime(), equalTo(beginOfYear.toLocalDateTime()));
        });
    }

    @Test
    public void export_hasNoRejectedOrUnderInvestigationItems() {
        entityBuilder(getEntitySupplier()).withIsRejected(true).withCause(OTHER).build();
        entityBuilder(getEntitySupplier()).withIsRejected(false).withCause(UNDER_INVESTIGATION).build();
        entityBuilder(getEntitySupplier()).withIsRejected(true).withCause(UNDER_INVESTIGATION).build();

        onSavedAndAuthenticated(privilegedApiUser, () -> {
            final ODA_DeceasedAnimals result = exportFeature.export(currentYear());
            assertThat(result.getDeceasedAnimal(), hasSize(0));
        });
    }

    @Theory
    public void export_returnsAllCausesButUnderInvestigation(final OtherwiseDeceasedCause cause) {
        assumeTrue(cause != UNDER_INVESTIGATION); // Not tested here

        entityBuilder(getEntitySupplier()).withIsRejected(false).withCause(cause).build();
        onSavedAndAuthenticated(privilegedApiUser, () -> {
            final ODA_DeceasedAnimals result = exportFeature.export(currentYear());
            assertThat(result.getDeceasedAnimal(), hasSize(1), "should have one result");
            assertThat(result.getDeceasedAnimal().get(0).getCause().value(), equalTo(cause.name()), "cause mismatch");
        });
    }

    @Theory
    public void export_returnsAllAgesAndGenders(final GameAge age, final GameGender gender) {
        entityBuilder(getEntitySupplier()).withAge(age).withGender(gender).isIncluded().build();
        onSavedAndAuthenticated(privilegedApiUser, () -> {
            final ODA_DeceasedAnimals result = exportFeature.export(currentYear());
            assertThat(result.getDeceasedAnimal(), hasSize(1), "should have one result");
            assertThat(result.getDeceasedAnimal().get(0).getAge().value(), equalTo(age.name()), "age mismatch");
            assertThat(result.getDeceasedAnimal().get(0).getGender().value(), equalTo(gender.name()), "gender mismatch");
        });
    }

    @Theory
    public void export_returnFieldsCorrectly(final boolean noExactLocation) {
        final OtherwiseDeceased expected = entityBuilder(getEntitySupplier()).withNoExactLocation(noExactLocation).isIncluded().build();
        onSavedAndAuthenticated(privilegedApiUser, () -> {
            final ODA_DeceasedAnimals result = exportFeature.export(currentYear());
            assertThat(result.getDeceasedAnimal(), hasSize(1), "should have one result");
            final ODA_DeceasedAnimal actual = result.getDeceasedAnimal().get(0);
            assertThat(actual.getGameSpeciesCode(), equalTo(expected.getSpecies().getOfficialCode()), "species mismatch");
            assertThat(actual.getPointOfTime(), equalTo(expected.getPointOfTime().toLocalDateTime()), "pointOfTime mismatch");
            assertThat(actual.getGeoLocation().getLatitude(), equalTo(expected.getGeoLocation().getLatitude()), "latitude mismatch");
            assertThat(actual.getGeoLocation().getLongitude(), equalTo(expected.getGeoLocation().getLongitude()), "longitude mismatch");
            assertThat(actual.getGeoLocation().isNoExactLocation(), equalTo(expected.getNoExactLocation()), "noExactLocation mismatch");
            assertThat(actual.getCauseOther(), equalTo(expected.getCauseOther()), "causeOther mismatch");
            assertThat(actual.getDescription(), equalTo(expected.getDescription()), "description mismatch");
            assertThat(actual.getAge().value(), equalTo(expected.getAge().name()), "age mismatch");
            assertThat(actual.getGender().value(), equalTo(expected.getGender().name()), "gender mismatch");
            assertThat(actual.getCause().value(), equalTo(expected.getCause().name()), "cause mismatch");
        });
    }

    // Just authorisation tests for XML API. Content is tested by JSON tests and we trust in conversion.

    @Test(expected = AccessDeniedException.class)
    public void exportXml_failsWithoutPrivilege() {
        onSavedAndAuthenticated(createNewApiUser(), () -> exportFeature.exportXml(2021));
    }

    @Test
    public void exportXml_succeedsWithPrivilege() {
        onSavedAndAuthenticated(privilegedApiUser, () -> exportFeature.exportXml(2021));
    }

    /** Builder */

    private static OtherwiseDeceasedBuilder entityBuilder(final EntitySupplier es) {
        return new OtherwiseDeceasedBuilder().withEntitySupplier(es);
    }

    public static class OtherwiseDeceasedBuilder {

        private EntitySupplier es;
        private GameAge age;
        private GameGender gender;
        private DateTime pointOfTime;
        private boolean isRejected;
        private OtherwiseDeceasedCause cause;
        private boolean noExactLocation;

        public OtherwiseDeceasedBuilder withEntitySupplier(final EntitySupplier es) {
            this.es = es;
            return this;
        }

        public OtherwiseDeceasedBuilder withAge(final GameAge age) {
            this.age = age;
            return this;
        }

        public OtherwiseDeceasedBuilder withGender(final GameGender gender) {
            this.gender = gender;
            return this;
        }

        public OtherwiseDeceasedBuilder withPointOfTime(final DateTime pointOfTime) {
            this.pointOfTime = pointOfTime;
            return this;
        }

        public OtherwiseDeceasedBuilder withIsRejected(final boolean isRejected) {
            this.isRejected = isRejected;
            return this;
        }

        public OtherwiseDeceasedBuilder withCause(final OtherwiseDeceasedCause cause) {
            this.cause = cause;
            return this;
        }

        public OtherwiseDeceasedBuilder withNoExactLocation(final boolean noExactLocation) {
            this.noExactLocation = noExactLocation;
            return this;
        }

        public OtherwiseDeceasedBuilder isIncluded() {
            this.cause = OTHER;
            this.isRejected = false;
            return this;
        }

        public OtherwiseDeceased build() {
            requireNonNull(es, "EntitySupplier is null");

            final OtherwiseDeceased entity = es.newOtherwiseDeceased();
            Optional.ofNullable(age).ifPresent(entity::setAge);
            Optional.ofNullable(gender).ifPresent(entity::setGender);
            Optional.ofNullable(pointOfTime).ifPresent(entity::setPointOfTime);
            Optional.ofNullable(cause).ifPresent(entity::setCause);
            entity.setRejected(isRejected);
            entity.setNoExactLocation(noExactLocation);

            return entity;
        }
    }
}
