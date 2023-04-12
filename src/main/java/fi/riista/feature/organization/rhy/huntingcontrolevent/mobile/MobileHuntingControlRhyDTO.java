package fi.riista.feature.organization.rhy.huntingcontrolevent.mobile;

import fi.riista.feature.account.mobile.MobileOrganisationDTO;

import java.util.List;

public class MobileHuntingControlRhyDTO {

    public static MobileHuntingControlRhyDTO create(final MobileHuntingControlSpecVersion specVersion,
                                                    final MobileOrganisationDTO rhy,
                                                    final List<MobileGameWardenDTO> gameWardens,
                                                    final List<MobileHuntingControlEventDTO> events) {
        /**
         * This is a gentle reminder that different specVersions needs own handling. The current implementation is left
         * simple as there is no need for more complex solution (as someone might think that even this is overengineered).
         */
        switch (specVersion) {
            case _1:
                return createSpecVersion1(rhy, gameWardens, events);
            default:
                throw new UnsupportedOperationException("MobileHuntingControlSpecVersion " + specVersion + " not supported");
        }
    }

    private static MobileHuntingControlRhyDTO createSpecVersion1(final MobileOrganisationDTO rhy,
                                                                 final List<MobileGameWardenDTO> gameWardens,
                                                                 final List<MobileHuntingControlEventDTO> events) {

        final MobileHuntingControlRhyDTO dto = new MobileHuntingControlRhyDTO();
        dto.setSpecVersion(MobileHuntingControlSpecVersion._1);
        dto.setRhy(rhy);
        dto.setGameWardens(gameWardens);
        dto.setEvents(events);
        return dto;
    }

    private MobileHuntingControlSpecVersion specVersion;

    private MobileOrganisationDTO rhy;

    private List<MobileGameWardenDTO> gameWardens;

    private List<MobileHuntingControlEventDTO> events;

    // Accessors -->

    public MobileHuntingControlSpecVersion getSpecVersion() {
        return specVersion;
    }

    public void setSpecVersion(final MobileHuntingControlSpecVersion specVersion) {
        this.specVersion = specVersion;
    }

    public MobileOrganisationDTO getRhy() {
        return rhy;
    }

    public void setRhy(final MobileOrganisationDTO rhy) {
        this.rhy = rhy;
    }

    public List<MobileGameWardenDTO> getGameWardens() {
        return gameWardens;
    }

    public void setGameWardens(final List<MobileGameWardenDTO> gameWardens) {
        this.gameWardens = gameWardens;
    }

    public List<MobileHuntingControlEventDTO> getEvents() {
        return events;
    }

    public void setEvents(final List<MobileHuntingControlEventDTO> events) {
        this.events = events;
    }
}
