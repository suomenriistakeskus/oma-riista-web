package fi.riista.feature.organization.rhy.gamedamageinspection;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Component
public class GameDamageInspectionKmExpensesService {

    @Resource
    private GameDamageInspectionKmExpensesRepository gameDamageInspectionKmExpensesRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void addGameDamageInspectionKmExpenses(final GameDamageInspectionEvent event,
                                                  final List<GameDamageInspectionKmExpenseDTO> expenses) {
        expenses.forEach(expense ->
                gameDamageInspectionKmExpensesRepository.save(
                        new GameDamageInspectionKmExpense(event,
                                expense.getKilometers(),
                                expense.getExpenseUnit(),
                                expense.getExpenseType(),
                                expense.getAdditionalInfo())));
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void updateGameDamageInspectionKmExpenses(final GameDamageInspectionEvent event,
                                                     final List<GameDamageInspectionKmExpenseDTO> expenses) {
        deleteGameDamageInspectionKmExpenses(event);

        if (expenses != null) {
            addGameDamageInspectionKmExpenses(event, expenses);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void deleteGameDamageInspectionKmExpenses(final GameDamageInspectionEvent event) {
        gameDamageInspectionKmExpensesRepository.deleteByGameDamageInspectionEvent(event);
    }

}
