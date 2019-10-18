package fi.riista.feature.moderatorarea;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.QOrganisation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.springframework.util.StringUtils.hasText;

@Component
public class ModeratorAreaListFeature {

    @Resource
    private ModeratorAreaRepository moderatorAreaRepository;

    @Resource
    private ModeratorAreaDTOTransformer moderatorAreaDTOTransformer;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODERATOR')")
    public Slice<ModeratorAreaDTO> slice(final ModeratorAreaListRequestDTO dto) {
        final Pageable pageRequest = new PageRequest(dto.getPage(), dto.getSize(), Sort.Direction.DESC, "id");
        final Long userId = activeUserService.requireActiveUserId();
        final QModeratorArea AREA = QModeratorArea.moderatorArea;
        final Predicate predicate;

        if (hasText(dto.getRkaCode()) || hasText(dto.getSearchText()) || dto.getYear() != null) {
            final QOrganisation RKA = QOrganisation.organisation;
            final BooleanBuilder builder = new BooleanBuilder();

            if (dto.getYear() != null) {
                builder.and(AREA.year.eq(dto.getYear()));
            }

            if (hasText(dto.getRkaCode())) {
                builder.and(AREA.rka.eq(JPAExpressions.selectFrom(RKA)
                        .where(RKA.organisationType.eq(OrganisationType.RKA),
                                RKA.officialCode.eq(dto.getRkaCode()))));
            }

            if (hasText(dto.getSearchText())) {
                builder.andAnyOf(
                        AREA.externalId.eq(dto.getSearchText()),
                        AREA.name.likeIgnoreCase(dto.getSearchText() + "%"));
            }

            predicate = builder.getValue();

        } else {
            predicate = AREA.moderator.id.eq(userId);
        }

        final Slice<ModeratorArea> all = moderatorAreaRepository.findAllAsSlice(predicate, pageRequest);

        return moderatorAreaDTOTransformer.apply(all, pageRequest);
    }

}
