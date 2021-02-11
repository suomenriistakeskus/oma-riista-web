package fi.riista.feature.permit.application.dogevent.fixture;

import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;

import static java.util.Collections.singletonList;

class DogEventApplicationFixture {

    public final Person applicant;
    public final Riistanhoitoyhdistys rhy;
    public final HarvestPermitApplication application;

    public DogEventApplicationFixture(final EntitySupplier es,
                                      final HarvestPermitCategory category) {

        applicant = es.newPerson();
        rhy = es.newRiistanhoitoyhdistys();

        application = es.newHarvestPermitApplication(rhy, null, category);
        es.newHuntingDogEventPermitApplication(application);
        application.setStatus(HarvestPermitApplication.Status.DRAFT);
        application.setPermitHolder(PermitHolder.createHolderForPerson(applicant));
        application.setContactPerson(applicant);

        final HarvestPermitApplicationAttachment attachment = new HarvestPermitApplicationAttachment();
        attachment.setAttachmentType(HarvestPermitApplicationAttachment.Type.PROTECTED_AREA);
        application.setAttachments(singletonList(attachment));
    }

}
