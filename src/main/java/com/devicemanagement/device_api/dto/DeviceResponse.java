package com.devicemanagement.device_api.dto;

import com.devicemanagement.device_api.domain.DeviceState;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(
        UUID id,
        String name,
        String brand,
        DeviceState state,
        Instant creationTime
) {
}
