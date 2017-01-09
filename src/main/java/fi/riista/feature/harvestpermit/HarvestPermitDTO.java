package fi.riista.feature.harvestpermit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.DoNotValidate;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.entity.HasID;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.report.HarvestReport;
import fi.riista.feature.harvestpermit.report.state.HarvestReportStateTransitions;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;

public class HarvestPermitDTO extends BaseEntityDTO<Long> {

    public enum Inclusion {
        HARVEST_LIST,
        REPORT_LIST,
        END_OF_HUNTING_REPORT_REQUIRED
    }

    @Nonnull
    public static List<HarvestPermitDTO> create(@Nonnull final List<HarvestPermit> permits,
                                                @Nullable final SystemUser currentUser,
                                                @Nonnull final EnumSet<Inclusion> inclusions) {
        return F.mapNonNullsToList(permits, permit -> HarvestPermitDTO.create(permit, currentUser, inclusions));
    }

    @Nonnull
    public static HarvestPermitDTO create(@Nonnull final HarvestPermit permit,
                                          @Nullable final SystemUser currentUser,
                                          @Nonnull final EnumSet<Inclusion> inclusions) {
        Objects.requireNonNull(permit, "permit must not be null");

        final boolean isModeratorOrAdmin = currentUser != null && currentUser.isModeratorOrAdmin();

        final HarvestPermitDTO dto = new HarvestPermitDTO();
        DtoUtil.copyBaseFields(permit, dto);

        dto.setPermitNumber(permit.getPermitNumber());
        dto.setPermitType(permit.getPermitType());
        dto.setSpeciesAmounts(F.mapNonNullsToList(permit.getSpeciesAmounts(), HarvestPermitSpeciesAmountDTO::create));
        dto.setContactPersons(createContactPersonDTOs(permit, currentUser != null ? currentUser.getPerson() : null));
        dto.setHarvestsAsList(permit.isHarvestsAsList());

        dto.setEndOfHuntingReport(HarvestReportStub.create(permit.getEndOfHuntingReport(),
                isModeratorOrAdmin, !permit.isHarvestsAsList()));

        if (inclusions.contains(Inclusion.END_OF_HUNTING_REPORT_REQUIRED)) {
            dto.setEndOfHuntingReportRequired(permit.isEndOfHuntingReportRequired());
        }

        if (inclusions.contains(Inclusion.REPORT_LIST)) {
            final Long endOfHuntingReportId = F.getId(permit.getEndOfHuntingReport());

            dto.setHarvestReports(permit.getUndeletedHarvestReports().stream()
                    // remove end-of-hunting report from reportStubs
                    .filter(report -> !Objects.equals(endOfHuntingReportId, report.getId()))
                    .map(report -> HarvestReportStub.create(report, isModeratorOrAdmin, !permit.isHarvestsAsList()))
                    .collect(toList()));
        }

        if (inclusions.contains(Inclusion.HARVEST_LIST) && permit.isHarvestsAsList()) {
            dto.setHarvests(permit.getHarvests().stream()
                    .map(harvest -> HarvestStub.create(harvest, isModeratorOrAdmin))
                    .sorted(comparing(HarvestStub::getStateAcceptedToHarvestPermit, nullsLast(naturalOrder()))
                            .thenComparing(HarvestStub::getPointOfTime, reverseOrder()))
                    .collect(toList()));
        }

        return dto;
    }

    private static List<HarvestPermitContactPersonDTO> createContactPersonDTOs(
            @Nonnull final HarvestPermit permit,
            @Nullable final Person currentPerson) {
        Objects.requireNonNull(permit, "permit must not be null");

        final List<HarvestPermitContactPersonDTO> dtos = permit.getContactPersons().stream()
            .map(HarvestPermitContactPerson::getContactPerson)
            .map(HarvestPermitContactPersonDTO::createCanBeDeleted)
            .collect(toList());

        if (currentPerson != null) {
            for (HarvestPermitContactPersonDTO dto : dtos) {
                // contact person can't delete himself
                dto.setCanBeDeleted(!Objects.equals(dto.getId(), currentPerson.getId()));
            }
        }

        if (permit.getOriginalContactPerson() != null) {
            dtos.add(HarvestPermitContactPersonDTO.create(permit.getOriginalContactPerson(), false));
        }

        return dtos;
    }

    private Long id;
    private Integer rev;

    @FinnishHuntingPermitNumber
    private String permitNumber;
    private List<HarvestPermitSpeciesAmountDTO> speciesAmounts;
    private List<HarvestPermitContactPersonDTO> contactPersons;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitType;
    @DoNotValidate
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<HarvestReportStub> harvestReports;
    @DoNotValidate
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<HarvestStub> harvests;
    private boolean harvestsAsList;
    private HarvestReportStub endOfHuntingReport;
    private boolean endOfHuntingReportRequired;

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

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public void setSpeciesAmounts(List<HarvestPermitSpeciesAmountDTO> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public List<HarvestPermitSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<HarvestPermitContactPersonDTO> getContactPersons() {
        return contactPersons;
    }

    public void setContactPersons(List<HarvestPermitContactPersonDTO> contactPersons) {
        this.contactPersons = contactPersons;
    }

    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setHarvestReports(List<HarvestReportStub> harvestReports) {
        this.harvestReports = harvestReports;
    }

    public List<HarvestReportStub> getHarvestReports() {
        return harvestReports;
    }

    public List<HarvestStub> getHarvests() {
        return harvests;
    }

    public void setHarvests(List<HarvestStub> harvests) {
        this.harvests = harvests;
    }

    public boolean isHarvestsAsList() {
        return harvestsAsList;
    }

    public void setHarvestsAsList(boolean harvestsAsList) {
        this.harvestsAsList = harvestsAsList;
    }

    public void setEndOfHuntingReport(HarvestReportStub endOfHuntingReport) {
        this.endOfHuntingReport = endOfHuntingReport;
    }

    public HarvestReportStub getEndOfHuntingReport() {
        return endOfHuntingReport;
    }

    public boolean isEndOfHuntingReportRequired() {
        return endOfHuntingReportRequired;
    }

    public void setEndOfHuntingReportRequired(boolean endOfHuntingReportRequired) {
        this.endOfHuntingReportRequired = endOfHuntingReportRequired;
    }

    public static class HarvestReportStub implements HasID<Long> {
        public static HarvestReportStub create(HarvestReport report, boolean moderator, boolean singleHarvestPermit) {
            if (report == null) {
                return null;
            }

            final HarvestReportStub dto = new HarvestReportStub();
            dto.setId(report.getId());
            dto.setRev(report.getConsistencyVersion());
            dto.setState(report.getState());

            final HarvestReportStateTransitions.ReportRole role = moderator
                    ? HarvestReportStateTransitions.ReportRole.MODERATOR
                    : HarvestReportStateTransitions.ReportRole.AUTHOR_CONTACT_FOR_PERMIT;
            dto.setTransitions(HarvestReportStateTransitions.getTransitions(role, report.getState()));
            dto.setCanEdit(HarvestReportStateTransitions.canEdit(role, report.getState()));
            dto.setCanDelete(HarvestReportStateTransitions.canEdit(role, report.getState()));
            dto.setCanModeratorEdit(report.canModeratorEdit());
            dto.setCanModeratorDelete(report.canModeratorDelete());

            if (report.getHarvestPermit() != null && report.getHarvestPermit().getSpeciesAmounts() != null) {
                dto.setPermittedSpecies(report.getHarvestPermit().getSpeciesAmounts().stream()
                        .map(HarvestPermitSpeciesAmount::getGameSpecies)
                        .map(GameSpeciesDTO::create)
                        .collect(toList()));
            }
            dto.setAuthor(PersonWithHunterNumberDTO.create(report.getAuthor()));

            if (singleHarvestPermit && !report.getHarvests().isEmpty()) {
                Harvest harvest = report.getHarvests().iterator().next();
                dto.setPointOfTime(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()));
                dto.setAuthor(PersonWithHunterNumberDTO.create(harvest.getAuthor()));
                dto.setHunter(PersonWithHunterNumberDTO.create(harvest.getActualShooter()));
                HarvestSpecimen specimen = harvest.getSortedSpecimens().iterator().next();
                dto.setGender(specimen.getGender());
                dto.setAge(specimen.getAge());
                dto.setGameSpecies(GameSpeciesDTO.create(harvest.getSpecies()));
            }
            dto.setEndOfHuntingReport(report.isEndOfHuntingReport());
            return dto;
        }

        private Long id;
        private Integer rev;
        private HarvestReport.State state;

        private LocalDateTime pointOfTime;
        private PersonWithHunterNumberDTO author;
        private PersonWithHunterNumberDTO hunter;
        private GameGender gender;
        private GameAge age;
        private GameSpeciesDTO gameSpecies;
        private List<GameSpeciesDTO> permittedSpecies;
        private List<HarvestReport.State> transitions;
        private boolean canEdit;
        private boolean canDelete;
        private boolean canModeratorEdit;
        private boolean canModeratorDelete;
        private boolean endOfHuntingReport;

        public HarvestReportStub() {
        }

        @Override
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getRev() {
            return rev;
        }

        public void setRev(Integer rev) {
            this.rev = rev;
        }

        public HarvestReport.State getState() {
            return state;
        }

        public void setState(HarvestReport.State state) {
            this.state = state;
        }

        public LocalDateTime getPointOfTime() {
            return pointOfTime;
        }

        public void setPointOfTime(LocalDateTime pointOfTime) {
            this.pointOfTime = pointOfTime;
        }

        public PersonWithHunterNumberDTO getAuthor() {
            return author;
        }

        public void setAuthor(PersonWithHunterNumberDTO author) {
            this.author = author;
        }

        public PersonWithHunterNumberDTO getHunter() {
            return hunter;
        }

        public void setHunter(PersonWithHunterNumberDTO hunter) {
            this.hunter = hunter;
        }

        public GameGender getGender() {
            return gender;
        }

        public void setGender(GameGender gender) {
            this.gender = gender;
        }

        public GameAge getAge() {
            return age;
        }

        public void setAge(GameAge age) {
            this.age = age;
        }

        public GameSpeciesDTO getGameSpecies() {
            return gameSpecies;
        }

        public void setGameSpecies(GameSpeciesDTO gameSpecies) {
            this.gameSpecies = gameSpecies;
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

        public void setPermittedSpecies(List<GameSpeciesDTO> permittedSpecies) {
            this.permittedSpecies = permittedSpecies;
        }

        public List<GameSpeciesDTO> getPermittedSpecies() {
            return permittedSpecies;
        }

        public boolean isEndOfHuntingReport() {
            return endOfHuntingReport;
        }

        public void setEndOfHuntingReport(boolean endOfHuntingReport) {
            this.endOfHuntingReport = endOfHuntingReport;
        }
    }

    public static class HarvestStub {

        @Nonnull
        public static HarvestStub create(@Nonnull final Harvest harvest, final boolean moderatorOrAdmin) {
            final HarvestStub dto = new HarvestStub();
            dto.setId(harvest.getId());
            dto.setRev(harvest.getConsistencyVersion());
            dto.setStateAcceptedToHarvestPermit(harvest.getStateAcceptedToHarvestPermit());
            dto.setRhy(OrganisationNameDTO.create(harvest.getRhy()));
            dto.setPointOfTime(DateUtil.toLocalDateTimeNullSafe(harvest.getPointOfTime()));
            dto.setAuthor(PersonWithHunterNumberDTO.create(harvest.getAuthor()));
            dto.setHunter(PersonWithHunterNumberDTO.create(harvest.getActualShooter()));
            dto.setAmount(harvest.getAmount());
            dto.setGameSpecies(GameSpeciesDTO.create(harvest.getSpecies()));
            if (moderatorOrAdmin) {
                dto.setCanModeratorDelete(harvest.canModeratorDelete());
            }
            return dto;
        }

        private GameDiaryEntryType type = GameDiaryEntryType.HARVEST;
        private Long id;
        private Integer rev;
        private Harvest.StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit;
        private OrganisationNameDTO rhy;
        private LocalDateTime pointOfTime;
        private PersonWithHunterNumberDTO author;
        private PersonWithHunterNumberDTO hunter;
        private boolean canModeratorDelete;
        private String creator;

        @JsonProperty(value = "totalSpecimenAmount")
        private int amount;

        private GameSpeciesDTO gameSpecies;

        public GameDiaryEntryType getType() {
            return type;
        }

        public void setType(GameDiaryEntryType type) {
            this.type = type;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getRev() {
            return rev;
        }

        public void setRev(Integer rev) {
            this.rev = rev;
        }

        public Harvest.StateAcceptedToHarvestPermit getStateAcceptedToHarvestPermit() {
            return stateAcceptedToHarvestPermit;
        }

        public void setStateAcceptedToHarvestPermit(Harvest.StateAcceptedToHarvestPermit stateAcceptedToHarvestPermit) {
            this.stateAcceptedToHarvestPermit = stateAcceptedToHarvestPermit;
        }

        public OrganisationNameDTO getRhy() {
            return rhy;
        }

        public void setRhy(OrganisationNameDTO rhy) {
            this.rhy = rhy;
        }

        public LocalDateTime getPointOfTime() {
            return pointOfTime;
        }

        public void setPointOfTime(LocalDateTime pointOfTime) {
            this.pointOfTime = pointOfTime;
        }

        public PersonWithHunterNumberDTO getAuthor() {
            return author;
        }

        public void setAuthor(PersonWithHunterNumberDTO author) {
            this.author = author;
        }

        public PersonWithHunterNumberDTO getHunter() {
            return hunter;
        }

        public void setHunter(PersonWithHunterNumberDTO hunter) {
            this.hunter = hunter;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public GameSpeciesDTO getGameSpecies() {
            return gameSpecies;
        }

        public void setGameSpecies(GameSpeciesDTO gameSpecies) {
            this.gameSpecies = gameSpecies;
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
    }
}
