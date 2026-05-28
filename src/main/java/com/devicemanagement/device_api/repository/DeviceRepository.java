package com.devicemanagement.device_api.repository;

import com.devicemanagement.device_api.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findByBrand(String brand);

    List<Device> findByState(String state);

    List<Device> findByBrandAndState(String brand, String state);
}
