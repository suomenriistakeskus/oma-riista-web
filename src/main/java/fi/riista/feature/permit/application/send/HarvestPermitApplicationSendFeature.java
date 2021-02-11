package fi.riista.feature.permit.application.send;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationAuthorizationService;
import fi.riista.feature.permit.application.DocumentNumberAllocationService;
import fi.riista.feature.permit.application.validation.HarvestPermitApplicationValidationService;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.UUID;

@Component
public class HarvestPermitApplicationSendFeature {

    @Resource
    private HarvestPermitApplicationAuthorizationService harvestPermitApplicationAuthorizationService;

    @Resource
    private HarvestPermitApplicationValidationService harvestPermitApplicationValidationService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private DocumentNumberAllocationService documentNumberAllocationService;

    @Transactional
    public void sendApplication(final HarvestPermitApplicationSendDTO dto) {
        final HarvestPermitApplication application =
                harvestPermitApplicationAuthorizationService.updateApplication(dto.getId());

        // validate
        harvestPermitApplicationValidationService.validateApplicationForSending(application);

        application.setApplicationNumber(documentNumberAllocationService.allocateNextNumber());
        application.setStatus(HarvestPermitApplication.Status.ACTIVE);
        application.setUuid(UUID.randomUUID());

        final DateTime submitDate = activeUserService.isModeratorOrAdmin() && dto.getSubmitDate() != null
                ? dto.getSubmitDate().toDateTime(new LocalTime(12, 0))
                : DateUtil.now();

        application.setSubmitDate(submitDate);
        application.setApplicationYear(submitDate.getYear());

        if (application.getArea() != null) {
            application.getArea().setStatusLocked();
        }
    }
}
