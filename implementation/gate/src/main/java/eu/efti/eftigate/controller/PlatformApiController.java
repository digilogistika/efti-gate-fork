package eu.efti.eftigate.controller;

import eu.efti.edeliveryapconnector.dto.NotificationContentDto;
import eu.efti.edeliveryapconnector.dto.NotificationDto;
import eu.efti.edeliveryapconnector.dto.NotificationType;
import eu.efti.eftigate.controller.api.PlatformApiV1;
import eu.efti.eftigate.service.request.IdentifiersRequestService;
import eu.efti.eftigate.service.request.UilRequestService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/platform")
@Slf4j
public class PlatformApiController implements PlatformApiV1 {
    private final IdentifiersRequestService identifiersRequestService;
    private final UilRequestService uilRequestService;

    
    public ResponseEntity<String> postConsignmentIdentifiers(@RequestBody String body) {
        log.info("POST on /api/v1/platform/identifiers");
        try {
            identifiersRequestService.createOrUpdate(body, "acme");
        } catch (SAXException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.accepted().build();
    }


    public ResponseEntity<String> consignmentResponse(@RequestBody String body) {
        log.info("POST on /api/v1/platform/uil");

        NotificationDto notificationDto = NotificationDto
                .builder()
                .notificationType(NotificationType.RECEIVED)
                .content(NotificationContentDto
                        .builder()
                        .body(body)
                        .fromPartyId("acme")
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
