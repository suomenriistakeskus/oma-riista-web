package fi.riista.feature.permit.application.search.excel;

import com.querydsl.core.group.Group;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.QHarvestPermitApplication;
import fi.riista.feature.permit.application.QHarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.feature.permit.application.search.HarvestPermitApplicationSearchDTO;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.QPermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.feature.permit.decision.derogation.QPermitDecisionDerogationReason;
import fi.riista.feature.permit.decision.derogation.QPermitDecisionProtectedAreaType;
import fi.riista.feature.permit.decision.methods.ForbiddenMethodType;
import fi.riista.feature.permit.decision.methods.QPermitDecisionForbiddenMethod;
import fi.riista.feature.permit.decision.species.QPermitDecisionSpeciesAmount;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.querydsl.core.group.GroupBy.set;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

@Component
public class HarvestPermitApplicationSearchExcelFeature {
    private static final QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
    private static final QPermitDecision DECISION = QPermitDecision.permitDecision;
    private static final QPerson CONTACT_PERSON = new QPerson("contactPerson");
    private static final QSystemUser HANDLER_USER = QSystemUser.systemUser;
    private static final QHarvestPermitApplicationSpeciesAmount APPLICATION_SPA =
            QHarvestPermitApplicationSpeciesAmount.harvestPermitApplicationSpeciesAmount;
    private static final QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    private static final QPermitDecisionProtectedAreaType AREA_TYPE =
            QPermitDecisionProtectedAreaType.permitDecisionProtectedAreaType;
    private static final QPermitDecisionDerogationReason DEROGATION_REASON =
            QPermitDecisionDerogationReason.permitDecisionDerogationReason;
    private static final QPermitDecisionForbiddenMethod METHOD =
            QPermitDecisionForbiddenMethod.permitDecisionForbiddenMethod;
    private static final QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
    private static final QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;
    private static final QPermitDecisionSpeciesAmount DECISION_SPA =
            QPermitDecisionSpeciesAmount.permitDecisionSpeciesAmount;

    @Resource
    private HarvestPermitApplicationRepository harvestPermitApplicationRepository;

    @Resource
    private UserAuthorizationHelper userAuthorizationHelper;

    @Resource
    private MessageSource messageSource;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Transactional(readOnly = true)
    public HarvestPermitApplicationSearchExcelView export(final HarvestPermitApplicationSearchDTO dto,
                                                          final Locale locale) {
        userAuthorizationHelper.assertCoordinatorAnywhereOrModerator();

        final List<HarvestPermitApplication> results = harvestPermitApplicationRepository.search(dto);

        return new HarvestPermitApplicationSearchExcelView(
                transform(results, dto.getGameSpeciesCode()),
                new EnumLocaliser(messageSource, locale));
    }

    private List<HarvestPermitApplicationExcelResultDTO> transform(final List<HarvestPermitApplication> applications,
                                                                   final Integer gameSpeciesCode) {
        final Map<Long, String> personMap = collectContactPersonsByApplication(applications);
        final Map<Long, PermitDecision> decisionMap = collectDecisionsByApplications(applications);
        final Map<Long, String> handlerMap = collectHandlersByApplications(applications);
        final Map<Long, Set<ProtectedAreaType>> areaTypesMap = collectAreaTypesByDecisions(decisionMap.values());
        final Map<Long, Set<PermitDecisionDerogationReasonType>> derogationReasonsMap =
                collectDerogationReasonsByDecisions(decisionMap.values());
        final Map<Long, Set<ForbiddenMethodType>> forbiddenMethodsMap =
                collectForbiddenMethodsByDecisions(decisionMap.values());
        final Map<Long, OrganisationTuple> organisationMap = collectOrganisationsByApplications(applications);

        final Map<Long, List<GameSpecies>> appliedGameSpecies = collectSpeciesByApplications(applications, gameSpeciesCode);
        final Map<Long, Map<Integer, HarvestPermitApplicationSpeciesAmountDTO>> appliedAmounts =
                collectAppliedAmountsByApplications(applications, gameSpeciesCode);
        final Map<Long, Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>>> grantedAmounts =
                collectGrantedAmountsByDecisions(decisionMap.values(), gameSpeciesCode);

        return F.mapNonNullsToList(applications, application -> {
            final HarvestPermitApplicationExcelResultDTO.Builder builder =
                    HarvestPermitApplicationExcelResultDTO.builder();
            final OrganisationTuple organisationTuple = organisationMap.get(application.getId());

            // Fields from application
            builder.withApplicationNumber(application.getApplicationNumber())
                    .withApplicationYear(application.getApplicationYear())
                    .withRkaName(organisationTuple.getRkaName())
                    .withRhyName(organisationTuple.getRhyName())
                    .withSubmitDate(DateUtil.toLocalDateTimeNullSafe(application.getSubmitDate()))
                    .withHarvestPermitCategory(application.getHarvestPermitCategory())
                    .withContactPerson(personMap.get(application.getId()))
                    .withPermitHolder(PermitHolderDTO.createFrom(application.getPermitHolder()))
                    .withHandler(handlerMap.get(application.getId()))
                    .withStatus(application.getStatus())
                    .withAppliedSpeciesAmountsBySpecies(appliedAmounts.get(application.getId()));

            // Fields from decision
            Optional.ofNullable(decisionMap.get(application.getId())).ifPresent(decision -> {
                builder.withPermitTypeCode(decision.getPermitTypeCode())
                        .withDecisionStatus(decision.getStatus())
                        .withDecisionPublishDate(DateUtil.toLocalDateTimeNullSafe(decision.getPublishDate()))
                        .withDecisionType(decision.getDecisionType())
                        .withGrantStatus(decision.getGrantStatus())
                        .withAppealStatus(decision.getAppealStatus())
                        .withPermitSpeciesAmountsBySpecies(grantedAmounts.get(application.getId()));
            });

            builder.withGameSpecies(GameSpeciesDTO.transformList(appliedGameSpecies.getOrDefault(application.getId(), emptyList())))
                    .withProtectedAreaTypes(areaTypesMap.getOrDefault(application.getId(), emptySet()))
                    .withDecisionDerogationReasonTypes(derogationReasonsMap.getOrDefault(application.getId(),
                            emptySet()))
                    .withForbiddenMethodTypes(forbiddenMethodsMap.getOrDefault(application.getId(), emptySet()));
            return builder.build();
        });
    }

    private Map<Long, Set<ForbiddenMethodType>> collectForbiddenMethodsByDecisions(final Collection<PermitDecision> decisions) {
        return jpqlQueryFactory.from(METHOD)
                .innerJoin(METHOD.permitDecision, DECISION)
                .where(DECISION.in(decisions))
                .transform(groupBy(DECISION.application.id).as(set(METHOD.method)));
    }

    private Map<Long, Set<PermitDecisionDerogationReasonType>> collectDerogationReasonsByDecisions(final Collection<PermitDecision> decisions) {
        return jpqlQueryFactory.from(DEROGATION_REASON)
                .innerJoin(DEROGATION_REASON.permitDecision, DECISION)
                .where(DECISION.in(decisions))
                .transform(groupBy(DECISION.application.id).as(set(DEROGATION_REASON.reasonType)));
    }

    private Map<Long, Set<ProtectedAreaType>> collectAreaTypesByDecisions(final Collection<PermitDecision> decisions) {
        return jpqlQueryFactory.from(AREA_TYPE)
                .innerJoin(AREA_TYPE.permitDecision, DECISION)
                .where(AREA_TYPE.permitDecision.in(decisions))
                .transform(groupBy(DECISION.application.id).as(set(AREA_TYPE.protectedAreaType)));
    }

    private Map<Long, List<GameSpecies>> collectSpeciesByApplications(final List<HarvestPermitApplication> applications,
                                                                      final Integer gameSpeciesCode) {
        final Predicate speciesPredicate = F.mapNullable(gameSpeciesCode, code -> SPECIES.officialCode.eq(code));
        return jpqlQueryFactory.from(APPLICATION_SPA)
                .innerJoin(APPLICATION_SPA.gameSpecies, SPECIES)
                .where(APPLICATION_SPA.harvestPermitApplication.in(applications))
                .where(speciesPredicate)
                .transform(groupBy(APPLICATION_SPA.harvestPermitApplication.id).as(GroupBy.list(SPECIES)));
    }

    private Map<Long, Map<Integer, HarvestPermitApplicationSpeciesAmountDTO>> collectAppliedAmountsByApplications(final List<HarvestPermitApplication> applications,
                                                                                                                  final Integer gameSpeciesCode) {
        final Predicate speciesPredicate = F.mapNullable(gameSpeciesCode, code -> SPECIES.officialCode.eq(code));
        return jpqlQueryFactory.from(APPLICATION_SPA)
                .innerJoin(APPLICATION_SPA.gameSpecies, SPECIES)
                .where(APPLICATION_SPA.harvestPermitApplication.in(applications))
                .where(speciesPredicate)
                .transform(groupBy(APPLICATION_SPA.harvestPermitApplication.id)
                        .as(GroupBy.map(
                                SPECIES.officialCode,
                                Projections.constructor(HarvestPermitApplicationSpeciesAmountDTO.class, APPLICATION_SPA, SPECIES))));
    }

    /* Package private for testing */
    Map<Long, Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>>> collectGrantedAmountsByDecisions(final Collection<PermitDecision> decisions,
                                                                                                                      final Integer gameSpeciesCode) {
        final Predicate speciesPredicate = F.mapNullable(gameSpeciesCode, code -> SPECIES.officialCode.eq(code));
        return jpqlQueryFactory.from(DECISION_SPA)
                .innerJoin(DECISION_SPA.permitDecision, DECISION)
                .innerJoin(DECISION_SPA.gameSpecies, SPECIES)
                .innerJoin(DECISION.application, APPLICATION)
                .where(DECISION.in(decisions))
                .where(speciesPredicate)
                .transform(groupBy(APPLICATION.id).as(
                        GroupBy.map(
                                SPECIES.officialCode,
                                GroupBy.map(
                                        DECISION_SPA.beginDate.year(),
                                        Projections.constructor(ApplicationSearchDecisionSpeciesAmountDTO.class, DECISION_SPA, SPECIES)))));

    }

    private Map<Long, String> collectContactPersonsByApplication(final List<HarvestPermitApplication> applications) {
        return jpqlQueryFactory.from(APPLICATION)
                .innerJoin(APPLICATION.contactPerson, CONTACT_PERSON)
                .where(APPLICATION.in(applications))
                .transform(groupBy(APPLICATION.id).as(CONTACT_PERSON.firstName.concat(" ").concat(CONTACT_PERSON.lastName)));
    }

    private Map<Long, String> collectHandlersByApplications(final List<HarvestPermitApplication> applications) {
        return jpqlQueryFactory.from(DECISION)
                .innerJoin(DECISION.handler, HANDLER_USER)
                .where(DECISION.application.in(applications))
                .transform(groupBy(DECISION.application.id).as(HANDLER_USER.firstName.concat(" ").concat(HANDLER_USER.lastName)));
    }

    private Map<Long, PermitDecision> collectDecisionsByApplications(final List<HarvestPermitApplication> applications) {

        return jpqlQueryFactory.from(DECISION)
                .where(DECISION.application.in(applications))
                .transform(groupBy(DECISION.application.id).as(DECISION));
    }

    private Map<Long, OrganisationTuple> collectOrganisationsByApplications(final List<HarvestPermitApplication> applications) {

        return jpqlQueryFactory.from(APPLICATION)
                .innerJoin(APPLICATION.rhy, RHY)
                .innerJoin(RHY.parentOrganisation, RKA._super)
                .where(APPLICATION.in(applications))
                .transform(groupBy(APPLICATION.id).as(RKA.nameFinnish, RKA.nameSwedish, RHY.nameFinnish,
                        RHY.nameSwedish))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> OrganisationTuple.from(e.getValue())));
    }

    private static class OrganisationTuple {
        private final LocalisedString rkaName;
        private final LocalisedString rhyName;

        private static OrganisationTuple from(Group group) {
            return new OrganisationTuple(
                    LocalisedString.of(group.getOne(RKA.nameFinnish),
                            group.getOne(RKA.nameSwedish)),
                    LocalisedString.of(group.getOne(RHY.nameFinnish), group.getOne(RHY.nameSwedish)));
        }

        public OrganisationTuple(final LocalisedString rkaName, final LocalisedString rhyName) {
            this.rkaName = rkaName;
            this.rhyName = rhyName;
        }

        public LocalisedString getRkaName() {
            return rkaName;
        }

        public LocalisedString getRhyName() {
            return rhyName;
        }
    }

}
