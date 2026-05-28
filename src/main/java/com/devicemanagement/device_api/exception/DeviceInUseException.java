package com.devicemanagement.device_api.exception;

import java.util.UUID;

public class DeviceInUseException extends RuntimeException {
    public DeviceInUseException(String message) {
        super(message);
    }
}
