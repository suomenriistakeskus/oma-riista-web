package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.annualstats.AnnualShootingTestStatistics;
import fi.riista.feature.organization.rhy.annualstats.CommunicationStatistics;
import fi.riista.feature.organization.rhy.annualstats.GameDamageStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.HuntingControlStatistics;
import fi.riista.feature.organization.rhy.annualstats.JHTTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.LukeStatistics;
import fi.riista.feature.organization.rhy.annualstats.MetsahallitusStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherHunterTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherHuntingRelatedStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherPublicAdminStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyBasicInfo;
import fi.riista.feature.organization.rhy.annualstats.ShootingRangeStatistics;
import fi.riista.feature.organization.rhy.annualstats.SrvaEventStatistics;
import fi.riista.feature.organization.rhy.annualstats.StateAidTrainingStatistics;
import fi.riista.util.LocalisedString;

import javax.annotation.Nonnull;

import static fi.riista.util.NumberUtils.sumNullsToZero;
import static java.util.Objects.requireNonNull;

public class AnnualStatisticsExportItemDTO {

    public static AnnualStatisticsExportItemDTO create(@Nonnull final Riistanhoitoyhdistys rhy,
                                                       @Nonnull final Organisation rka,
                                                       @Nonnull final RhyAnnualStatistics statistics) {

        requireNonNull(rhy, "rhy is null");
        requireNonNull(rka, "rka is null");
        requireNonNull(statistics, "statistics is null");

        final AnnualStatisticsExportItemDTO dto = new AnnualStatisticsExportItemDTO();

        dto.setOrganisationCode(rhy.getOfficialCode());
        dto.setOrganisationName(rhy.getNameLocalisation());
        dto.setParentOrganisationCode(rka.getOfficialCode());
        dto.setParentOrganisationName(rka.getNameLocalisation());

        dto.setBasicInfo(statistics.getOrCreateBasicInfo());

        dto.setHunterExams(statistics.getOrCreateHunterExams());
        dto.setShootingTests(statistics.getOrCreateShootingTests());
        dto.setGameDamage(statistics.getOrCreateGameDamage());
        dto.setHuntingControl(statistics.getOrCreateHuntingControl());
        dto.setOtherPublicAdmin(statistics.getOrCreateOtherPublicAdmin());

        dto.setSrva(statistics.getOrCreateSrva());

        dto.setHunterExamTraining(statistics.getOrCreateHunterExamTraining());
        dto.setJhtTraining(statistics.getOrCreateJhtTraining());
        dto.setStateAidTraining(statistics.getOrCreateStateAidTraining());
        dto.setOtherHunterTraining(statistics.getOrCreateOtherHunterTraining());
        dto.setOtherTraining(statistics.getOrCreateOtherTraining());

        dto.setOtherHuntingRelated(statistics.getOrCreateOtherHuntingRelated());
        dto.setCommunication(statistics.getOrCreateCommunication());
        dto.setShootingRanges(statistics.getOrCreateShootingRanges());
        dto.setLuke(statistics.getOrCreateLuke());
        dto.setMetsahallitus(statistics.getOrCreateMetsahallitus());

        dto.setAllTrainingEvents(statistics.countAllTrainingEvents());
        dto.setAllTrainingParticipants(statistics.countAllTrainingParticipants());

        return dto;
    }

    public static AnnualStatisticsExportItemDTO aggregate(@Nonnull final Iterable<AnnualStatisticsExportItemDTO> list) {
        requireNonNull(list);

        final AnnualStatisticsExportItemDTO result = new AnnualStatisticsExportItemDTO();

        result.setBasicInfo(RhyBasicInfo.reduce(list, dto -> dto.getBasicInfo()));

        result.setHunterExams(HunterExamStatistics.reduce(list, dto -> dto.getHunterExams()));
        result.setShootingTests(AnnualShootingTestStatistics.reduce(list, dto -> dto.getShootingTests()));
        result.setGameDamage(GameDamageStatistics.reduce(list, dto -> dto.getGameDamage()));
        result.setHuntingControl(HuntingControlStatistics.reduce(list, dto -> dto.getHuntingControl()));
        result.setOtherPublicAdmin(OtherPublicAdminStatistics.reduce(list, dto -> dto.getOtherPublicAdmin()));

        result.setSrva(SrvaEventStatistics.reduce(list, dto -> dto.getSrva()));

        result.setHunterExamTraining(HunterExamTrainingStatistics.reduce(list, dto -> dto.getHunterExamTraining()));
        result.setJhtTraining(JHTTrainingStatistics.reduce(list, dto -> dto.getJhtTraining()));
        result.setStateAidTraining(StateAidTrainingStatistics.reduce(list, dto -> dto.getStateAidTraining()));
        result.setOtherHunterTraining(OtherHunterTrainingStatistics.reduce(list, dto -> dto.getOtherHunterTraining()));
        result.setOtherTraining(OtherTrainingStatistics.reduce(list, dto -> dto.getOtherTraining()));

        result.setOtherHuntingRelated(OtherHuntingRelatedStatistics.reduce(list, dto -> dto.getOtherHuntingRelated()));
        result.setCommunication(CommunicationStatistics.reduce(list, dto -> dto.getCommunication()));
        result.setShootingRanges(ShootingRangeStatistics.reduce(list, dto -> dto.getShootingRanges()));
        result.setLuke(LukeStatistics.reduce(list, dto -> dto.getLuke()));
        result.setMetsahallitus(MetsahallitusStatistics.reduce(list, dto -> dto.getMetsahallitus()));

        result.setAllTrainingEvents(sumNullsToZero(list, dto -> dto.getAllTrainingEvents()));
        result.setAllTrainingParticipants(sumNullsToZero(list, dto -> dto.getAllTrainingParticipants()));

        return result;
    }

    private String organisationCode;
    private LocalisedString organisationName;
    private String parentOrganisationCode;
    private LocalisedString parentOrganisationName;

    private RhyBasicInfo basicInfo;
    private HunterExamStatistics hunterExams;
    private AnnualShootingTestStatistics shootingTests;
    private HuntingControlStatistics huntingControl;
    private GameDamageStatistics gameDamage;
    private OtherPublicAdminStatistics otherPublicAdmin;
    private SrvaEventStatistics srva;
    private HunterExamTrainingStatistics hunterExamTraining;
    private JHTTrainingStatistics jhtTraining;
    private StateAidTrainingStatistics stateAidTraining;
    private OtherHunterTrainingStatistics otherHunterTraining;
    private OtherTrainingStatistics otherTraining;
    private OtherHuntingRelatedStatistics otherHuntingRelated;
    private CommunicationStatistics communication;
    private ShootingRangeStatistics shootingRanges;
    private LukeStatistics luke;
    private MetsahallitusStatistics metsahallitus;

    private int allTrainingEvents;
    private int allTrainingParticipants;

    // Accessors -->

    public String getOrganisationCode() {
        return organisationCode;
    }

    public void setOrganisationCode(final String organisationCode) {
        this.organisationCode = organisationCode;
    }

    public LocalisedString getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(final LocalisedString organisationName) {
        this.organisationName = organisationName;
    }

    public String getParentOrganisationCode() {
        return parentOrganisationCode;
    }

    public void setParentOrganisationCode(final String parentOrganisationCode) {
        this.parentOrganisationCode = parentOrganisationCode;
    }

    public LocalisedString getParentOrganisationName() {
        return parentOrganisationName;
    }

    public void setParentOrganisationName(final LocalisedString parentOrganisationName) {
        this.parentOrganisationName = parentOrganisationName;
    }

    public RhyBasicInfo getBasicInfo() {
        return basicInfo;
    }

    public void setBasicInfo(final RhyBasicInfo basicInfo) {
        this.basicInfo = basicInfo;
    }

    public HunterExamStatistics getHunterExams() {
        return hunterExams;
    }

    public void setHunterExams(final HunterExamStatistics hunterExams) {
        this.hunterExams = hunterExams;
    }

    public AnnualShootingTestStatistics getShootingTests() {
        return shootingTests;
    }

    public void setShootingTests(final AnnualShootingTestStatistics shootingTests) {
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

    public HunterExamTrainingStatistics getHunterExamTraining() {
        return hunterExamTraining;
    }

    public void setHunterExamTraining(final HunterExamTrainingStatistics hunterExamTraining) {
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

    public int getAllTrainingEvents() {
        return allTrainingEvents;
    }

    public void setAllTrainingEvents(final int allTrainingEvents) {
        this.allTrainingEvents = allTrainingEvents;
    }

    public int getAllTrainingParticipants() {
        return allTrainingParticipants;
    }

    public void setAllTrainingParticipants(final int allTrainingParticipants) {
        this.allTrainingParticipants = allTrainingParticipants;
    }
}
