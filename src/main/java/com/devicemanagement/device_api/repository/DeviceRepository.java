package com.devicemanagement.device_api.repository;

import com.devicemanagement.device_api.domain.Device;
import com.devicemanagement.device_api.domain.DeviceState;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Page<Device> findByBrand(String brand, Pageable pageable);

    Page<Device> findByState(DeviceState state, Pageable pageable);

    Page<Device> findByBrandAndState(String brand, DeviceState state, Pageable pageable);
}
