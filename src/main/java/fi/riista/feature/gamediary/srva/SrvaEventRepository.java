package fi.riista.feature.gamediary.srva;


import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.person.Person;

public interface SrvaEventRepository extends BaseRepository<SrvaEvent, Long>, SrvaEventRepositoryCustom {
    SrvaEvent findByAuthorAndMobileClientRefId(Person author, Long mobileClientRefId);
}
