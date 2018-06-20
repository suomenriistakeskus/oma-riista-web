package fi.riista.feature.organization.rhy.annualstats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.DtoUtil;
import fi.riista.validation.DoNotValidate;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static java.util.Objects.requireNonNull;

public class RhyAnnualStatisticsDTO extends BaseEntityDTO<Long> {

    public static RhyAnnualStatisticsDTO create(@Nonnull final RhyAnnualStatistics entity) {
        requireNonNull(entity);

        final int calendarYear = entity.getYear();

        final RhyAnnualStatisticsDTO dto = new RhyAnnualStatisticsDTO(entity.getRhy().getId(), calendarYear);
        DtoUtil.copyBaseFields(entity, dto);

        dto.setState(entity.getState());

        final StateAidTrainingStatistics stateAidTraining = entity.getOrCreateStateAidTraining();

        dto.setBasicInfo(RhyBasicInfoDTO.create(entity.getOrCreateBasicInfo()));
        dto.setHunterExams(HunterExamStatisticsDTO.create(entity.getOrCreateHunterExams()));
        dto.setShootingTests(AnnualShootingTestStatisticsDTO.create(entity.getOrCreateShootingTests()));
        dto.setGameDamage(entity.getOrCreateGameDamage());
        dto.setHuntingControl(entity.getOrCreateHuntingControl());
        dto.setOtherPublicAdmin(entity.getOrCreateOtherPublicAdmin());
        dto.setSrva(entity.getOrCreateSrva());
        dto.setHunterExamTraining(HunterExamTrainingStatisticsDTO.create(entity.getOrCreateHunterExamTraining()));
        dto.setJhtTraining(entity.getOrCreateJhtTraining());
        dto.setStateAidTraining(stateAidTraining);
        dto.setOtherHunterTraining(entity.getOrCreateOtherHunterTraining());
        dto.setOtherTraining(entity.getOrCreateOtherTraining());
        dto.setOtherHuntingRelated(entity.getOrCreateOtherHuntingRelated());
        dto.setCommunication(entity.getOrCreateCommunication());
        dto.setShootingRanges(entity.getOrCreateShootingRanges());
        dto.setLuke(entity.getOrCreateLuke());
        dto.setMetsahallitus(entity.getOrCreateMetsahallitus());

        dto.setAllTrainingEvents(entity.countAllTrainingEvents());
        dto.setAllTrainingParticipants(entity.countAllTrainingParticipants());
        dto.setStateAidHunterTrainingEvents(stateAidTraining.countStateAidHunterTrainingEvents());
        dto.setSchoolAndCollegeTrainingEvents(stateAidTraining.countSchoolAndCollegeTrainingEvents());

        dto.setStateAidAffectingQuantitiesLastModified(entity.getTimeOfLastManualModificationToStateAidAffectingQuantities());
        dto.setPublicAdministrationTasksLastModified(entity.getTimeOfLastManualModificationToPublicAdministrationTasks());

        dto.setReadyForInspection(entity.isReadyForInspection());
        dto.setCompleteForApproval(entity.isCompleteForApproval());

        return dto;
    }

    private Long id;
    private Integer rev;

    private long rhyId;

    @Min(2017)
    private int year;

    private RhyAnnualStatisticsState state;

    @Valid
    @NotNull
    private RhyBasicInfoDTO basicInfo;

    @Valid
    @NotNull
    private HunterExamStatisticsDTO hunterExams;

    @Valid
    @NotNull
    private AnnualShootingTestStatisticsDTO shootingTests;

    @Valid
    @NotNull
    private HuntingControlStatistics huntingControl;

    @Valid
    @NotNull
    private GameDamageStatistics gameDamage;

    @Valid
    @NotNull
    private OtherPublicAdminStatistics otherPublicAdmin;

    @DoNotValidate
    private SrvaEventStatistics srva;

    @Valid
    @NotNull
    private HunterExamTrainingStatisticsDTO hunterExamTraining;

    @Valid
    @NotNull
    private JHTTrainingStatistics jhtTraining;

    @Valid
    @NotNull
    private StateAidTrainingStatistics stateAidTraining;

    @Valid
    @NotNull
    private OtherHunterTrainingStatistics otherHunterTraining;

    @Valid
    @NotNull
    private OtherTrainingStatistics otherTraining;

    @Valid
    @NotNull
    private OtherHuntingRelatedStatistics otherHuntingRelated;

    @Valid
    @NotNull
    private CommunicationStatistics communication;

    @Valid
    @NotNull
    private ShootingRangeStatistics shootingRanges;

    @Valid
    @NotNull
    private LukeStatistics luke;

    @Valid
    @NotNull
    private MetsahallitusStatistics metsahallitus;

    @JsonIgnore
    private Integer allTrainingEvents;

    @JsonIgnore
    private Integer allTrainingParticipants;

    @JsonIgnore
    private Integer stateAidHunterTrainingEvents;

    @JsonIgnore
    private Integer schoolAndCollegeTrainingEvents;

    @JsonIgnore
    private DateTime stateAidAffectingQuantitiesLastModified;

    @JsonIgnore
    private DateTime publicAdministrationTasksLastModified;

    @JsonIgnore
    private Boolean readyForInspection;

    @JsonIgnore
    private Boolean completeForApproval;

    public RhyAnnualStatisticsDTO() {
    }

    public RhyAnnualStatisticsDTO(final long rhyId, final int year) {
        this.rhyId = rhyId;
        this.year = year;
    }

    @AssertTrue
    public boolean isYearLessThanOrEqualToCurrentYear() {
        return DateUtil.today().getYear() >= year;
    }

    // Accessors -->

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public long getRhyId() {
        return rhyId;
    }

    public void setRhyId(final long rhyId) {
        this.rhyId = rhyId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public RhyAnnualStatisticsState getState() {
        return state;
    }

    public void setState(final RhyAnnualStatisticsState state) {
        this.state = state;
    }

    public RhyBasicInfoDTO getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(final RhyBasicInfoDTO basicInfo) {
        this.basicInfo = basicInfo;
    }

    public HunterExamStatisticsDTO getHunterExams() {
        return hunterExams;
    }

    public void setHunterExams(final HunterExamStatisticsDTO hunterExams) {
        this.hunterExams = hunterExams;
    }

    public AnnualShootingTestStatisticsDTO getShootingTests() {
        return shootingTests;
    }

    public void setShootingTests(final AnnualShootingTestStatisticsDTO shootingTests) {
        this.shootingTests = shootingTests;
    }

    public HuntingControlStatistics getHuntingControl() {
        return huntingControl;
    }

    public void setHuntingControl(final HuntingControlStatistics huntingControl) {
        this.huntingControl = huntingControl;
    }

    public GameDamageStatistics getGameDamage() {
        return gameDamage;
    }

    public void setGameDamage(final GameDamageStatistics gameDamage) {
        this.gameDamage = gameDamage;
    }

    public OtherPublicAdminStatistics getOtherPublicAdmin() {
        return otherPublicAdmin;
    }

    public void setOtherPublicAdmin(final OtherPublicAdminStatistics otherPublicAdmin) {
        this.otherPublicAdmin = otherPublicAdmin;
    }

    public SrvaEventStatistics getSrva() {
        return srva;
    }

    public void setSrva(final SrvaEventStatistics srva) {
        this.srva = srva;
    }

    public HunterExamTrainingStatisticsDTO getHunterExamTraining() {
        return hunterExamTraining;
    }

    public void setHunterExamTraining(final HunterExamTrainingStatisticsDTO hunterExamTraining) {
        this.hunterExamTraining = hunterExamTraining;
    }

    public JHTTrainingStatistics getJhtTraining() {
        return jhtTraining;
    }

    public void setJhtTraining(final JHTTrainingStatistics jhtTraining) {
        this.jhtTraining = jhtTraining;
    }

    public StateAidTrainingStatistics getStateAidTraining() {
        return stateAidTraining;
    }

    public void setStateAidTraining(final StateAidTrainingStatistics stateAidTraining) {
        this.stateAidTraining = stateAidTraining;
    }

    public OtherHunterTrainingStatistics getOtherHunterTraining() {
        return otherHunterTraining;
    }

    public void setOtherHunterTraining(final OtherHunterTrainingStatistics otherHunterTraining) {
        this.otherHunterTraining = otherHunterTraining;
    }

    public OtherTrainingStatistics getOtherTraining() {
        return otherTraining;
    }

    public void setOtherTraining(final OtherTrainingStatistics otherTraining) {
        this.otherTraining = otherTraining;
    }

    public OtherHuntingRelatedStatistics getOtherHuntingRelated() {
        return otherHuntingRelated;
    }

    public void setOtherHuntingRelated(final OtherHuntingRelatedStatistics otherHuntingRelated) {
        this.otherHuntingRelated = otherHuntingRelated;
    }

    public CommunicationStatistics getCommunication() {
        return communication;
    }

    public void setCommunication(final CommunicationStatistics communication) {
        this.communication = communication;
    }

    public ShootingRangeStatistics getShootingRanges() {
        return shootingRanges;
    }

    public void setShootingRanges(final ShootingRangeStatistics shootingRanges) {
        this.shootingRanges = shootingRanges;
    }

    public LukeStatistics getLuke() {
        return luke;
    }

    public void setLuke(final LukeStatistics luke) {
        this.luke = luke;
    }

    public MetsahallitusStatistics getMetsahallitus() {
        return metsahallitus;
    }

    public void setMetsahallitus(final MetsahallitusStatistics metsahallitus) {
        this.metsahallitus = metsahallitus;
    }

    @JsonProperty
    public Integer getAllTrainingEvents() {
        return allTrainingEvents;
    }

    @JsonIgnore
    public void setAllTrainingEvents(final Integer allTrainingEvents) {
        this.allTrainingEvents = allTrainingEvents;
    }

    @JsonProperty
    public Integer getAllTrainingParticipants() {
        return allTrainingParticipants;
    }

    @JsonIgnore
    public void setAllTrainingParticipants(final Integer allTrainingParticipants) {
        this.allTrainingParticipants = allTrainingParticipants;
    }

    @JsonProperty
    public Integer getStateAidHunterTrainingEvents() {
        return stateAidHunterTrainingEvents;
    }

    @JsonIgnore
    public void setStateAidHunterTrainingEvents(final Integer stateAidHunterTrainingEvents) {
        this.stateAidHunterTrainingEvents = stateAidHunterTrainingEvents;
    }

    @JsonProperty
    public Integer getSchoolAndCollegeTrainingEvents() {
        return schoolAndCollegeTrainingEvents;
    }

    @JsonIgnore
    public void setSchoolAndCollegeTrainingEvents(final Integer schoolAndCollegeTrainingEvents) {
        this.schoolAndCollegeTrainingEvents = schoolAndCollegeTrainingEvents;
    }

    @JsonProperty
    public DateTime getStateAidAffectingQuantitiesLastModified() {
        return stateAidAffectingQuantitiesLastModified;
    }

    @JsonIgnore
    public void setStateAidAffectingQuantitiesLastModified(final DateTime stateAidAffectingQuantitiesLastModified) {
        this.stateAidAffectingQuantitiesLastModified = stateAidAffectingQuantitiesLastModified;
    }

    @JsonProperty
    public DateTime getPublicAdministrationTasksLastModified() {
        return publicAdministrationTasksLastModified;
    }

    @JsonIgnore
    public void setPublicAdministrationTasksLastModified(final DateTime publicAdministrationTasksLastModified) {
        this.publicAdministrationTasksLastModified = publicAdministrationTasksLastModified;
    }

    @JsonProperty
    public Boolean getReadyForInspection() {
        return readyForInspection;
    }

    @JsonIgnore
    public void setReadyForInspection(final Boolean readyForInspection) {
        this.readyForInspection = readyForInspection;
    }

    @JsonProperty
    public Boolean getCompleteForApproval() {
        return completeForApproval;
    }

    @JsonIgnore
    public void setCompleteForApproval(final Boolean completeForApproval) {
        this.completeForApproval = completeForApproval;
    }
}
