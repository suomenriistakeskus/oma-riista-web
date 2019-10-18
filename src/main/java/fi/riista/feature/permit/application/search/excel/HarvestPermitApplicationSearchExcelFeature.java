package fi.riista.feature.permit.application.search.excel;

import com.querydsl.core.group.Group;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.gamediary.QGameSpecies;
import fi.riista.feature.organization.QRiistakeskuksenAlue;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.feature.organization.rhy.QRiistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationRepository;
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
import static java.util.Collections.emptySet;

@Component
public class HarvestPermitApplicationSearchExcelFeature {
    private static QHarvestPermitApplication APPLICATION = QHarvestPermitApplication.harvestPermitApplication;
    private static QPermitDecision DECISION = QPermitDecision.permitDecision;
    private static QPerson CONTACT_PERSON = new QPerson("contactPerson");
    private static QSystemUser HANDLER_USER = QSystemUser.systemUser;
    private static QHarvestPermitApplicationSpeciesAmount APPLICATION_SPA =
            QHarvestPermitApplicationSpeciesAmount.harvestPermitApplicationSpeciesAmount;
    private static QGameSpecies SPECIES = QGameSpecies.gameSpecies;
    private static QPermitDecisionProtectedAreaType AREA_TYPE =
            QPermitDecisionProtectedAreaType.permitDecisionProtectedAreaType;
    private static QPermitDecisionDerogationReason DEROGATION_REASON =
            QPermitDecisionDerogationReason.permitDecisionDerogationReason;
    private static QPermitDecisionForbiddenMethod METHOD = QPermitDecisionForbiddenMethod.permitDecisionForbiddenMethod;
    private static QRiistanhoitoyhdistys RHY = QRiistanhoitoyhdistys.riistanhoitoyhdistys;
    private static QRiistakeskuksenAlue RKA = QRiistakeskuksenAlue.riistakeskuksenAlue;

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

        return new HarvestPermitApplicationSearchExcelView(transform(results), new EnumLocaliser(messageSource,
                locale));
    }

    private List<HarvestPermitApplicationExcelResultDTO> transform(List<HarvestPermitApplication> applications) {
        final Map<Long, String> personMap = collectContactPersonsByApplication(applications);
        final Map<Long, PermitDecision> decisionMap = collectDecisionsByApplications(applications);
        final Map<Long, String> handlerMap = collectHandlersByApplications(applications);
        final Map<Long, Set<LocalisedString>> speciesMap = collectSpeciesByApplications(applications);
        final Map<Long, Set<ProtectedAreaType>> areaTypesMap = collectAreaTypesByDecisions(decisionMap.values());
        final Map<Long, Set<PermitDecisionDerogationReasonType>> derogationReasonsMap =
                collectDerogationReasonsByDecisions(decisionMap.values());
        final Map<Long, Set<ForbiddenMethodType>> forbiddenMethodsMap =
                collectForbiddenMethodsByDecisions(decisionMap.values());
        final Map<Long, OrganisationTuple> organisationMap = collectOrganisationsByApplications(applications);


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
                    .withStatus(application.getStatus());

            // Fields from decision
            Optional.ofNullable(decisionMap.get(application.getId())).ifPresent(decision -> {
                builder.withDecisionStatus(decision.getStatus())
                        .withDecisionType(decision.getDecisionType())
                        .withGrantStatus(decision.getGrantStatus())
                        .withAppealStatus(decision.getAppealStatus());
            });

            builder.withGameSpeciesNames(speciesMap.getOrDefault(application.getId(), emptySet()))
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

    private Map<Long, Set<LocalisedString>> collectSpeciesByApplications(final List<HarvestPermitApplication> applications) {
        return jpqlQueryFactory.from(APPLICATION_SPA)
                .innerJoin(APPLICATION_SPA.gameSpecies, SPECIES)
                .where(APPLICATION_SPA.harvestPermitApplication.in(applications))
                .transform(groupBy(APPLICATION_SPA.harvestPermitApplication.id).as(set(SPECIES.nameLocalisation())));
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
