package fi.riista.feature.organization.occupation;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OccupationRepositoryCustom {
    List<Occupation> findActiveByOrganisation(Organisation organisation);

    List<Occupation> findActiveByParentOrganisation(Organisation organisation);

    Map<Long, Set<Occupation>> findActiveByOccupationTypeGroupByOrganisationId(OccupationType occupationType);

    void deleteByOrganisation(Organisation organisation);

    void deleteOccupationInFuture(Organisation organisation,
                                  OccupationType occupationType,
                                  Person person);

    void endOccupationsForDeceased();

    boolean alreadyExists(OccupationDTO dto);

    Map<Organisation, Occupation> listCoordinators(List<Organisation> organisations);
}
