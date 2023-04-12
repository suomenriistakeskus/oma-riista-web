package fi.riista.feature.huntingclub.search;

import fi.riista.feature.huntingclub.HuntingClubRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.springframework.util.StringUtils.hasText;

@Service
public class HuntingClubSearchFeature {

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Nonnull
    @Transactional(readOnly = true)
    public HuntingClubNameDTO findNameById(final @Nonnull Long huntingClubId) {

        return Optional.ofNullable(huntingClubRepository
                .getOne(huntingClubId))
                .map(HuntingClubNameDTO::create)
                .orElseThrow(() -> HuntingClubNotFoundException.byId(huntingClubId));
    }

    @Nonnull
    @Transactional(readOnly = true)
    public HuntingClubNameDTO findNameByOfficialCode(final String officialCode) {
        checkArgument(hasText(officialCode), "empty officialCode");

        return Optional.ofNullable(huntingClubRepository
                        .findByOfficialCode(officialCode))
                .map(HuntingClubNameDTO::create)
                .orElseThrow(() -> HuntingClubNotFoundException.byOfficialCode(officialCode));
    }
}
