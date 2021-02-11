package fi.riista.feature.harvestpermit.report.search;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.harvestpermit.report.search.HarvestReportSearchDTO.SearchType;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public class HarvestReportSearchRepositoryTest extends EmbeddedDatabaseTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Resource
    private HarvestReportSearchRepository harvestReportSearchRepository;

    public List<Harvest> moderatorSearch(final List<HarvestReportState> states) {
        return harvestReportSearchRepository.queryForList(new HarvestReportSearchDTO(SearchType.MODERATOR, states));
    }

    public List<Harvest> moderatorSearchForPerson(final Long personId) {
        final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(SearchType.MODERATOR,
                Arrays.asList(HarvestReportState.values()));
        dto.setPersonId(personId);
        return harvestReportSearchRepository.queryForList(dto);
    }

    public List<Harvest> moderatorSearch(final HarvestPermit permit) {
        final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(SearchType.MODERATOR,
                Arrays.asList(HarvestReportState.values()));
        dto.setPermitNumber(permit.getPermitNumber());
        return harvestReportSearchRepository.queryForList(dto);
    }

    public List<Harvest> moderatorSearch(final HarvestSeason season) {
        final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(SearchType.MODERATOR,
                Arrays.asList(HarvestReportState.values()));
        dto.setSeasonId(season.getId());
        return harvestReportSearchRepository.queryForList(dto);
    }

    public List<Harvest> moderatorSearch(final GameSpecies species) {
        final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(SearchType.MODERATOR,
                Arrays.asList(HarvestReportState.values()));
        dto.setGameSpeciesCode(species.getOfficialCode());
        return harvestReportSearchRepository.queryForList(dto);
    }

    public List<Harvest> moderatorSearch(final RiistakeskuksenAlue rka) {
        final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(SearchType.MODERATOR,
                Arrays.asList(HarvestReportState.values()));
        dto.setAreaId(rka.getId());
        return harvestReportSearchRepository.queryForList(dto);
    }

    public List<Harvest> moderatorSearch(final Riistanhoitoyhdistys rhy) {
        final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(SearchType.MODERATOR,
                Arrays.asList(HarvestReportState.values()));
        dto.setRhyId(rhy.getId());
        return harvestReportSearchRepository.queryForList(dto);
    }

    public List<Harvest> moderatorSearch(final String text) {
        final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(
                SearchType.MODERATOR, Arrays.asList(HarvestReportState.values()));
        dto.setText(text);
        return harvestReportSearchRepository.queryForList(dto);
    }

    public List<Harvest> coordinatorSearch(final Riistanhoitoyhdistys rhy) {
        final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(SearchType.COORDINATOR,
                Arrays.asList(HarvestReportState.values()));
        dto.setRhyId(rhy.getId());
        return harvestReportSearchRepository.queryForList(dto);
    }

    public List<Harvest> coordinatorSearch(final List<HarvestReportState> states) {
        return harvestReportSearchRepository.queryForList(new HarvestReportSearchDTO(SearchType.COORDINATOR, states));
    }

    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;

    @Before
    public void generateHarvestNotVisibleToAnyOne() {
        this.rka = model().newRiistakeskuksenAlue();
        this.rhy = model().newRiistanhoitoyhdistys(this.rka);

        // private diary entry
        model().newHarvest();

        // harvest with rhy
        model().newHarvest().setRhy(rhy);

        // harvest proposed for permit
        model().newHarvest(model().newHarvestPermit(rhy), model().newGameSpecies())
                .setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.PROPOSED);

        // harvest accepted for permit
        model().newHarvest(model().newHarvestPermit(rhy), model().newGameSpecies())
                .setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        // harvest rejected from permit
        model().newHarvest(model().newHarvestPermit(rhy), model().newGameSpecies())
                .setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.REJECTED);
    }

    @Test
    public void testSmoke_Coordinator() {
        testSmoke(SearchType.COORDINATOR);
    }

    @Test
    public void testSmoke_Moderator() {
        testSmoke(SearchType.MODERATOR);
    }

    private void testSmoke(final HarvestReportSearchDTO.SearchType searchType) {
        final GameSpecies species = model().newGameSpecies();
        final HarvestSeason season = model().newHarvestSeason(species);
        final HarvestPermit permit = model().newHarvestPermit(rhy);

        final Harvest h1 = model().newHarvest();
        h1.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        h1.setHarvestReportAuthor(h1.getAuthor());
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestSeason(season);
        h1.setHarvestReportMemo("hello world");
        h1.setRhy(rhy);

        final Harvest h2 = model().newHarvest();
        h2.setHarvestReportState(HarvestReportState.APPROVED);
        h2.setHarvestReportAuthor(h2.getAuthor());
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestPermit(permit);
        h2.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);
        h2.setRhy(rhy);

        persistInNewTransaction();

        runInTransaction(() -> {
            final LocalDate harvestDate = h1.getPointOfTimeAsLocalDate();
            final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(
                    searchType, singletonList(HarvestReportState.SENT_FOR_APPROVAL));

            dto.setBeginDate(harvestDate);
            dto.setEndDate(harvestDate);
            dto.setSeasonId(season.getId());
            dto.setText("hello world");
            dto.setRhyId(rhy.getId());

            assertEquals(singletonList(h1), harvestReportSearchRepository.queryForList(dto));
        });

        runInTransaction(() -> {
            final LocalDate harvestDate = h2.getPointOfTimeAsLocalDate();
            final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(
                    searchType, singletonList(HarvestReportState.APPROVED));

            dto.setBeginDate(harvestDate);
            dto.setEndDate(harvestDate);
            dto.setPermitNumber(dto.getPermitNumber());
            dto.setRhyId(rhy.getId());

            assertEquals(singletonList(h2), harvestReportSearchRepository.queryForList(dto));
        });
    }

    @Test
    public void testSearchTypeRequired() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("missing searchType");

        runInTransaction(() -> {
            final HarvestReportSearchDTO dto = new HarvestReportSearchDTO();
            dto.setStates(singletonList(HarvestReportState.APPROVED));
            harvestReportSearchRepository.queryForList(dto);
        });
    }

    @Test
    public void testFilterHarvestReportState() {
        final GameSpecies species = model().newGameSpecies();
        final HarvestSeason season = model().newHarvestSeason(species);

        final Harvest h1 = model().newHarvest();
        h1.setHarvestReportState(HarvestReportState.APPROVED);
        h1.setHarvestReportAuthor(h1.getAuthor());
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestSeason(season);

        final Harvest h2 = model().newHarvest();
        h2.setHarvestReportState(HarvestReportState.REJECTED);
        h2.setHarvestReportAuthor(h2.getAuthor());
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestSeason(season);

        final Harvest h3 = model().newHarvest();
        h3.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        h3.setHarvestReportAuthor(h3.getAuthor());
        h3.setHarvestReportDate(DateUtil.now());
        h3.setHarvestSeason(season);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            assertEquals(singletonList(h1), moderatorSearch(singletonList(HarvestReportState.APPROVED)));
            assertEquals(singletonList(h2), moderatorSearch(singletonList(HarvestReportState.REJECTED)));
            assertEquals(singletonList(h3), moderatorSearch(singletonList(HarvestReportState.SENT_FOR_APPROVAL)));
            assertThat(moderatorSearch(asList(HarvestReportState.APPROVED, HarvestReportState.REJECTED, HarvestReportState.SENT_FOR_APPROVAL)), containsInAnyOrder(h1, h2, h3));
        });
    }

    @Test
    public void testFilterHarvestReportPerson() {
        final GameSpecies species = model().newGameSpecies();
        final HarvestSeason season = model().newHarvestSeason(species);
        final Person author1 = model().newPerson();
        final Person shooter1 = model().newPerson();
        final Person author2 = model().newPerson();
        final Person shooter2 = model().newPerson();
        final Person reportAuthor1 = model().newPerson();
        final Person reportAuthor2 = model().newPerson();

        final Harvest h1 = model().newHarvest(author1, shooter1);
        h1.setHarvestReportState(HarvestReportState.APPROVED);
        h1.setHarvestReportAuthor(reportAuthor1);
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestSeason(season);

        final Harvest h2 = model().newHarvest(author1, shooter1);
        h2.setHarvestReportState(HarvestReportState.REJECTED);
        h2.setHarvestReportAuthor(reportAuthor2);
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestSeason(season);

        final Harvest h3 = model().newHarvest(author2, shooter2);
        h3.setHarvestReportState(HarvestReportState.SENT_FOR_APPROVAL);
        h3.setHarvestReportAuthor(reportAuthor2);
        h3.setHarvestReportDate(DateUtil.now());
        h3.setHarvestSeason(season);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            assertThat(moderatorSearchForPerson(author1.getId()), containsInAnyOrder(h1, h2));
            assertThat(moderatorSearchForPerson(shooter1.getId()), containsInAnyOrder(h1, h2));
            assertEquals(singletonList(h3), moderatorSearchForPerson(author2.getId()));
            assertEquals(singletonList(h3), moderatorSearchForPerson(shooter2.getId()));
            assertThat(moderatorSearchForPerson(reportAuthor2.getId()), empty());
        });
    }

    @Test
    public void testFilterHarvestReportState_StateRequired() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("states is required");

        runInTransaction(() -> moderatorSearch(emptyList()));
    }

    @Test
    public void testFilterDateRange() {
        final Harvest harvest = model().newHarvest();
        harvest.setHarvestReportState(HarvestReportState.APPROVED);
        harvest.setHarvestReportAuthor(harvest.getAuthor());
        harvest.setHarvestReportDate(DateUtil.now());
        harvest.setHarvestSeason(model().newHarvestSeason(harvest.getSpecies()));

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            final HarvestReportSearchDTO dto = new HarvestReportSearchDTO(
                    SearchType.MODERATOR, singletonList(HarvestReportState.APPROVED));

            final LocalDate harvestDate = harvest.getPointOfTimeAsLocalDate();

            dto.setBeginDate(harvestDate);
            dto.setEndDate(harvestDate);

            assertEquals(singletonList(harvest), harvestReportSearchRepository.queryForList(dto));

            dto.setBeginDate(harvestDate.minusDays(1));
            dto.setEndDate(harvestDate);
            assertEquals(singletonList(harvest), harvestReportSearchRepository.queryForList(dto));

            dto.setBeginDate(harvestDate.minusDays(1));
            dto.setEndDate(harvestDate.minusDays(1));
            assertEquals(emptyList(), harvestReportSearchRepository.queryForList(dto));

            dto.setBeginDate(harvestDate.plusDays(1));
            dto.setEndDate(harvestDate.plusDays(1));
            assertEquals(emptyList(), harvestReportSearchRepository.queryForList(dto));

            dto.setBeginDate(harvestDate);
            dto.setEndDate(harvestDate.plusDays(1));
            assertEquals(singletonList(harvest), harvestReportSearchRepository.queryForList(dto));
        });
    }

    @Test
    public void testFilterPermitNumber() {
        final HarvestPermit p1 = model().newHarvestPermit(rhy);
        final HarvestPermit p2 = model().newHarvestPermit(rhy);

        final Harvest h1 = model().newHarvest();
        h1.setHarvestReportState(HarvestReportState.APPROVED);
        h1.setHarvestReportAuthor(h1.getAuthor());
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestPermit(p1);
        h1.setRhy(rhy);
        h1.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        final Harvest h2 = model().newHarvest();
        h2.setHarvestReportState(HarvestReportState.APPROVED);
        h2.setHarvestReportAuthor(h2.getAuthor());
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestPermit(p2);
        h2.setRhy(rhy);
        h2.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        final Harvest h3 = model().newHarvest();
        h3.setHarvestReportState(HarvestReportState.APPROVED);
        h3.setHarvestReportAuthor(h3.getAuthor());
        h3.setHarvestReportDate(DateUtil.now());
        h3.setRhy(rhy);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            assertEquals(singletonList(h1), moderatorSearch(p1));
            assertEquals(singletonList(h2), moderatorSearch(p2));
        });
    }

    @Test
    public void testFilterSeason() {
        final GameSpecies g1 = model().newGameSpecies();
        final GameSpecies g2 = model().newGameSpecies();
        final HarvestSeason s1 = model().newHarvestSeason(g1);
        final HarvestSeason s2 = model().newHarvestSeason(g2);

        // first season
        final Harvest h1 = model().newHarvest();
        h1.setHarvestReportState(HarvestReportState.APPROVED);
        h1.setHarvestReportAuthor(h1.getAuthor());
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestSeason(s1);

        // second season
        final Harvest h2 = model().newHarvest();
        h2.setHarvestReportState(HarvestReportState.APPROVED);
        h2.setHarvestReportAuthor(h2.getAuthor());
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestSeason(s2);

        // no season
        final Harvest h3 = model().newHarvest();
        h3.setHarvestReportState(HarvestReportState.APPROVED);
        h3.setHarvestReportAuthor(h3.getAuthor());
        h3.setHarvestReportDate(DateUtil.now());
        h3.setHarvestSeason(null);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            assertEquals(singletonList(h1), moderatorSearch(s1));
            assertEquals(singletonList(h2), moderatorSearch(s2));
        });
    }

    @Test
    public void testFilterGameSpecies() {
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final GameSpecies g1 = model().newGameSpecies();
        final GameSpecies g2 = model().newGameSpecies();

        // with permit, correct species
        final Harvest h1 = model().newHarvest(g1);
        h1.setHarvestReportState(HarvestReportState.APPROVED);
        h1.setHarvestReportAuthor(h1.getAuthor());
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestPermit(permit);
        h1.setRhy(rhy);
        h1.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        // with permit, different species
        final Harvest h2 = model().newHarvest(g2);
        h2.setHarvestReportState(HarvestReportState.APPROVED);
        h2.setHarvestReportAuthor(h2.getAuthor());
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestPermit(permit);
        h2.setRhy(rhy);
        h2.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        // no permit, correct species
        final Harvest h3 = model().newHarvest(g1);
        h3.setHarvestReportState(HarvestReportState.APPROVED);
        h3.setHarvestReportAuthor(h3.getAuthor());
        h3.setHarvestReportDate(DateUtil.now());

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            assertEquals(singletonList(h1), moderatorSearch(g1));
            assertEquals(singletonList(h2), moderatorSearch(g2));
        });
    }

    @Test
    public void testModerator_FilterRKA() {
        final GameSpecies species = model().newGameSpecies();
        final HarvestSeason season = model().newHarvestSeason(species);
        final RiistakeskuksenAlue otherRka = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys otherRhy = model().newRiistanhoitoyhdistys(otherRka);
        final HarvestPermit permit = model().newHarvestPermit(this.rhy);
        final Riistanhoitoyhdistys permitHarvestRhy = model().newRiistanhoitoyhdistys();

        // correct rka
        final Harvest h1 = model().newHarvest();
        h1.setHarvestReportState(HarvestReportState.APPROVED);
        h1.setHarvestReportAuthor(h1.getAuthor());
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestSeason(season);
        h1.setRhy(rhy);

        // other rka
        final Harvest h2 = model().newHarvest();
        h2.setHarvestReportState(HarvestReportState.APPROVED);
        h2.setHarvestReportAuthor(h1.getAuthor());
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestSeason(season);
        h2.setRhy(otherRhy);

        // correct permit rka - not included
        final Harvest h3 = model().newHarvest();
        h3.setHarvestReportState(HarvestReportState.APPROVED);
        h3.setHarvestReportAuthor(h3.getAuthor());
        h3.setHarvestReportDate(DateUtil.now());
        h3.setRhy(permitHarvestRhy);
        h3.setHarvestPermit(permit);
        h3.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            assertEquals(singletonList(h1), moderatorSearch(rka));
            assertEquals(singletonList(h2), moderatorSearch(otherRka));
        });
    }

    @Test
    public void testModerator_FilterRHY() {
        final GameSpecies species = model().newGameSpecies();
        final HarvestSeason season = model().newHarvestSeason(species);
        final Riistanhoitoyhdistys otherRhy = model().newRiistanhoitoyhdistys(rka);
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final Riistanhoitoyhdistys permitHarvestRhy = model().newRiistanhoitoyhdistys();

        // correct rhy
        final Harvest h1 = model().newHarvest();
        h1.setHarvestReportState(HarvestReportState.APPROVED);
        h1.setHarvestReportAuthor(h1.getAuthor());
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestSeason(season);
        h1.setRhy(rhy);

        // other rhy
        final Harvest h2 = model().newHarvest();
        h2.setHarvestReportState(HarvestReportState.APPROVED);
        h2.setHarvestReportAuthor(h1.getAuthor());
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestSeason(season);
        h2.setRhy(otherRhy);

        // correct permit rhy - not included
        final Harvest h3 = model().newHarvest();
        h3.setHarvestReportState(HarvestReportState.APPROVED);
        h3.setHarvestReportAuthor(h3.getAuthor());
        h3.setHarvestReportDate(DateUtil.now());
        h3.setRhy(permitHarvestRhy);
        h3.setHarvestPermit(permit);
        h3.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            assertEquals(singletonList(h1), moderatorSearch(rhy));
            assertEquals(singletonList(h2), moderatorSearch(otherRhy));
        });
    }

    @Test
    public void testModerator_FilterText() {
        final GameSpecies species = model().newGameSpecies();
        final HarvestSeason season = model().newHarvestSeason(species);

        final Harvest h1 = model().newHarvest();
        h1.setHarvestReportState(HarvestReportState.APPROVED);
        h1.setHarvestReportAuthor(h1.getAuthor());
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestReportMemo("hello world");
        h1.setHarvestSeason(season);

        final Harvest h2 = model().newHarvest();
        h2.setHarvestReportState(HarvestReportState.APPROVED);
        h2.setHarvestReportAuthor(h2.getAuthor());
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestReportMemo("foo bar");
        h2.setHarvestSeason(season);

        final Harvest h3 = model().newHarvest();
        h3.setHarvestReportState(HarvestReportState.APPROVED);
        h3.setHarvestReportAuthor(h3.getAuthor());
        h3.setHarvestReportDate(DateUtil.now());
        h3.setHarvestReportMemo(null);
        h3.setHarvestSeason(season);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            assertEquals(singletonList(h1), moderatorSearch("hello"));
            assertEquals(singletonList(h1), moderatorSearch("world"));
            assertEquals(singletonList(h1), moderatorSearch("hello world"));
            assertEquals(singletonList(h2), moderatorSearch("foo"));
        });
    }

    @Test
    public void testCoordinator_FilterRhy() {
        final GameSpecies species = model().newGameSpecies();
        final HarvestSeason season = model().newHarvestSeason(species);
        final Riistanhoitoyhdistys otherRhy = model().newRiistanhoitoyhdistys(rka);
        final HarvestPermit permit = model().newHarvestPermit(rhy);
        final Riistanhoitoyhdistys permitHarvestRhy = model().newRiistanhoitoyhdistys();


        // correct rhy
        final Harvest h1 = model().newHarvest();
        h1.setHarvestReportState(HarvestReportState.APPROVED);
        h1.setHarvestReportAuthor(h1.getAuthor());
        h1.setHarvestReportDate(DateUtil.now());
        h1.setHarvestSeason(season);
        h1.setRhy(rhy);

        // other rhy
        final Harvest h2 = model().newHarvest();
        h2.setHarvestReportState(HarvestReportState.APPROVED);
        h2.setHarvestReportAuthor(h1.getAuthor());
        h2.setHarvestReportDate(DateUtil.now());
        h2.setHarvestSeason(season);
        h2.setRhy(otherRhy);

        // correct permit rhy
        final Harvest h3 = model().newHarvest();
        h3.setHarvestReportState(HarvestReportState.APPROVED);
        h3.setHarvestReportAuthor(h3.getAuthor());
        h3.setHarvestReportDate(DateUtil.now());
        h3.setRhy(permitHarvestRhy);
        h3.setHarvestPermit(permit);
        h3.setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit.ACCEPTED);

        runInTransaction(() -> {
            persistInCurrentlyOpenTransaction();

            assertThat(coordinatorSearch(this.rhy), containsInAnyOrder(h1, h3));
            assertThat(coordinatorSearch(otherRhy), containsInAnyOrder(h2));
        });
    }

    @Test
    public void testCoordinator_RhyRequired() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("missing rhyId");

        runInTransaction(() -> harvestReportSearchRepository.queryForList(new HarvestReportSearchDTO(
                SearchType.COORDINATOR, singletonList(HarvestReportState.APPROVED))));
    }
}
