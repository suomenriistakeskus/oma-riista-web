package fi.riista.feature.pub.occupation;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.pub.occupation.PublicOccupationSearchFeature;
import fi.riista.feature.pub.occupation.PublicOccupationTypeDTO;
import fi.riista.feature.pub.occupation.PublicOccupationsAndOrganisationsDTO;
import fi.riista.feature.pub.occupation.PublicOrganisationDTO;
import fi.riista.feature.pub.occupation.PublicOccupationSearchParameters;
import fi.riista.util.DateUtil;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PublicOccupationSearchFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private PublicOccupationSearchFeature occupationSearchFeature;

    @Nonnull
    private static CustomTypeSafeMatcher<PublicOccupationTypeDTO> equalToOccupationType(final OrganisationType organisationType,
                                                                                        final OccupationType occupationType) {
        return new CustomTypeSafeMatcher<PublicOccupationTypeDTO>(
                String.format("occupationType=%s organisationType=%s", occupationType, organisationType)) {
            @Override
            protected boolean matchesSafely(final PublicOccupationTypeDTO o) {
                return o.getOccupationType() == occupationType && o.getOrganisationType() == organisationType;
            }
        };
    }

    @Nonnull
    private static CustomTypeSafeMatcher<AddressDTO> equalToAddress(final Address address) {
        return new CustomTypeSafeMatcher<AddressDTO>("Address does not match") {
            @Override
            protected boolean matchesSafely(final AddressDTO o) {
                return Objects.equals(o.getStreetAddress(), address.getStreetAddress()) &&
                        Objects.equals(o.getCity(), address.getCity()) &&
                        Objects.equals(o.getPostalCode(), address.getPostalCode());
            }
        };
    }

    @Test
    public void testOccupationTypes() {
        final List<PublicOccupationTypeDTO> result = occupationSearchFeature.getAllOccupationTypes();

        assertThat(result, contains(Arrays.asList(
                equalToOccupationType(OrganisationType.RK, OccupationType.PUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.RK, OccupationType.VARAPUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.RK, OccupationType.HALLITUKSEN_JASEN),
                equalToOccupationType(OrganisationType.RK, OccupationType.HALLITUKSEN_VARAJASEN),
                equalToOccupationType(OrganisationType.VRN, OccupationType.PUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.VRN, OccupationType.VARAPUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.VRN, OccupationType.JASEN),
                equalToOccupationType(OrganisationType.VRN, OccupationType.VARAJASEN),
                equalToOccupationType(OrganisationType.ARN, OccupationType.PUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.ARN, OccupationType.VARAPUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.ARN, OccupationType.JASEN),
                equalToOccupationType(OrganisationType.ARN, OccupationType.VARAJASEN),
                equalToOccupationType(OrganisationType.RHY, OccupationType.TOIMINNANOHJAAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.SRVA_YHTEYSHENKILO),
                equalToOccupationType(OrganisationType.RHY, OccupationType.PETOYHDYSHENKILO),
                equalToOccupationType(OrganisationType.RHY, OccupationType.METSASTYKSENVALVOJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.METSASTAJATUTKINTOON_VALMENTAVAN_KOULUTUKSEN_KOULUTTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.PUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.VARAPUHEENJOHTAJA),
                equalToOccupationType(OrganisationType.RHY, OccupationType.HALLITUKSEN_JASEN),
                equalToOccupationType(OrganisationType.RHY, OccupationType.HALLITUKSEN_VARAJASEN),
                equalToOccupationType(OrganisationType.RHY, OccupationType.JALJESTYSKOIRAN_OHJAAJA_HIRVI),
                equalToOccupationType(OrganisationType.RHY, OccupationType.JALJESTYSKOIRAN_OHJAAJA_PIENET_HIRVIELAIMET),
                equalToOccupationType(OrganisationType.RHY, OccupationType.JALJESTYSKOIRAN_OHJAAJA_SUURPEDOT)
        )));
    }

    @Test
    public void testOnlyValid() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();

        final Occupation past = model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);
        past.setEndDate(DateUtil.today().minusDays(1));

        final Occupation future = model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);
        future.setBeginDate(DateUtil.today().plusDays(1));

        final Occupation active = model().newOccupation(rhy, model().newPerson(), OccupationType.TOIMINNANOHJAAJA);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));
        assertEquals(active.getPerson().getFullName(), result.getOccupations().get(0).getPersonName());
    }

    @Test
    public void testCoordinator() {
        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys();
        final Riistanhoitoyhdistys rhy2 = model().newRiistanhoitoyhdistys();
        final Person person1 = model().newPerson();
        final Person person2 = model().newPerson();
        model().newOccupation(rhy1, person1, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy1, person1, OccupationType.SRVA_YHTEYSHENKILO);
        model().newOccupation(rhy2, person2, OccupationType.TOIMINNANOHJAAJA);
        model().newOccupation(rhy2, person2, OccupationType.METSASTYKSENVALVOJA);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        assertThat(result.getOccupations(), hasSize(2));
        assertThat(result.getOrganisations(), hasSize(2));
    }

    @Test
    public void testCoordinator_UseRhyInformation() {
        // RHY has address
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setAddress(model().newAddress());
        rhy.setEmail(model().zeroPaddedNumber(3) + "@invalid");
        rhy.setPhoneNumber(model().phoneNumber());

        // Coordinator has address, email and phoneNumber
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.TOIMINNANOHJAAJA);
        person.setMrAddress(model().newAddress());
        person.setPhoneNumber(model().phoneNumber());

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));

        final PublicOrganisationDTO rhyDTO = result.getOrganisations().get(0);
        assertThat(rhyDTO.getAddress(), equalToAddress(rhy.getAddress()));
        assertEquals(rhyDTO.getPhoneNumber(), rhy.getPhoneNumber());
        assertEquals(rhyDTO.getEmail(), rhy.getEmail());
    }

    @Test
    public void testCoordinator_UseCoordinatorInformation() {
        // RHY does not have address, email or phoneNumber
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        rhy.setAddress(null);
        rhy.setEmail(null);
        rhy.setPhoneNumber(null);

        // Coordinator has address
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.TOIMINNANOHJAAJA);
        person.setMrAddress(model().newAddress());
        person.setPhoneNumber(model().phoneNumber());

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.TOIMINNANOHJAAJA)
                        .withOrganisationType(OrganisationType.RHY)
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));

        final PublicOrganisationDTO rhyDTO = result.getOrganisations().get(0);
        assertThat(rhyDTO.getAddress(), equalToAddress(person.getMrAddress()));
        assertEquals(rhyDTO.getPhoneNumber(), person.getPhoneNumber());
        assertEquals(rhyDTO.getEmail(), person.getEmail());
    }

    @Test
    public void testSrva_ByArea() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys(rka);
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.SRVA_YHTEYSHENKILO)
                        .withOrganisationType(OrganisationType.RHY)
                        .withAreaId(rka.getOfficialCode())
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));
    }

    @Test
    public void testSrva_ByRhy() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        final Person person = model().newPerson();
        model().newOccupation(rhy, person, OccupationType.SRVA_YHTEYSHENKILO);

        persistInNewTransaction();

        final PublicOccupationsAndOrganisationsDTO result = occupationSearchFeature.findOccupationsAndOrganisations(
                PublicOccupationSearchParameters.builder()
                        .withOccupationType(OccupationType.SRVA_YHTEYSHENKILO)
                        .withOrganisationType(OrganisationType.RHY)
                        .withRhyId(rhy.getOfficialCode())
                        .build());

        assertThat(result.getOccupations(), hasSize(1));
        assertThat(result.getOrganisations(), hasSize(1));
    }
}
