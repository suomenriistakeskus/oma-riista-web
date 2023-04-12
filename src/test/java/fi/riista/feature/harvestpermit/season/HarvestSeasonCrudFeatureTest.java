package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_HARVEST_SEASONS;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_PARTRIDGE;
import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaType.HALLIALUE;
import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaType.NORPPAALUE;
import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaType.PORONHOITOALUE;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class HarvestSeasonCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestSeasonCrudFeature feature;

    @Resource
    private HarvestSeasonRepository repository;

    @Test
    public void testListHarvestSeasons() {
        final GameSpecies species = model().newGameSpecies();
        final LocalDate beginDate = new LocalDate(2021, 12, 23);
        final LocalDate endDate = new LocalDate(2021, 12, 31);
        final LocalDate endOfReportingDate = new LocalDate(2022, 1, 7);
        final HarvestSeason season = model().newHarvestSeason(species, beginDate, endDate, endOfReportingDate);

        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "Area 1", "Area 1 sv");
        final HarvestQuota quota = model().newHarvestQuota(season, area, 10);

        final HarvestArea area2 = model().newHarvestArea(PORONHOITOALUE, "Area 2", "Area 2 sv");
        final HarvestQuota quota2 = model().newHarvestQuota(season, area2, 20);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final List<HarvestSeasonDTO> list = feature.listHarvestSeasons(2021);
            assertThat(list, hasSize(1));

            final HarvestSeasonDTO dto = list.get(0);
            assertThat(dto.getSpecies().getCode(), is(equalTo(species.getOfficialCode())));
            assertThat(dto.getBeginDate(), is(equalTo(beginDate)));
            assertThat(dto.getEndDate(), is(equalTo(endDate)));
            assertThat(dto.getEndOfReportingDate(), is(equalTo(endOfReportingDate)));

            final List<HarvestQuotaDTO> quotaList = dto.getQuotas();
            assertThat(quotaList, hasSize(2));

            final List<HarvestQuotaDTO> quotaDTOList = quotaList.stream()
                    .filter(q -> q.getId().equals(quota.getId())).collect(Collectors.toList());
            assertThat(quotaDTOList, hasSize(1));

            final HarvestQuotaDTO quotaDTO = quotaDTOList.get(0);
            assertThat(quotaDTO.getQuota(), is(equalTo(quota.getQuota())));

            final HarvestAreaDTO areaDTO = quotaDTO.getHarvestArea();
            assertThat(areaDTO.getNameFI(), is(equalTo(area.getNameFinnish())));
            assertThat(areaDTO.getNameSV(), is(equalTo(area.getNameSwedish())));;

            final List<HarvestQuotaDTO> quotaDTOList2 = quotaList.stream()
                    .filter(q -> q.getId().equals(quota2.getId())).collect(Collectors.toList());
            assertThat(quotaDTOList2, hasSize(1));

            final HarvestQuotaDTO quotaDTO2 = quotaDTOList2.get(0);
            assertThat(quotaDTO2.getQuota(), is(equalTo(quota2.getQuota())));

            final HarvestAreaDTO areaDTO2 = quotaDTO2.getHarvestArea();
            assertThat(areaDTO2.getNameFI(), is(equalTo(area2.getNameFinnish())));
            assertThat(areaDTO2.getNameSV(), is(equalTo(area2.getNameSwedish())));;
        });
    }

    @Test
    public void testListQuotaAreas() {
        final HarvestArea bearArea = model().newHarvestArea(PORONHOITOALUE, "Karhu", "Björn");
        final HarvestArea greySealArea = model().newHarvestArea(HALLIALUE, "Halli", "Gråsäl");
        final HarvestArea ringedSealArea = model().newHarvestArea(NORPPAALUE, "Itämerennorppa", "Östersjövikare");

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final Map<HarvestArea.HarvestAreaType, List<HarvestAreaDTO>> areas = feature.listQuotaAreas();
            assertThat(areas.size(), is(equalTo(3)));

            final List<HarvestAreaDTO> bearList = areas.get(PORONHOITOALUE);
            assertThat(bearList, hasSize(1));
            assertThat(bearList.get(0).getId(), is(equalTo(bearArea.getId())));

            final List<HarvestAreaDTO> greySealList = areas.get(HALLIALUE);
            assertThat(greySealList, hasSize(1));
            assertThat(greySealList.get(0).getId(), is(equalTo(greySealArea.getId())));

            final List<HarvestAreaDTO> ringedSealList = areas.get(NORPPAALUE);
            assertThat(ringedSealList, hasSize(1));
            assertThat(ringedSealList.get(0).getId(), is(equalTo(ringedSealArea.getId())));
        });
    }

    @Test
    public void testCreate_withoutQuotas() {
        model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();
            feature.create(dto);

            runInTransaction(() -> {
                final List<HarvestSeason> seasons = repository.findAll();
                assertThat(seasons, hasSize(1));

                final HarvestSeason season = seasons.get(0);

                assertSeason(season, dto);

                assertThat(season.getQuotas(), hasSize(0));
            });
        });
    }

    @Test
    public void testCreate_withQuota() {
        model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "Alue", "Område");
        area.setOfficialCode("1");

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();

            final HarvestAreaDTO areaDTO = new HarvestAreaDTO();
            areaDTO.setOfficialCode("1");
            areaDTO.setHarvestAreaType(PORONHOITOALUE);

            final HarvestQuotaDTO quotaDTO = new HarvestQuotaDTO();
            quotaDTO.setQuota(10);
            quotaDTO.setHarvestArea(areaDTO);

            dto.setQuotas(singletonList(quotaDTO));

            feature.create(dto);

            runInTransaction(() -> {
                final List<HarvestSeason> seasons = repository.findAll();
                assertThat(seasons, hasSize(1));

                final HarvestSeason season = seasons.get(0);

                assertSeason(season, dto);

                final Set<HarvestQuota> quotas = season.getQuotas();
                assertThat(quotas, hasSize(1));
                final HarvestQuota quota = quotas.stream().findFirst().get();

                assertQuota(quota, quotaDTO);
            });
        });
    }

    @Test
    public void testUpdate() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final LocalDate beginDate = new LocalDate(2021, 12, 23);
        final LocalDate endDate = new LocalDate(2021, 12, 31);
        final LocalDate endOfReportingDate = new LocalDate(2022, 1, 7);
        final HarvestSeason season = model().newHarvestSeason(species, beginDate, endDate, endOfReportingDate);

        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "Area 1", "Area 1 sv");
        final HarvestQuota quota = model().newHarvestQuota(season, area, 10);

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = HarvestSeasonDTO.createWithSpeciesAndQuotas(season, singletonList(quota));
            dto.setBeginDate(beginDate.plusDays(1));
            dto.setEndDate(endDate.plusDays(1));
            dto.setEndOfReportingDate(endOfReportingDate.plusDays(1));
            dto.getQuotas().get(0).setQuota(20);
            feature.update(dto);

            runInTransaction(() -> {
                final List<HarvestSeason> seasons = repository.findAll();
                assertThat(seasons, hasSize(1));
                final HarvestSeason updatedSeason = seasons.get(0);

                assertSeason(updatedSeason, dto);

                final Set<HarvestQuota> quotas = updatedSeason.getQuotas();
                assertThat(quotas, hasSize(1));
                final HarvestQuota updatedQuota = quotas.stream().findFirst().get();

                assertQuota(updatedQuota, dto.getQuotas().get(0));
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreate_moderatorWithoutPrivileges() {
        model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final HarvestSeasonDTO dto = createDTO();
            feature.create(dto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreate_user() {
        model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");

        onSavedAndAuthenticated(createNewUser(), () -> {
            final HarvestSeasonDTO dto = createDTO();
            feature.create(dto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_moderatorWithoutPrivileges() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final LocalDate beginDate = new LocalDate(2021, 12, 23);
        final LocalDate endDate = new LocalDate(2021, 12, 31);
        final LocalDate endOfReportingDate = new LocalDate(2022, 1, 7);
        final HarvestSeason season = model().newHarvestSeason(species, beginDate, endDate, endOfReportingDate);

        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "Area 1", "Area 1 sv");
        final HarvestQuota quota = model().newHarvestQuota(season, area, 10);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final HarvestSeasonDTO dto = HarvestSeasonDTO.createWithSpeciesAndQuotas(season, singletonList(quota));
            dto.setBeginDate(beginDate.plusDays(1));
            dto.setEndDate(endDate.plusDays(1));
            dto.setEndOfReportingDate(endOfReportingDate.plusDays(1));
            dto.getQuotas().get(0).setQuota(20);
            feature.update(dto);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_user() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final LocalDate beginDate = new LocalDate(2021, 12, 23);
        final LocalDate endDate = new LocalDate(2021, 12, 31);
        final LocalDate endOfReportingDate = new LocalDate(2022, 1, 7);
        final HarvestSeason season = model().newHarvestSeason(species, beginDate, endDate, endOfReportingDate);

        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "Area 1", "Area 1 sv");
        final HarvestQuota quota = model().newHarvestQuota(season, area, 10);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final HarvestSeasonDTO dto = HarvestSeasonDTO.createWithSpeciesAndQuotas(season, singletonList(quota));
            dto.setBeginDate(beginDate.plusDays(1));
            dto.setEndDate(endDate.plusDays(1));
            dto.setEndOfReportingDate(endOfReportingDate.plusDays(1));
            dto.getQuotas().get(0).setQuota(20);
            feature.update(dto);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_overlappingBeginDateFirstPeriod() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        model().newHarvestSeason(species, new LocalDate(2022, 1, 1), new LocalDate(2022, 1, 15), new LocalDate(2022, 1, 20));

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();
            dto.setBeginDate(new LocalDate(2022, 1, 5));
            dto.setEndDate(new LocalDate(2022, 1, 20));
            dto.setEndOfReportingDate(new LocalDate(2022, 1, 30));
            dto.setBeginDate2(null);
            dto.setEndDate2(null);
            dto.setEndOfReportingDate2(null);
            feature.create(dto);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_overlappingBeginDateSecondPeriod() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        final HarvestSeason season = model().newHarvestSeason(species, new LocalDate(2022, 1, 1), new LocalDate(2022, 1, 15), new LocalDate(2022, 1, 20));
        season.setBeginDate2(new LocalDate(2022, 2, 1));
        season.setEndDate2(new LocalDate(2022, 2, 10));
        season.setEndOfReportingDate2(new LocalDate(2022, 2, 15));

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();
            dto.setBeginDate(new LocalDate(2022, 2, 5));
            dto.setEndDate(new LocalDate(2022, 2, 20));
            dto.setEndOfReportingDate(new LocalDate(2022, 2, 25));
            dto.setBeginDate2(null);
            dto.setEndDate2(null);
            dto.setEndOfReportingDate2(null);
            feature.create(dto);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_overlappingEndDateFirstPeriod() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        model().newHarvestSeason(species, new LocalDate(2022, 1, 1), new LocalDate(2022, 1, 15), new LocalDate(2022, 1, 20));

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();
            dto.setBeginDate(new LocalDate(2021, 12, 31));
            dto.setEndDate(new LocalDate(2022, 1, 10));
            dto.setEndOfReportingDate(new LocalDate(2022, 1, 30));
            dto.setBeginDate2(null);
            dto.setEndDate2(null);
            dto.setEndOfReportingDate2(null);
            feature.create(dto);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_overlappingEndDateSecondPeriod() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        final HarvestSeason season = model().newHarvestSeason(species, new LocalDate(2022, 1, 1), new LocalDate(2022, 1, 15), new LocalDate(2022, 1, 20));
        season.setBeginDate2(new LocalDate(2022, 2, 1));
        season.setEndDate2(new LocalDate(2022, 2, 10));
        season.setEndOfReportingDate2(new LocalDate(2022, 2, 15));

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();
            dto.setBeginDate(new LocalDate(2022, 1, 20));
            dto.setEndDate(new LocalDate(2022, 2, 5));
            dto.setEndOfReportingDate(new LocalDate(2022, 2, 20));
            dto.setBeginDate2(null);
            dto.setEndDate2(null);
            dto.setEndOfReportingDate2(null);
            feature.create(dto);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_overlappingBeginDate2FirstPeriod() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        model().newHarvestSeason(species, new LocalDate(2022, 1, 1), new LocalDate(2022, 1, 15), new LocalDate(2022, 1, 20));

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();
            dto.setBeginDate(new LocalDate(2021, 12, 1));
            dto.setEndDate(new LocalDate(2021, 12, 10));
            dto.setEndOfReportingDate(new LocalDate(2021, 12, 30));
            dto.setBeginDate2(new LocalDate(2022, 1, 10));
            dto.setEndDate2(new LocalDate(2022, 1, 16));
            dto.setEndOfReportingDate2(new LocalDate(2022, 1, 20));
            feature.create(dto);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_overlappingBeginDate2SecondPeriod() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        final HarvestSeason season = model().newHarvestSeason(species, new LocalDate(2022, 1, 1), new LocalDate(2022, 1, 15), new LocalDate(2022, 1, 20));
        season.setBeginDate2(new LocalDate(2022, 2, 1));
        season.setEndDate2(new LocalDate(2022, 2, 10));
        season.setEndOfReportingDate2(new LocalDate(2022, 2, 15));

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();
            dto.setBeginDate(new LocalDate(2021, 12, 1));
            dto.setEndDate(new LocalDate(2021, 12, 10));
            dto.setEndOfReportingDate(new LocalDate(2021, 12, 30));
            dto.setBeginDate2(new LocalDate(2022, 1, 20));
            dto.setEndDate2(new LocalDate(2022, 2, 5));
            dto.setEndOfReportingDate2(new LocalDate(2022, 2, 20));
            feature.create(dto);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_overlappingEndDate2FirstPeriod() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        model().newHarvestSeason(species, new LocalDate(2022, 1, 1), new LocalDate(2022, 1, 15), new LocalDate(2022, 1, 20));

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();
            dto.setBeginDate(new LocalDate(2021, 12, 1));
            dto.setEndDate(new LocalDate(2021, 12, 10));
            dto.setEndOfReportingDate(new LocalDate(2021, 12, 30));
            dto.setBeginDate2(new LocalDate(2021, 12, 30));
            dto.setEndDate2(new LocalDate(2022, 1, 5));
            dto.setEndOfReportingDate2(new LocalDate(2022, 1, 20));
            feature.create(dto);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreate_overlappingEndDate2SecondPeriod() {
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        final HarvestSeason season = model().newHarvestSeason(species, new LocalDate(2022, 1, 1), new LocalDate(2022, 1, 15), new LocalDate(2022, 1, 20));
        season.setBeginDate2(new LocalDate(2022, 2, 1));
        season.setEndDate2(new LocalDate(2022, 2, 10));
        season.setEndOfReportingDate2(new LocalDate(2022, 2, 15));

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestSeasonDTO dto = createDTO();
            dto.setBeginDate(new LocalDate(2021, 12, 1));
            dto.setEndDate(new LocalDate(2021, 12, 10));
            dto.setEndOfReportingDate(new LocalDate(2021, 12, 30));
            dto.setBeginDate2(new LocalDate(2022, 1, 20));
            dto.setEndDate2(new LocalDate(2022, 2, 5));
            dto.setEndOfReportingDate2(new LocalDate(2022, 2, 20));
            feature.create(dto);
        });
    }

    @Test
    public void testCopyHarvestSeasons() {
        final GameSpecies bear = model().newGameSpecies(OFFICIAL_CODE_BEAR, GameCategory.GAME_MAMMAL, "Karhu", "Björn", "Bear");
        final HarvestSeason bearSeason = model().newHarvestSeason(bear, new LocalDate(2021, 8, 1), new LocalDate(2021, 8, 15), new LocalDate(2021, 8, 20));
        bearSeason.setBeginDate2(new LocalDate(2021, 9, 1));
        bearSeason.setEndDate2(new LocalDate(2021, 9, 10));
        bearSeason.setEndOfReportingDate2(new LocalDate(2021, 9, 15));

        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "Area 1", "Area 1 sv");
        final HarvestQuota quota = model().newHarvestQuota(bearSeason, area, 10);
        bearSeason.setQuotas(Collections.singleton(quota));

        final GameSpecies partridge = model().newGameSpecies(OFFICIAL_CODE_PARTRIDGE, GameCategory.FOWL, "Peltopyy", "Rapphöna", "Partridge");
        final HarvestSeason partridgeSeason = model().newHarvestSeason(partridge, new LocalDate(2021, 10, 1), new LocalDate(2021, 10, 15), new LocalDate(2021, 10, 20));

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            feature.copyHarvestSeasons(2022);

            runInTransaction(() -> {
                final List<HarvestSeason> copiedSeasonList = repository.findBySeasonInHuntingYear(2022);
                assertThat(copiedSeasonList, hasSize(2));

                copiedSeasonList.forEach(copiedSeason -> {
                    final GameSpecies seasonSpecies = copiedSeason.getSpecies();
                    switch (seasonSpecies.getOfficialCode()) {
                        case OFFICIAL_CODE_BEAR:
                            assertCopiedSeason(bearSeason, copiedSeason);
                            break;
                        case OFFICIAL_CODE_PARTRIDGE:
                            assertCopiedSeason(partridgeSeason, copiedSeason);
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                });
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCopyHarvestSeasons_moderatorWithoutPrivileges() {
        final GameSpecies partridge = model().newGameSpecies(OFFICIAL_CODE_PARTRIDGE, GameCategory.FOWL, "Peltopyy", "Rapphöna", "Partridge");
        final HarvestSeason partridgeSeason = model().newHarvestSeason(partridge, new LocalDate(2021, 10, 1), new LocalDate(2021, 10, 15), new LocalDate(2021, 10, 20));
        partridgeSeason.setBeginDate2(new LocalDate(2021, 11, 1));
        partridgeSeason.setEndDate2(new LocalDate(2021, 11, 10));
        partridgeSeason.setEndOfReportingDate2(new LocalDate(2021, 11, 15));

        onSavedAndAuthenticated(createNewModerator(), () -> feature.copyHarvestSeasons(2022));
    }

    @Test(expected = AccessDeniedException.class)
    public void testCopyHarvestSeasons_user() {
        final GameSpecies partridge = model().newGameSpecies(OFFICIAL_CODE_PARTRIDGE, GameCategory.FOWL, "Peltopyy", "Rapphöna", "Partridge");
        final HarvestSeason partridgeSeason = model().newHarvestSeason(partridge, new LocalDate(2021, 10, 1), new LocalDate(2021, 10, 15), new LocalDate(2021, 10, 20));
        partridgeSeason.setBeginDate2(new LocalDate(2021, 11, 1));
        partridgeSeason.setEndDate2(new LocalDate(2021, 11, 10));
        partridgeSeason.setEndOfReportingDate2(new LocalDate(2021, 11, 15));

        onSavedAndAuthenticated(createNewUser(), () -> feature.copyHarvestSeasons(2022));
    }

    private void assertCopiedSeason(final HarvestSeason from, final HarvestSeason to) {
        assertThat(to.getNameFinnish(), is(equalTo(from.getNameFinnish())));
        assertThat(to.getNameSwedish(), is(equalTo(from.getNameSwedish())));
        assertThat(to.getBeginDate(), is(equalTo(from.getBeginDate().plusYears(1))));
        assertThat(to.getEndDate(), is(equalTo(from.getEndDate().plusYears(1))));
        assertThat(to.getEndOfReportingDate(), is(equalTo(from.getEndOfReportingDate().plusYears(1))));
        assertThat(to.getBeginDate2(), is(equalTo(F.mapNullable(from.getBeginDate2(), d -> d.plusYears(1)))));
        assertThat(to.getEndDate2(), is(equalTo(F.mapNullable(from.getEndDate2(), d -> d.plusYears(1)))));
        assertThat(to.getEndOfReportingDate2(), is(equalTo(F.mapNullable(from.getEndOfReportingDate2(), d -> d.plusYears(1)))));

        final Set<HarvestQuota> fromQuotas = from.getQuotas();
        if (fromQuotas == null || fromQuotas.isEmpty()) {
            assertThat(to.getQuotas(), hasSize(0));
        } else {
            final Optional<HarvestQuota> fromQuotaOpt = fromQuotas.stream().findFirst();
            final HarvestQuota fromQuota = fromQuotaOpt.get();

            final Set<HarvestQuota> toQuotas = to.getQuotas();
            assertThat(toQuotas, hasSize(1));

            final Optional<HarvestQuota> toQuotaOpt = toQuotas.stream().findFirst();
            assertThat(toQuotaOpt.isPresent(), is(true));

            final HarvestQuota toQuota = toQuotaOpt.get();
            assertThat(toQuota.getQuota(), is(equalTo(fromQuota.getQuota())));
            assertThat(toQuota.getHuntingSuspended(), is(equalTo(fromQuota.getHuntingSuspended())));

            final HarvestArea fromArea = fromQuota.getHarvestArea();
            final HarvestArea toArea = toQuota.getHarvestArea();
            assertThat(toArea.getId(), is(equalTo(fromArea.getId())));
        }
    }

    private HarvestSeasonDTO createDTO() {
        final HarvestSeasonDTO dto = new HarvestSeasonDTO();
        dto.setGameSpeciesCode(OFFICIAL_CODE_BEAR);

        final LocalisedString name = LocalisedString.of("Karhu kiintiö", "Björn kvot");
        dto.setName(name.asMap());

        final LocalDate beginDate = new LocalDate(2021, 8, 1);
        final LocalDate endDate = new LocalDate(2021, 8, 31);
        final LocalDate endOfReportingDate = new LocalDate(2021, 9, 14);
        dto.setBeginDate(beginDate);
        dto.setEndDate(endDate);
        dto.setEndOfReportingDate(endOfReportingDate);

        final LocalDate beginDate2 = new LocalDate(2022, 7, 1);
        final LocalDate endDate2 = new LocalDate(2022, 7, 31);
        final LocalDate endOfReportingDate2 = new LocalDate(2022, 8, 14);
        dto.setBeginDate2(beginDate2);
        dto.setEndDate2(endDate2);
        dto.setEndOfReportingDate2(endOfReportingDate2);

        return dto;
    }

    private void assertSeason(final HarvestSeason season, final HarvestSeasonDTO dto) {
        final GameSpecies species = season.getSpecies();
        assertThat(species.getOfficialCode(), is(equalTo(OFFICIAL_CODE_BEAR)));

        final LocalisedString name = LocalisedString.fromMap(dto.getName());
        assertThat(season.getNameFinnish(), is(equalTo(name.getFinnish())));
        assertThat(season.getNameSwedish(), is(equalTo(name.getSwedish())));

        assertThat(season.getBeginDate(), is(equalTo(dto.getBeginDate())));
        assertThat(season.getEndDate(), is(equalTo(dto.getEndDate())));
        assertThat(season.getEndOfReportingDate(), is(equalTo(dto.getEndOfReportingDate())));

        assertThat(season.getBeginDate2(), is(equalTo(dto.getBeginDate2())));
        assertThat(season.getEndDate2(), is(equalTo(dto.getEndDate2())));
        assertThat(season.getEndOfReportingDate2(), is(equalTo(dto.getEndOfReportingDate2())));
    }

    private void assertQuota(final HarvestQuota quota, final HarvestQuotaDTO dto) {
        assertThat(quota.getQuota(), is(equalTo(dto.getQuota())));

        final HarvestArea area = quota.getHarvestArea();
        assertThat(area, is(notNullValue()));
        final HarvestAreaDTO areaDTO = dto.getHarvestArea();
        assertThat(area.getOfficialCode(), is(equalTo(areaDTO.getOfficialCode())));
        assertThat(area.getType(), is(equalTo(areaDTO.getHarvestAreaType())));
    }
}
