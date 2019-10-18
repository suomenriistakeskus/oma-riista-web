package fi.riista.feature.permit.application.search;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.repository.BaseRepositoryImpl;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.feature.permit.decision.derogation.QPermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.derogation.QPermitDecisionProtectedAreaType;
import fi.riista.feature.permit.decision.methods.ForbiddenMethodType;
import fi.riista.feature.permit.decision.methods.QPermitDecisionForbiddenMethod;
import fi.riista.feature.permit.decision.revision.QPermitDecisionRevision;
import fi.riista.util.F;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class HarvestPermitApplicationSearchQueryBuilder {

    private static final int MINIMUM_LOCAL_APPLICATION_YEAR = 2018;
    private final static QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
    private final static QPermitDecision DECISION = QPermitDecision.permitDecision;
    private final QPermitDecisionDerogationReason DEROGATION_REASON =
            QPermitDecisionDerogationReason.permitDecisionDerogationReason;
    private final QPermitDecisionProtectedAreaType PROTECTED_AREA =
            QPermitDecisionProtectedAreaType.permitDecisionProtectedAreaType;
    private final QPermitDecisionForbiddenMethod FORBIDDEN_METHOD =
            QPermitDecisionForbiddenMethod.permitDecisionForbiddenMethod;


    private final JPQLQueryFactory jpqlQueryFactory;
    private List<String> rhyCodes;
    private Long handlerId;
    private Set<HarvestPermitApplicationSearchDTO.StatusSearch> status;
    private Set<PermitDecision.DecisionType> decisionType;
    private Set<PermitDecision.AppealStatus> appealStatus;
    private Set<PermitDecision.GrantStatus> grantStatus;
    private Set<PermitDecisionDerogationReasonType> derogationReason;
    private Set<ProtectedAreaType> protectedArea;
    private Set<ForbiddenMethodType> forbiddenMethod;
    private Integer huntingYear;
    private HarvestPermitCategory harvestPermitCategory;
    private Integer gameSpeciesCode;

    public HarvestPermitApplicationSearchQueryBuilder(final JPQLQueryFactory jpqlQueryFactory) {
        this.jpqlQueryFactory = jpqlQueryFactory;
    }

    public HarvestPermitApplicationSearchQueryBuilder withStatus(final Set<HarvestPermitApplicationSearchDTO.StatusSearch> value) {
        this.status = value;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withDecisionType(final Set<PermitDecision.DecisionType> value) {
        this.decisionType = value;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withAppealStatus(final Set<PermitDecision.AppealStatus> value) {
        this.appealStatus = value;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withGrantStatus(final Set<PermitDecision.GrantStatus> value) {
        this.grantStatus = value;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withDerogationReason(final Set<PermitDecisionDerogationReasonType> value) {
        this.derogationReason = value;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withProtectedArea(final Set<ProtectedAreaType> value) {
        this.protectedArea = value;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withForbiddenMethod(final Set<ForbiddenMethodType> value) {
        this.forbiddenMethod = value;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
        return this;
    }

    public HarvestPermitApplicationSearchQueryBuilder withHarvestPermitCategory(final HarvestPermitCategory harvestPermitCategory) {
        this.harvestPermitCategory = harvestPermitCategory;
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

    private JPQLQuery<?> build() {
        final JPQLQuery<?> query = jpqlQueryFactory.from(APPLICATION)
                .leftJoin(APPLICATION.decision, DECISION);

        if (!F.isNullOrEmpty(rhyCodes)) {
            final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
            query.join(APPLICATION.rhy, RHY);
            query.where(APPLICATION.rhy.officialCode.in(rhyCodes));
        }

        if (huntingYear != null) {
            query.where(APPLICATION.applicationYear.eq(huntingYear));
        } else {
            query.where(APPLICATION.applicationYear.goe(MINIMUM_LOCAL_APPLICATION_YEAR));
        }

        if (harvestPermitCategory != null) {
            query.where(APPLICATION.harvestPermitCategory.eq(harvestPermitCategory));
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
                        b.or(APPLICATION.status.ne(HarvestPermitApplication.Status.HIDDEN)
                                .and(DECISION.status.eq(PermitDecision.Status.DRAFT))
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
                    default:
                        throw new IllegalArgumentException("Unknown status: " + s);
                }
            }
            query.where(b.getValue());
        } else {
            query.where(APPLICATION.status.notIn(EnumSet.of(
                    HarvestPermitApplication.Status.HIDDEN,
                    HarvestPermitApplication.Status.DRAFT)));
        }

        if (!F.isNullOrEmpty(decisionType)) {
            query.where(DECISION.decisionType.in(decisionType));
        }

        if (!F.isNullOrEmpty(appealStatus)) {
            query.where(DECISION.appealStatus.in(appealStatus));
        }

        if (!F.isNullOrEmpty(grantStatus)) {
            query.where(DECISION.grantStatus.in(grantStatus));
        }

        if (!F.isNullOrEmpty(derogationReason)) {
            query.where(JPAExpressions.selectOne()
                    .from(DEROGATION_REASON)
                    .where(DEROGATION_REASON.permitDecision.eq(DECISION))
                    .where(DEROGATION_REASON.reasonType.in(derogationReason))
                    .exists());
        }

        if (!F.isNullOrEmpty(protectedArea)) {
            query.where(JPAExpressions.selectOne()
                    .from(PROTECTED_AREA)
                    .where(PROTECTED_AREA.permitDecision.eq(DECISION))
                    .where(PROTECTED_AREA.protectedAreaType.in(protectedArea))
                    .exists());
        }

        if (!F.isNullOrEmpty(forbiddenMethod)) {
            query.where(JPAExpressions.selectOne()
                    .from(FORBIDDEN_METHOD)
                    .where(FORBIDDEN_METHOD.permitDecision.eq(DECISION))
                    .where(FORBIDDEN_METHOD.method.in(forbiddenMethod))
                    .exists());
        }

        final QPermitDecisionRevision REV = QPermitDecisionRevision.permitDecisionRevision;

        if (handlerId != null) {
            final BooleanExpression currentHandler = DECISION.handler.id.eq(handlerId);
            final BooleanExpression revisionCreatedByUser = JPAExpressions.selectOne()
                    .from(REV)
                    .where(REV.permitDecision.eq(DECISION))
                    .where(REV.auditFields.createdByUserId.eq(handlerId))
                    .exists();

            query.where(currentHandler.or(revisionCreatedByUser));
        }

        return query;
    }

    public List<Integer> listYears() {
        return build().select(APPLICATION.applicationYear).distinct().fetch();
    }

    public Slice<HarvestPermitApplication> slice(final Pageable pageRequest) {
        Objects.requireNonNull(pageRequest);

        final JPQLQuery<HarvestPermitApplication> query = build().select(APPLICATION)
                .orderBy(APPLICATION.applicationYear.desc(), APPLICATION.applicationNumber.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize() + 1);

        return BaseRepositoryImpl.toSlice(query.fetch(), pageRequest);
    }

    public List<HarvestPermitApplication> list() {

        final JPQLQuery<HarvestPermitApplication> query = build().select(APPLICATION)
                .orderBy(APPLICATION.applicationYear.desc(), APPLICATION.applicationNumber.desc());

        return query.fetch();
    }
}
