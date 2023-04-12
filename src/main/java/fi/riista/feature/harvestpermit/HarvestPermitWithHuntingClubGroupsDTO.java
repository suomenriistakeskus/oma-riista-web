package fi.riista.feature.harvestpermit;

import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubDTO;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupDTO;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.util.F;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class HarvestPermitWithHuntingClubGroupsDTO {

    @Nonnull
    public static HarvestPermitWithHuntingClubGroupsDTO create(final @Nonnull HarvestPermit harvestPermit,
                                                               final @Nonnull List<HuntingClubGroup> groups,
                                                               final @Nonnull List<Organisation> clubs,
                                                               final @Nonnull Occupation occupation) {
        requireNonNull(harvestPermit);
        requireNonNull(groups);
        requireNonNull(clubs);
        requireNonNull(occupation);

        final List<HuntingClubDTO> huntingClubDTOs = F.mapNonNullsToList(clubs, HuntingClubDTO::create);
        final OccupationDTO groupOccupationDTO = OccupationDTO.create(occupation, false, true);

        ArrayList<HuntingClubGroupDTO> huntingClubGroupDTOS = F.mapNonNullsToList(groups, g -> HuntingClubGroupDTO.create(g, null, null));
        return new HarvestPermitWithHuntingClubGroupsDTO(harvestPermit.getId(), harvestPermit.getPermitNumber(),
                harvestPermit.getPermitType(), harvestPermit.getPermitTypeCode(), F.getId(harvestPermit.getOriginalPermit()),
                harvestPermit.getHarvestReportState(), huntingClubDTOs, huntingClubGroupDTOS, groupOccupationDTO);
    }

    private HarvestPermitWithHuntingClubGroupsDTO(final @Nonnull Long permitId,
                                                  final @Nonnull String permitNumber,
                                                  final @Nonnull String permitType,
                                                  final @Nonnull String permitTypeCode,
                                                  final Long originalPermitId,
                                                  final HarvestReportState harvestReportState,
                                                  final List<HuntingClubDTO> huntingClubDTOS,
                                                  final List<HuntingClubGroupDTO> groupDTOS,
                                                  final OccupationDTO groupOccupation) {
        this.id = requireNonNull(permitId);
        this.permitNumber = requireNonNull(permitNumber);
        this.permitType = requireNonNull(permitType);
        this.permitTypeCode = requireNonNull(permitTypeCode);
        this.originalPermitId = originalPermitId;
        this.harvestReportState = harvestReportState;
        this.huntingClubs = huntingClubDTOS;
        this.huntingClubGroups = groupDTOS;
        this.groupOccupation = groupOccupation;
    }

    private final Long id;
    private final String permitNumber;
    private final String permitType;
    private final String permitTypeCode;
    private final Long originalPermitId;
    private final HarvestReportState harvestReportState;
    private final List<HuntingClubGroupDTO> huntingClubGroups;
    private final List<HuntingClubDTO> huntingClubs;
    private final OccupationDTO groupOccupation;

    public Long getId() {
        return id;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public String getPermitType() {
        return permitType;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public Long getOriginalPermitId() {
        return originalPermitId;
    }

    public HarvestReportState getHarvestReportState() {
        return harvestReportState;
    }

    public List<HuntingClubGroupDTO> getHuntingClubGroups() {
        return huntingClubGroups;
    }

    public void addHuntingClubGroup(final HuntingClubGroupDTO huntingClubGroupDTO) {
        huntingClubGroups.add(huntingClubGroupDTO);
    }

    public List<HuntingClubDTO> getHuntingClubs() {
        return huntingClubs;
    }

    public OccupationDTO getGroupOccupation() {
        return groupOccupation;
    }
}
