package fi.riista.feature.permit.application.bird.applicant;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplicationBasicDetailsDTO;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.create.HarvestPermitApplicationCreateDTO;
import fi.riista.feature.permit.application.create.HarvestPermitApplicationCreateFeature;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.Locales;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

public class BirdPermitApplicationApplicantFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private BirdPermitApplicationApplicantFeature birdPermitApplicationFeature;

    @Resource
    private HarvestPermitApplicationCreateFeature harvestPermitApplicationCreateFeature;

    private Person applicationContactPerson;
    private SystemUser contactPersonUser;

    private HarvestPermitApplicationBasicDetailsDTO applicationDTO;

    @Before
    public void setup() {
        withPerson(contactPerson -> {
            applicationContactPerson = contactPerson;
            contactPersonUser = createUser(contactPerson);
            persistInNewTransaction();

            onSavedAndAuthenticated(contactPersonUser, () -> {
                HarvestPermitApplicationCreateDTO createDTO = new HarvestPermitApplicationCreateDTO();
                createDTO.setApplicationName("Lintulupa");
                createDTO.setHuntingYear(2019);
                createDTO.setCategory(HarvestPermitCategory.BIRD);
                createDTO.setPersonId(applicationContactPerson.getId());

                applicationDTO =
                        harvestPermitApplicationCreateFeature.create(createDTO, Locales.FI);
            });
        });
    }

    @Test
    public void updatePermitHolder() {
        onSavedAndAuthenticated(contactPersonUser, () -> {
            String holderName = "Updated Name";
            String holderCode = "012345";

            birdPermitApplicationFeature.updatePermitHolder(
                    applicationDTO.getId(),
                    PermitHolderDTO.create(holderName, holderCode, PermitHolder.PermitHolderType.PERSON));

            PermitHolderDTO holder = birdPermitApplicationFeature.getPermitHolderInfo(applicationDTO.getId());
            assertEquals(holderName, holder.getName());
        });
    }

}
