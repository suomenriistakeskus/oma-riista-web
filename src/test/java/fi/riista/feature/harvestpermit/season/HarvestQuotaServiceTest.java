package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.riista.feature.account.user.SystemUserPrivilege.MODERATE_HARVEST_SEASONS;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_BEAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_GREY_SEAL;
import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaType.NORPPAALUE;
import static fi.riista.feature.harvestpermit.season.HarvestArea.HarvestAreaType.PORONHOITOALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class HarvestQuotaServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestQuotaService service;

    @Resource
    private HarvestQuotaRepository repository;

    @Test
    public void testCreateOrUpdateQuotas_create() {
        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "alue", "område");
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final HarvestSeason season = model().newHarvestSeason(species);

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestQuotaDTO quotaDTO = new HarvestQuotaDTO();
            quotaDTO.setQuota(10);
            quotaDTO.setHarvestArea(HarvestAreaDTO.create(area));
            runInTransaction(() -> service.createOrUpdateQuotas(Collections.singletonList(quotaDTO), season));

            runInTransaction(() -> {
                final List<HarvestQuota> quotaList = repository.findAll();
                assertThat(quotaList, hasSize(1));

                final HarvestQuota quota = quotaList.get(0);
                assertThat(quota.getQuota(), is(equalTo(quotaDTO.getQuota())));
                assertThat(quota.getHarvestSeason().getId(), is(equalTo(season.getId())));
                assertThat(quota.getHarvestArea().getId(), is(equalTo(area.getId())));
            });
        });
    }

    @Test
    public void testCreateOrUpdateQuotas_update() {
        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "alue", "område");
        final HarvestArea area2 = model().newHarvestArea(NORPPAALUE, "alue", "område");
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final HarvestSeason season = model().newHarvestSeason(species);
        final GameSpecies species2 = model().newGameSpecies(OFFICIAL_CODE_GREY_SEAL);
        final HarvestSeason season2 = model().newHarvestSeason(species2);
        final HarvestQuota quota = model().newHarvestQuota(season, area, 1);

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestQuotaDTO quotaDTO = HarvestQuotaDTO.create(quota);
            quotaDTO.setQuota(10);
            quotaDTO.setHarvestArea(HarvestAreaDTO.create(area2));
            runInTransaction(() -> service.createOrUpdateQuotas(Collections.singletonList(quotaDTO), season2));

            runInTransaction(() -> {
                final List<HarvestQuota> quotaList = repository.findAll();
                assertThat(quotaList, hasSize(1));

                final HarvestQuota updated = quotaList.get(0);
                assertThat(updated.getQuota(), is(equalTo(quotaDTO.getQuota())));
                assertThat(updated.getHarvestSeason().getId(), is(equalTo(season.getId())));
                assertThat(updated.getHarvestArea().getId(), is(equalTo(area.getId())));
            });
        });
    }

    @Test
    public void testCreateOrUpdateQuotas_createAndUpdate() {
        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "alue", "område");
        area.setOfficialCode("1");
        final HarvestArea area2 = model().newHarvestArea(PORONHOITOALUE, "alue2", "område2");
        area2.setOfficialCode("2");
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final HarvestSeason season = model().newHarvestSeason(species);
        final HarvestQuota quota = model().newHarvestQuota(season, area, 1);

        onSavedAndAuthenticated(createNewModerator(MODERATE_HARVEST_SEASONS), () -> {
            final HarvestQuotaDTO createDTO = new HarvestQuotaDTO();
            createDTO.setQuota(2);
            createDTO.setHarvestArea(HarvestAreaDTO.create(area2));

            final HarvestQuotaDTO updateDTO = HarvestQuotaDTO.create(quota);
            updateDTO.setQuota(10);
            updateDTO.setHarvestArea(HarvestAreaDTO.create(area2));
            runInTransaction(() -> service.createOrUpdateQuotas(Arrays.asList(createDTO, updateDTO), season));

            runInTransaction(() -> {
                final List<HarvestQuota> quotaList = repository.findAll();
                assertThat(quotaList, hasSize(2));

                quotaList.forEach(q -> {
                    assertThat(q.getHarvestSeason().getId(), is(equalTo(season.getId())));

                    if (q.getId() == quota.getId()) {
                        assertThat(q.getQuota(), is(equalTo(updateDTO.getQuota())));
                        assertThat(q.getHarvestArea().getId(), is(equalTo(area.getId())));
                    } else {
                        assertThat(q.getId(), is(notNullValue()));
                        assertThat(q.getQuota(), is(equalTo(createDTO.getQuota())));
                        assertThat(q.getHarvestArea().getId(), is(equalTo(area2.getId())));
                    }
                });
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateOrUpdateQuotas_moderatorWithoutPrivileges() {
        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "alue", "område");
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final HarvestSeason season = model().newHarvestSeason(species);

        onSavedAndAuthenticated(createNewModerator(), () -> {
            final HarvestQuotaDTO quotaDTO = new HarvestQuotaDTO();
            quotaDTO.setQuota(10);
            quotaDTO.setHarvestArea(HarvestAreaDTO.create(area));
            runInTransaction(() -> service.createOrUpdateQuotas(Collections.singletonList(quotaDTO), season));
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testCreateOrUpdateQuotas_user() {
        final HarvestArea area = model().newHarvestArea(PORONHOITOALUE, "alue", "område");
        final GameSpecies species = model().newGameSpecies(OFFICIAL_CODE_BEAR);
        final HarvestSeason season = model().newHarvestSeason(species);

        onSavedAndAuthenticated(createNewUser(), () -> {
            final HarvestQuotaDTO quotaDTO = new HarvestQuotaDTO();
            quotaDTO.setQuota(10);
            quotaDTO.setHarvestArea(HarvestAreaDTO.create(area));
            runInTransaction(() -> service.createOrUpdateQuotas(Collections.singletonList(quotaDTO), season));
        });
    }
}
