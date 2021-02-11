package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.fixture.HarvestDTOBuilderFactory;
import fi.riista.feature.gamediary.harvest.mutation.exception.HarvestPermitNotApplicableForDeerHuntingException;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenAssertionBuilder;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenValidationException;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.junit.Test;

import java.util.List;

import static fi.riista.feature.gamediary.DeerHuntingType.DOG_HUNTING;
import static fi.riista.feature.gamediary.DeerHuntingType.OTHER;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.harvest.Harvest.StateAcceptedToHarvestPermit.PROPOSED;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion.CURRENTLY_SUPPORTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

public class HarvestFeature_DeerPilotTest extends HarvestFeatureTestBase
        implements HarvestDTOBuilderFactory, HuntingGroupFixtureMixin {

    @Override
    public HarvestSpecVersion getDefaultSpecVersion() {
        return CURRENTLY_SUPPORTED;
    }

    @Test
    public void testCreate_withDeerHuntingType() {
        final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();

        withHuntingGroupFixture(species, fixture -> {

            model().newDeerPilot(fixture.permit);

            final Person author = fixture.groupMember;
            final Person actor = fixture.clubMember;

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO inputDto = create(species)
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .withDeerHuntingType(DOG_HUNTING)
                        .withSpecimen(ADULT_MALE)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    assertThat(harvest.getDeerHuntingType(), is(DOG_HUNTING));
                    assertThat(harvest.getDeerHuntingOtherTypeDescription(), is(nullValue()));

                    assertAuthorAndActor(harvest, F.getId(author), F.getId(actor));
                });
            });
        });
    }

    @Test
    public void testCreate_withDeerHuntingType_usingPermitNumber() {
        withPerson(author -> withPerson(actor -> {

            final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();
            final HarvestPermit permit = model().newHarvestPermit();

            model().newHarvestPermitSpeciesAmount(permit, species);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(species)
                        .withPermitNumber(permit.getPermitNumber())
                        .withAuthorInfo(author)
                        .withActorInfo(actor)
                        .withDeerHuntingType(DOG_HUNTING)
                        .withSpecimen(ADULT_MALE)
                        .build();

                assertCreateThrows(HarvestPermitNotApplicableForDeerHuntingException.class, dto);
            });
        }));
    }

    @Test
    public void testCreate_withNewAntlerFields() {
        final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();

        withHuntingGroupFixture(species, fixture -> {

            model().newDeerPilot(fixture.permit);

            onSavedAndAuthenticated(createUser(fixture.groupMember), () -> {

                final HarvestDTO inputDto = create(species)
                        .withSpecimen(ADULT_MALE)
                        .build();

                final HarvestDTO outputDto = invokeCreateHarvest(inputDto);

                runInTransaction(() -> {
                    final Harvest harvest = assertHarvestCreated(outputDto.getId());

                    final List<HarvestSpecimen> specimens = findSpecimens(harvest);
                    assertThat(specimens, hasSize(1));

                    HarvestSpecimenAssertionBuilder.builder()
                            .whiteTailedDeerAdultMaleFieldsPresent()
                            .whiteTailedDeerFields2020EqualTo(inputDto.getSpecimens().get(0))
                            .verify(specimens.get(0));
                });
            });
        });
    }

    @Test
    public void testCreate_withNewAntlerFields_whenDeerPilotNotActive() {
        final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();

        withHuntingGroupFixture(species, fixture -> {

            onSavedAndAuthenticated(createUser(fixture.groupMember), () -> {

                final HarvestDTO inputDto = create(species)
                        .withSpecimen(ADULT_MALE)
                        .build();

                // Populate `antlersWidth` manually because it is not supported in specVersion 8
                // but expected to exist in specVersion 7 assertions.
                final HarvestSpecimenDTO specimenDTO = inputDto.getSpecimens().get(0);
                specimenDTO.setAntlersWidth(nextPositiveIntAtMost(100));

                assertThrows(HarvestSpecimenValidationException.class, () -> invokeCreateHarvest(inputDto));
            });
        });
    }

    @Test
    public void testUpdate_withDeerHuntingType() {
        final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();

        withHuntingGroupFixture(species, fixture -> {

            model().newDeerPilot(fixture.permit);

            final Person author = fixture.groupMember;

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setDeerHuntingType(OTHER);
            harvest.setDeerHuntingOtherTypeDescription("Other description");

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withDeerHuntingType(DOG_HUNTING)
                        .withDeerHuntingOtherTypeDescription(null)
                        .withSpecimen(ADULT_MALE)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertThat(updated.getDeerHuntingType(), is(DOG_HUNTING));
                    assertThat(updated.getDeerHuntingOtherTypeDescription(), is(nullValue()));
                });
            });
        });
    }

    @Test
    public void testUpdate_whenChangingPermitAssociatedHarvestToDeerHarvest() {
        final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();

        withHuntingGroupFixture(species, fixture -> {

            model().newDeerPilot(fixture.permit);

            final Person author = fixture.groupMember;

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(fixture.permit);
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            harvest.setRhy(fixture.rhy);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withDeerHuntingType(DOG_HUNTING)
                        .withPermitNumber(null)
                        .withSpecimen(ADULT_MALE)
                        .build();

                invokeUpdateHarvest(dto);

                runInTransaction(() -> {
                    final Harvest updated = harvestRepo.getOne(harvest.getId());
                    assertVersion(updated, 1);

                    assertThat(updated.getDeerHuntingType(), is(DOG_HUNTING));
                    assertThat(updated.getHarvestPermit(), is(nullValue()));
                });
            });
        });
    }

    @Test
    public void testUpdate_deerHuntingTypeNotAllowedForNonPilotPermit() {
        final GameSpecies species = model().newGameSpeciesWhiteTailedDeer();

        withRhy(rhy -> withHuntingGroupFixture(rhy, species, fixture -> {

            model().newDeerPilot(fixture.permit);

            final HarvestPermit otherPermit = model().newHarvestPermit(rhy);
            model().newHarvestPermitSpeciesAmount(otherPermit, species);

            final Person author = fixture.groupMember;

            final Harvest harvest = model().newHarvest(species, author);
            harvest.setHarvestPermit(otherPermit);
            harvest.setStateAcceptedToHarvestPermit(PROPOSED);
            harvest.setRhy(rhy);

            onSavedAndAuthenticated(createUser(author), () -> {

                final HarvestDTO dto = create(harvest)
                        .withDeerHuntingType(DOG_HUNTING)
                        .withSpecimen(ADULT_MALE)
                        .build();

                assertUpdateThrows(HarvestPermitNotApplicableForDeerHuntingException.class, dto);

                runInTransaction(() -> {
                    assertVersion(harvestRepo.getOne(harvest.getId()), 0);
                });
            });
        }));
    }
}
