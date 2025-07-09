package eu.efti.eftigate.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MetaDataDto {
    private List<String> gateIds;
    private List<String> platformIds;
    private List<String> authorityNames;
}

