package fi.riista.feature.push;

import fi.riista.feature.account.user.ActiveUserService;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Component
public class RegisterMobileClientDeviceFeature {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterMobileClientDeviceFeature.class);

    @Resource
    private MobileClientDeviceRepository mobileClientDeviceRepository;

    @Resource
    private ActiveUserService activeUserService;

    @Transactional
    public void registerDevice(final MobilePushRegistrationDTO dto) {
        final String activeUserName = activeUserService.getActiveUsernameOrNull();

        LOG.info("RegisterMobile version:{} platform:{} device:{} username:{}",
                dto.getClientVersion(), dto.getPlatform(), dto.getDeviceName(), activeUserName);

        final MobileClientDevice existingDevice = mobileClientDeviceRepository.findByPushToken(dto.getPushToken());

        if (existingDevice != null) {
            updateDevice(dto, existingDevice);
        } else {
            createDevice(dto);
        }
    }

    private void createDevice(final MobilePushRegistrationDTO dto) {
        final MobileClientDevice newDevice = new MobileClientDevice();
        newDevice.setPerson(activeUserService.requireActivePerson());
        updateDeviceFields(dto, newDevice);

        mobileClientDeviceRepository.save(newDevice);
    }

    private void updateDevice(final MobilePushRegistrationDTO dto,
                              final MobileClientDevice device) {
        final Person activePerson = activeUserService.requireActivePerson();

        if (!activePerson.equals(device.getPerson())) {
            LOG.warn("Person has changed for deviceId={} from personId={} to personId={}",
                    device.getId(), F.getId(device.getPerson()), F.getId(activePerson));
            device.setPerson(activePerson);
        }

        updateDeviceFields(dto, device);
    }

    private static void updateDeviceFields(final MobilePushRegistrationDTO dto,
                                           final MobileClientDevice device) {
        device.setPlatform(dto.getPlatform());
        device.setClientVersion(dto.getClientVersion());
        device.setDeviceName(dto.getDeviceName());
        device.setPushToken(dto.getPushToken());
    }
}
