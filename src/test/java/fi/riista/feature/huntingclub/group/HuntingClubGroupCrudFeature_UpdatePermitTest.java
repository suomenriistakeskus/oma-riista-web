package fi.riista.feature.huntingclub.group;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Resource;

import java.util.List;
import java.util.stream.Collectors;

import static fi.riista.feature.organization.occupation.OccupationType.RYHMAN_METSASTYKSENJOHTAJA;
import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.feature.organization.person.ContactInfoShare.SAME_PERMIT_LEVEL;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class HuntingClubGroupCrudFeature_UpdatePermitTest extends EmbeddedDatabaseTest
        implements HuntingGroupFixtureMixin {

    @Resource
    private HuntingClubGroupCrudFeature huntingClubGroupCrudFeature;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Test
    public void testPermitModifiedTimestamp_existingChanged() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();

        final HarvestPermit permit1 = model().newHarvestPermit(rhy);
        final HarvestPermit permit2 = model().newHarvestPermit(rhy);

        doTestPermitModifiedTimestamp(true,
                model().newHarvestPermitSpeciesAmount(permit1, species),
                model().newHarvestPermitSpeciesAmount(permit2, species));
    }

    @Test
    public void testPermitModifiedTimestamp_existingNulled() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final GameSpecies species = model().newGameSpecies();

        doTestPermitModifiedTimestamp(true,
                model().newHarvestPermitSpeciesAmount(permit, species),
                null);
    }

    @Test
    public void testPermitModifiedTimestamp_nullChanged() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermit permit = model().newHarvestPermit(rhy);

        doTestPermitModifiedTimestamp(true,
                null,
                model().newHarvestPermitSpeciesAmount(permit, species));
    }

    @Test
    public void testPermitModifiedTimestamp_nullKept() {
        doTestPermitModifiedTimestamp(false, null, null);
    }

    @Test
    public void testPermitModifiedTimestamp_existingKept() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final GameSpecies species = model().newGameSpecies();
        final HarvestPermitSpeciesAmount hpsa = model().newHarvestPermitSpeciesAmount(permit, species);

        doTestPermitModifiedTimestamp(false, hpsa, hpsa);
    }

    @Test
    public void testConnectingGroupToPermitCopiesContactShareInfoFromOtherOccupation() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final HarvestPermit permit = model().newMooselikePermit(rhy);
        final GameSpecies moose = model().newGameSpeciesMoose();
        final HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(permit, moose);

        withHuntingGroupFixture(spa, group1fixture -> {

            group1fixture.groupLeaderOccupation.setContactInfoShare(SAME_PERMIT_LEVEL);
            group1fixture.groupLeaderOccupation.setNameVisibility(true);
            group1fixture.groupLeaderOccupation.setPhoneNumberVisibility(true);
            group1fixture.groupLeaderOccupation.setEmailVisibility(true);

            final HuntingClub club = model().newHuntingClub(rhy);
            final Occupation clubContact = model().newOccupation(club, model().newPerson(rhy), SEURAN_YHDYSHENKILO);
            final HuntingClubGroup group = model().newHuntingClubGroup(club, moose);
            final Occupation leader =
                    model().newOccupation(group, group1fixture.groupLeader, RYHMAN_METSASTYKSENJOHTAJA);


            onSavedAndAuthenticated(createUser(clubContact.getPerson()), () -> {
                HuntingClubGroupDTO dto = new HuntingClubGroupDTO();
                dto.setId(group.getId());
                dto.setPermit(HuntingClubGroupDTO.PermitDTO.create(permit));
                dto.setGameSpeciesCode(moose.getOfficialCode());
                dto.setHuntingYear(permit.getPermitYear());
                dto.setClubId(club.getId());
                dto.setNameFI("FI");
                dto.setNameSV("SV");
                huntingClubGroupCrudFeature.update(dto);

            });

            final List<Occupation> activeOccupations = occupationRepository.findActiveByPerson(group1fixture.groupLeader).stream()
                    .filter(occ -> occ.getOccupationType() == RYHMAN_METSASTYKSENJOHTAJA)
                    .filter(occ -> occ.getOrganisation().equals(group))
                    .collect(Collectors.toList());

            assertThat(activeOccupations, hasSize(1));
            activeOccupations.forEach(occ -> {
                assertThat(occ.getContactInfoShare(), equalTo(SAME_PERMIT_LEVEL));
                assertThat(occ.isNameVisibility(), equalTo(true));
                assertThat(occ.isPhoneNumberVisibility(), equalTo(true));
                assertThat(occ.isEmailVisibility(), equalTo(true));
            });

        });
    }

    private void doTestPermitModifiedTimestamp(
            final boolean permitModificationTimeIsChanged,
            final HarvestPermitSpeciesAmount speciesAmount,
            final HarvestPermitSpeciesAmount speciesAmount2) {

        final HarvestPermit originalPermit = speciesAmount != null ? speciesAmount.getHarvestPermit() : null;
        final HarvestPermit newPermit = speciesAmount2 != null ? speciesAmount2.getHarvestPermit() : null;

        final GameSpecies species = speciesAmount != null
                ? speciesAmount.getGameSpecies()
                : speciesAmount2 != null ? speciesAmount2.getGameSpecies() : model().newGameSpecies();

        withRhy(rhy -> withPerson(person -> {
            final HuntingClub club = model().newHuntingClub(rhy);
            final HuntingClubArea area = model().newHuntingClubArea(club);
            final HuntingClubGroup group = model().newHuntingClubGroup(club, species);
            group.setHuntingYear(area.getHuntingYear());
            group.setHuntingArea(area);
            model().newOccupation(club, person, OccupationType.SEURAN_YHDYSHENKILO);

            group.updateHarvestPermit(originalPermit);

            onSavedAndAuthenticated(createUser(person), () -> {
                final DateTime originalPermitUpdateTime = group.getHarvestPermitModificationTime();
                huntingClubGroupCrudFeature.update(HuntingClubGroupDTO.create(group, group.getSpecies(), newPermit));

                runInTransaction(() -> {
                    final HuntingClubGroup updatedGroup = huntingClubGroupRepository.getOne(group.getId());

                    if (permitModificationTimeIsChanged) {
                        assertNotEquals(originalPermitUpdateTime, updatedGroup.getHarvestPermitModificationTime());
                    } else {
                        assertEquals(originalPermitUpdateTime, updatedGroup.getHarvestPermitModificationTime());
                    }
                });
            });
        }));
    }
}
