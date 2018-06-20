package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.common.entity.Required;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.fields.RequiredHarvestFields;
import fi.riista.util.F;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.EnumSet;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
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
        return harvest;
    }

    @Test
    public void testValidateAll_AllRequired_Success() {
        final Harvest harvest = createHarvestWithAllFields();
        final RequiredHarvestFields.Report fields = createFields(Required.YES);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateAll_AllRequired_Failure() {
        final Harvest harvest = new Harvest();
        final RequiredHarvestFields.Report fields = createFields(Required.YES);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

        validator.validateAll();

        assertTrue(validator.hasErrors());
        assertThat(validator.getIllegalFields(), hasSize(0));

        final EnumSet<HarvestFieldName> es = EnumSet.complementOf(EnumSet.of(HarvestFieldName.HUNTING_PARTY));
        assertThat(validator.getMissingFields(), containsInAnyOrder(F.mapNonNullsToSet(es, Matchers::equalTo)));
    }

    @Test
    public void testValidateAll_AllVoluntary_Given() {
        final Harvest harvest = createHarvestWithAllFields();
        final RequiredHarvestFields.Report fields = createFields(Required.VOLUNTARY);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateAll_AllVoluntary_Missing() {
        final Harvest harvest = new Harvest();
        final RequiredHarvestFields.Report fields = createFields(Required.VOLUNTARY);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateAll_AllIllegal_Success() {
        final Harvest harvest = new Harvest();
        final RequiredHarvestFields.Report fields = createFields(Required.NO);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

        validator.validateAll();

        assertFalse(validator.hasErrors());
        assertThat(validator.getMissingFields(), hasSize(0));
        assertThat(validator.getIllegalFields(), hasSize(0));
    }

    @Test
    public void testValidateAll_AllIllegal_Failure() {
        final Harvest harvest = createHarvestWithAllFields();
        final RequiredHarvestFields.Report fields = createFields(Required.NO);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

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

        final RequiredHarvestFields.Report fields = createFields(Required.NO, Required.YES);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

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

        final RequiredHarvestFields.Report fields = createFields(Required.NO, Required.YES);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

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

        final RequiredHarvestFields.Report fields = createFields(Required.NO, Required.YES);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

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

        final RequiredHarvestFields.Report fields = createFields(Required.NO, Required.YES);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

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

        final RequiredHarvestFields.Report fields = createFields(Required.NO, Required.YES);
        final HarvestFieldValidator validator = new HarvestFieldValidator(fields, harvest);

        validator.validateAll();

        assertTrue(validator.hasErrors());
        assertThat(validator.getIllegalFields(), containsInAnyOrder(HarvestFieldName.HUNTING_PARTY));
        assertThat(validator.getMissingFields(), hasSize(0));
    }

    private RequiredHarvestFields.Report createFields(final Required required) {
        return createFields(required, required);
    }

    private RequiredHarvestFields.Report createFields(final Required required, final Required huntingAreaTypeAndParty) {
        // bogus constructor fields, because all methods are overridden
        return new RequiredHarvestFields.Report(2017, 1, HarvestReportingType.BASIC) {
            @Override
            public Required getPermitNumber() {
                return required;
            }

            @Override
            public Required getHarvestArea() {
                return required;
            }

            @Override
            public Required getHuntingMethod() {
                return required;
            }

            @Override
            public Required getFeedingPlace() {
                return required;
            }

            @Override
            public Required getTaigaBeanGoose() {
                return required;
            }

            @Override
            public Required getLukeStatus() {
                return required;
            }

            @Override
            public Required getHuntingAreaType() {
                return huntingAreaTypeAndParty;
            }

            @Override
            public Required getHuntingParty() {
                return huntingAreaTypeAndParty;
            }

            @Override
            public Required getHuntingAreaSize() {
                return required;
            }

            @Override
            public Required getReportedWithPhoneCall() {
                return required;
            }
        };
    }
}
