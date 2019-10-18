package fi.riista.feature.harvestpermit.search;

import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitNotFoundException;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HarvestPermitSearchFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitSearchFeature harvestPermitSearchFeature;

    @Test
    public void testFindPermitNumber() {
        final HarvestPermit permit = model().newHarvestPermit(createUserWithPerson().getPerson());
        persistInNewTransaction();

        final HarvestPermitExistsDTO dto = harvestPermitSearchFeature.findPermitNumber(permit.getPermitNumber());
        assertNotNull(dto);
        assertEquals(permit.getId(), dto.getId());
    }

    @Test(expected = HarvestPermitNotFoundException.class)
    public void testFindPermitNumber_forNonExistingPermit() {
        harvestPermitSearchFeature.findPermitNumber("1234567");
    }

    @Test(expected = HarvestPermitNotFoundException.class)
    public void testFindPermitNumber_forMooselikePermit() {
        withRhy(rhy -> withPerson(person -> {

            final HarvestPermit moosePermit = model().newMooselikePermit(rhy);
            model().newHarvestPermitContactPerson(moosePermit, person);

            persistInNewTransaction();

            harvestPermitSearchFeature.findPermitNumber(moosePermit.getPermitNumber());
        }));
    }

    @Test(expected = HarvestPermitNotFoundException.class)
    public void testFindPermitNumber_forAmendmentPermit() {
        withPerson(person -> {

            final HarvestPermit originalPermit = model().newHarvestPermit(person);
            final HarvestPermit amendmentPermit = model().newHarvestPermit(originalPermit, person);

            persistInNewTransaction();

            harvestPermitSearchFeature.findPermitNumber(amendmentPermit.getPermitNumber());
        });
    }

}
