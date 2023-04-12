package fi.riista.feature.huntingclub.group;

import com.github.jknack.handlebars.Handlebars;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.mail.MailService;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.util.DateUtil;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class HuntingClubGroupLeaderEmailService {
    @Resource
    private MailService mailService;

    @Resource
    private Handlebars handlebars;

    @Resource
    private MessageSource messageSource;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void sendGroupLeaderNotificationEmail(final Occupation occupation,
                                                 final HarvestPermit permit,
                                                 final Organisation club,
                                                 final HuntingClubGroup group) {
        if (occupation.getOccupationType() == OccupationType.RYHMAN_METSASTYKSENJOHTAJA &&
                group.getHuntingYear() >= DateUtil.huntingYear()) {

            mailService.send(new HuntingClubGroupLeaderEmail(handlebars, messageSource)
                    .withRecipient(occupation.getPerson().getEmail())
                    .withHuntingClubName(club.getNameLocalisation())
                    .withPermitNumber(permit.getPermitNumber())
                    .withHuntingGroupName(group.getNameLocalisation())
                    .withSpeciesName(group.getSpecies().getNameLocalisation())
                    .build(mailService.getDefaultFromAddress()));
        }

    }
}
