package com.devicemanagement.device_api.dto;

import com.devicemanagement.device_api.domain.DeviceState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateDeviceRequest(
        @NotBlank(message = "name is required")
        String name,
        @NotBlank(message = "brand is required")
        String brand,
        @NotNull(message = "state is required")
        DeviceState state
) {
}
