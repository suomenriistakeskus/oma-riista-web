package fi.riista.feature.huntingclub.members;

import java.util.List;

public class PermitHuntingLeaderContactInfoDTO {

    private List<HuntingClubContactDetailDTO> partnerLeaders;
    private List<HuntingClubContactDetailDTO> otherLeaders;

    public PermitHuntingLeaderContactInfoDTO(List<HuntingClubContactDetailDTO> partnerLeaders, List<HuntingClubContactDetailDTO> otherLeaders) {
        this.partnerLeaders = partnerLeaders;
        this.otherLeaders = otherLeaders;
    }

    public List<HuntingClubContactDetailDTO> getPartnerLeaders() {
        return partnerLeaders;
    }

    public List<HuntingClubContactDetailDTO> getOtherLeaders() {
        return otherLeaders;
    }
}
