package fi.riista.feature.huntingclub.members.notification;

import com.github.jknack.handlebars.Handlebars;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.mail.MailMessageDTO;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.EmailResolver;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
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

import static fi.riista.util.Collect.idSet;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class HuntingLeaderEmailSenderService {

    static class MailData {
        public final Set<String> emailAddresses;
        public final Organisation club;
        public final Organisation rhy;
        public final List<GroupEmailDto> groupRows;

        public MailData(Set<String> emailAddresses, Organisation club, Organisation rhy, List<GroupEmailDto> groupRows) {
            this.emailAddresses = emailAddresses;
            this.club = club;
            this.rhy = rhy;
            this.groupRows = groupRows;
        }
    }

    private static final String EMAIL_TEMPLATE = "email_hunting_leaders_changed";
    private static final String EMAIL_TEMPLATE_SV = "email_hunting_leaders_changed.sv";

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Resource
    private HuntingClubGroupRepository huntingClubGroupRepository;

    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private EmailResolver emailResolver;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<MailData> sendMails(final List<Occupation> changedLeaders) {
        if (changedLeaders.isEmpty()) {
            return Collections.emptyList();
        }

        final List<Occupation> allGroupLeaders = getGroupLeadersOfCurrentAndFutureHuntingYears(changedLeaders);
        final Map<Long, HuntingClubGroup> groupsById = getGroupsByIdFromGroupsOccupations(allGroupLeaders);
        final Map<Organisation, Map<HuntingClubGroup, List<Occupation>>> clubToGroupsToOccupations =
                allGroupLeaders.stream().collect(groupingBy(
                        o -> o.getOrganisation().getParentOrganisation(),
                        groupingBy(o -> groupsById.get(o.getOrganisation().getId()))));
        final List<MailData> mailData = clubToGroupsToOccupations.entrySet().stream()
                .flatMap(e -> generateMailData(e.getKey(), e.getValue()))
                .collect(toList());
        mailData.forEach(r -> sendMails(r.emailAddresses, r.club, r.groupRows));
        return mailData;
    }

    private Stream<? extends MailData> generateMailData(final Organisation club,
                                                        final Map<HuntingClubGroup, List<Occupation>> occupationsByGroup) {

        final Map<Riistanhoitoyhdistys, List<Map.Entry<HuntingClubGroup, List<Occupation>>>> groupsByRhy =
                occupationsByGroup.entrySet().stream().collect(groupingBy(o -> o.getKey().getHarvestPermit().getRhy()));

        return groupsByRhy.entrySet().stream().map(e -> generateMailData(club, e.getKey(), e.getValue()));
    }

    private MailData generateMailData(final Organisation club,
                                      final Riistanhoitoyhdistys rhy,
                                      final List<Map.Entry<HuntingClubGroup, List<Occupation>>> groupAndOccupations) {

        final List<GroupEmailDto> groupEmailRows = groupAndOccupations.stream()
                .map(e -> createDto(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(GroupEmailDto::getNameFinnish))
                .collect(toList());

        final Set<String> allEmails = Sets.union(emailResolver.findRhyContactEmails(rhy), emailResolver.findClubContactEmails(club));
        return new MailData(allEmails, club, rhy, groupEmailRows);
    }

    private void sendMails(final Set<String> coordinatorEmails,
                           final Organisation club,
                           final List<GroupEmailDto> groupRows) {

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

    private static GroupEmailDto createDto(final HuntingClubGroup group,
                                           final List<Occupation> leaders) {

        final String permitNumber = group.getHarvestPermit() != null ? group.getHarvestPermit().getPermitNumber() : null;
        return new GroupEmailDto(group.getNameLocalisation(), group.getSpecies().getNameLocalisation(), permitNumber,
                leaders.stream()
                        .map(LeaderEmailDto::new)
                        .sorted(Comparator.comparing(LeaderEmailDto::getOrder))
                        .collect(toList())
        );
    }

    private List<Occupation> getGroupLeadersOfCurrentAndFutureHuntingYears(final List<Occupation> changedLeaders) {
        final Set<Long> clubIds = changedLeaders.stream()
                .map(occ -> occ.getOrganisation().getParentOrganisation())
                .collect(idSet());

        final QOccupation occupation = QOccupation.occupation;
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final int currentHuntingYear = DateUtil.huntingYear();

        return jpqlQueryFactory.selectFrom(occupation)
                .join(occupation.organisation, group._super)
                .where(occupation.organisation.parentOrganisation.id.in(clubIds),
                        occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA),
                        occupation.validAndNotDeleted(),
                        group.harvestPermit.isNotNull(),
                        group.huntingYear.goe(currentHuntingYear)
                ).fetch();
    }

    private Map<Long, HuntingClubGroup> getGroupsByIdFromGroupsOccupations(final List<Occupation> allGroupsOccupations) {
        final Set<Long> groupIds = allGroupsOccupations.stream().map(Occupation::getOrganisation).collect(idSet());
        return F.indexById(huntingClubGroupRepository.findAll(groupIds));
    }
}
