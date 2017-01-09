package fi.riista.feature.huntingclub.area;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gis.zone.QGISZone;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class HuntingClubAreaSizeService {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Optional<Double> getHuntingPermitAreaSize(final HarvestPermit permit,
                                                     final HuntingClub club) {
        Objects.requireNonNull(permit, "permit is null");
        Objects.requireNonNull(club, "club is null");

        if (!permit.isMooselikePermitType()) {
            return Optional.empty();
        }

        final QHuntingClubGroup huntingClubGroup = QHuntingClubGroup.huntingClubGroup;
        final QHuntingClubArea huntingClubArea = QHuntingClubArea.huntingClubArea;
        final QGISZone zone = QGISZone.gISZone;

        final List<Tuple> distinctAreaSizes = queryFactory
                .from(huntingClubGroup)
                .join(huntingClubGroup.huntingArea, huntingClubArea)
                .join(huntingClubArea.zone, zone)
                // Group has correct permit and club is permit partner
                .where(huntingClubGroup.harvestPermit.eq(permit),
                        huntingClubGroup.parentOrganisation.eq(club),
                        zone.computedAreaSize.isNotNull())
                // Fetch all distinct zones with areaSize used by club groups
                .distinct()
                .select(zone.id, zone.computedAreaSize)
                .fetch();

        // Return value only if all groups are using same HuntingArea definition
        // This is a temporary workaround, because overlapping group area calculation is not yet implemented.
        return distinctAreaSizes.size() != 1
                ? Optional.empty()
                : Optional.of(distinctAreaSizes.stream()
                .mapToDouble(tuple -> tuple.get(zone.computedAreaSize))
                .sum());
    }
}
