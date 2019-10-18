package fi.riista.feature.harvestregistry;

import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.domain.Slice;

import javax.annotation.Resource;

import static fi.riista.util.DateUtil.today;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertFalse;

public class HarvestRegistryFeatureTest extends EmbeddedDatabaseTest {

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
}
