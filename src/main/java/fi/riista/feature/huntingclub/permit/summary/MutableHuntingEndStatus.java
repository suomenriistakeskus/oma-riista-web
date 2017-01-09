package fi.riista.feature.huntingclub.permit.summary;

import org.joda.time.LocalDate;

public interface MutableHuntingEndStatus extends HuntingEndStatus {

    void setHuntingFinished(boolean huntingFinished);

    void setHuntingEndDate(LocalDate huntingEndDate);

}
