package fi.riista.feature.permit.decision;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.action.PermitDecisionAction;
import fi.riista.feature.permit.decision.action.PermitDecisionActionRepository;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDelivery;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryRepository;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDeliveryService;
import fi.riista.feature.permit.decision.document.PermitDecisionTextService;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmountRepository;
import fi.riista.security.EntityPermission;
import fi.riista.util.DateUtil;
import fi.riista.util.Locales;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
public class PermitDecisionFeature {

    @Resource
    private PermitDecisionRepository permitDecisionRepository;

    @Resource
    private PermitDecisionSpeciesAmountRepository permitDecisionSpeciesAmountRepository;

    @Resource
    private PermitDecisionActionRepository permitDecisionActionRepository;

    @Resource
    private PermitDecisionTextService permitDecisionTextService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private PermitDecisionDeliveryService permitDecisionDeliveryService;

    @Resource
    private PermitDecisionDeliveryRepository permitDecisionDeliveryRepository;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    @Transactional(readOnly = true)
    public PermitDecisionDTO getDecision(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.READ);
        final PermitDecisionDocument htmlDocument = decision.getDocument() != null
                ? PermitDecisionDocumentTransformer.MARKDOWN_TO_HTML.copy(decision.getDocument())
                : new PermitDecisionDocument();
        final List<HarvestPermit> permitList = harvestPermitRepository.findByPermitDecision(decision);
        final HarvestPermit harvestPermit = permitList.size() == 1 ? permitList.get(0) : null;

        return PermitDecisionDTO.create(decision, htmlDocument, harvestPermit,
                decision.isHandler(activeUserService.requireActiveUser()));
    }

    @Transactional(readOnly = true)
    public Long getDecisionApplicationId(final long id) {
        return permitDecisionRepository.getOne(id).getApplication().getId();
    }

    @Transactional
    public Long getOrCreateDecisionForApplication(final CreatePermitDecisionDTO dto) {
        final HarvestPermitApplication application = requireEntityService.requireHarvestPermitApplication(
                dto.getApplicationId(), EntityPermission.READ);

        final PermitDecision decision = permitDecisionRepository.findOneByApplication(application);

        return decision != null ? decision.getId() : createDecision(application).getId();
    }

    private PermitDecision createDecision(final HarvestPermitApplication application) {
        final PermitDecision decision = new PermitDecision();
        decision.setApplication(application);
        decision.setRhy(application.getRhy());
        decision.setContactPerson(application.getContactPerson());
        decision.setPermitHolder(application.getPermitHolder());
        decision.setHta(application.getArea().findLargestHta().orElse(null));
        decision.setPaymentAmount(PermitDecision.DECISION_PRICE_MOOSELIKE);
        decision.setLocale(Locales.getLocaleByLanguageCode(application.getContactPerson().getLanguageCode()));

        permitDecisionRepository.save(decision);

        final List<PermitDecisionSpeciesAmount> decisionSpeciesAmounts = new LinkedList<>();

        for (final HarvestPermitApplicationSpeciesAmount source : application.getSpeciesAmounts()) {
            final PermitDecisionSpeciesAmount target = new PermitDecisionSpeciesAmount();
            target.setPermitDecision(decision);
            target.setGameSpecies(source.getGameSpecies());
            target.setAmount(source.getAmount());

            final int huntingYear = application.getHuntingYear();
            target.setBeginDate(HarvestPermit.getDefaultMooselikeBeginDate(huntingYear));
            target.setEndDate(HarvestPermit.getDefaultMooselikeEndDate(huntingYear));

            decisionSpeciesAmounts.add(permitDecisionSpeciesAmountRepository.save(target));
        }

        decision.setSpeciesAmounts(decisionSpeciesAmounts);

        final List<PermitDecisionDelivery> deliveries = permitDecisionDeliveryService.generateDeliveries(decision, Collections.emptyList());
        decision.setDelivery(deliveries);
        permitDecisionDeliveryRepository.save(deliveries);

        permitDecisionTextService.fillInBlanks(decision);

        decision.updateGrantStatus();

        return decision;
    }

    @Transactional
    public void assignApplication(final long id) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(id, EntityPermission.UPDATE);

        if (decision.getHandler() == null) {
            // First handler -> Create default action text for decision
            final PermitDecisionAction action = new PermitDecisionAction();
            action.setPermitDecision(decision);
            action.setPointOfTime(DateUtil.now());
            action.setActionType(PermitDecisionAction.ActionType.KUULEMINEN);
            action.setText(Locales.isSwedish(decision.getLocale())
                    ? "Finlands viltcentral har med stöd av jaktlagen 26 § 2 moment hört regionala intressegrupper" +
                    " för att trafikskador och skador på jordbruk och skog ska bli beaktade."
                    : "Suomen riistakeskus on kuullut metsästyslain 26 §:n 2 momentin  nojalla liikenne-, maatalous-" +
                    " ja metsävahinkojen huomioon ottamiseksi alueellisia sidosryhmiä.");
            action.setDecisionText(action.getText());

            permitDecisionActionRepository.save(action);
        }

        // Update text
        decision.getDocument().setProcessing(permitDecisionTextService.generateProcessing(decision));

        // decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.setHandler(activeUserService.requireActiveUser());
    }

    @Transactional(readOnly = true)
    public PermitDecisionPublishSettingsDTO getPublishSettings(final long decisionId) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(decisionId, EntityPermission.UPDATE);
        final PermitDecisionPublishSettingsDTO dto = new PermitDecisionPublishSettingsDTO();
        dto.setLocale(decision.getLocale());

        if (decision.getPublishDate() != null) {
            dto.setPublishDate(decision.getPublishDate().toLocalDate());
            dto.setPublishTime(decision.getPublishDate().toLocalTime());
        }

        return dto;
    }

    @Transactional
    public void updatePublishSettings(final PermitDecisionPublishSettingsDTO dto) {
        final PermitDecision decision = requireEntityService.requirePermitDecision(dto.getDecisionId(), EntityPermission.UPDATE);
        decision.assertStatus(PermitDecision.Status.DRAFT);
        decision.assertHandler(activeUserService.requireActiveUser());

        decision.setPublishDate(dto.getPublishDate().toDateTime(dto.getPublishTime()));
        decision.setLocale(dto.getLocale());

        permitDecisionTextService.fillInBlanks(decision);
    }
}
