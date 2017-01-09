package fi.riista.feature.account.todo;

import fi.riista.feature.gamediary.GameDiaryFeature;
import fi.riista.feature.harvestpermit.HarvestPermitCrudFeature;
import fi.riista.feature.huntingclub.members.invitation.HuntingClubMemberInvitationFeature;
import fi.riista.feature.gamediary.srva.SrvaCrudFeature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class AccountTodoFeature {

    @Resource
    private GameDiaryFeature diaryFeature;

    @Resource
    private HarvestPermitCrudFeature harvestPermitCrudFeature;

    @Resource
    private HuntingClubMemberInvitationFeature huntingClubInvitationFeature;

    @Resource
    private SrvaCrudFeature srvaCrudFeature;

    @Transactional(readOnly = true)
    public AccountTodoCountDTO todoCount() {
        long harvestsRequiringAction = diaryFeature.countAllHarvestsRequiringAction();
        long permitsRequiringAction = harvestPermitCrudFeature.countAllPermitsRequiringAction();
        long invitations = huntingClubInvitationFeature.countInvitations();
        return new AccountTodoCountDTO(harvestsRequiringAction, permitsRequiringAction, invitations);
    }

    @Transactional(readOnly = true)
    public AccountSrvaTodoCountDTO srvaTodoCount(final long rhyId) {
        long unfinishedSrvaEvents = srvaCrudFeature.countUnfinishedSrvaEvents(rhyId);
        return new AccountSrvaTodoCountDTO(unfinishedSrvaEvents);
    }
}
