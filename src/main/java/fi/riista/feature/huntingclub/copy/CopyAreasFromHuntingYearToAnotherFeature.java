package fi.riista.feature.huntingclub.copy;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubAreaCrudFeature;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class CopyAreasFromHuntingYearToAnotherFeature {

    private static final Logger LOG = LoggerFactory.getLogger(CopyAreasFromHuntingYearToAnotherFeature.class);

    @Resource
    private JPQLQueryFactory queryFactory;

    @Resource
    private HuntingClubAreaCrudFeature feature;

    @Transactional
    public void copyAreas(final int fromYear, final int toYear) {
        final QHuntingClubArea huntingClubArea = QHuntingClubArea.huntingClubArea;
        final QHuntingClubArea nextYearArea = new QHuntingClubArea("nextYearArea");

        final JPQLQuery<HuntingClub> clubsHavingAreaNextYear = JPAExpressions.selectFrom(nextYearArea)
                .select(nextYearArea.club).distinct()
                .where(nextYearArea.huntingYear.eq(toYear)
                        .and(nextYearArea.active.eq(true)));

        final List<Long> areaIdsToCopy = queryFactory
                .from(huntingClubArea)
                .select(huntingClubArea.id)
                .where(huntingClubArea.huntingYear.eq(fromYear)
                        .and(huntingClubArea.active.eq(true))
                        .and(huntingClubArea.club.notIn(clubsHavingAreaNextYear))
                )
                .fetch();

        LOG.info(String.format("Going to copy from year %s to year %s areas: %s", fromYear, toYear, areaIdsToCopy));
        areaIdsToCopy.stream()
                .map(id -> new HuntingClubAreaCopyDTO(id, toYear, true))
                .forEach(dto -> {
                    LOG.info("Copying area id:" + dto.getId());
                    feature.copyWithoutTransform(dto);
                    LOG.info("Copying area id:" + dto.getId() + " complete.");
                });
        LOG.info("Copying done.");
    }
}
