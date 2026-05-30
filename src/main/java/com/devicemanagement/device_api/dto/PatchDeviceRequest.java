package com.devicemanagement.device_api.dto;

import com.devicemanagement.device_api.domain.DeviceState;

public record PatchDeviceRequest(
        String name,
        String brand,
        DeviceState state
) {
}