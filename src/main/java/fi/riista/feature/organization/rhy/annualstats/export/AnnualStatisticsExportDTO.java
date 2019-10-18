package fi.riista.feature.organization.rhy.annualstats.export;

import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.rhy.annualstats.AnnualShootingTestStatistics;
import fi.riista.feature.organization.rhy.annualstats.CommunicationStatistics;
import fi.riista.feature.organization.rhy.annualstats.GameDamageStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterExamTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.HunterTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.YouthTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.HuntingControlStatistics;
import fi.riista.feature.organization.rhy.annualstats.JHTTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.LukeStatistics;
import fi.riista.feature.organization.rhy.annualstats.MetsahallitusStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherHunterTrainingStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherHuntingRelatedStatistics;
import fi.riista.feature.organization.rhy.annualstats.OtherPublicAdminStatistics;
import fi.riista.feature.organization.rhy.annualstats.PublicEventStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatistics;
import fi.riista.feature.organization.rhy.annualstats.RhyBasicInfo;
import fi.riista.feature.organization.rhy.annualstats.ShootingRangeStatistics;
import fi.riista.feature.organization.rhy.annualstats.SrvaEventStatistics;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

public class AnnualStatisticsExportDTO {

    public static AnnualStatisticsExportDTO create(@Nonnull final OrganisationNameDTO rhy,
                                                   @Nonnull final OrganisationNameDTO rka,
                                                   @Nonnull final RhyAnnualStatistics statistics) {

        requireNonNull(rhy, "rhy is null");
        requireNonNull(rka, "rka is null");
        requireNonNull(statistics, "statistics is null");

        final AnnualStatisticsExportDTO dto = new AnnualStatisticsExportDTO();

        dto.setOrganisation(rhy);
        dto.setParentOrganisation(rka);

        dto.setBasicInfo(statistics.getOrCreateBasicInfo());

        dto.setHunterExams(statistics.getOrCreateHunterExams());
        dto.setShootingTests(statistics.getOrCreateShootingTests());
        dto.setGameDamage(statistics.getOrCreateGameDamage());
        dto.setHuntingControl(statistics.getOrCreateHuntingControl());
        dto.setOtherPublicAdmin(statistics.getOrCreateOtherPublicAdmin());

        dto.setSrva(statistics.getOrCreateSrva());

        dto.setHunterExamTraining(statistics.getOrCreateHunterExamTraining());
        dto.setJhtTraining(statistics.getOrCreateJhtTraining());
        dto.setHunterTraining(statistics.getOrCreateHunterTraining());
        dto.setYouthTraining(statistics.getOrCreateYouthTraining());
        dto.setOtherHunterTraining(statistics.getOrCreateOtherHunterTraining());
        dto.setPublicEvents(statistics.getOrCreatePublicEvents());

        dto.setOtherHuntingRelated(statistics.getOrCreateOtherHuntingRelated());
        dto.setCommunication(statistics.getOrCreateCommunication());
        dto.setShootingRanges(statistics.getOrCreateShootingRanges());
        dto.setLuke(statistics.getOrCreateLuke());
        dto.setMetsahallitus(statistics.getOrCreateMetsahallitus());

        return dto;
    }

    public static AnnualStatisticsExportDTO aggregate(@Nonnull final Iterable<AnnualStatisticsExportDTO> list) {
        requireNonNull(list);

        final AnnualStatisticsExportDTO result = new AnnualStatisticsExportDTO();

        result.setBasicInfo(RhyBasicInfo.reduce(list, dto -> dto.getBasicInfo()));

        result.setHunterExams(HunterExamStatistics.reduce(list, dto -> dto.getHunterExams()));
        result.setShootingTests(AnnualShootingTestStatistics.reduce(list, dto -> dto.getShootingTests()));
        result.setGameDamage(GameDamageStatistics.reduce(list, dto -> dto.getGameDamage()));
        result.setHuntingControl(HuntingControlStatistics.reduce(list, dto -> dto.getHuntingControl()));
        result.setOtherPublicAdmin(OtherPublicAdminStatistics.reduce(list, dto -> dto.getOtherPublicAdmin()));

        result.setSrva(SrvaEventStatistics.reduce(list, dto -> dto.getSrva()));

        result.setHunterExamTraining(HunterExamTrainingStatistics.reduce(list, dto -> dto.getHunterExamTraining()));
        result.setJhtTraining(JHTTrainingStatistics.reduce(list, dto -> dto.getJhtTraining()));
        result.setHunterTraining(HunterTrainingStatistics.reduce(list, dto -> dto.getHunterTraining()));
        result.setYouthTraining(YouthTrainingStatistics.reduce(list, dto -> dto.getYouthTraining()));
        result.setOtherHunterTraining(OtherHunterTrainingStatistics.reduce(list, dto -> dto.getOtherHunterTraining()));
        result.setPublicEvents(PublicEventStatistics.reduce(list, dto -> dto.getPublicEvents()));

        result.setOtherHuntingRelated(OtherHuntingRelatedStatistics.reduce(list, dto -> dto.getOtherHuntingRelated()));
        result.setCommunication(CommunicationStatistics.reduce(list, dto -> dto.getCommunication()));
        result.setShootingRanges(ShootingRangeStatistics.reduce(list, dto -> dto.getShootingRanges()));
        result.setLuke(LukeStatistics.reduce(list, dto -> dto.getLuke()));
        result.setMetsahallitus(MetsahallitusStatistics.reduce(list, dto -> dto.getMetsahallitus()));

        return result;
    }

    private OrganisationNameDTO organisation;
    private OrganisationNameDTO parentOrganisation;

    private RhyBasicInfo basicInfo;
    private HunterExamStatistics hunterExams;
    private AnnualShootingTestStatistics shootingTests;
    private HuntingControlStatistics huntingControl;
    private GameDamageStatistics gameDamage;
    private OtherPublicAdminStatistics otherPublicAdmin;
    private SrvaEventStatistics srva;
    private HunterExamTrainingStatistics hunterExamTraining;
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

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    // Accessors -->

    public RhyBasicInfo getBasicInfo() {
        return basicInfo;
    }

    public OrganisationNameDTO getOrganisation() {
        return organisation;
    }

    public void setOrganisation(final OrganisationNameDTO organisation) {
        this.organisation = organisation;
    }

    public OrganisationNameDTO getParentOrganisation() {
        return parentOrganisation;
    }

    public void setParentOrganisation(final OrganisationNameDTO parentOrganisation) {
        this.parentOrganisation = parentOrganisation;
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
}
