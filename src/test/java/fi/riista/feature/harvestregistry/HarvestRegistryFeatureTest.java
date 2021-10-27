package fi.riista.feature.harvestregistry;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Slice;

import javax.annotation.Resource;

import static fi.riista.feature.account.user.SystemUserPrivilege.HARVEST_REGISTRY;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.today;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertFalse;

public class HarvestRegistryFeatureTest extends EmbeddedDatabaseTest implements HarvestRegistryItemFixtureMixin {

    @Resource
    private HarvestRegistryFeature feature;

    private Person personWithNoHunterNumber;

    @Before
    public void setup() {
        personWithNoHunterNumber = model().newPerson();
        personWithNoHunterNumber.setHunterNumber(null);

        persistInNewTransaction();
    }

    @Test
    public void testPersonWithNoHunterNumberGetsEmptyResult() {
        onSavedAndAuthenticated(createUser(personWithNoHunterNumber), () -> {
            final HarvestRegistryRequestDTO dto = new HarvestRegistryRequestDTO();
            dto.setAllSpecies(true);
            dto.setBeginDate(today());
            dto.setEndDate(today().plusMonths(1));
            dto.setPage(0);
            dto.setPageSize(10);
            final Slice<HarvestRegistryItemDTO> slice = feature.listMine(dto, Locales.FI);

            assertThat(slice.getContent(), hasSize(0));
            assertFalse(slice.hasNext());
        });
    }

    @Test
    public void testGetPage_endTime() {
        final LocalDate endDate = today().minusDays(1);
        final HarvestRegistryRequestDTO requestDTO = HarvestRegistryRequestDTO.Builder.builder()
                .withEndDate(endDate)
                .withBeginDate(endDate.minusYears(5))
                .withAllSpecies(true)
                .withPage(0)
                .withPageSize(1_000)
                .build();

        final GameSpecies moose = model().newGameSpecies(OFFICIAL_CODE_MOOSE);
        final LocalTime lastMinuteOfDay = new LocalTime(23, 59);

        withItem(moose, tooNewFixture -> {
            tooNewFixture.item.setPointOfTime(DateUtil.toDateTimeNullSafe(today(), lastMinuteOfDay));
            withItem(moose, fixture -> {
                final LocalDate fixtureDate = today().minusDays(1);
                fixture.item.setPointOfTime(DateUtil.toDateTimeNullSafe(fixtureDate, lastMinuteOfDay));

                onSavedAndAuthenticated(createNewModerator(HARVEST_REGISTRY), () -> {
                    final Slice<HarvestRegistryItemDTO> dtos = feature.listPaged(requestDTO);

                    assertThat(dtos.hasNext(), is(false));
                    assertThat(dtos.getContent(), hasSize(1));

                    final HarvestRegistryItemDTO itemDTO = dtos.getContent().get(0);
                    assertThat(itemDTO.getId(), equalTo(fixture.item.getId()));
                    assertThat(itemDTO.getDate(), equalTo(fixtureDate));
                });
            });
        });
    }
}
