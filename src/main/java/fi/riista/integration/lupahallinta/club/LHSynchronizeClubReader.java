package fi.riista.integration.lupahallinta.club;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.sql.SQHta;
import fi.riista.sql.SQLhOrg;
import fi.riista.sql.SQOccupation;
import fi.riista.sql.SQOrganisation;
import org.springframework.batch.item.adapter.ItemReaderAdapter;

import java.sql.Date;
import java.util.Iterator;
import java.util.List;

public class LHSynchronizeClubReader extends ItemReaderAdapter<LHSynchronizeClubItem> {

    private final SQLQueryFactory sqlQueryFactory;
    private Iterator<LHSynchronizeClubItem> iterator = null;

    public LHSynchronizeClubReader(final SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    @Override
    public LHSynchronizeClubItem read() throws Exception {
        if (this.iterator == null) {
            this.iterator = loadItems().iterator();
        }

        return this.iterator.hasNext() ? this.iterator.next() : null;
    }

    private List<LHSynchronizeClubItem> loadItems() {
        final SQLhOrg LH = new SQLhOrg("lh");
        final SQOrganisation CLUB = new SQOrganisation("club");
        final SQOrganisation RHY = new SQOrganisation("rhy");
        final SQHta HTA = new SQHta("hta");

        return sqlQueryFactory.select(Projections
                .constructor(LHSynchronizeClubItem.class,
                        CLUB.organisationId,
                        LH.longitude.coalesce(CLUB.longitude),
                        LH.latitude.coalesce(CLUB.latitude),
                        LH.nameFinnish.coalesce(CLUB.nameFinnish),
                        LH.nameSwedish.coalesce(CLUB.nameSwedish),
                        LH.areaSize,
                        RHY.organisationId.coalesce(CLUB.parentOrganisationId),
                        HTA.gid))
                .from(CLUB)
                .join(LH).on(
                        LH.officialCode.eq(CLUB.officialCode),
                        CLUB.organisationType.eq(OrganisationType.CLUB.name()))
                .leftJoin(RHY).on(
                        LH.rhyOfficialCode.eq(RHY.officialCode),
                        RHY.organisationType.eq(OrganisationType.RHY.name()))
                .leftJoin(HTA).on(LH.mooseAreaCode.eq(HTA.numero))
                .where(CLUB.organisationId.notIn(sqlOrganisationIdsWithContactPerson()))
                .distinct()
                .fetch();
    }

    private static SQLQuery<Long> sqlOrganisationIdsWithContactPerson() {
        final SQOccupation OCCUPATION = new SQOccupation("contact_occ");
        final DateExpression<Date> CURRENT_DATE = DateExpression.currentDate(Date.class);
        final ComparableExpression<Date> beginDate = OCCUPATION.beginDate.coalesce(CURRENT_DATE).getValue();
        final ComparableExpression<Date> endDate = OCCUPATION.endDate.coalesce(CURRENT_DATE).getValue();

        return SQLExpressions.select(OCCUPATION.organisationId)
                .from(OCCUPATION)
                .where(OCCUPATION.occupationType.eq(OccupationType.SEURAN_YHDYSHENKILO.name()),
                        CURRENT_DATE.between(beginDate, endDate),
                        OCCUPATION.deletionTime.isNull());
    }
}
