package fi.riista.feature.organization.rhy.huntingcontrolevent;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.util.DateUtil;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HuntingControlEventChangeService {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private HuntingControlEventChangeRepository changeRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addModify(final HuntingControlEvent event, final String reasonForChange) {
        changeRepository.save(HuntingControlEventChange.create(
                event,
                DateUtil.now(),
                activeUserService.requireActiveUserId(),
                ChangeHistory.ChangeType.MODIFY,
                reasonForChange));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addCreate(final HuntingControlEvent event) {
        changeRepository.save(HuntingControlEventChange.create(
                event,
                DateUtil.now(),
                activeUserService.requireActiveUserId(),
                ChangeHistory.ChangeType.CREATE,
                null));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addNewAttachments(final HuntingControlEvent event, final List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            final String filenames = files.stream()
                    .map(MultipartFile::getName)
                    .collect(Collectors.joining(", "));
            changeRepository.save(HuntingControlEventChange.create(
                    event,
                    DateUtil.now(),
                    activeUserService.requireActiveUserId(),
                    ChangeHistory.ChangeType.ADD_ATTACHMENTS,
                    filenames));
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addDeleteAttachment(final HuntingControlEvent event, final String filename) {
        changeRepository.save(HuntingControlEventChange.create(
                event,
                DateUtil.now(),
                activeUserService.requireActiveUserId(),
                ChangeHistory.ChangeType.DELETE_ATTACHMENT,
                filename));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addChangeStatus(final HuntingControlEvent event, final String status) {
        changeRepository.save(HuntingControlEventChange.create(
                event,
                DateUtil.now(),
                activeUserService.requireActiveUserId(),
                ChangeHistory.ChangeType.CHANGE_STATUS,
                status));
    }

}
