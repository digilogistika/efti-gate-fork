package eu.efti.eftigate.controller;

import eu.efti.commons.dto.UilDto;
import eu.efti.eftigate.controller.api.DatasetControllerApi;
import eu.efti.eftigate.dto.RequestIdDto;
import eu.efti.eftigate.service.DatasetSearchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
@Slf4j
public class DatasetController implements DatasetControllerApi {

    private final DatasetSearchService datasetSearchService;

    @Override
    public ResponseEntity<RequestIdDto> getDataset(String gateId, String platformId, String datasetId, List<String> subsetIds) {
        log.info("GET on /v1/dataset with params gateId: {}, platformId: {}, datasetId: {} and subsets: {}", gateId, platformId, datasetId, subsetIds);
        UilDto dto = UilDto.builder()
                .gateId(gateId)
                .platformId(platformId)
                .datasetId(datasetId)
                .subsetIds(subsetIds)
                .build();
        return new ResponseEntity<>(datasetSearchService.getDataset(dto), HttpStatus.OK);
    }
}
