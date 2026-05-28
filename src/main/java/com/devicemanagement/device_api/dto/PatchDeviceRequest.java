package com.devicemanagement.device_api.dto;

import com.devicemanagement.device_api.domain.DeviceState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PatchDeviceRequest(
        String name,
        String brand,
        DeviceState state
) {
}
