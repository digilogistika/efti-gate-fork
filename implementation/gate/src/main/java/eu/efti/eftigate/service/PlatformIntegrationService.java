package eu.efti.eftigate.service;

import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.enums.RequestTypeEnum;
import eu.efti.commons.exception.TechnicalException;
import eu.efti.eftigate.dto.RabbitRequestDto;
import eu.efti.eftigate.repository.PlatformRepository;
import eu.efti.eftigate.service.request.NotesRequestService;
import eu.efti.eftigate.service.request.UilRequestService;
import eu.efti.v1.consignment.common.SupplyChainConsignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlatformIntegrationService {

    private final UilRequestService uilRequestService;

    private final NotesRequestService notesRequestService;

    private final PlatformApiService platformApiService;

    private final DomibusIntegrationService domibusIntegrationService;

    private final PlatformRepository platformRepository;


    public boolean platformExists(String platformId) {
        return platformRepository.existsByPlatformId(platformId);
    }

    public void handle(final RabbitRequestDto rabbitRequestDto, ControlDto control, Optional<String> note) {
        Objects.requireNonNull(control.getPlatformId());

        String platformId = control.getPlatformId();
        boolean platformUsesRestApi = true;

        if (platformId.isEmpty() || !platformExists(platformId)) {
            throw new IllegalArgumentException("platform " + platformId + " does not exist");
        } else {
            final RequestTypeEnum requestTypeEnum = control.getRequestType();
            if (platformUsesRestApi) {
                try {
                    if (RequestTypeEnum.LOCAL_UIL_SEARCH.equals(requestTypeEnum)) {
                        uilRequestService.manageRestRequestInProgress(control.getRequestId());
                        SupplyChainConsignment res = platformApiService.callGetConsignmentSubsets(platformId, control.getDatasetId(), Set.copyOf(control.getSubsetIds()));
                        uilRequestService.manageRestResponseReceived(control.getRequestId(), res);
                    } else if (RequestTypeEnum.NOTE_SEND.equals(requestTypeEnum)) {
                        notesRequestService.manageRestRequestInProgress(control.getRequestId());
                        platformApiService.callPostConsignmentFollowup(platformId, control.getDatasetId(), note.orElseThrow());
                        notesRequestService.manageRestRequestDone(control.getRequestId());
                    } else if (RequestTypeEnum.EXTERNAL_ASK_UIL_SEARCH.equals(requestTypeEnum)) {
                        uilRequestService.manageRestRequestInProgress(control.getRequestId());
                        SupplyChainConsignment res = platformApiService.callGetConsignmentSubsets(platformId, control.getDatasetId(), Set.copyOf(control.getSubsetIds()));
                        uilRequestService.manageRestResponseReceived(control.getRequestId(), res);
                    } else if (RequestTypeEnum.EXTERNAL_NOTE_SEND.equals(requestTypeEnum)) {
                        platformApiService.callPostConsignmentFollowup(platformId, control.getDatasetId(), note.orElseThrow());
                    } else {
                        throw new TechnicalException("unexpected request type: " + requestTypeEnum);
                    }
                } catch (PlatformIntegrationServiceException e) {
                    throw new RuntimeException(e);
                }
            } else {
                domibusIntegrationService.trySendDomibus(rabbitRequestDto, requestTypeEnum, control.getPlatformId());
            }
        }
    }
}
