package fi.riista.feature.harvestregistry;

import com.querydsl.core.types.Predicate;
import fi.riista.feature.organization.person.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface HarvestRegistryItemRepositoryCustom {

    Slice<HarvestRegistryItem> findByPerson(Person person, Predicate predicate, Pageable pageRequest);
}
