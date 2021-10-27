package fi.riista.feature.harvestregistry;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;

import java.util.Date;

public class HarvestRegistryQueries {

    private static final QHarvestRegistryItem ITEM = QHarvestRegistryItem.harvestRegistryItem;

    public static final OrderSpecifier<Date> POINT_OF_TIME_ORDERING = new OrderSpecifier(Order.DESC, ITEM.pointOfTime);

    public static Predicate predicateFromDTO(final HarvestRegistryRequestDTO dto) {

        // Use start of day at the next day of the end criteria to include items on the last day of the period
        final DateTime endDate = DateUtil.toDateTimeNullSafe(dto.getEndDate().plusDays(1));
        final DateTime beginDate = DateUtil.toDateTimeNullSafe(dto.getBeginDate());

        BooleanExpression predicate = ITEM.pointOfTime.between(beginDate, endDate);

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
