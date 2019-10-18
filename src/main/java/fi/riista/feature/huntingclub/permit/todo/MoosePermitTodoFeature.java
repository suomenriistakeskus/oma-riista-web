package fi.riista.feature.huntingclub.permit.todo;

import fi.riista.feature.RequireEntityService;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.security.EntityPermission;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class MoosePermitTodoFeature {

    @Resource
    private RequireEntityService entityService;

    @Resource
    private MoosePermitTodoService moosePermitTodoService;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Transactional(readOnly = true)
    public MoosePermitTodoDTO listTodosForClub(final long clubId, final int year) {
        if (harvestPermitLockedByDateService.isDateLockedForHuntingYear(year)) {
            return MoosePermitTodoDTO.noTodo(clubId);
        }

        final HuntingClub huntingClub = entityService.requireHuntingClub(clubId, EntityPermission.NONE);

        return moosePermitTodoService.listTodosForClub(huntingClub, year);
    }
}
