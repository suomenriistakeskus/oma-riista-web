package fi.riista.feature.huntingclub.permit.summary;

import org.joda.time.LocalDate;

public interface HuntingEndStatus {

    Long getClubId();

    int getGameSpeciesCode();

    boolean isHuntingFinished();

    LocalDate getHuntingEndDate();

}
