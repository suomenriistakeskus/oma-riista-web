package fi.riista.feature.gamediary.todo;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestDTOTransformer;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.gamediary.GameDiarySpecs.authorAndRejectedForPermit;
import static fi.riista.feature.gamediary.GameDiarySpecs.authorOrShooterAndHarvestReportRequiredAndMissing;
import static fi.riista.feature.gamediary.GameDiarySpecs.permitContactPersonAndProposedToPermit;

@Component
public class GameDiaryTodoFeature {

    @Resource
    private ActiveUserService activeUserService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestDTOTransformer harvestDtoTransformer;

    @Transactional(readOnly = true)
    public GameDiaryTodoHarvestDTO listAllHarvestsRequiringAction(Long personId) {
        final Person person = personId != null && activeUserService.isModeratorOrAdmin()
                ? requireEntityService.requirePerson(personId, EntityPermission.READ)
                : activeUserService.requireActivePerson();

        // Kirjaaja: saalisilmoitus tekemättä
        final List<Harvest> requireReport = harvestRepository.findAll(
                authorOrShooterAndHarvestReportRequiredAndMissing(person));

        // Kirjaaja: listattava saaliit jotka hylätty luvasta
        final List<Harvest> rejectedForPermit = harvestRepository.findAll(
                authorAndRejectedForPermit(person));

        // Luvan yhteyshenkilö: listattava saaliit jotka odottavat hyväksyntää lupaan
        final List<Harvest> permitContactPersonWithoutDecision = harvestRepository.findAll(
                permitContactPersonAndProposedToPermit(person));

        return GameDiaryTodoHarvestDTO.create(harvestDtoTransformer)
                .withReportRequired(requireReport)
                .withRejectedFromPermit(rejectedForPermit)
                .withPendingApprovalToPermit(permitContactPersonWithoutDecision)
                .build();
    }

}
