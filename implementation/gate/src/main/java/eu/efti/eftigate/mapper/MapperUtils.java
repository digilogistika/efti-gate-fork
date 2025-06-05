package eu.efti.eftigate.mapper;

import eu.efti.commons.dto.ControlDto;
import eu.efti.commons.dto.RequestDto;
import eu.efti.commons.dto.identifiers.ConsignmentDto;
import eu.efti.commons.dto.identifiers.api.ConsignmentApiDto;
import eu.efti.eftigate.dto.PlatformHeaderDto;
import eu.efti.eftigate.dto.RabbitRequestDto;
import eu.efti.eftigate.entity.ControlEntity;
import eu.efti.eftigate.entity.ErrorEntity;
import eu.efti.eftigate.entity.IdentifiersRequestEntity;
import eu.efti.eftigate.entity.PlatformHeaderEntity;
import eu.efti.eftigate.entity.RequestEntity;
import eu.efti.eftigate.entity.UilRequestEntity;
import eu.efti.identifiersregistry.IdentifiersMapper;
import eu.efti.v1.edelivery.Consignment;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MapperUtils {

    private final ModelMapper modelMapper;
    private final IdentifiersMapper identifiersMapper;

    public ControlEntity controlDtoToControlEntity(final ControlDto controlDto) {
        final ControlEntity controlEntity = modelMapper.map(controlDto, ControlEntity.class);

        //ça marche pas sinon
        if (controlDto.getError() != null) {
            final ErrorEntity errorEntity = new ErrorEntity();
            errorEntity.setErrorCode(controlDto.getError().getErrorCode());
            errorEntity.setErrorDescription(controlDto.getError().getErrorDescription());
            errorEntity.setId(controlDto.getError().getId());
            controlEntity.setError(errorEntity);
        }
        return controlEntity;
    }

    public ControlDto controlEntityToControlDto(final ControlEntity controlEntity) {
        final ControlDto controlDto = modelMapper.map(controlEntity, ControlDto.class);
        final List<ConsignmentDto> consignmentDtoList = CollectionUtils.emptyIfNull(controlEntity.getRequests()).stream()
                .filter(IdentifiersRequestEntity.class::isInstance)
                .map(IdentifiersRequestEntity.class::cast)
                .filter(identifiersRequestEntity -> identifiersRequestEntity.getIdentifiersResults() != null
                        && CollectionUtils.isNotEmpty(identifiersRequestEntity.getIdentifiersResults().getConsignments()))
                .flatMap(request -> request.getIdentifiersResults().getConsignments().stream())
                .sorted(Comparator.comparing(ConsignmentDto::getGateId))
                .toList();
        controlDto.setIdentifiersResults(consignmentDtoList);
        final byte[] byteArray = CollectionUtils.emptyIfNull(controlEntity.getRequests()).stream()
                .filter(UilRequestEntity.class::isInstance)
                .map(UilRequestEntity.class::cast)
                .map(UilRequestEntity::getReponseData)
                .filter(ArrayUtils::isNotEmpty)
                .collect(ByteArrayOutputStream::new, (byteArrayOutputStream, bytes) -> byteArrayOutputStream.write(bytes, 0, bytes.length), (arrayOutputStream, byteArrayOutputStream) -> {
                })
                .toByteArray();
        controlDto.setEftiData(byteArray);
        return controlDto;
    }

    public <T extends RequestEntity> T requestDtoToRequestEntity(final RequestDto requestDto, final Class<T> destinationClass) {
        return modelMapper.map(requestDto, destinationClass);
    }

    public <T extends RequestDto> T rabbitRequestDtoToRequestDto(final RabbitRequestDto rabbitRequestDto, final Class<T> destinationClass) {
        return modelMapper.map(rabbitRequestDto, destinationClass);
    }

    public <T extends RequestEntity, D extends RequestDto> D requestToRequestDto(final T requestEntity, final Class<D> destinationClass) {
        return modelMapper.map(requestEntity, destinationClass);
    }

    public ConsignmentDto eDeliveryToDto(final Consignment consignment) {
        //todo fix this double mapping
        return this.modelMapper.map(this.identifiersMapper.eDeliveryToEntity(consignment), ConsignmentDto.class);
    }

    public List<ConsignmentDto> eDeliveryToDto(final List<Consignment> consignments) {
        return CollectionUtils.emptyIfNull(consignments).stream().map(this::eDeliveryToDto).toList();

    }

    public Consignment dtoToEdelivery(final ConsignmentDto consignment) {
        //todo fix double mapping
        return this.identifiersMapper.entityToEdelivery(this.dtoToEntity(consignment));
    }

    public List<Consignment> dtoToEdelivery(final List<ConsignmentDto> consignments) {
        return CollectionUtils.emptyIfNull(consignments).stream().map(this::dtoToEdelivery).toList();

    }

    public ConsignmentDto entityToDto(final eu.efti.identifiersregistry.entity.Consignment consignment) {
        return modelMapper.map(consignment, ConsignmentDto.class);
    }

    public eu.efti.identifiersregistry.entity.Consignment dtoToEntity(final ConsignmentDto consignmentDto) {
        return modelMapper.map(consignmentDto, eu.efti.identifiersregistry.entity.Consignment.class);
    }

    public List<eu.efti.identifiersregistry.entity.Consignment> dtoToEntity(final List<ConsignmentDto> consignmentDtoList) {
        return CollectionUtils.emptyIfNull(consignmentDtoList).stream().map(this::dtoToEntity).toList();
    }

    public List<ConsignmentApiDto> consignmentDtoToApiDto(List<ConsignmentDto> consignmentDtos) {
        return CollectionUtils.emptyIfNull(consignmentDtos).stream().map(this::dtoToApiDto).toList();
    }

    public List<PlatformHeaderDto> headerEntityListToHeaderDtoList(List<PlatformHeaderEntity> platformHeaderEntities) {
        return platformHeaderEntities.stream().map(entity -> modelMapper.map(entity, PlatformHeaderDto.class)).toList();
    }

    public List<PlatformHeaderEntity> headerDtoListToHeaderEntityList(List<PlatformHeaderDto> platformHeaderDtos) {
        return platformHeaderDtos.stream().map(dto -> modelMapper.map(dto, PlatformHeaderEntity.class)).toList();
    }


    private ConsignmentApiDto dtoToApiDto(ConsignmentDto consignmentDto) {
        return modelMapper.map(consignmentDto, ConsignmentApiDto.class);
    }
}
