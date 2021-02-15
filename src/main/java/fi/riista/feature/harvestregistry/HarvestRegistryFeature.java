package fi.riista.feature.harvestregistry;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.types.Predicate;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

import static fi.riista.security.EntityPermission.READ;

@Component
public class HarvestRegistryFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestRegistryItemRepository harvestRegistryItemRepository;

    @Resource
    private HarvestRegistryItemToDTOTransformer harvestRegistryItemToDTOTransformer;

    @Transactional(readOnly = true)
    public Slice<HarvestRegistryItemDTO> listMine(final HarvestRegistryRequestDTO dto, final Locale locale) {
        final Person person = activeUserService.requireActivePerson();
        return fetchItems(dto, person, locale);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('HARVEST_REGISTRY')")
    @Transactional(readOnly = true)
    public Slice<HarvestRegistryItemDTO> listForPerson(final long personId,
                                                       final HarvestRegistryRequestDTO dto, final Locale locale) {
        final Person person = requireEntityService.requirePerson(personId, READ);

        return fetchItems(dto, person, locale);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('HARVEST_REGISTRY')")
    @Transactional(readOnly = true)
    public Slice<HarvestRegistryItemDTO> listPaged(final HarvestRegistryRequestDTO dto, final Locale locale) {
        final PageRequest pageRequest = pageRequest(dto);

        Predicate predicate = HarvestRegistryQueries.predicateFromDTO(dto);

        final Slice<HarvestRegistryItem> slice = harvestRegistryItemRepository.findAllAsSlice(predicate,
                pageRequest, HarvestRegistryQueries.POINT_OF_TIME_ORDERING);

        return transformToSlice(slice.getContent(), slice.hasNext(), pageRequest(dto), locale);
    }

    private Slice<HarvestRegistryItemDTO> transformToSlice(final List<HarvestRegistryItem> items,
                                                           final boolean hasNext,
                                                           final Pageable pageRequest,
                                                           final Locale locale) {
        final List<HarvestRegistryItemDTO> dtos = harvestRegistryItemToDTOTransformer.apply(items);
        return new SliceImpl<>(dtos, pageRequest, hasNext);
    }

    private Slice<HarvestRegistryItemDTO> fetchItems(final HarvestRegistryRequestDTO dto, final Person person,
                                                     final Locale locale) {
        final Pageable pageRequest = pageRequest(dto);

        if (person.getHunterNumber() == null) {
            return transformToSlice(ImmutableList.of(), false, pageRequest, locale);
        }
        final Predicate predicate = HarvestRegistryQueries.predicateFromDTO(dto);
        final Slice<HarvestRegistryItem> slice = harvestRegistryItemRepository.findByPerson(person,
                predicate, pageRequest);

        return transformToSlice(slice.getContent(), slice.hasNext(), pageRequest(dto), locale);
    }

    private static PageRequest pageRequest(final HarvestRegistryRequestDTO dto) {
        return PageRequest.of(dto.getPage(), dto.getPageSize());
    }

}
