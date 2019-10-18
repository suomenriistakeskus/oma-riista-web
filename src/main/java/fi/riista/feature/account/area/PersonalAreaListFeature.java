package fi.riista.feature.account.area;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class PersonalAreaListFeature {

    @Resource
    private PersonalAreaRepository personalAreaRepository;

    @Resource
    private PersonalAreaDTOTransformer personalAreaDTOTransformer;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    public Slice<PersonalAreaDTO> listMinePaged(final Pageable pageRequest) {
        final Person person = activeUserService.requireActivePerson();
        final Slice<PersonalArea> all = fetchSlice(pageRequest, person);

        return personalAreaDTOTransformer.apply(all, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<PersonalAreaDTO> listMine() {
        final Person person = activeUserService.requireActivePerson();
        return fetchAreas(person);
    }

    // For moderator

    @Transactional(readOnly = true)
    public Slice<PersonalAreaDTO> listForPersonPaged(final long personId, final Pageable pageRequest) {
        Preconditions.checkState(activeUserService.isModeratorOrAdmin(), "Must be moderator or admin");
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.READ);
        final Slice<PersonalArea> all = fetchSlice(pageRequest, person);

        return personalAreaDTOTransformer.apply(all, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<PersonalAreaDTO> listForPerson(final long personId) {
        Preconditions.checkState(activeUserService.isModeratorOrAdmin(), "Must be moderator or admin");
        final Person person = requireEntityService.requirePerson(personId, EntityPermission.READ);
        return fetchAreas(person);
    }

    private Slice<PersonalArea> fetchSlice(final Pageable pageRequest, final Person person) {
        final BooleanExpression predicate = QPersonalArea.personalArea.person.eq(person);
        return personalAreaRepository.findAllAsSlice(predicate, pageRequest);
    }

    private List<PersonalAreaDTO> fetchAreas(final Person person) {
        final BooleanExpression predicate = QPersonalArea.personalArea.person.eq(person);
        final Iterable<PersonalArea> all = personalAreaRepository.findAll(predicate);

        return personalAreaDTOTransformer.apply(ImmutableList.copyOf(all));
    }
}
