package fi.riista.feature.permit.area;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gis.zone.QGISZone;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.huntingclub.area.QHuntingClubArea;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.area.partner.QHarvestPermitAreaPartner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

public class HarvestPermitAreaRepositoryImpl implements HarvestPermitAreaRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitArea> listActiveApplicationAreas(final HuntingClub club, final int huntingYear) {
        final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
        final QHarvestPermitArea PERMIT_AREA = QHarvestPermitArea.harvestPermitArea;
        final QHarvestPermitAreaPartner PARTNER = QHarvestPermitAreaPartner.harvestPermitAreaPartner;
        final QHuntingClubArea CLUB_AREA = QHuntingClubArea.huntingClubArea;

        final JPQLQuery<HuntingClubArea> subAllClubAreas = JPAExpressions.selectFrom(CLUB_AREA).where(CLUB_AREA.club.eq(club));
        final JPQLQuery<HarvestPermitArea> subAreaIdsAsPartner = JPAExpressions.select(PARTNER.harvestPermitArea)
                .from(PARTNER)
                .where(PARTNER.sourceArea.in(subAllClubAreas));

        return jpqlQueryFactory.select(PERMIT_AREA)
                .from(APPLICATION)
                .join(APPLICATION.area, PERMIT_AREA)
                .where(APPLICATION.applicationYear.eq(huntingYear),
                        APPLICATION.status.eq(HarvestPermitApplication.Status.ACTIVE),
                        PERMIT_AREA.in(subAreaIdsAsPartner))
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findPartnerZoneIds(final HarvestPermitArea permitArea) {
        final QHarvestPermitAreaPartner PARTNER = QHarvestPermitAreaPartner.harvestPermitAreaPartner;
        final QHuntingClubArea CLUB_AREA = QHuntingClubArea.huntingClubArea;
        final QGISZone ZONE = QGISZone.gISZone;

        return jpqlQueryFactory
                .select(ZONE.id)
                .from(PARTNER)
                .join(PARTNER.sourceArea, CLUB_AREA)
                .join(CLUB_AREA.zone, ZONE)
                .where(PARTNER.harvestPermitArea.eq(permitArea))
                .fetch();
    }

}
