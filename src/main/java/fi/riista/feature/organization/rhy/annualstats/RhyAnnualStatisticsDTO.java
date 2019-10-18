package fi.riista.feature.organization.rhy.annualstats;

import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.common.dto.BaseEntityEventDTO;
import org.joda.time.DateTime;

public class RhyAnnualStatisticsDTO extends BaseEntityDTO<Long> {

    private Long id;
    private Integer rev;

    private long rhyId;
    private int year;

    private RhyAnnualStatisticsState state;

    private RhyBasicInfoDTO basicInfo;

    private HunterExamStatisticsDTO hunterExams;

    private AnnualShootingTestStatisticsDTO shootingTests;

    private HuntingControlStatistics huntingControl;

    private GameDamageStatistics gameDamage;

    private OtherPublicAdminStatistics otherPublicAdmin;

    private SrvaEventStatistics srva;

    private HunterExamTrainingStatisticsDTO hunterExamTraining;

    private JHTTrainingStatistics jhtTraining;

    private HunterTrainingStatistics hunterTraining;

    private YouthTrainingStatistics youthTraining;

    private OtherHunterTrainingStatistics otherHunterTraining;

    private PublicEventStatistics publicEvents;

    private OtherHuntingRelatedStatistics otherHuntingRelated;

    private CommunicationStatistics communication;

    private ShootingRangeStatistics shootingRanges;

    private LukeStatistics luke;

    private MetsahallitusStatistics metsahallitus;

    private Integer subsidizableOtherTrainingEvents;
    private Integer subsidizableStudentAndYouthTrainingEvents;

    private DateTime quantitiesContributingToSubsidyLastModified;
    private DateTime jhtQuantitiesLastModified;

    private Integer allTrainingEvents;
    private Integer allTrainingParticipants;

    private Boolean readyForInspection;
    private Boolean completeForApproval;

    private BaseEntityEventDTO submitEvent;

    public RhyAnnualStatisticsDTO() {
    }

    public RhyAnnualStatisticsDTO(final long rhyId, final int year) {
        this.rhyId = rhyId;
        this.year = year;
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

    public HunterTrainingStatistics getHunterTraining() {
        return hunterTraining;
    }

    public void setHunterTraining(final HunterTrainingStatistics hunterTraining) {
        this.hunterTraining = hunterTraining;
    }

    public YouthTrainingStatistics getYouthTraining() {
        return youthTraining;
    }

    public void setYouthTraining(final YouthTrainingStatistics youthTraining) {
        this.youthTraining = youthTraining;
    }

    public OtherHunterTrainingStatistics getOtherHunterTraining() {
        return otherHunterTraining;
    }

    public void setOtherHunterTraining(final OtherHunterTrainingStatistics otherHunterTraining) {
        this.otherHunterTraining = otherHunterTraining;
    }

    public PublicEventStatistics getPublicEvents() {
        return publicEvents;
    }

    public void setPublicEvents(final PublicEventStatistics publicEvents) {
        this.publicEvents = publicEvents;
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

    public Integer getSubsidizableOtherTrainingEvents() {
        return subsidizableOtherTrainingEvents;
    }

    public void setSubsidizableOtherTrainingEvents(final Integer subsidizableOtherTrainingEvents) {
        this.subsidizableOtherTrainingEvents = subsidizableOtherTrainingEvents;
    }

    public Integer getSubsidizableStudentAndYouthTrainingEvents() {
        return subsidizableStudentAndYouthTrainingEvents;
    }

    public void setSubsidizableStudentAndYouthTrainingEvents(final Integer subsidizableStudentAndYouthTrainingEvents) {
        this.subsidizableStudentAndYouthTrainingEvents = subsidizableStudentAndYouthTrainingEvents;
    }

    public DateTime getQuantitiesContributingToSubsidyLastModified() {
        return quantitiesContributingToSubsidyLastModified;
    }

    public void setQuantitiesContributingToSubsidyLastModified(final DateTime quantitiesContributingToSubsidyLastModified) {
        this.quantitiesContributingToSubsidyLastModified = quantitiesContributingToSubsidyLastModified;
    }

    public DateTime getJhtQuantitiesLastModified() {
        return jhtQuantitiesLastModified;
    }

    public void setJhtQuantitiesLastModified(final DateTime jhtQuantitiesLastModified) {
        this.jhtQuantitiesLastModified = jhtQuantitiesLastModified;
    }

    public Integer getAllTrainingEvents() {
        return allTrainingEvents;
    }

    public void setAllTrainingEvents(final Integer allTrainingEvents) {
        this.allTrainingEvents = allTrainingEvents;
    }

    public Integer getAllTrainingParticipants() {
        return allTrainingParticipants;
    }

    public void setAllTrainingParticipants(final Integer allTrainingParticipants) {
        this.allTrainingParticipants = allTrainingParticipants;
    }

    public Boolean getReadyForInspection() {
        return readyForInspection;
    }

    public void setReadyForInspection(final Boolean readyForInspection) {
        this.readyForInspection = readyForInspection;
    }

    public Boolean getCompleteForApproval() {
        return completeForApproval;
    }

    public void setCompleteForApproval(final Boolean completeForApproval) {
        this.completeForApproval = completeForApproval;
    }

    public BaseEntityEventDTO getSubmitEvent() {
        return submitEvent;
    }

    public void setSubmitEvent(final BaseEntityEventDTO submitEvent) {
        this.submitEvent = submitEvent;
    }

}
