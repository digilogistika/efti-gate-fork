package eu.efti.eftigate.controller;

import eu.efti.edeliveryapconnector.dto.NotificationContentDto;
import eu.efti.edeliveryapconnector.dto.NotificationDto;
import eu.efti.edeliveryapconnector.dto.NotificationType;
import eu.efti.eftigate.controller.api.PlatformApiV1;
import eu.efti.eftigate.exception.XApiKeyValidationexception;
import eu.efti.eftigate.service.PlatformIdentityService;
import eu.efti.eftigate.service.request.IdentifiersRequestService;
import eu.efti.eftigate.service.request.UilRequestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/platform")
@Slf4j
public class PlatformApiController implements PlatformApiV1 {
    private final IdentifiersRequestService identifiersRequestService;
    private final PlatformIdentityService platformIdentityService;
    private final UilRequestService uilRequestService;


    public ResponseEntity<String> postConsignmentIdentifiers(
            @RequestBody String body,
            @RequestHeader("X-API-Key") String apiKey
    ) {
        log.info("POST on /api/v1/platform/identifiers");
        try {
            platformIdentityService.validateXApiKeyHeader(apiKey);
        } catch (XApiKeyValidationexception e) {
            log.error("X-API-Key validation failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }

        try {
            String name = platformIdentityService.getPlatformNameFromHeader(apiKey);
            identifiersRequestService.createOrUpdate(body, name);
        } catch (SAXException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.accepted().build();
    }


    public ResponseEntity<String> consignmentResponse(
            @RequestBody String body,
            @RequestHeader("X-API-Key") String apiKey
    ) {
        log.info("POST on /api/v1/platform/uil");
        try {
            platformIdentityService.validateXApiKeyHeader(apiKey);
        } catch (XApiKeyValidationexception e) {
            log.error("X-API-Key validation failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }

        String name = platformIdentityService.getPlatformNameFromHeader(apiKey);
        NotificationDto notificationDto = NotificationDto
                .builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto
                        .builder()
                        .body(body)
                        .fromPartyId(name)
                        .build()
                )
                .build();
        try {
            uilRequestService.manageResponseReceived(notificationDto);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
