package fi.riista.feature.huntingclub.area;

import com.google.common.base.Preconditions;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.AbstractCrudFeature;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gis.zone.GISZone;
import fi.riista.feature.gis.zone.GISZoneRepository;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.huntingclub.copy.CopyClubGroupService;
import fi.riista.feature.huntingclub.copy.HuntingClubAreaCopyDTO;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.Locales;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Component
public class HuntingClubAreaCrudFeature extends AbstractCrudFeature<Long, HuntingClubArea, HuntingClubAreaDTO> {

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HuntingClubAreaRepository huntingClubAreaRepository;

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private HuntingClubAreaDTOTransformer huntingClubAreaDTOTransformer;

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private CopyClubGroupService copyClubGroupService;

    @Resource
    private GISZoneRepository gisZoneRepository;

    @Resource
    private MessageSource messageSource;

    @Resource
    private SecureRandom secureRandom;

    @Value("${map.latest.metsahallitus.year}")
    private int latestMetsahallitusYear;

    @Override
    protected JpaRepository<HuntingClubArea, Long> getRepository() {
        return huntingClubAreaRepository;
    }

    @Override
    protected HuntingClubAreaDTO toDTO(@Nonnull final HuntingClubArea entity) {
        return huntingClubAreaDTOTransformer.apply(entity);
    }

    @Override
    protected void updateEntity(final HuntingClubArea entity, final HuntingClubAreaDTO dto) {
        if (entity.isNew()) {
            entity.setMetsahallitusYear(latestMetsahallitusYear);
            entity.setClub(huntingClubRepository.getOne(dto.getClubId()));

        } else {
            final long attachedGroups = huntingClubGroupRepository.countByHuntingArea(entity);

            if (attachedGroups > 0) {
                Preconditions.checkArgument(Objects.equals(dto.getHuntingYear(), entity.getHuntingYear()),
                        "huntingYear cannot be changed");
                Preconditions.checkArgument(dto.isActive(), "area cannot be deactivated");
            }
        }
        entity.setHuntingYear(dto.getHuntingYear());
        entity.setActive(entity.isNew() || dto.isActive());
        entity.setNameFinnish(dto.getNameFI());
        entity.setNameSwedish(dto.getNameSV());

        if (entity.getExternalId() == null) {
            entity.generateAndStoreExternalId(secureRandom);
        }
    }

    @Transactional(readOnly = true)
    public List<HuntingClubAreaDTO> listByClubAndYear(final long clubId,
                                                      final Integer year,
                                                      final boolean activeOnly) {
        final HuntingClub club = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);

        final Predicate[] predicates = createPredicate(club, year, activeOnly);

        return huntingClubAreaDTOTransformer.apply(new JPAQuery<>(entityManager).from(QHuntingClubArea.huntingClubArea)
                .where(predicates)
                .select(QHuntingClubArea.huntingClubArea)
                .fetch());
    }

    private static Predicate[] createPredicate(final HuntingClub club,
                                               final Integer year,
                                               final boolean activeOnly) {
        final List<BooleanExpression> predicates = new LinkedList<>();
        predicates.add(QHuntingClubArea.huntingClubArea.club.eq(club));

        if (year != null) {
            predicates.add(QHuntingClubArea.huntingClubArea.huntingYear.eq(year));
        }

        if (activeOnly) {
            predicates.add(QHuntingClubArea.huntingClubArea.active.isTrue());
        }

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    @Transactional(readOnly = true)
    public List<Integer> listHuntingYears(Long clubId) {
        final HuntingClub club = requireEntityService.requireHuntingClub(clubId, EntityPermission.READ);

        return huntingClubAreaRepository.listHuntingYears(club);
    }

    @Transactional
    public HuntingClubAreaDTO copy(final HuntingClubAreaCopyDTO dto) {
        return toDTO(copyWithoutTransform(dto));
    }

    @Transactional
    public HuntingClubArea copyWithoutTransform(final HuntingClubAreaCopyDTO dto) {
        final HuntingClubArea originalArea = requireEntity(dto.getId(), EntityPermission.CREATE);
        final boolean useSuffix = originalArea.getHuntingYear() == dto.getHuntingYear();

        final HuntingClubArea area = new HuntingClubArea(
                originalArea.getClub(),
                originalArea.getNameFinnish() + (useSuffix ? suffix(Locales.FI) : ""),
                originalArea.getNameSwedish() + (useSuffix ? suffix(Locales.SV) : ""),
                dto.getHuntingYear(),
                originalArea.getMetsahallitusYear(),
                null);

        area.generateAndStoreExternalId(secureRandom);

        Optional.ofNullable(originalArea.getZone())
                .map(originalZone -> gisZoneRepository.copyZone(originalZone, new GISZone()))
                .ifPresent(area::setZone);

        getRepository().saveAndFlush(area);

        if (dto.isCopyGroups()) {
            copyClubGroupService.copyGroupsHavingArea(originalArea, area);
        }
        return area;
    }

    private String suffix(Locale locale) {
        return " " + messageSource.getMessage("copy.suffix.caps", null, locale);
    }
}
