package fi.riista.feature.permit.application.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.util.F;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HarvestPermitApplicationSearchQueryBuilder {

    private final static QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
    private final static QPermitDecision DECISION = QPermitDecision.permitDecision;

    private final JPQLQueryFactory jpqlQueryFactory;
    private List<String> rhyCodes;
    private Long handlerId;
    private Set<HarvestPermitApplicationSearchDTO.StatusSearch> status;
    private Integer huntingYear;
    private Integer gameSpeciesCode;
    private long maxQueryResult = -1;

    public HarvestPermitApplicationSearchQueryBuilder(final JPQLQueryFactory jpqlQueryFactory) {
        this.jpqlQueryFactory = jpqlQueryFactory;
    }

    public HarvestPermitApplicationSearchQueryBuilder withStatus(final Set<HarvestPermitApplicationSearchDTO.StatusSearch> value) {
        this.status = value;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withRhy(final String rhyOfficialCode) {
        if (StringUtils.hasText(rhyOfficialCode)) {
            this.rhyCodes = Collections.singletonList(rhyOfficialCode);
        }
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withRka(final String rkaOfficialCode) {
        if (StringUtils.hasText(rkaOfficialCode)) {
            final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
            final QOrganisation RKA = QOrganisation.organisation;
            this.rhyCodes = Objects.requireNonNull(jpqlQueryFactory
                    .select(RHY.officialCode)
                    .from(RHY)
                    .join(RHY.parentOrganisation, RKA)
                    .where(RKA.officialCode.eq(rkaOfficialCode))
                    .fetch());
        }
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withHandler(final Long handlerId) {
        this.handlerId = handlerId;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withMaxQueryResults(final long maxQueryResult) {
        this.maxQueryResult = maxQueryResult;
        return this;
    }

    private JPQLQuery<?> build() {
        final JPQLQuery<?> query = jpqlQueryFactory.from(APPLICATION)
                .leftJoin(APPLICATION.decision, DECISION);

        if (!F.isNullOrEmpty(rhyCodes)) {
            final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
            query.join(APPLICATION.rhy, RHY);
            query.where(APPLICATION.rhy.officialCode.in(rhyCodes));
        }

        if (huntingYear != null) {
            query.where(APPLICATION.huntingYear.eq(huntingYear));
        }

        if (gameSpeciesCode != null) {
            query.where(APPLICATION.speciesAmounts.any().gameSpecies.officialCode.eq(gameSpeciesCode));
        }

        if (!F.isNullOrEmpty(status)) {
            final BooleanBuilder b = new BooleanBuilder();
            for (HarvestPermitApplicationSearchDTO.StatusSearch s : status) {
                switch (s) {
                    case ACTIVE:
                        b.or(APPLICATION.status.eq(HarvestPermitApplication.Status.ACTIVE)
                                .and(DECISION.isNull().or(DECISION.status.eq(PermitDecision.Status.DRAFT)))
                                .and(DECISION.handler.isNull())
                        );
                        break;
                    case DRAFT:
                        b.or(DECISION.status.eq(PermitDecision.Status.DRAFT)
                                .and(DECISION.handler.isNotNull()));
                        break;
                    case AMENDING:
                        b.or(APPLICATION.status.eq(HarvestPermitApplication.Status.AMENDING));
                        break;
                    case LOCKED:
                        b.or(DECISION.status.eq(PermitDecision.Status.LOCKED));
                        break;
                    case PUBLISHED:
                        b.or(DECISION.status.eq(PermitDecision.Status.PUBLISHED));
                        break;
                }
            }
            query.where(b.getValue());
        } else {
            query.where(APPLICATION.status.notIn(EnumSet.of(
                    HarvestPermitApplication.Status.CANCELLED,
                    HarvestPermitApplication.Status.DRAFT)));
        }

        if (handlerId != null) {
            query.where(DECISION.handler.isNotNull())
                    .where(DECISION.handler.id.eq(handlerId));
        }

        return query;
    }

    public List<Integer> listYears() {
        return build().select(APPLICATION.huntingYear).distinct().fetch();
    }

    public List<HarvestPermitApplication> list() {
        final JPQLQuery<HarvestPermitApplication> query = build().select(APPLICATION)
                .orderBy(APPLICATION.huntingYear.desc(), APPLICATION.applicationNumber.desc());

        if (maxQueryResult > 0) {
            return query.limit(maxQueryResult).fetch();
        }

        return query.fetch();
    }
}
