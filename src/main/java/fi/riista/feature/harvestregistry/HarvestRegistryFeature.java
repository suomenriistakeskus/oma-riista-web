package fi.riista.feature.harvestregistry;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.types.Predicate;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;

import static fi.riista.feature.harvestregistry.HarvestRegistryCoordinatorSearchReason.HUNTING_CONTROL;
import static fi.riista.feature.harvestregistry.HarvestRegistryItemDTO.Fields.COMMON;
import static fi.riista.feature.harvestregistry.HarvestRegistryItemDTO.Fields.COMMON_WITH_SHOOTER;
import static fi.riista.feature.harvestregistry.HarvestRegistryItemDTO.Fields.FULL;
import static fi.riista.security.EntityPermission.READ;

@Component
public class HarvestRegistryFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private UserAuthorizationHelper authorizationHelper;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestRegistryItemRepository harvestRegistryItemRepository;

    @Resource
    private HarvestRegistryItemToDTOTransformer harvestRegistryItemToDTOTransformer;

    @Transactional(readOnly = true)
    public Slice<HarvestRegistryItemDTO> listMine(final HarvestRegistryRequestDTO dto, final Locale locale) {
        final Person person = activeUserService.requireActivePerson();
        return fetchItems(dto, person, FULL);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('HARVEST_REGISTRY')")
    @Transactional(readOnly = true)
    public Slice<HarvestRegistryItemDTO> listForPerson(final long personId,
                                                       final HarvestRegistryRequestDTO dto) {
        final Person person = requireEntityService.requirePerson(personId, READ);

        return fetchItems(dto, person, FULL);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasPrivilege('HARVEST_REGISTRY')")
    @Transactional(readOnly = true)
    public Slice<HarvestRegistryItemDTO> listPaged(final HarvestRegistryRequestDTO dto) {
        final PageRequest pageRequest = pageRequest(dto);

        Predicate predicate = HarvestRegistryQueries.predicateFromDTO(dto);

        final Slice<HarvestRegistryItem> slice = harvestRegistryItemRepository.findAllAsSlice(predicate,
                pageRequest, HarvestRegistryQueries.POINT_OF_TIME_ORDERING);

        return transformToSlice(slice.getContent(), slice.hasNext(), pageRequest(dto), FULL);
    }

    @Transactional(readOnly = true)
    public Slice<HarvestRegistryItemDTO> listForCoordinator(final HarvestRegistryCoordinatorRequestDTO dto) {

        authorizationHelper.assertCoordinatorOrModerator(dto.getRhyId());
        final Riistanhoitoyhdistys riistanhoitoyhdistys =
                requireEntityService.requireRiistanhoitoyhdistys(dto.getRhyId(), READ);

        final HarvestRegistryRequestDTO requestDTO = dto.toHarvestReqistryRequestDTO(riistanhoitoyhdistys);

        final PageRequest pageRequest = pageRequest(requestDTO);

        Predicate predicate = HarvestRegistryQueries.predicateFromDTO(requestDTO);

        final Slice<HarvestRegistryItem> slice = harvestRegistryItemRepository.findAllAsSlice(predicate,
                pageRequest, HarvestRegistryQueries.POINT_OF_TIME_ORDERING);

        final HarvestRegistryItemDTO.Fields includedFields = dto.getSearchReason() == HUNTING_CONTROL
                ? COMMON_WITH_SHOOTER
                : COMMON;
        return transformToSlice(slice.getContent(), slice.hasNext(), pageRequest(requestDTO), includedFields);
    }

    private Slice<HarvestRegistryItemDTO> transformToSlice(final List<HarvestRegistryItem> items,
                                                           final boolean hasNext,
                                                           final Pageable pageRequest,
                                                           final HarvestRegistryItemDTO.Fields includedFields) {
        final List<HarvestRegistryItemDTO> dtos = harvestRegistryItemToDTOTransformer.transform(items, includedFields);
        return new SliceImpl<>(dtos, pageRequest, hasNext);
    }

    private Slice<HarvestRegistryItemDTO> fetchItems(final HarvestRegistryRequestDTO dto, final Person person,
                                                     final HarvestRegistryItemDTO.Fields includedFields) {
        final Pageable pageRequest = pageRequest(dto);

        if (person.getHunterNumber() == null) {
            return transformToSlice(ImmutableList.of(), false, pageRequest, includedFields);
        }
        final Predicate predicate = HarvestRegistryQueries.predicateFromDTO(dto);
        final Slice<HarvestRegistryItem> slice = harvestRegistryItemRepository.findByPerson(person,
                predicate, pageRequest);

        return transformToSlice(slice.getContent(), slice.hasNext(), pageRequest(dto), includedFields);
    }

    private static PageRequest pageRequest(final HarvestRegistryRequestDTO dto) {
        return PageRequest.of(dto.getPage(), dto.getPageSize());
    }

}
