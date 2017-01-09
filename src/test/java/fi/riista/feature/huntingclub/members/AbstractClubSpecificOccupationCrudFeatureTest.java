package fi.riista.feature.huntingclub.members;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.person.PersonDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.ContactInfoShare;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import javaslang.Tuple;

import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public abstract class AbstractClubSpecificOccupationCrudFeatureTest extends EmbeddedDatabaseTest {

    protected void doTestContactInfoShare(final Function<Long, List<OccupationDTO>> listMembers,
                                          final Organisation org,
                                          final OccupationType userType,
                                          final boolean userCanSeeContactInfo,
                                          final ContactInfoShare share,
                                          final boolean canSeeRegistered) {

        final Occupation member = createMember(
                org,
                org.getOrganisationType() == OrganisationType.CLUB ? OccupationType.SEURAN_JASEN : OccupationType.RYHMAN_JASEN,
                share);
        createNewUser(member.getPerson().getByName(), member.getPerson());

        final Occupation user = createMember(org, userType, null);
        final Person person = user.getPerson();
        final SystemUser newUser = createNewUser(person.getByName(), person);

        persistInNewTransaction();
        authenticate(newUser);

        final List<OccupationDTO> dtos = listMembers.apply(org.getId());
        dtos.stream()
                .filter(dto -> dto.getId().equals(member.getId()))
                .forEach(dto -> {
                    final Person memberPerson = member.getPerson();
                    final PersonDTO dtoPerson = dto.getPerson();
                    assertEquals(
                            getExpected(userCanSeeContactInfo, memberPerson),
                            Tuple.of(dtoPerson.getEmail(), dtoPerson.getPhoneNumber())
                    );
                    assertEquals(canSeeRegistered, dtoPerson.isRegistered());
                    assertAddress(userCanSeeContactInfo, memberPerson.getAddress(), dtoPerson.getAddress());
                });
    }

    private static void assertAddress(boolean userCanSeeContactInfo, Address address, AddressDTO addressDTO) {
        if (userCanSeeContactInfo) {
            assertNotNull(address);
            assertNotNull(addressDTO);
            assertEquals(address.getStreetAddress(), addressDTO.getStreetAddress());
            assertEquals(address.getCity(), addressDTO.getCity());
            assertEquals(address.getPostalCode(), addressDTO.getPostalCode());
            assertEquals(address.getCountry(), addressDTO.getCountry());
        } else {
            assertNull(addressDTO);
        }
    }

    private static Object getExpected(boolean userCanSeeContactInfo, Person p) {
        return userCanSeeContactInfo
                ? Tuple.of(p.getEmail(), p.getPhoneNumber())
                : Tuple.of(null, null);
    }

    private Occupation createMember(Organisation org, OccupationType type, ContactInfoShare share) {
        final Occupation occ = model().newOccupation(org, model().newPerson(), type);
        occ.getPerson().setOtherAddress(model().newAddress());
        occ.setContactInfoShare(share);
        return occ;
    }
}
