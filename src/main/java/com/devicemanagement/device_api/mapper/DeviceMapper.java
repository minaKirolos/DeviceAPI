package com.devicemanagement.device_api.mapper;

import com.devicemanagement.device_api.domain.Device;
import com.devicemanagement.device_api.dto.CreateDeviceRequest;
import com.devicemanagement.device_api.dto.DeviceResponse;

public final class DeviceMapper {

    private DeviceMapper() {
    }

    // handle create
    public static Device toEntity(CreateDeviceRequest request) {
        return new Device(request.name(), request.brand(), request.state());
    }

    // handle response
    public static DeviceResponse toResponse(Device device) {
        return new DeviceResponse(device.getId(),
                device.getName(),
                device.getBrand(),
                device.getState(),
                device.getCreationTime());
    }
}
