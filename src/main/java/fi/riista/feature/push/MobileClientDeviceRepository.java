package fi.riista.feature.push;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MobileClientDeviceRepository extends JpaRepository<MobileClientDevice, Long> {
    MobileClientDevice findByPushToken(String pushToken);
}
