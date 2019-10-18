package fi.riista.integration.metsahallitus.permit;

import fi.riista.feature.organization.person.Person;

import java.util.List;

public interface MetsahallitusPermitRepositoryCustom {
    List<MetsahallitusPermit> findByPerson(Person person);
}
