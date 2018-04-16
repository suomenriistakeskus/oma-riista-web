package fi.riista.feature.huntingclub.permit;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.allocation.HarvestPermitAllocation;
import fi.riista.feature.harvestpermit.season.MooselikePrice;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.permit.allocation.HuntingClubPermitAllocationDTO;
import fi.riista.feature.huntingclub.permit.summary.MooseHuntingSummary;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fi.riista.util.DateUtil.today;
import static fi.riista.util.NumberUtils.bigDecimalEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HuntingClubPermitFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingClubPermitFeature feature;

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
    private MooselikePrice mooselikePrice;

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
            final int currentHuntingYear = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();

            permitHolder = model().newHuntingClub(rhy);
            permitPartner = model().newHuntingClub(rhy);

            permit = createPermit(rhy);
            speciesAmount = createSpeciesAmount(permit, currentHuntingYear);
            holderGroup = model().newHuntingClubGroup(permitHolder, speciesAmount);
            partnerGroup = model().newHuntingClubGroup(permitPartner, speciesAmount);
            partnerGroup2 = model().newHuntingClubGroup(permitPartner, speciesAmount);

            mooselikePrice = model().newMooselikePrice(speciesAmount.resolveHuntingYear(), species,
                    BigDecimal.valueOf(120), BigDecimal.valueOf(50));

            nextYearPermit = createPermit(rhy);
            nextYearSpeciesAmount = createSpeciesAmount(nextYearPermit, currentHuntingYear + 1);
            nextYearHolderGroup = model().newHuntingClubGroup(permitHolder, nextYearSpeciesAmount);
            nextYearPartnerGroup = model().newHuntingClubGroup(permitPartner, nextYearSpeciesAmount);
            nextYearPartnerGroup2 = model().newHuntingClubGroup(permitPartner, nextYearSpeciesAmount);
            model().newMooselikePrice(nextYearSpeciesAmount.resolveHuntingYear(), species,
                    BigDecimal.valueOf(220), BigDecimal.valueOf(250));

            model().newOccupation(permitPartner, person, OccupationType.SEURAN_JASEN);
            model().newHuntingClubGroupMember(person, partnerGroup);

            otherSpeciesAmount = createSpeciesAmount(permit, currentHuntingYear, otherSpecies);
            otherSpeciesPartnerGroup = model().newHuntingClubGroup(permitPartner, otherSpeciesAmount);
        });
    }

    private HarvestPermit createPermit(final Riistanhoitoyhdistys rhy) {
        final HarvestPermit p = model().newMooselikePermit(rhy);
        p.setPermitHolder(permitHolder);
        Stream.of(permitHolder, permitPartner).forEach(p.getPermitPartners()::add);
        return p;
    }

    private HarvestPermitSpeciesAmount createSpeciesAmount(final HarvestPermit permit, final int huntingYear) {
        return createSpeciesAmount(permit, huntingYear, this.species);
    }

    private HarvestPermitSpeciesAmount createSpeciesAmount(final HarvestPermit permit, final int huntingYear, final GameSpecies gameSpecies) {
        final HarvestPermitSpeciesAmount s = model().newHarvestPermitSpeciesAmount(permit, gameSpecies);
        s.setCreditorReference(creditorReference());
        s.setBeginDate(DateUtil.huntingYearBeginDate(huntingYear));
        s.setEndDate(new LocalDate(huntingYear, 12, 31));
        return s;
    }

    @Test
    public void testGetPermit_forSpeciesAmount() {
        // This won't be picked into DTO because species is different.
        model().newHarvestPermitSpeciesAmount(permit, model().newGameSpecies(), 3.0f);

        onSavedAndAuthenticated(user, () -> {

            final HuntingClubPermitDTO dto =
                    feature.getPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            assertEquals(speciesAmount.getId(), dto.getSpeciesAmount().getId());
        });
    }

    @Test
    public void testGetPermit_forAmendmentPermits() {
        final LocalDate today = today();

        final HarvestPermit aPermit1 = model().newHarvestPermit(permit);

        final HarvestPermitSpeciesAmount speciesAmount2 =
                model().newHarvestPermitSpeciesAmount(aPermit1, species, 2.0f);
        speciesAmount2.setBeginDate(today.minusDays(1));

        // This won't be picked into DTO because beginDate is greater than with previous one.
        model().newHarvestPermitSpeciesAmount(aPermit1, species, 3.0f).setBeginDate(today);

        // This won't be picked into DTO because different species.
        model().newHarvestPermitSpeciesAmount(aPermit1, model().newGameSpecies(), 5.0f)
                .setBeginDate(today.minusDays(2));

        final HarvestPermit aPermit2 = model().newHarvestPermit(permit);

        final HarvestPermitSpeciesAmount speciesAmount5 =
                model().newHarvestPermitSpeciesAmount(aPermit2, species, 8.0f);
        speciesAmount5.setBeginDate(today.minusDays(1));

        // This won't be picked into DTO because beginDate is greater than with previous one.
        model().newHarvestPermitSpeciesAmount(aPermit2, species, 13.0f).setBeginDate(today);

        // This won't be picked into DTO because of different species.
        model().newHarvestPermitSpeciesAmount(aPermit2, model().newGameSpecies(), 21.0f)
                .setBeginDate(today.minusDays(2));

        final HarvestPermit permit2 = model().newHarvestPermit(permit.getRhy());
        permit2.setPermitHolder(permitHolder);
        final HarvestPermit aPermit3 = model().newHarvestPermit(permit2);

        // This won't be picked into DTO because different original permit.
        model().newHarvestPermitSpeciesAmount(aPermit3, species, 34.0f).setBeginDate(today.minusDays(3));

        onSavedAndAuthenticated(user, () -> {

            final HuntingClubPermitDTO dto =
                    feature.getPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            assertEquals(
                    ImmutableMap.of(
                            aPermit1.getPermitNumber(), speciesAmount2.getAmount(),
                            aPermit2.getPermitNumber(), speciesAmount5.getAmount()),
                    dto.getAmendmentPermits());
        });
    }

    @Test
    public void testGetPermit_forAllocations() {
        final HarvestPermitAllocation holderAllocation =
                model().newHarvestPermitAllocation(permit, species, permitHolder);

        // Should not affect output because of different species.
        model().newHarvestPermitSpeciesAmount(permit, model().newGameSpecies());

        // The club/group created next should not affect output because club is not added to partner group.
        final HuntingClub nonPartner = model().newHuntingClub(permit.getRhy());
        model().newOccupation(nonPartner, person, OccupationType.SEURAN_JASEN);

        final HuntingClubGroup nonPartnerGroup = model().newHuntingClubGroup(nonPartner);
        model().newHuntingClubGroupMember(person, nonPartnerGroup);

        onSavedAndAuthenticated(user, () -> {

            final HuntingClubPermitDTO dto =
                    feature.getPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            final List<HuntingClubPermitAllocationDTO> allocations = dto.getAllocations();
            assertEquals(2, allocations.size());

            final Map<Long, HuntingClubPermitAllocationDTO> allocationByClubId =
                    F.index(allocations, HuntingClubPermitAllocationDTO::getHuntingClubId);

            assertAllocations(
                    allocationByClubId.get(permitHolder.getId()),
                    holderAllocation.getAdultMales(),
                    holderAllocation.getAdultFemales(),
                    holderAllocation.getYoung(),
                    holderAllocation.getTotal());

            assertAllocations(allocationByClubId.get(permitPartner.getId()), 0, 0, 0, 0f);
        });
    }

    @Test
    public void testGetPermit_forHarvestCountsWhenNoHarvestsExist() {
        onSavedAndAuthenticated(user, () -> {

            final HuntingClubPermitDTO dto =
                    feature.getPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            final Map<Long, HuntingClubPermitCountDTO> allCounts = dto.getHarvestCounts();
            assertEquals(2, allCounts.size());

            assertCounts(allCounts.get(permitHolder.getId()), 0, 0, 0, 0, 0, 0, 0, 0);
            assertCounts(allCounts.get(permitPartner.getId()), 0, 0, 0, 0, 0, 0, 0, 0);

            Map<Long, HuntingClubPermitPaymentDTO> payments = dto.getPayments();
            assertEquals(2, payments.size());

            assertPayment(payments.get(permitHolder.getId()), 0, 0);
            assertPayment(payments.get(permitPartner.getId()), 0, 0);

            assertPayment(dto.getTotalPayment(), 0, 0, mooselikePrice);
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
                    feature.getPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            final Map<Long, HuntingClubPermitCountDTO> allCounts = dto.getHarvestCounts();
            assertEquals(2, allCounts.size());

            assertCounts(allCounts.get(permitHolder.getId()), 0, 1, 0, 0, 0, 0, 0, 0);
            assertCounts(allCounts.get(permitPartner.getId()), (2 + 1), 4, 6, 8, 0, 1, 2, 3);

            Map<Long, HuntingClubPermitPaymentDTO> payments = dto.getPayments();
            assertEquals(2, payments.size());

            int holdersAdultPayment = 120;
            int holdersYoungPayment = 0;
            assertPayment(payments.get(permitHolder.getId()), holdersAdultPayment, holdersYoungPayment);

            int partnersAdultPayment = ((2 + 1) + 4 - 0 - 1) * 120;
            int partnersYoungPayment = (6 + 8 - 2 - 3) * 50;
            assertPayment(payments.get(permitPartner.getId()), partnersAdultPayment, partnersYoungPayment);

            assertPayment(dto.getTotalPayment(),
                    holdersAdultPayment + partnersAdultPayment,
                    holdersYoungPayment + partnersYoungPayment,
                    mooselikePrice);
            assertEquals(speciesAmount.getEndDate().plusDays(7), dto.getTotalPayment().getDueDate());

            assertNotNull(dto.getTotalStatistics());
            assertEquals(6, dto.getTotalStatistics().getDayCount());
            assertEquals(22, dto.getTotalStatistics().getHarvestCount());
            assertEquals(22, dto.getTotalStatistics().getObservationCount());
            assertEquals(12, dto.getTotalStatistics().getHunterCount());

            final HuntingClubPermitDTO dtoNextYear =
                    feature.getPermit(permitPartner.getId(), nextYearPermit.getId(), species.getOfficialCode());

            final Map<Long, HuntingClubPermitCountDTO> nextYearAllCounts = dtoNextYear.getHarvestCounts();
            assertEquals(2, nextYearAllCounts.size());

            assertCounts(nextYearAllCounts.get(permitHolder.getId()), 0, 0, 0, 0, 0, 0, 0, 0);
            assertCounts(nextYearAllCounts.get(permitPartner.getId()), 0, 0, 0, 0, 0, 0, 0, 0);
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
                    feature.getPermit(permitPartner.getId(), permit.getId(), species.getOfficialCode());

            final Map<Long, HuntingClubPermitCountDTO> allCounts = dto.getHarvestCounts();
            assertEquals(2, allCounts.size());

            assertCounts(allCounts.get(permitHolder.getId()), 1, 0, 0, 0, 0, 0, 0, 0);
            assertCounts(allCounts.get(permitPartner.getId()), 2 + 1, 0, 0, 0, 0, 0, 0, 0);

            final HuntingClubPermitDTO dtoNextYear =
                    feature.getPermit(permitPartner.getId(), nextYearPermit.getId(), species.getOfficialCode());

            final Map<Long, HuntingClubPermitCountDTO> nextYearAllCounts = dtoNextYear.getHarvestCounts();
            assertEquals(2, nextYearAllCounts.size());

            assertCounts(nextYearAllCounts.get(permitHolder.getId()), 0, 0, 3, 0, 0, 0, 0, 0);
            assertCounts(nextYearAllCounts.get(permitPartner.getId()), 0, 0, (2 + 2), 0, 0, 0, 0, 0);
        });
    }

    @Test
    public void testGetPermit_forHuntingEndDates() {
        // Create another permit for which partner is added as a permit holder.
        final HarvestPermit anotherPermit = model().newHarvestPermit(permit.getRhy());
        anotherPermit.setPermitHolder(permitPartner);
        anotherPermit.getPermitPartners().add(permitPartner);
        HarvestPermitSpeciesAmount spa = model().newHarvestPermitSpeciesAmount(anotherPermit, species);
        spa.setCreditorReference(creditorReference());

        // Need to flush before creating MooseHuntingSummary in order to have
        // harvest_permit_partners table populated.
        persistInNewTransaction();

        final MooseHuntingSummary summary = model().newMooseHuntingSummary(permit, permitPartner, true);

        onSavedAndAuthenticated(user, () -> {

            final long partnerId = permitPartner.getId();

            final HuntingClubPermitDTO dto1 = feature.getPermit(partnerId, permit.getId(), species.getOfficialCode());
            final HuntingClubPermitDTO dto2 =
                    feature.getPermit(partnerId, anotherPermit.getId(), species.getOfficialCode());

            assertEquals(
                    summary.getHuntingEndDate(), dto1.getSummaryForPartnersTable().get(partnerId).getHuntingEndDate());
            assertTrue(dto2.getSummaryForPartnersTable().get(partnerId).isEmpty());
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
            final HuntingClubPermitAllocationDTO allocation, final int am, final int af, final int y, final float total) {

        assertEquals(am, allocation.getAdultMales().intValue());
        assertEquals(af, allocation.getAdultFemales().intValue());
        assertEquals(y, allocation.getYoung().intValue());
        assertTrue(NumberUtils.equal(total, allocation.getTotal()));
    }

    private static void assertCounts(HuntingClubPermitCountDTO counts,
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

    private static void assertPayment(HuntingClubPermitTotalPaymentDTO dto, int adultPayment, int youngPayment, MooselikePrice price) {
        assertPayment(dto, adultPayment, youngPayment);

        bigDecimalEquals(price.getAdultPrice(), dto.getAdultPrice());
        bigDecimalEquals(price.getYoungPrice(), dto.getYoungPrice());
    }
}
