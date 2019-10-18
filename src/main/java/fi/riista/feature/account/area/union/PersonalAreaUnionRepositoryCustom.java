package fi.riista.feature.account.area.union;

import java.util.Optional;

public interface PersonalAreaUnionRepositoryCustom {

    Optional<PersonalAreaUnion> findByExternalId(String externalId);

}
