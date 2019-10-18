package fi.riista.feature.harvestregistry;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.util.DateUtil;

import java.util.Date;

public class HarvestRegistryQueries {

    private static final QHarvestRegistryItem ITEM = QHarvestRegistryItem.harvestRegistryItem;

    public static final OrderSpecifier<Date> POINT_OF_TIME_ORDERING = new OrderSpecifier(Order.DESC, ITEM.pointOfTime);

    public static Predicate predicateFromDTO(final HarvestRegistryRequestDTO dto) {
        BooleanExpression predicate = ITEM.pointOfTime.between(
                DateUtil.toDateNullSafe(dto.getBeginDate()),
                DateUtil.toDateNullSafe(dto.getEndDate()));

        if (!dto.isAllSpecies()) {
            predicate = predicate.and(ITEM.species.officialCode.in(dto.getSpecies()));
        }

        if (dto.getMunicipalityCode() != null) {
            predicate = predicate.and(ITEM.municipalityCode.eq(dto.getMunicipalityCode()));
        }

        if (dto.getRkaCode() != null) {
            predicate = predicate.and(ITEM.rkaCode.eq(dto.getRkaCode()));
        }

        if (dto.getRhyCode() != null) {
            predicate = predicate.and(ITEM.rhyCode.eq(dto.getRhyCode()));
        }

        if (dto.getShooterHunterNumber() != null) {
            predicate = predicate.and(ITEM.shooterHunterNumber.eq(dto.getShooterHunterNumber()));
        }

        return predicate;
    }

    // Prevent instantiation
    private HarvestRegistryQueries() {
    }
}
