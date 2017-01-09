package fi.riista.feature.harvestpermit.report;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class HarvestReportDTOBase extends BaseEntityDTO<Long> {

    public static HarvestReportDTOBase copyBaseFields(final HarvestReport report,
                                                      final HarvestReportDTOBase dto,
                                                      final SystemUser user,
                                                      final Map<Long, SystemUser> moderatorCreators) {

        DtoUtil.copyBaseFields(report, dto);

        dto.setAuthorInfo(PersonWithHunterNumberDTO.create(report.getAuthor()));
        dto.setState(report.getState());

        if (user.isModeratorOrAdmin()) {
            dto.setDescription(report.getDescription());

            SystemUser creator = moderatorCreators.get(report.getCreatedByUserId());
            if (creator != null) {
                dto.setCreator(creator.getFullName());
            }
        } else {
            dto.setDescription(null);
        }

        dto.setTransitions(report.getTransitions(user));
        dto.setCanEdit(report.canEdit(user));
        dto.setCanDelete(report.canDelete(user));
        dto.setCanModeratorEdit(report.canModeratorEdit());
        dto.setCanModeratorDelete(report.canModeratorDelete());
        dto.setEndOfHuntingReport(report.isEndOfHuntingReport());

        if (report.getHarvestPermit() != null) {
            dto.setPermitId(report.getHarvestPermit().getId());

            // Collect unique GameSpecies
            final Set<GameSpecies> species = report.getHarvestPermit().getSpeciesAmounts().stream()
                    .map(HarvestPermitSpeciesAmount::getGameSpecies)
                    .collect(toSet());

            dto.setPermittedSpecies(F.mapNonNullsToList(species, GameSpeciesDTO::create));
        }

        return dto;
    }

    private Long id;
    private Integer rev;

    private HarvestReport.State state;

    @Valid
    private PersonWithHunterNumberDTO authorInfo;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String description;

    private List<HarvestReport.State> transitions;
    private boolean canEdit;
    private boolean canDelete;
    private boolean canModeratorEdit;
    private boolean canModeratorDelete;

    private Long permitId;
    private List<GameSpeciesDTO> permittedSpecies;
    private boolean endOfHuntingReport;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String creator;

    private Long rhyId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public HarvestReport.State getState() {
        return state;
    }

    public void setState(HarvestReport.State state) {
        this.state = state;
    }

    public PersonWithHunterNumberDTO getAuthorInfo() {
        return authorInfo;
    }

    public void setAuthorInfo(PersonWithHunterNumberDTO authorInfo) {
        this.authorInfo = authorInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<HarvestReport.State> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<HarvestReport.State> transitions) {
        this.transitions = transitions;
    }

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public boolean isCanModeratorEdit() {
        return canModeratorEdit;
    }

    public void setCanModeratorEdit(boolean canModeratorEdit) {
        this.canModeratorEdit = canModeratorEdit;
    }

    public boolean isCanModeratorDelete() {
        return canModeratorDelete;
    }

    public void setCanModeratorDelete(boolean canModeratorDelete) {
        this.canModeratorDelete = canModeratorDelete;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Long getRhyId() {
        return rhyId;
    }

    public void setRhyId(Long rhyId) {
        this.rhyId = rhyId;
    }

    public Long getPermitId() {
        return permitId;
    }

    public void setPermitId(Long permitId) {
        this.permitId = permitId;
    }

    public List<GameSpeciesDTO> getPermittedSpecies() {
        return permittedSpecies;
    }

    public void setPermittedSpecies(List<GameSpeciesDTO> permittedSpecies) {
        this.permittedSpecies = permittedSpecies;
    }

    public boolean isEndOfHuntingReport() {
        return endOfHuntingReport;
    }

    public void setEndOfHuntingReport(boolean endOfHuntingReport) {
        this.endOfHuntingReport = endOfHuntingReport;
    }
}
