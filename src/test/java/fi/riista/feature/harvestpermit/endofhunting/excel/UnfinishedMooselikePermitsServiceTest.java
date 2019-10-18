package fi.riista.feature.harvestpermit.endofhunting.excel;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.harvestpermit.fixture.MooselikePermitFixtureMixin;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

import static fi.riista.test.Asserts.assertEmpty;
import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UnfinishedMooselikePermitsServiceTest extends EmbeddedDatabaseTest implements MooselikePermitFixtureMixin {

    @Resource
    private UnfinishedMooselikePermitsService service;

    @Test
    public void testFindUnfinishedWithinMooselikeHunting_happyCase() {
        withMooselikePermitFixture(f -> {

            persistInNewTransaction();

            final List<UnfinishedMooselikePermitDTO> list = service.findUnfinishedWithinMooselikeHunting(f.huntingYear);

            assertEquals(1, list.size());
            verifyDTO(list.iterator().next(), f.speciesAmount, f.originalContactPerson.getMrAddress());
        });
    }

    @Test
    public void testFindUnfinishedWithinMooselikeHunting_verifyNonMooselikePermitNotIncluded() {
        withMooselikePermitFixture(f -> {

            f.permit.setPermitTypeCode("123");

            persistInNewTransaction();

            assertEmpty(service.findUnfinishedWithinMooselikeHunting(f.huntingYear));
        });
    }

    @Test
    public void testFindUnfinishedWithinMooselikeHunting_verifyUnfinishedSpeciesAmountNotIncluded() {
        withMooselikePermitFixture(f -> {

            f.speciesAmount.setMooselikeHuntingFinished(true);

            persistInNewTransaction();

            assertEmpty(service.findUnfinishedWithinMooselikeHunting(f.huntingYear));
        });
    }

    @Test
    public void testFindUnfinishedWithinMooselikeHunting_verifyFilteringByHuntingYear() {
        withMooselikePermitFixture(f -> {

            persistInNewTransaction();

            assertEmpty(service.findUnfinishedWithinMooselikeHunting(f.huntingYear - 1));
        });
    }

    @Test
    public void testFindUnfinishedWithinMooselikeHunting_otherAddressUsedIfMrAddressMissing() {
        withMooselikePermitFixture(f -> {

            f.originalContactPerson.setMrAddress(null);
            f.originalContactPerson.setOtherAddress(model().newAddress());

            persistInNewTransaction();

            final List<UnfinishedMooselikePermitDTO> list = service.findUnfinishedWithinMooselikeHunting(f.huntingYear);

            assertEquals(1, list.size());
            verifyDTO(list.iterator().next(), f.speciesAmount, f.originalContactPerson.getOtherAddress());
        });
    }

    @Test
    public void testFindUnfinishedWithinMooselikeHunting_addressOfOriginalContactPersonMayBeMissing() {
        withMooselikePermitFixture(f -> {

            f.originalContactPerson.setMrAddress(null);
            f.originalContactPerson.setOtherAddress(null);

            persistInNewTransaction();

            final List<UnfinishedMooselikePermitDTO> list = service.findUnfinishedWithinMooselikeHunting(f.huntingYear);

            assertEquals(1, list.size());
            verifyDTO(list.iterator().next(), f.speciesAmount, null);
        });
    }

    private static void verifyDTO(final UnfinishedMooselikePermitDTO dto,
                                  final HarvestPermitSpeciesAmount expectedSpeciesAmount,
                                  final Address expectedAddress) {

        assertEquals(expectedSpeciesAmount.getId(), Long.valueOf(dto.getSpeciesAmountId()));

        final HarvestPermit permit = expectedSpeciesAmount.getHarvestPermit();
        assertEquals(permit.getPermitNumber(), dto.getPermitNumber());

        final Riistanhoitoyhdistys rhy = permit.getRhy();
        assertEquals(rhy.getNameLocalisation(), dto.getRhyName());

        final GameSpecies species = expectedSpeciesAmount.getGameSpecies();
        assertEquals(species.getOfficialCode(), dto.getGameSpeciesCode());
        assertEquals(species.getNameLocalisation(), dto.getSpeciesName());

        final PermitHolder permitHolder = permit.getPermitHolder();
        assertEquals(permitHolder.getCode(), dto.getPermitHolderCustomerNumber());
        assertEquals(permitHolder.getName(), dto.getPermitHolderName());

        final Person originalContactPerson = permit.getOriginalContactPerson();
        final PersonContactInfoDTO personDTO =
                requireNonNull(dto.getOriginalContactPerson(), "originalContactPerson is null");

        assertEquals(originalContactPerson.getFirstName(), personDTO.getFirstName());
        assertEquals(originalContactPerson.getLastName(), personDTO.getLastName());
        assertEquals(originalContactPerson.getHunterNumber(), personDTO.getHunterNumber());
        assertEquals(originalContactPerson.getPhoneNumber(), personDTO.getPhoneNumber());
        assertEquals(originalContactPerson.getEmail(), personDTO.getEmail());

        final AddressDTO addressDTO = requireNonNull(personDTO.getAddress(), "address of contact person is null");

        if (expectedAddress != null) {
            assertEquals(expectedAddress.getStreetAddress(), addressDTO.getStreetAddress());
            assertEquals(expectedAddress.getPostalCode(), addressDTO.getPostalCode());
            assertEquals(expectedAddress.getCity(), addressDTO.getCity());

            final String country =
                    Optional.ofNullable(expectedAddress.getCountry()).orElseGet(expectedAddress::getCountryCode);
            assertEquals(country, addressDTO.getCountry());

        } else {
            assertNull(addressDTO.getStreetAddress());
            assertNull(addressDTO.getPostalCode());
            assertNull(addressDTO.getCity());
            assertNull(addressDTO.getCountry());
        }
    }
}
