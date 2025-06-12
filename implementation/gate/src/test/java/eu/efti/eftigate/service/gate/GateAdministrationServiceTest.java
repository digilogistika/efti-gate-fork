package eu.efti.eftigate.service.gate;

import eu.efti.commons.enums.CountryIndicator;
import eu.efti.eftigate.dto.GateDto;
import eu.efti.eftigate.entity.GateEntity;
import eu.efti.eftigate.exception.GateAlreadyExistsException;
import eu.efti.eftigate.exception.GateDoesNotExistException;
import eu.efti.eftigate.repository.GateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GateAdministrationServiceTest {

    @Mock
    private GateRepository gateRepository;

    @InjectMocks
    private GateAdministrationService gateAdministrationService;

    private GateDto gateDto;
    private GateEntity gateEntity;

    @BeforeEach
    void setUp() {
        gateDto = GateDto.builder()
                .gateId("TEST-GATE-1")
                .country(CountryIndicator.EE)
                .build();

        gateEntity = GateEntity.builder()
                .gateId("TEST-GATE-1")
                .country(CountryIndicator.EE)
                .build();
    }

    @Test
    void givenNewGate_whenRegisterGate_thenGateIsRegistered() {
        // Given
        when(gateRepository.findByGateId(gateDto.getGateId())).thenReturn(null);
        when(gateRepository.save(any(GateEntity.class))).thenReturn(gateEntity);

        // When
        String result = gateAdministrationService.registerGate(gateDto);

        // Then
        assertEquals("Gate TEST-GATE-1 added successfully", result);
        verify(gateRepository).findByGateId(gateDto.getGateId());
        verify(gateRepository).save(any(GateEntity.class));
    }

    @Test
    void givenExistingGate_whenRegisterGate_thenThrowsException() {
        // Given
        when(gateRepository.findByGateId(gateDto.getGateId())).thenReturn(gateEntity);

        // When & Then
        assertThrows(GateAlreadyExistsException.class, () -> 
            gateAdministrationService.registerGate(gateDto)
        );

        verify(gateRepository).findByGateId(gateDto.getGateId());
        verify(gateRepository, never()).save(any(GateEntity.class));
    }

    @Test
    void givenExistingGate_whenDeleteGate_thenGateIsDeleted() {
        // Given
        when(gateRepository.findByGateId(gateDto.getGateId())).thenReturn(gateEntity);
        doNothing().when(gateRepository).delete(gateEntity);

        // When
        String result = gateAdministrationService.deleteGate(gateDto.getGateId());

        // Then
        assertEquals("Gate TEST-GATE-1 deleted successfully", result);
        verify(gateRepository).findByGateId(gateDto.getGateId());
        verify(gateRepository).delete(gateEntity);
    }

    @Test
    void givenNonExistingGate_whenDeleteGate_thenThrowsException() {
        // Given
        when(gateRepository.findByGateId(gateDto.getGateId())).thenReturn(null);

        // When & Then
        assertThrows(GateDoesNotExistException.class, () -> 
            gateAdministrationService.deleteGate(gateDto.getGateId())
        );

        verify(gateRepository).findByGateId(gateDto.getGateId());
        verify(gateRepository, never()).delete(any(GateEntity.class));
    }
} 