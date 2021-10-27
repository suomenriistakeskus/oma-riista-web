package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.DeerHuntingType;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestField;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFieldsImpl;
import fi.riista.util.F;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.EnumSet;

import static fi.riista.feature.gamediary.harvest.fields.RequiredHarvestField.NO;
import static fi.riista.feature.gamediary.harvest.fields.RequiredHarvestField.VOLUNTARY;
import static fi.riista.feature.gamediary.harvest.fields.RequiredHarvestField.YES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HarvestFieldValidatorTest {

    private static Harvest createHarvestWithAllFields() {
        final Harvest harvest = new Harvest();
        harvest.setHuntingMethod(HuntingMethod.SHOT);
        harvest.setFeedingPlace(Boolean.FALSE);
        harvest.setSubSpeciesCode(GameSpecies.OFFICIAL_CODE_TAIGA_BEAN_GOOSE);
        harvest.setHuntingAreaType(HuntingAreaType.HUNTING_SOCIETY);
        harvest.setHuntingAreaSize(1.0);
        harvest.setHuntingParty("a");
        harvest.setReportedWithPhoneCall(Boolean.TRUE);
        harvest.setDeerHuntingType(DeerHuntingType.DOG_HUNTING);
        return harvest;
    }

    @Test
    public void testValidateAll_AllRequired_Success() {
        final Harvest harvest = createHarvestWithAllFields();
        final RequiredHarvestFields.Report fields = createFields(YES);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateAll_AllRequired_Failure() {
        final Harvest harvest = new Harvest();
        final RequiredHarvestFields.Report fields = createFields(YES);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertTrue(validator.hasErrors());
        assertThat(validator.getIllegalFields(), hasSize(0));

        final EnumSet<HarvestFieldName> es = EnumSet.complementOf(EnumSet.of(HarvestFieldName.HUNTING_PARTY));
        assertThat(validator.getMissingFields(), containsInAnyOrder(F.mapNonNullsToSet(es, Matchers::equalTo)));
    }

    @Test
    public void testValidateAll_AllVoluntary_Given() {
        final Harvest harvest = createHarvestWithAllFields();
        final RequiredHarvestFields.Report fields = createFields(VOLUNTARY);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateAll_AllVoluntary_Missing() {
        final Harvest harvest = new Harvest();
        final RequiredHarvestFields.Report fields = createFields(VOLUNTARY);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateAll_AllIllegal_Success() {
        final Harvest harvest = new Harvest();
        final RequiredHarvestFields.Report fields = createFields(NO);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateAll_AllIllegal_Failure() {
        final Harvest harvest = createHarvestWithAllFields();
        final RequiredHarvestFields.Report fields = createFields(NO);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertTrue(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), containsInAnyOrder(
                F.mapNonNullsToSet(HarvestFieldName.values(), Matchers::equalTo)));
    }

    @Test
    public void testValidateHuntingParty_Society_Valid() {
        final Harvest harvest = new Harvest();
        harvest.setHuntingAreaType(HuntingAreaType.HUNTING_SOCIETY);
        harvest.setHuntingParty("a");

        final RequiredHarvestFields.Report fields = createFieldsForHuntingParty(NO, YES);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateHuntingParty_Society_Missing() {
        final Harvest harvest = new Harvest();
        harvest.setHuntingAreaType(HuntingAreaType.HUNTING_SOCIETY);
        harvest.setHuntingParty(null);

        final RequiredHarvestFields.Report fields = createFieldsForHuntingParty(NO, YES);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertTrue(validator.hasErrors());
        assertThat(validator.getMissingFields(), containsInAnyOrder(HarvestFieldName.HUNTING_PARTY));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateHuntingParty_Society_Empty() {
        final Harvest harvest = new Harvest();
        harvest.setHuntingAreaType(HuntingAreaType.HUNTING_SOCIETY);
        harvest.setHuntingParty("");

        final RequiredHarvestFields.Report fields = createFieldsForHuntingParty(NO, YES);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertTrue(validator.hasErrors());
        assertThat(validator.getMissingFields(), containsInAnyOrder(HarvestFieldName.HUNTING_PARTY));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateHuntingParty_Property_Valid() {
        final Harvest harvest = new Harvest();
        harvest.setHuntingAreaType(HuntingAreaType.PROPERTY);
        harvest.setHuntingParty(null);

        final RequiredHarvestFields.Report fields = createFieldsForHuntingParty(NO, YES);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateHuntingParty_Property_Illegal() {
        final Harvest harvest = new Harvest();
        harvest.setHuntingAreaType(HuntingAreaType.PROPERTY);
        harvest.setHuntingParty("a");

        final RequiredHarvestFields.Report fields = createFieldsForHuntingParty(NO, YES);
        final HarvestFieldValidator validator = createValidator(fields, harvest);

        validator.validateAll();

        assertTrue(validator.hasErrors());
        assertThat(validator.getIllegalFields(), containsInAnyOrder(HarvestFieldName.HUNTING_PARTY));
        assertThat(validator.getMissingFields(), hasSize(0));
    }

    @Test
    public void testValidateDeerHuntingType_whenUserInPilot() {
        final Harvest harvest = new Harvest();
        harvest.setDeerHuntingType(DeerHuntingType.DOG_HUNTING);

        final RequiredHarvestFields.Report fields = createFieldsForDeerHuntingType(NO, VOLUNTARY);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateDeerHuntingType_whenUserNotInPilot() {
        final Harvest harvest = new Harvest();
        harvest.setDeerHuntingType(DeerHuntingType.DOG_HUNTING);

        final RequiredHarvestFields.Report fields = createFieldsForDeerHuntingType(NO, NO);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

        validator.validateAll();

        assertTrue(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(1));
    }

    private static RequiredHarvestFields.Report createFields(final RequiredHarvestField required) {
        return createFields(required, required, required);
    }

    private static RequiredHarvestFields.Report createFieldsForHuntingParty(final RequiredHarvestField defaultValue,
                                                                            final RequiredHarvestField huntingAreaTypeAndParty) {

        return createFields(defaultValue, huntingAreaTypeAndParty, defaultValue);
    }

    private static RequiredHarvestFields.Report createFieldsForDeerHuntingType(final RequiredHarvestField defaultValue,
                                                                               final RequiredHarvestField deerHuntingType) {
        return createFields(defaultValue, defaultValue, deerHuntingType);
    }

    private static RequiredHarvestFields.Report createFields(final RequiredHarvestField defaultValue,
                                                             final RequiredHarvestField huntingAreaTypeAndParty,
                                                             final RequiredHarvestField deerHuntingType) {

        // bogus constructor fields, because all methods are overridden
        return new RequiredHarvestFieldsImpl.ReportImpl(2017, 1, HarvestReportingType.BASIC) {
            @Override
            public RequiredHarvestField getPermitNumber() {
                return defaultValue;
            }

            @Override
            public RequiredHarvestField getHarvestArea() {
                return defaultValue;
            }

            @Override
            public RequiredHarvestField getHuntingMethod() {
                return defaultValue;
            }

            @Override
            public RequiredHarvestField getFeedingPlace() {
                return defaultValue;
            }

            @Override
            public RequiredHarvestField getTaigaBeanGoose() {
                return defaultValue;
            }

            @Override
            public RequiredHarvestField getLukeStatus() {
                return defaultValue;
            }

            @Override
            public RequiredHarvestField getHuntingAreaType() {
                return huntingAreaTypeAndParty;
            }

            @Override
            public RequiredHarvestField getHuntingParty() {
                return huntingAreaTypeAndParty;
            }

            @Override
            public RequiredHarvestField getHuntingAreaSize() {
                return defaultValue;
            }

            @Override
            public RequiredHarvestField getReportedWithPhoneCall() {
                return defaultValue;
            }

            @Override
            public RequiredHarvestField getDeerHuntingType() {
                return deerHuntingType;
            }
        };
    }

    // Deer pilot flag is expected to be meaningless/ineffective when calling this.
    private static HarvestFieldValidator createValidator(final RequiredHarvestFields.Report requirements,
                                                         final Harvest harvest) {

        return new HarvestFieldValidator(requirements, harvest);
    }
}
