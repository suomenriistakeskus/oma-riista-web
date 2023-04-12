package fi.riista.feature.common.decision.nomination;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionAction;
import fi.riista.feature.common.decision.nomination.authority.NominationDecisionAuthority;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.storage.FileStorageService;
import fi.riista.feature.storage.metadata.FileType;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NominationDecisionFeatureTest extends EmbeddedDatabaseTest {

    private NominationDecision decision;
    private Riistanhoitoyhdistys rhy;
    private Person coordinator;
    private SystemUser moderator;

    @Resource
    private NominationDecisionFeature feature;

    @Resource
    private NominationDecisionRepository repository;

    @Resource
    private FileStorageService fileStorageService;

    @Before
    public void setup() {
        rhy = model().newRiistanhoitoyhdistys();
        coordinator = model().newPersonWithAddress();
        model().newOccupation(rhy, coordinator, OccupationType.TOIMINNANOHJAAJA);
        final DeliveryAddress deliveryAddress = DeliveryAddress.create(rhy.getNameFinnish(), coordinator.getAddress());

        decision = model().newNominationDecision(rhy, OccupationType.METSASTYKSENVALVOJA, coordinator, deliveryAddress);

        moderator = createNewModerator();
        decision.setHandler(moderator);
    }

    @Test(expected = AccessDeniedException.class)
    public void testAuthentication_unauthorized() {
        persistInNewTransaction();

        feature.getDecision(decision.getId());
        fail("Should have thrown an exception");
    }

    @Test
    public void testGetSmoke() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            final NominationDecisionDTO dto = feature.getDecision(feature.getDecision(decision.getId()).getId());
            assertEquals(decision.getDecisionType(), dto.getDecisionType());
            assertEquals(rhy.getId(), dto.getRhy().getId());
            assertEquals(decision.getOccupationType(), dto.getOccupationType());
            assertEquals(decision.getDeliveryAddress().getStreetAddress(), dto.getDeliveryAddress().getStreetAddress());
            assertEquals(decision.getDeliveryAddress().getPostalCode(), dto.getDeliveryAddress().getPostalCode());
            assertEquals(decision.getDeliveryAddress().getCity(), dto.getDeliveryAddress().getCity());
            assertEquals(decision.getDeliveryAddress().getCountry(), dto.getDeliveryAddress().getCountry());
        });
    }

    @Test
    public void testDelete() throws IOException {
        final NominationDecisionAction action = model().newNominationDecisionAction(decision);

        final PersistentFileMetadata actionAttachment = fileStorageService.storeFile(UUID.randomUUID(),
                "foobar".getBytes(),
                FileType.DECISION_ACTION_ATTACHMENT,
                MediaType.APPLICATION_PDF_VALUE,
                "action_attachment.pdf");
        model().newNominationDecisionActionAttachment(action, actionAttachment);

        final PersistentFileMetadata decisionAttachment = fileStorageService.storeFile(UUID.randomUUID(),
                "foobar".getBytes(),
                FileType.DECISION_ATTACHMENT,
                MediaType.APPLICATION_PDF_VALUE,
                "action_attachment.pdf");
        model().newNominationDecisionAttachment(decision, decisionAttachment);

        final NominationDecisionAuthority presenter = model().newNominationDecisionAuthority(decision);
        decision.setPresenter(presenter);
        final NominationDecisionAuthority decisionMaker = model().newNominationDecisionAuthority(decision);
        decision.setDecisionMaker(decisionMaker);

        model().newNominationDecisionDelivery(decision);

        final DeliveryAddress deliveryAddress = DeliveryAddress.create(rhy.getNameFinnish(), coordinator.getAddress());
        final NominationDecision referencing = model().newNominationDecision(rhy, OccupationType.METSASTYKSENVALVOJA, coordinator, deliveryAddress);
        referencing.setReference(decision);

        onSavedAndAuthenticated(moderator, () -> {
            final long decisionId = decision.getId();

            final NominationDecisionDTO dto = feature.getDecision(feature.getDecision(decisionId).getId());
            assertThat(dto.isCanDelete(), is(equalTo(true)));

            feature.delete(decisionId);

            final List<NominationDecision> decisions = repository.findAll();
            assertThat(decisions, hasSize(1));
            assertThat(decisions.get(0).getId(), is(equalTo(referencing.getId())));
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelete_publishedRevision() {
        model().newNominationDecisionRevision(decision);

        onSavedAndAuthenticated(moderator, () -> {
            final long decisionId = decision.getId();

            final NominationDecisionDTO dto = feature.getDecision(feature.getDecision(decisionId).getId());
            assertThat(dto.isCanDelete(), is(equalTo(false)));

            feature.delete(decisionId);
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDelete_noHandler() {
        decision.setHandler(null);

        onSavedAndAuthenticated(moderator, () -> {
            final long decisionId = decision.getId();

            final NominationDecisionDTO dto = feature.getDecision(feature.getDecision(decisionId).getId());
            assertThat(dto.isCanDelete(), is(equalTo(false)));

            feature.delete(decisionId);
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_unauthorized() {
        onSavedAndAuthenticated(createNewUser(), () -> {
            feature.delete(decision.getId());
        });
    }
}
