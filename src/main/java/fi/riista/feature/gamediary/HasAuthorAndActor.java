package fi.riista.feature.gamediary;

import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;

public interface HasAuthorAndActor {

    PersonWithHunterNumberDTO getAuthorInfo();

    PersonWithHunterNumberDTO getActorInfo();

}
