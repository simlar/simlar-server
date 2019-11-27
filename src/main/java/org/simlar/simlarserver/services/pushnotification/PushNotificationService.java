package org.simlar.simlarserver.services.pushnotification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simlar.simlarserver.data.DeviceType;
import org.simlar.simlarserver.database.models.PushNotification;
import org.simlar.simlarserver.database.repositories.PushNotificationsRepository;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@AllArgsConstructor
@Slf4j
@Component
final class PushNotificationService {
    private final PushNotificationsRepository pushNotificationsRepository;
    private final ApplePushNotificationService applePushNotificationService;
    private final GooglePushNotificationService googlePushNotificationService;

    @Nullable
    public String sendPushNotification(final SimlarId simlarId) {
        if (simlarId == null) {
            return null;
        }

        final PushNotification pushNotification = pushNotificationsRepository.findBySimlarId(simlarId.get());
        final DeviceType deviceType = pushNotification == null ? null : pushNotification.getDeviceType();
        if (deviceType == null) {
            log.error("no device type found for simlarId '{}'", simlarId);
            return null;
        }

        //noinspection SwitchStatement
        switch (deviceType) {
            case ANDROID:
                return googlePushNotificationService.requestPushNotification(pushNotification.getPushId());
            case IOS_VOIP:
                return applePushNotificationService.requestVoipPushNotification(ApplePushServer.PRODUCTION, pushNotification.getPushId());
            case IOS_VOIP_DEVELOPMENT:
                return applePushNotificationService.requestVoipPushNotification(ApplePushServer.SANDBOX, pushNotification.getPushId());
            case IOS:
            case IOS_DEVELOPMENT:
            default:
                log.error("unsupported device type '{}'", deviceType);
                return null;
        }
    }
}
