package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEvent;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventAttachmentService;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventChangeService;
import fi.riista.feature.organization.rhy.huntingcontrolevent.HuntingControlEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

import static fi.riista.feature.organization.rhy.RiistanhoitoyhdistysAuthorization.RhyPermission.LIST_HUNTING_CONTROL_EVENTS;
import static java.util.Collections.singletonList;

@Service
public class MobileHuntingControlEventAttachmentFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingControlEventRepository huntingControlEventRepository;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingControlEventAttachmentService attachmentService;

    @Resource
    private MobileHuntingControlHelper helper;

    @Resource
    private HuntingControlEventChangeService changeService;

    @Transactional(rollbackFor = IOException.class)
    public Long addAttachment(final long eventId, final UUID uuid, final MultipartFile file) throws IOException {

        final Person authenticatedPerson = activeUserService.requireActivePerson();
        final HuntingControlEvent event =  huntingControlEventRepository.findById(eventId).get();
        final Riistanhoitoyhdistys rhy = requireEntityService.requireRiistanhoitoyhdistys(event.getRhy().getId(), LIST_HUNTING_CONTROL_EVENTS);

        helper.assertValidGameWarden(rhy, event.getDate());
        helper.assertPersonIsEventInspector(event, authenticatedPerson);
        helper.assertEventIsEditable(event);

        changeService.addNewAttachments(event, singletonList(file));
        return attachmentService.addAttachment(event, file, uuid);
    }

}
