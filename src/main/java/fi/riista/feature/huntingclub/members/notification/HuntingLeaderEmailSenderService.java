package fi.riista.feature.huntingclub.members.notification;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.HuntingClubRepository;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.EmailResolver;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysEmailService;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.F;
import org.joda.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static fi.riista.feature.organization.occupation.OccupationType.SEURAN_YHDYSHENKILO;
import static fi.riista.util.Collect.idSet;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Component
public class HuntingLeaderEmailSenderService {

    static class MailData {
        public final Set<String> emailAddresses;
        public final Organisation club;
        public final Organisation rhy;
        public final List<GroupEmailDTO> groupRows;

        public MailData(final Set<String> emailAddresses,
                        final Organisation club,
                        final Organisation rhy,
                        final List<GroupEmailDTO> groupRows) {

            this.emailAddresses = emailAddresses;
            this.club = club;
            this.rhy = rhy;
            this.groupRows = groupRows;
        }
    }

    private static final String EMAIL_TEMPLATE = "email_hunting_leaders_changed";
    private static final String EMAIL_TEMPLATE_SV = "email_hunting_leaders_changed.sv";

    private static final Comparator<GroupEmailDTO> GROUP_ORDERING =
            comparing(GroupEmailDTO::getHuntingYear).reversed()
                    .thenComparing(GroupEmailDTO::getPermitNumber)
                    .thenComparing(GroupEmailDTO::getNameFinnish);

    @Resource
    private HuntingClubRepository huntingClubRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private HuntingLeaderChangeNotificationQueries queries;

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private RiistanhoitoyhdistysEmailService riistanhoitoyhdistysEmailService;

    @Resource
    private EmailResolver emailResolver;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<MailData> sendMails(final List<Occupation> changedLeaders) {
        if (changedLeaders.isEmpty()) {
            return Collections.emptyList();
        }

        final List<GroupEmailDTO> groups = getHuntingGroups(changedLeaders);

        final Set<Long> uniqueClubIds = groups.stream().map(GroupEmailDTO::getClubId).collect(toSet());
        final Map<Long, HuntingClub> clubIndex = F.indexById(huntingClubRepository.findAllById(uniqueClubIds));

        final Set<Long> uniqueRhyIds = groups.stream().map(GroupEmailDTO::getRhyId).collect(toSet());
        final Map<Long, Riistanhoitoyhdistys> rhyIndex = F.indexById(rhyRepository.findAllById(uniqueRhyIds));

        final Map<HuntingClub, List<GroupEmailDTO>> groupsByClubId =
                groups.stream().collect(groupingBy(dto -> clubIndex.get(dto.getClubId())));

        final List<MailData> mailData = groupsByClubId.entrySet()
                .stream()
                .flatMap(e -> generateMailData(e.getKey(), rhyIndex, e.getValue()))
                .collect(toList());

        mailData.forEach(r -> sendMails(r.emailAddresses, r.club, r.groupRows));

        return mailData;
    }

    private List<GroupEmailDTO> getHuntingGroups(final List<Occupation> changedLeaders) {
        final Set<Long> clubIds = changedLeaders.stream()
                .map(occ -> occ.getOrganisation().getParentOrganisation())
                .collect(idSet());

        return queries.resolveHuntingGroups(queries.getGroupLeadersOfCurrentAndFutureHuntingYears(clubIds));
    }

    private Stream<? extends MailData> generateMailData(final Organisation club,
                                                        final Map<Long, Riistanhoitoyhdistys> rhyIndex,
                                                        final List<GroupEmailDTO> groups) {

        final Map<Long, List<GroupEmailDTO>> groupsByRhyId =
                groups.stream().sorted(GROUP_ORDERING).collect(groupingBy(GroupEmailDTO::getRhyId));

        return groupsByRhyId.entrySet()
                .stream()
                .map(entry -> {
                    final Long rhyId = entry.getKey();
                    final Riistanhoitoyhdistys rhy = rhyIndex.get(rhyId);

                    final Set<String> allEmails = Sets.union(
                            riistanhoitoyhdistysEmailService.resolveEmails(rhy),
                            emailResolver.findEmailsOfOccupiedPersons(club, SEURAN_YHDYSHENKILO));

                    return new MailData(allEmails, club, rhy, entry.getValue());
                });
    }

    private void sendMails(final Set<String> coordinatorEmails,
                           final Organisation club,
                           final List<GroupEmailDTO> groupRows) {

        final Map<String, Object> model = ImmutableMap.<String, Object>builder()
                .put("club", club.getNameFinnish())
                .put("clubCustomerNumber", club.getOfficialCode())
                .put("groups", groupRows)
                .build();

        mailService.send(MailMessageDTO.builder()
                .withFrom(mailService.getDefaultFromAddress())
                .withRecipients(coordinatorEmails)
                .withSubject(String.format("Metsästyksenjohtajailmoitus (%s)", club.getNameFinnish()))
                .withScheduledTimeAfter(Duration.standardHours(1))
                .appendHandlebarsBody(handlebars, EMAIL_TEMPLATE, model)
                .appendBody("\n<hr>\n")
                .appendHandlebarsBody(handlebars, EMAIL_TEMPLATE_SV, model)
                .build());
    }
}
