package fi.riista.feature.huntingclub.members.invitation;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.huntingclub.HuntingClub;

import java.util.List;

public interface HuntingClubMemberInvitationRepository extends BaseRepository<HuntingClubMemberInvitation, Long> {

    List<HuntingClubMemberInvitation> getByHuntingClub(HuntingClub club);
}
