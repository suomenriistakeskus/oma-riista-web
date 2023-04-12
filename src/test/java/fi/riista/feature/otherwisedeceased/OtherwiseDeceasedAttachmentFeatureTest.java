package fi.riista.feature.otherwisedeceased;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.error.NotFoundException;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class OtherwiseDeceasedAttachmentFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private OtherwiseDeceasedFeature feature;

    @Resource
    private OtherwiseDeceasedAttachmentFeature attachmentFeature;

    @Resource
    private OtherwiseDeceasedRepository repository;

    private SystemUser privilegedModerator;
    private SystemUser notAuthorised;
    private GameSpecies species;

    @Before
    public void setUp() {
        final EntitySupplier es = getEntitySupplier();
        privilegedModerator = createNewModerator(SystemUserPrivilege.MUUTOIN_KUOLLEET);
        notAuthorised = createNewModerator();
        species = es.newGameSpecies();
        es.newMunicipality();
        es.newRiistanhoitoyhdistys();
        persistInNewTransaction();
    }

    @Test(expected = AccessDeniedException.class)
    public void getAttachment_moderatorMustHavePrivilege() throws IOException {
        authenticate(notAuthorised);
        attachmentFeature.getAttachment(1);
    }

    @Test(expected = NotFoundException.class)
    public void getAttachment_butItDoesNotExist() throws IOException {
        authenticate(privilegedModerator);
        attachmentFeature.getAttachment(1);
    }

    @Test
    public void getAttachment_successfully() throws IOException {
        authenticate(privilegedModerator);
        final MultipartFile file = newAttachment();
        final OtherwiseDeceasedDTO dto = newOtherwiseDeceasedDTO();
        final OtherwiseDeceasedDTO saved = feature.save(dto, Arrays.asList(file));
        persistInNewTransaction();

        runInTransaction(() -> {
            final OtherwiseDeceased entity = repository.findById(saved.getId()).orElseThrow(NotFoundException::new);
            assertThat(entity.getAttachments(), hasSize(1));
        });
        assertThat(saved.getAttachments(), hasSize(1));
        final ResponseEntity<byte[]> downloadedFile = attachmentFeature.getAttachment(saved.getAttachments().get(0).getId());
        assertThat(downloadedFile.getBody(), equalTo(file.getBytes()));
    }

    @Test(expected = AccessDeniedException.class)
    public void deleteAttachment_moderatorMustHavePrivilege() {
        onSavedAndAuthenticated(notAuthorised, () -> attachmentFeature.deleteAttachment(1));
    }

    @Test(expected = NotFoundException.class)
    public void deleteAttachment_butItDoesNotExist() {
        onSavedAndAuthenticated(privilegedModerator, () -> attachmentFeature.deleteAttachment(1));
    }

    @Test
    public void deleteAttachment_andItsGone() throws IOException {
        authenticate(privilegedModerator);
        // Create item with an attachment.
        final MultipartFile file = newAttachment();
        final OtherwiseDeceasedDTO dto = newOtherwiseDeceasedDTO();
        final OtherwiseDeceasedDTO saved = feature.save(dto, Arrays.asList(file));
        persistInNewTransaction();

        attachmentFeature.deleteAttachment(saved.getAttachments().get(0).getId());
        persistInNewTransaction();

        runInTransaction(() -> {
            final OtherwiseDeceased entity = repository.findById(saved.getId()).orElseThrow(NotFoundException::new);
            assertThat(entity.getAttachments(), hasSize(0));
            assertThat(entity.getChangeHistory(), hasSize(2));
            final OtherwiseDeceasedChange change = entity.getChangeHistory().get(1);
            assertThat(change.getUserId(), equalTo(privilegedModerator.getId()),"change user mismatch");
            assertThat(change.getChangeType(), equalTo(OtherwiseDeceasedChange.ChangeType.DELETE_ATTACHMENT), "change type not DELETE_ATTACHMENT");
        });

    }

    private MultipartFile newAttachment() {
        final byte[] attachmentData = new byte[4096];
        new Random().nextBytes(attachmentData);
        final String filename = "test" + nextLong() + ".png";
        return new MockMultipartFile(filename, "//test/" + filename, "image/png", attachmentData);
    }

    private OtherwiseDeceasedDTO newOtherwiseDeceasedDTO() {
        final OtherwiseDeceasedDTO dto = new OtherwiseDeceasedDTO();
        dto.setGameSpeciesCode(species.getOfficialCode());
        dto.setAge(some(GameAge.class));
        dto.setGender(some(GameGender.class));
        dto.setWeight(weight());
        dto.setPointOfTime(LocalDateTime.now());
        dto.setNoExactLocation(someBoolean());
        dto.setGeoLocation(geoLocation());
        dto.setCause(some(OtherwiseDeceasedCause.class));
        dto.setCauseDescription("Cause other " + nextLong());
        dto.setSource(some(OtherwiseDeceasedSource.class));
        dto.setSourceDescription("Source other " + nextLong());
        dto.setDescription("Description " + nextLong());
        dto.setAdditionalInfo("Additional ingo " + nextLong());
        return dto;
    }
}
