package fi.riista.feature.organization.rhy.annualreport;

import fi.riista.feature.organization.occupation.OccupationDTO;
import fi.riista.feature.organization.rhy.annualstats.RhyAnnualStatisticsDTO;

import java.util.List;

public class RhyAnnualReportDTO {
    private RhyAnnualStatisticsDTO annualStatistics;

    private List<OccupationDTO> boardChairs;
    private List<OccupationDTO> boardViceChairs;
    private List<OccupationDTO> boardMembers;
    private List<OccupationDTO> landOwnerRepresentatives;
    private List<OccupationDTO> mhRepresentatives;

    private List<OccupationDTO> carnivoreOfficials;

    private List<OccupationDTO> hunterExamTrainingOfficials;

    private String coordinatorName;

    private long rhyMeetingCount;
    private long boardMeetingCount;

    private String rhyName;

    RhyAnnualReportDTO(final RhyAnnualStatisticsDTO annualStatistics,
                       final List<OccupationDTO> boardChairs,
                       final List<OccupationDTO> boardViceChairs,
                       final List<OccupationDTO> boardMembers,
                       final List<OccupationDTO> landOwnerRepresentatives,
                       final List<OccupationDTO> mhRepresentatives,
                       final List<OccupationDTO> carnivoreOfficials,
                       final List<OccupationDTO> hunterExamTrainingOfficials,
                       final String coordinatorName,
                       final long rhyMeetingCount,
                       final long boardMeetingCount,
                       final String rhyName) {
        this.annualStatistics = annualStatistics;

        this.boardChairs = boardChairs;
        this.boardViceChairs = boardViceChairs;
        this.boardMembers = boardMembers;
        this.landOwnerRepresentatives = landOwnerRepresentatives;
        this.mhRepresentatives = mhRepresentatives;

        this.carnivoreOfficials = carnivoreOfficials;

        this.hunterExamTrainingOfficials = hunterExamTrainingOfficials;

        this.coordinatorName = coordinatorName;

        this.rhyMeetingCount = rhyMeetingCount;
        this.boardMeetingCount = boardMeetingCount;

        this.rhyName = rhyName;
    }

    public static RhyAnnualReportDTO create(final RhyAnnualStatisticsDTO annualStatisticsDTO,
                                            final List<OccupationDTO> boardChairs,
                                            final List<OccupationDTO> boardViceChairs,
                                            final List<OccupationDTO> boardMembers,
                                            final List<OccupationDTO> landOwnerRepresentatives,
                                            final List<OccupationDTO> mhRepresentatives,
                                            final List<OccupationDTO> carnivoreOfficials,
                                            final List<OccupationDTO> hunterExamTrainingOfficials,
                                            final String coordinatorName,
                                            final long rhyMeetingCount,
                                            final long boardMeetingCount,
                                            final String rhyName) {
        final RhyAnnualReportDTO dto =
                new RhyAnnualReportDTO(annualStatisticsDTO,
                        boardChairs, boardViceChairs, boardMembers,
                        landOwnerRepresentatives,
                        mhRepresentatives,
                        carnivoreOfficials, hunterExamTrainingOfficials,
                        coordinatorName,
                        rhyMeetingCount, boardMeetingCount,
                        rhyName);

        return dto;
    }

    public RhyAnnualStatisticsDTO getAnnualStatistics() {
        return annualStatistics;
    }

    public List<OccupationDTO> getBoardChairs() {
        return boardChairs;
    }

    public List<OccupationDTO> getBoardViceChairs() {
        return boardViceChairs;
    }

    public List<OccupationDTO> getBoardMembers() {
        return boardMembers;
    }

    public String getCoordinatorName() {
        return coordinatorName;
    }

    public long getRhyMeetingCount() {
        return rhyMeetingCount;
    }

    public long getBoardMeetingCount() {
        return boardMeetingCount;
    }

    public List<OccupationDTO> getLandOwnerRepresentatives() {
        return landOwnerRepresentatives;
    }

    public List<OccupationDTO> getMhRepresentatives() {
        return mhRepresentatives;
    }

    public List<OccupationDTO> getCarnivoreOfficials() {
        return carnivoreOfficials;
    }

    public List<OccupationDTO> getHunterExamTrainingOfficials() {
        return hunterExamTrainingOfficials;
    }

    public String getRhyName() {
        return rhyName;
    }
}
