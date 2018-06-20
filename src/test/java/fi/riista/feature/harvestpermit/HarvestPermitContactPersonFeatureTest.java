package fi.riista.feature.harvestpermit;

import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static fi.riista.test.Asserts.assertEmpty;
import static fi.riista.util.Collect.idSet;
import static org.junit.Assert.assertEquals;

public class HarvestPermitContactPersonFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestPermitContactPersonFeature harvestPermitContactPersonFeature;

    @Resource
    private HarvestPermitContactPersonRepository contactPersonRepository;

    @Test
    public void testUpdateContactPersons() {
        withRhy(rhy -> {
            final Person person1 = model().newPerson();
            final Person person2 = model().newPerson();

            final HarvestPermit permit = model().newHarvestPermit(rhy);
            final Person originalContactPerson = permit.getOriginalContactPerson();

            final HarvestPermitContactPerson oldContactPerson1 = model().newHarvestPermitContactPerson(permit, person1);
            final HarvestPermitContactPerson oldContactPerson2 = model().newHarvestPermitContactPerson(permit, person2);

            final HarvestPermit permit2 = model().newHarvestPermit(rhy);
            final HarvestPermitContactPerson permit2ContactPerson =
                    model().newHarvestPermitContactPerson(permit2, person1);

            final Person person3 = model().newPerson();
            final Person person4 = model().newPerson();

            onSavedAndAuthenticated(createNewModerator(), () -> {
                harvestPermitContactPersonFeature.updateContactPersons(permit.getId(), Arrays.asList(
                        HarvestPermitContactPersonDTO.create(person3),
                        HarvestPermitContactPersonDTO.create(person4),
                        // should be ignored
                        HarvestPermitContactPersonDTO.create(originalContactPerson)));

                assertEmpty(contactPersonRepository.findAll(F.getUniqueIds(oldContactPerson1, oldContactPerson2)));

                final List<HarvestPermitContactPerson> updatedContactPersons =
                        contactPersonRepository.findByHarvestPermit(permit);
                assertEquals(2, updatedContactPersons.size());

                assertEquals(
                        F.getUniqueIds(person3, person4),
                        updatedContactPersons.stream()
                                .map(HarvestPermitContactPerson::getContactPerson)
                                .collect(idSet()));

                // Assert that permit2 is left intact.
                assertEquals(
                        F.getUniqueIds(permit2ContactPerson),
                        F.getUniqueIds(contactPersonRepository.findByHarvestPermit(permit2)));
            });
        });
    }

}
