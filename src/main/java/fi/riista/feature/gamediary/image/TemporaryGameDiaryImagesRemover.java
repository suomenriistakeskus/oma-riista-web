package fi.riista.feature.gamediary.image;

import fi.riista.feature.gamediary.GameDiaryEntryType;
import fi.riista.util.F;
import fi.riista.util.jpa.JpaSpecs;
import org.joda.time.DateTime;
import org.joda.time.ReadablePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

@Component
public class TemporaryGameDiaryImagesRemover {

    private static final Logger LOG = LoggerFactory.getLogger(TemporaryGameDiaryImagesRemover.class);

    private static final Specification<GameDiaryImage> SPEC_FOR_IMAGE_NOT_RELATED_TO_ANY_GAME_DIARY_ENTRY =
            JpaSpecs.and(Arrays.stream(GameDiaryEntryType.values())
                    .map(type -> {
                        switch (type) {
                            case HARVEST:
                                return GameDiaryImage_.harvest;
                            case OBSERVATION:
                                return GameDiaryImage_.observation;
                            case SRVA:
                                return GameDiaryImage_.srvaEvent;
                            default:
                                throw new IllegalStateException(String.format(
                                        "Unhandled GameDiaryEntryType: %s", type.name()));
                        }
                    })
                    .map(JpaSpecs::isNull)
                    .collect(Collectors.toList()));

    @Resource
    private GameDiaryImageRepository gameDiaryImageRepository;

    @Transactional
    public void removeExpiredTemporaryImages(final ReadablePeriod expirationTime) {
        final DateTime olderThan = DateTime.now().minus(expirationTime);

        final List<GameDiaryImage> expired = gameDiaryImageRepository.findAll(
                where(SPEC_FOR_IMAGE_NOT_RELATED_TO_ANY_GAME_DIARY_ENTRY)
                        .and(JpaSpecs.creationTimeOlderThan(olderThan)));

        if (expired.size() > 0) {
            LOG.debug("Removing GameDiaryImages ids:" + F.getNonNullIds(expired));
            gameDiaryImageRepository.deleteInBatch(expired);
        }
    }

}
