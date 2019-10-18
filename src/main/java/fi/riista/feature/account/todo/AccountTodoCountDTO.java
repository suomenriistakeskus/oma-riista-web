package fi.riista.feature.account.todo;

public class AccountTodoCountDTO {

    private final long todoCount;

    public AccountTodoCountDTO(final long todoCount) {
        this.todoCount = todoCount;
    }

    public long getTodoCount() {
        return todoCount;
    }
}
