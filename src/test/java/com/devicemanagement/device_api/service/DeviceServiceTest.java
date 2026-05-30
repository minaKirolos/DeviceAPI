package com.devicemanagement.device_api.service;

import com.devicemanagement.device_api.domain.Device;
import com.devicemanagement.device_api.domain.DeviceState;
import com.devicemanagement.device_api.dto.CreateDeviceRequest;
import com.devicemanagement.device_api.dto.DeviceResponse;
import com.devicemanagement.device_api.dto.PatchDeviceRequest;
import com.devicemanagement.device_api.dto.UpdateDeviceRequest;
import com.devicemanagement.device_api.exception.DeviceInUseException;
import com.devicemanagement.device_api.exception.DeviceNotFoundException;
import com.devicemanagement.device_api.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeviceServiceTest {

    @Mock
    private DeviceRepository repository;

    private DeviceService service;

    @BeforeEach
    void setUp() {
        service = new DeviceService(repository);
    }

    @Test
    void create_savesAndReturnsDevice() {
        when(repository.save(any(Device.class))).thenAnswer(inv -> inv.getArgument(0));

        DeviceResponse response = service.create(
                new CreateDeviceRequest("Pixel", "Google", DeviceState.AVAILABLE));

        assertThat(response.name()).isEqualTo("Pixel");
        assertThat(response.brand()).isEqualTo("Google");
        assertThat(response.state()).isEqualTo(DeviceState.AVAILABLE);
        verify(repository).save(any(Device.class));
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id)).isInstanceOf(DeviceNotFoundException.class);
    }

    @Test
    void fullUpdate_whenInUseAndNameChanges_throwsInUse() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(new Device("Old", "BrandX", DeviceState.IN_USE)));

        assertThatThrownBy(() -> service.fullUpdate(id,
                new UpdateDeviceRequest("New", "BrandX", DeviceState.IN_USE)))
                .isInstanceOf(DeviceInUseException.class);
    }

    @Test
    void fullUpdate_whenInUseButOnlyStateChanges_succeeds() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(new Device("Same", "BrandX", DeviceState.IN_USE)));

        DeviceResponse response = service.fullUpdate(id,
                new UpdateDeviceRequest("Same", "BrandX", DeviceState.AVAILABLE));

        assertThat(response.state()).isEqualTo(DeviceState.AVAILABLE);
    }

    @Test
    void partialUpdate_whenInUseAndBrandChanges_throwsInUse() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(new Device("Name", "OldBrand", DeviceState.IN_USE)));

        assertThatThrownBy(() -> service.partialUpdate(id,
                new PatchDeviceRequest(null, "NewBrand", null)))
                .isInstanceOf(DeviceInUseException.class);
    }

    @Test
    void partialUpdate_onlyChangesProvidedFields() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(new Device("Name", "Brand", DeviceState.AVAILABLE)));

        DeviceResponse response = service.partialUpdate(id,
                new PatchDeviceRequest(null, null, DeviceState.INACTIVE));

        assertThat(response.name()).isEqualTo("Name");
        assertThat(response.brand()).isEqualTo("Brand");
        assertThat(response.state()).isEqualTo(DeviceState.INACTIVE);
    }

    @Test
    void delete_whenInUse_throwsAndDoesNotDelete() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(new Device("Name", "Brand", DeviceState.IN_USE)));

        assertThatThrownBy(() -> service.delete(id)).isInstanceOf(DeviceInUseException.class);
        verify(repository, never()).delete(any(Device.class));
    }

    @Test
    void delete_whenAvailable_deletes() {
        UUID id = UUID.randomUUID();
        Device device = new Device("Name", "Brand", DeviceState.AVAILABLE);
        when(repository.findById(id)).thenReturn(Optional.of(device));

        service.delete(id);

        verify(repository).delete(device);
    }

}
