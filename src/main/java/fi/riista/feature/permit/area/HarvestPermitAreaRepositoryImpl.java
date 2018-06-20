package fi.riista.feature.permit.area;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.permit.area.partner.QHarvestPermitAreaPartner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

public class HarvestPermitAreaRepositoryImpl implements HarvestPermitAreaRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<Integer> listHuntingYears(final HuntingClub club) {
        final QHarvestPermitArea PERMIT_AREA = QHarvestPermitArea.harvestPermitArea;
        final QHarvestPermitAreaPartner PARTNER = QHarvestPermitAreaPartner.harvestPermitAreaPartner;
        final QHuntingClubArea CLUB_AREA = QHuntingClubArea.huntingClubArea;

        final JPQLQuery<HuntingClubArea> subAllClubAreas = JPAExpressions.selectFrom(CLUB_AREA).where(CLUB_AREA.club.eq(club));
        final JPQLQuery<Long> subAreaIdsAsPartner = JPAExpressions.select(PARTNER.harvestPermitArea.id)
                .from(PARTNER)
                .where(PARTNER.sourceArea.in(subAllClubAreas));

        return jpqlQueryFactory.select(PERMIT_AREA.huntingYear).from(PERMIT_AREA)
                .where(PERMIT_AREA.club.eq(club).or(PERMIT_AREA.id.in(subAreaIdsAsPartner)))
                .orderBy(PERMIT_AREA.huntingYear.asc())
                .distinct()
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitArea> listByClub(final HuntingClub club, final int huntingYear) {
        final QHarvestPermitArea PERMIT_AREA = QHarvestPermitArea.harvestPermitArea;
        final QHarvestPermitAreaPartner PARTNER = QHarvestPermitAreaPartner.harvestPermitAreaPartner;
        final QHuntingClubArea CLUB_AREA = QHuntingClubArea.huntingClubArea;

        final JPQLQuery<HuntingClubArea> subAllClubAreas = JPAExpressions.selectFrom(CLUB_AREA).where(CLUB_AREA.club.eq(club));
        final JPQLQuery<Long> subAreaIdsAsPartner = JPAExpressions.select(PARTNER.harvestPermitArea.id)
                .from(PARTNER)
                .where(PARTNER.sourceArea.in(subAllClubAreas));

        return jpqlQueryFactory.selectFrom(PERMIT_AREA)
                .where(PERMIT_AREA.huntingYear.eq(huntingYear),
                        PERMIT_AREA.club.eq(club).or(PERMIT_AREA.id.in(subAreaIdsAsPartner)),
                        PERMIT_AREA.lifecycleFields.deletionTime.isNull())
                .fetch();
    }
}
