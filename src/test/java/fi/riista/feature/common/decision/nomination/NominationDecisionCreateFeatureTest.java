package fi.riista.feature.common.decision.nomination;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.fixture.OrganisationFixtureMixin;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.common.decision.nomination.NominationDecision.NominationDecisionType.NOMINATION;
import static fi.riista.feature.organization.occupation.OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

public class NominationDecisionCreateFeatureTest extends EmbeddedDatabaseTest implements OrganisationFixtureMixin {

    @Resource
    private NominationDecisionCreateFeature feature;

    @Resource
    private NominationDecisionRepository repository;

    @Test
    public void testCreateNominationDecision_rhyHasAddress() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            // Frontend does not support saving country for RHY address
            final Address addressNoCountry = model().newAddress();
            addressNoCountry.setCountry(null);
            rhy.setAddress(addressNoCountry);

            final CreateNominationDecisionDTO dto = createDecision(rhy);

            runInTransaction(() -> {
                final List<NominationDecision> decisions = repository.findAll();
                assertThat(decisions, hasSize(1));
                final NominationDecision decision = decisions.get(0);
                final Address rhyAddress = rhy.getAddress();

                assertDecisionData(decision, dto, rhy);
                assertAddress(decision, rhyAddress);
            });
        });
    }

    @Test
    public void testCreateNominationDecision_rhyDoesNotHaveAddress() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            final CreateNominationDecisionDTO dto = createDecision(rhy);

            runInTransaction(() -> {
                final List<NominationDecision> decisions = repository.findAll();
                assertThat(decisions, hasSize(1));
                final NominationDecision decision = decisions.get(0);
                final Address coordinatorAddress = coordinator.getAddress();

                assertDecisionData(decision, dto, rhy);
                assertAddress(decision, coordinatorAddress);
            });
        });
    }

    private CreateNominationDecisionDTO createDecision(final Riistanhoitoyhdistys rhy) {
        final CreateNominationDecisionDTO dto = new CreateNominationDecisionDTO();

        onSavedAndAuthenticated(createNewModerator(), () -> {
            dto.setNominationDecisionType(NOMINATION);
            dto.setOccupationType(AMPUMAKOKEEN_VASTAANOTTAJA);
            dto.setRhyCode(rhy.getOfficialCode());
            dto.setLocale(Locales.FI);

            feature.createNominationDecision(dto);
        });

        return dto;

    }

    private void assertDecisionData(final NominationDecision decision, final CreateNominationDecisionDTO dto,
                                    final Riistanhoitoyhdistys rhy) {
        assertThat(decision.getDecisionType(), equalTo(dto.getNominationDecisionType()));
        assertThat(decision.getOccupationType(), equalTo(dto.getOccupationType()));
        assertThat(decision.getRhy(), equalTo(rhy));
        assertThat(decision.getDeliveryAddress().getRecipient(), equalTo(rhy.getNameFinnish()));
    }

    private void assertAddress(final NominationDecision decision, final Address rhyAddress) {
        final DeliveryAddress deliveryAddress = decision.getDeliveryAddress();

        assertThat(deliveryAddress.getStreetAddress(), equalTo(rhyAddress.getStreetAddress()));
        assertThat(deliveryAddress.getPostalCode(), equalTo(rhyAddress.getPostalCode()));
        assertThat(deliveryAddress.getCity(), equalTo(rhyAddress.getCity()));
        assertThat(deliveryAddress.getCountry(), equalTo(rhyAddress.getCountry()));
    }
}
