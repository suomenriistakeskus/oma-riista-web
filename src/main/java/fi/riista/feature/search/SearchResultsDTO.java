package fi.riista.feature.search;


import fi.riista.util.F;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class SearchResultsDTO {
    public static Result createResult(Long id, String description) {
        return new Result(id, description);
    }

    // Results grouped by category
    private final Map<SearchResultType, Collection<Result>> results;

    public SearchResultsDTO() {
        this.results = new LinkedHashMap<>();
    }

    public <T> void addResults(@Nonnull SearchResultType resultType,
                               @Nonnull List<? extends T> results,
                               @Nonnull Function<T, Result> transformer) {
        Objects.requireNonNull(resultType, "Null is not a valid type for search results");
        Objects.requireNonNull(results, "Null passed as result type");
        Objects.requireNonNull(transformer);

        if (!results.isEmpty()) {
            this.results.put(resultType, F.mapNonNullsToList(results, transformer));
        }
    }

    public Map<SearchResultType, Collection<Result>> getResults() {
        return results;
    }

    public static class Result {
        private final Long id;
        private final String description;

        public Result(final Long id, final String description) {
            this.id = Objects.requireNonNull(id);
            this.description = Objects.requireNonNull(description);
        }

        public Long getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }
    }
}
