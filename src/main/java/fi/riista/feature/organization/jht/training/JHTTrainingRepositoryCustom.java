package fi.riista.feature.organization.jht.training;

import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import org.joda.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface JHTTrainingRepositoryCustom {
    Page<JHTTraining> searchPage(
            @Nonnull Pageable pageRequest,
            @Nonnull JHTTrainingSearchDTO.SearchType searchType,
            @Nonnull OccupationType occupationType,
            @Nullable JHTTraining.TrainingType trainingType,
            @Nullable String trainingLocation,
            @Nullable Riistanhoitoyhdistys rhy,
            @Nullable Person person,
            @Nullable LocalDate beginDate,
            @Nullable LocalDate endDate);
}
