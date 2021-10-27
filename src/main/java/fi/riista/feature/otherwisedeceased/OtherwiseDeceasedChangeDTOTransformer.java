package fi.riista.feature.otherwisedeceased;

import com.querydsl.core.types.dsl.BooleanExpression;
import fi.riista.feature.account.user.QSystemUser;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Component
public class OtherwiseDeceasedChangeDTOTransformer extends ListTransformer<OtherwiseDeceasedChange, OtherwiseDeceasedChangeDTO> {

    @Resource
    private UserRepository userRepository;

    @Nonnull
    @Override
    public List<OtherwiseDeceasedChangeDTO> transform(@Nonnull final List<OtherwiseDeceasedChange> entities) {
        final Map<Long, SystemUser> userMap = mapUserById(entities);
        return createDTOList(entities, userMap);
    }

    public Map<Long, List<OtherwiseDeceasedChangeDTO>> transform(@Nonnull final Map<Long, List<OtherwiseDeceasedChange>> entityMap) {
        final List<Long> userIds = entityMap.values()
                .stream()
                .flatMap(e -> e.stream())
                .map(OtherwiseDeceasedChange::getUserId)
                .distinct()
                .collect(toList());

        final Map<Long, SystemUser> userMap = F.indexById(userRepository.findAllById(userIds));
        final Map<Long, List<OtherwiseDeceasedChangeDTO>> resultMap = new HashMap<>();
        entityMap.forEach((k, v) -> resultMap.put(k, createDTOList(v, userMap)));
        return resultMap;
    }

    private List<OtherwiseDeceasedChangeDTO> createDTOList(final List<OtherwiseDeceasedChange> entities, final Map<Long, SystemUser> userMap) {
        return entities.stream()
                .map(e -> OtherwiseDeceasedChangeDTO.create(e, userMap.get(e.getUserId())))
                .collect(toList());
    }

    private Map<Long, SystemUser> mapUserById(final List<OtherwiseDeceasedChange> entities) {

        final Set<Long> ids = entities.stream()
                .map(OtherwiseDeceasedChange::getUserId)
                .collect(toSet());
        final QSystemUser USER = QSystemUser.systemUser;
        final BooleanExpression predicate = USER.id.in(ids);

        return userRepository.findAllAsStream(predicate)
                .collect(toMap(SystemUser::getId, item -> item));
    }
}
