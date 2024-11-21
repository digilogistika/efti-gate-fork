package eu.efti.platformgatesimulator.controller;

import eu.efti.platformgatesimulator.controller.api.V0Api;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/gate-api")
public class GateApiController implements V0Api {
    @Override
    public ResponseEntity<Object> getConsignmentSubsets(String datasetId, Set<String> subsetId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> postConsignmentFollowup(String datasetId, String body) {
        return null;
    }
}
