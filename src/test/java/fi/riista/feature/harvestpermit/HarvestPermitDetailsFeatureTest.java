package fi.riista.feature.harvestpermit;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.harvestpermit.allocation.HarvestPermitAllocation;
import fi.riista.feature.harvestpermit.allocation.MoosePermitAllocationDTO;
import fi.riista.feature.harvestpermit.payment.HuntingClubPermitPaymentDTO;
import fi.riista.feature.harvestpermit.payment.HuntingClubPermitTotalPaymentDTO;
import fi.riista.feature.harvestpermit.payment.MooselikePrice;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.permit.HuntingClubPermitDTO;
import fi.riista.feature.huntingclub.permit.endofhunting.moosesummary.MooseHuntingSummary;
import fi.riista.feature.huntingclub.permit.partner.HarvestPermitPartnerDTO;
import fi.riista.feature.huntingclub.permit.statistics.HarvestCountDTO;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.NumberUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.stream.Stream;

import static fi.riista.util.DateUtil.today;
import static fi.riista.util.NumberUtils.bigDecimalEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HarvestPermitDetailsFeatureTest extends EmbeddedDatabaseTest {

    private static HarvestPermitPartnerDTO getPartner(final HuntingClubPermitDTO dto, final long partnerId) {
        return dto.getPartners().stream().filter(p -> Objects.equals(p.getHuntingClubId(), partnerId))
                .findFirst().orElse(null);
    }

    @Resource
    private HarvestPermitDetailsFeature harvestPermitDetailsFeature;

    private int dayCount = 0;

    private SystemUser user;
    private Person person;

    private GameSpecies species;
    private GameSpecies otherSpecies;
    private HuntingClub permitHolder;
    private HuntingClub permitPartner;

    private HarvestPermit permit;
    private HuntingClubGroup holderGroup;
    private HuntingClubGroup partnerGroup;
    private HuntingClubGroup partnerGroup2;
    private HarvestPermitSpeciesAmount speciesAmount;

    private HarvestPermit nextYearPermit;
    private HuntingClubGroup nextYearHolderGroup;
    private HuntingClubGroup nextYearPartnerGroup;
    private HuntingClubGroup nextYearPartnerGroup2;
    private HarvestPermitSpeciesAmount nextYearSpeciesAmount;

    private HuntingClubGroup otherSpeciesPartnerGroup;
    private HarvestPermitSpeciesAmount otherSpeciesAmount;

    @Before
    public void before() {
        user = createUserWithPerson();
        person = user.getPerson();

        species = model().newGameSpeciesMoose();
        otherSpecies = model().newGameSpecies(1, GameCategory.GAME_MAMMAL, "muu", "annan", "other");

        withRhy(rhy -> {
            final int currentHuntingYear = DateUtil.huntingYear();

            permitHolder = model().newHuntingClub(rhy);
            permitPartner = model().newHuntingClub(rhy);

            permit = createPermit(rhy, currentHuntingYear);
            speciesAmount = createSpeciesAmount(permit);
            holderGroup = model().newHuntingClubGroup(permitHolder, speciesAmount);
            partnerGroup = model().newHuntingClubGroup(permitPartner, speciesAmount);
            partnerGroup2 = model().newHuntingClubGroup(permitPartner, speciesAmount);

            nextYearPermit = createPermit(rhy, currentHuntingYear + 1);
            nextYearSpeciesAmount = createSpeciesAmount(nextYearPermit);
            nextYearHolderGroup = model().newHuntingClubGroup(permitHolder, nextYearSpeciesAmount);
            nextYearPartnerGroup = model().newHuntingClubGroup(permitPartner, nextYearSpeciesAmount);
            nextYearPartnerGroup2 = model().newHuntingClubGroup(permitPartner, nextYearSpeciesAmount);

            model().newOccupation(permitPartner, person, OccupationType.SEURAN_JASEN);
            model().newHuntingClubGroupMember(person, partnerGroup);

            otherSpeciesAmount = createSpeciesAmount(permit, otherSpecies);
            otherSpeciesPartnerGroup = model().newHuntingClubGroup(permitPartner, otherSpeciesAmount);
        });
    }

    private HarvestPermit createPermit(final Riistanhoitoyhdistys rhy, final int huntingYear) {
        final HarvestPermit p = model().newMooselikePermit(rhy, huntingYear);
        p.setHuntingClub(permitHolder);
        p.setPermitHolder(PermitHolder.createHolderForClub(permitHolder));

        Stream.of(permitHolder, permitPartner).forEach(p.getPermitPartners()::add);
        return p;
    }

    private HarvestPermitSpeciesAmount createSpeciesAmount(final HarvestPermit permit) {
        return createSpeciesAmount(permit, this.species);
    }

    private HarvestPermitSpeciesAmount createSpeciesAmount(final HarvestPermit permit, final GameSpecies gameSpecies) {
        final int huntingYear = permit.getPermitYear();

        final HarvestPermitSpeciesAmount s = model().newHarvestPermitSpeciesAmount(permit, gameSpecies);
        s.setBeginDate(DateUtil.huntingYearBeginDate(huntingYear));
        s.setEndDate(new LocalDate(huntingYear, 12, 31));
        return s;
    }

    @Test
    public void testGetPermit_forSpeciesAmount() {
        // This won't be picked into DTO because species is different.
        model().newHarvestPermitSpeciesAmount(permit, model().newDeerSubjectToClubHunting(), 3.0f);

        onSavedAndAuthenticated(user, () -> {

            final HuntingClubPermitDTO dto =
                    harvestPermitDetailsFeature.getClubPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            assertEquals(speciesAmount.getGameSpecies().getOfficialCode(), dto.getGameSpeciesCode());
        });
    }

    @Test
    public void testGetPermit_forAmendmentPermits() {
        final LocalDate today = today();

        final HarvestPermit aPermit1 = model().newHarvestPermit(permit);

        final HarvestPermitSpeciesAmount speciesAmount2 =
                model().newHarvestPermitSpeciesAmount(aPermit1, species, 2.0f);
        speciesAmount2.setBeginDate(today.minusDays(1));

        // Duplicate species amount for same species is included in sum
        model().newHarvestPermitSpeciesAmount(aPermit1, species, 3.0f).setBeginDate(today);

        final GameSpecies deer = model().newDeerSubjectToClubHunting();

        // This won't be picked into DTO because different species.
        model().newHarvestPermitSpeciesAmount(aPermit1, deer, 5.0f).setBeginDate(today.minusDays(2));

        final HarvestPermit aPermit2 = model().newHarvestPermit(permit);

        final HarvestPermitSpeciesAmount speciesAmount5 =
                model().newHarvestPermitSpeciesAmount(aPermit2, species, 8.0f);
        speciesAmount5.setBeginDate(today.minusDays(1));

        // Duplicate species amount for same species is included in sum
        model().newHarvestPermitSpeciesAmount(aPermit2, species, 13.0f).setBeginDate(today);

        // This won't be picked into DTO because of different species.
        model().newHarvestPermitSpeciesAmount(aPermit2, deer, 21.0f).setBeginDate(today.minusDays(2));

        final HarvestPermit permit2 = model().newHarvestPermit(permit.getRhy());
        permit2.setHuntingClub(permitHolder);
        permit2.setPermitHolder(PermitHolder.createHolderForClub(permitHolder));
        final HarvestPermit aPermit3 = model().newHarvestPermit(permit2);

        // This won't be picked into DTO because different original permit.
        model().newHarvestPermitSpeciesAmount(aPermit3, species, 34.0f).setBeginDate(today.minusDays(3));

        onSavedAndAuthenticated(user, () -> {

            final HuntingClubPermitDTO dto =
                    harvestPermitDetailsFeature.getClubPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            assertEquals(2f + 3f + 8f + 13f, dto.getAmendmentAmount(), 0.001);
        });
    }

    @Test
    public void testGetPermit_forAllocations() {
        final HarvestPermitAllocation holderAllocation =
                model().newHarvestPermitAllocation(permit, species, permitHolder);

        // Should not affect output because of different species.
        model().newHarvestPermitSpeciesAmount(permit, model().newDeerSubjectToClubHunting());

        // The club/group created next should not affect output because club is not added to partner group.
        final HuntingClub nonPartner = model().newHuntingClub(permit.getRhy());
        model().newOccupation(nonPartner, person, OccupationType.SEURAN_JASEN);

        final HuntingClubGroup nonPartnerGroup = model().newHuntingClubGroup(nonPartner, species);
        model().newHuntingClubGroupMember(person, nonPartnerGroup);

        onSavedAndAuthenticated(user, () -> {

            final HuntingClubPermitDTO dto =
                    harvestPermitDetailsFeature.getClubPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            assertEquals(2, dto.getPartners().size());

            assertAllocations(
                    getPartner(dto, permitHolder.getId()).getAllocation(),
                    holderAllocation.getAdultMales(),
                    holderAllocation.getAdultFemales(),
                    holderAllocation.getYoung(),
                    holderAllocation.getTotal());

            assertAllocations(getPartner(dto, permitPartner.getId()).getAllocation(), 0, 0, 0, 0f);
        });
    }

    @Test
    public void testGetPermit_forHarvestCountsWhenNoHarvestsExist() {
        onSavedAndAuthenticated(user, () -> {

            final HuntingClubPermitDTO dto =
                    harvestPermitDetailsFeature.getClubPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            assertEquals(2, dto.getPartners().size());

            assertCounts(getPartner(dto, permitHolder.getId()).getHarvestCount(), 0, 0, 0, 0, 0, 0, 0, 0);
            assertCounts(getPartner(dto, permitPartner.getId()).getHarvestCount(), 0, 0, 0, 0, 0, 0, 0, 0);

            assertPayment(getPartner(dto, permitHolder.getId()).getPayment(), 0, 0);
            assertPayment(getPartner(dto, permitPartner.getId()).getPayment(), 0, 0);

            assertPayment(dto.getTotalPayment(), 0, 0);
        });
    }

    @Test
    public void testGetPermit_forHarvestsAndObservations() {
        createHuntingDay(holderGroup, 1, 0, GameAge.ADULT, GameGender.FEMALE, 1);

        createHuntingDay(partnerGroup, 2, 0, GameAge.ADULT, GameGender.MALE, 1);
        createHuntingDay(partnerGroup, 4, 1, GameAge.ADULT, GameGender.FEMALE, 2);
        createHuntingDay(partnerGroup, 6, 2, GameAge.YOUNG, GameGender.MALE, 3);
        createHuntingDay(partnerGroup, 8, 3, GameAge.YOUNG, GameGender.FEMALE, 4);
        createHuntingDay(partnerGroup2, 1, 0, GameAge.ADULT, GameGender.MALE, 1);

        // this should not be counted to statistics, becuase group species does not match
        final GroupHuntingDay notCountedDay = model().newGroupHuntingDay(otherSpeciesPartnerGroup, today());
        notCountedDay.setNumberOfHunters(1);
        createHarvest(false, GameAge.ADULT, GameGender.MALE, notCountedDay, this.otherSpecies);

        onSavedAndAuthenticated(user, () -> {
            final HuntingClubPermitDTO dto =
                    harvestPermitDetailsFeature.getClubPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            assertEquals(2, dto.getPartners().size());

            assertCounts(getPartner(dto, permitHolder.getId()).getHarvestCount(), 0, 1, 0, 0, 0, 0, 0, 0);
            assertCounts(getPartner(dto, permitPartner.getId()).getHarvestCount(), (2 + 1), 4, 6, 8, 0, 1, 2, 3);

            int holdersAdultPayment = 120;
            int holdersYoungPayment = 0;
            assertPayment(getPartner(dto, permitHolder.getId()).getPayment(), holdersAdultPayment, holdersYoungPayment);

            int partnersAdultPayment = ((2 + 1) + 4 - 0 - 1) * 120;
            int partnersYoungPayment = (6 + 8 - 2 - 3) * 50;
            assertPayment(getPartner(dto, permitPartner.getId()).getPayment(), partnersAdultPayment, partnersYoungPayment);

            assertPayment(dto.getTotalPayment(),
                    holdersAdultPayment + partnersAdultPayment,
                    holdersYoungPayment + partnersYoungPayment);

            assertNotNull(dto.getTotalStatistics());
            assertEquals(6, dto.getTotalStatistics().getDayCount());
            assertEquals(22, dto.getTotalStatistics().getHarvestCount());
            assertEquals(22, dto.getTotalStatistics().getObservationCount());
            assertEquals(12, dto.getTotalStatistics().getHunterCount());

            final HuntingClubPermitDTO dtoNextYear =
                    harvestPermitDetailsFeature.getClubPermit(permitPartner.getId(), nextYearPermit.getId(), species.getOfficialCode());

            assertEquals(2, dtoNextYear.getPartners().size());

            assertCounts(getPartner(dtoNextYear, permitHolder.getId()).getHarvestCount(), 0, 0, 0, 0, 0, 0, 0, 0);
            assertCounts(getPartner(dtoNextYear, permitPartner.getId()).getHarvestCount(), 0, 0, 0, 0, 0, 0, 0, 0);
        });
    }

    @Test
    public void testGetPermit_multiplePermits() {
        createHuntingDay(holderGroup, 1, 0, GameAge.ADULT, GameGender.MALE, 1);
        createHuntingDay(partnerGroup, 2, 0, GameAge.ADULT, GameGender.MALE, 1);
        createHuntingDay(partnerGroup2, 1, 0, GameAge.ADULT, GameGender.MALE, 1);

        createHuntingDay(nextYearHolderGroup, 3, 0, GameAge.YOUNG, GameGender.MALE, 1);
        createHuntingDay(nextYearPartnerGroup, 2, 0, GameAge.YOUNG, GameGender.MALE, 1);
        createHuntingDay(nextYearPartnerGroup2, 2, 0, GameAge.YOUNG, GameGender.MALE, 1);

        onSavedAndAuthenticated(user, () -> {
            final HuntingClubPermitDTO dto =
                    harvestPermitDetailsFeature.getClubPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            assertEquals(2, dto.getPartners().size());

            assertCounts(getPartner(dto, permitHolder.getId()).getHarvestCount(), 1, 0, 0, 0, 0, 0, 0, 0);
            assertCounts(getPartner(dto, permitPartner.getId()).getHarvestCount(), 2 + 1, 0, 0, 0, 0, 0, 0, 0);

            final HuntingClubPermitDTO dtoNextYear =
                    harvestPermitDetailsFeature.getClubPermit(permitPartner.getId(), nextYearPermit.getId(), species.getOfficialCode());

            assertEquals(2, dtoNextYear.getPartners().size());

            assertCounts(getPartner(dtoNextYear, permitHolder.getId()).getHarvestCount(), 0, 0, 3, 0, 0, 0, 0, 0);
            assertCounts(getPartner(dtoNextYear, permitPartner.getId()).getHarvestCount(), 0, 0, (2 + 2), 0, 0, 0, 0, 0);
        });
    }

    @Test
    public void testGetPermit_forHuntingEndDates() {
        // Create another permit for which partner is added as a permit holder.
        final HarvestPermit anotherPermit = model().newHarvestPermit(permit.getRhy());
        anotherPermit.setHuntingClub(permitPartner);
        anotherPermit.setPermitHolder(PermitHolder.createHolderForClub(permitPartner));
        anotherPermit.getPermitPartners().add(permitPartner);
        model().newHarvestPermitSpeciesAmount(anotherPermit, species);

        // Need to flush before creating MooseHuntingSummary in order to have
        // harvest_permit_partners table populated.
        persistInNewTransaction();

        final MooseHuntingSummary summary = model().newMooseHuntingSummary(permit, permitPartner, true);

        onSavedAndAuthenticated(user, () -> {

            final long partnerId = permitPartner.getId();

            final HuntingClubPermitDTO dto1 = harvestPermitDetailsFeature.getClubPermit(partnerId, permit.getId(), species.getOfficialCode());
            final HuntingClubPermitDTO dto2 =
                    harvestPermitDetailsFeature.getClubPermit(partnerId, anotherPermit.getId(), species.getOfficialCode());

            assertEquals(summary.getHuntingEndDate(), getPartner(dto1, partnerId).getSummary().getHuntingEndDate());
            assertTrue(getPartner(dto2, partnerId).getSummary().isEmpty());
        });
    }

    private void createHuntingDay(HuntingClubGroup group, int howManyHarvestsAndObservations, int howManyNotEdible, GameAge age, GameGender gender, int numberOfHunters) {
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, today().minusDays(dayCount++));
        huntingDay.setNumberOfHunters(numberOfHunters);

        for (int i = 0; i < howManyHarvestsAndObservations; i++) {
            createHarvest(i < howManyNotEdible, age, gender, huntingDay, this.species);
            createHarvest(i < howManyNotEdible, age, gender, huntingDay, this.otherSpecies);
            createObservation(huntingDay, this.species);
            createObservation(huntingDay, this.otherSpecies);
        }
    }

    private void createObservation(GroupHuntingDay huntingDay, GameSpecies species) {
        Observation observation = model().newObservation(species, model().newPerson(), huntingDay);
        observation.setAmount(1);
    }

    private void createHarvest(
            boolean notEdible, GameAge age, GameGender gender, GroupHuntingDay huntingDay, GameSpecies species) {

        final Harvest harvest = model().newHarvest(species, model().newPerson(), huntingDay);

        final HarvestSpecimen specimen = model().newHarvestSpecimen(harvest, age, gender);
        specimen.setNotEdible(notEdible);
    }

    private static void assertAllocations(
            final MoosePermitAllocationDTO allocation, final int am, final int af, final int y, final float total) {

        assertEquals(am, allocation.getAdultMales().intValue());
        assertEquals(af, allocation.getAdultFemales().intValue());
        assertEquals(y, allocation.getYoung().intValue());
        assertTrue(NumberUtils.equal(total, allocation.getTotal()));
    }

    private static void assertCounts(HarvestCountDTO counts,
                                     int am, int af, int ym, int yf,
                                     int amne, int afne, int ymne, int yfne) {

        assertEquals(am, counts.getNumberOfAdultMales());
        assertEquals(af, counts.getNumberOfAdultFemales());
        assertEquals(ym, counts.getNumberOfYoungMales());
        assertEquals(yf, counts.getNumberOfYoungFemales());

        assertEquals(amne + afne, counts.getNumberOfNonEdibleAdults());
        assertEquals(ymne + yfne, counts.getNumberOfNonEdibleYoungs());
    }

    private static void assertPayment(HuntingClubPermitPaymentDTO dto, int adultPayment, int youngPayment) {
        bigDecimalEquals(adultPayment, dto.getAdultsPayment());
        bigDecimalEquals(youngPayment, dto.getYoungPayment());
        bigDecimalEquals(adultPayment + youngPayment, dto.getTotalPayment());
    }

    private static void assertPayment(HuntingClubPermitTotalPaymentDTO dto, int adultPayment, int youngPayment) {
        final MooselikePrice mooselikePrice = MooselikePrice.get(GameSpecies.OFFICIAL_CODE_MOOSE);
        bigDecimalEquals(mooselikePrice.getAdultPrice(), dto.getAdultPrice());
        bigDecimalEquals(mooselikePrice.getYoungPrice(), dto.getYoungPrice());
        bigDecimalEquals(adultPayment, dto.getAdultsPayment());
        bigDecimalEquals(youngPayment, dto.getYoungPayment());
        bigDecimalEquals(adultPayment + youngPayment, dto.getTotalPayment());
    }
}
