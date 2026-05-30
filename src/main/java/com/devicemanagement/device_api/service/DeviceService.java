package com.devicemanagement.device_api.service;

import com.devicemanagement.device_api.domain.Device;
import com.devicemanagement.device_api.domain.DeviceState;
import com.devicemanagement.device_api.dto.CreateDeviceRequest;
import com.devicemanagement.device_api.dto.DeviceResponse;
import com.devicemanagement.device_api.dto.PatchDeviceRequest;
import com.devicemanagement.device_api.dto.UpdateDeviceRequest;
import com.devicemanagement.device_api.exception.DeviceInUseException;
import com.devicemanagement.device_api.exception.DeviceNotFoundException;
import com.devicemanagement.device_api.mapper.DeviceMapper;
import com.devicemanagement.device_api.repository.DeviceRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceService {
    private final DeviceRepository repository;

    public DeviceService(DeviceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DeviceResponse create(CreateDeviceRequest request) {
        Device device = DeviceMapper.toEntity(request);
        return DeviceMapper.toResponse(repository.save(device));
    }

    @Transactional(readOnly = true)
    public DeviceResponse getById(UUID id) {
        return DeviceMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(DeviceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getByBrand(String brand, Pageable pageable) {
        return repository.findByBrand(brand, pageable).map(DeviceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getByState(DeviceState state, Pageable pageable) {
        return repository.findByState(state, pageable).map(DeviceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<DeviceResponse> getByBrandAndState(String brand, DeviceState state, Pageable pageable) {
        return repository.findByBrandAndState(brand, state, pageable)
                .map(DeviceMapper::toResponse);
    }

    @Transactional
    public DeviceResponse fullUpdate(UUID id, UpdateDeviceRequest request) {
        Device device = findOrThrow(id);
        guardNameBrandChange(device, request.name(), request.brand());
        device.setName(request.name());
        device.setBrand(request.brand());
        device.setState(request.state());
        return DeviceMapper.toResponse(device);
    }

    @Transactional
    public DeviceResponse partialUpdate(UUID id, PatchDeviceRequest request) {
        Device device = findOrThrow(id);

        String newName = request.name() != null ? request.name() : device.getName();
        String newBrand = request.brand() != null ? request.brand() : device.getBrand();
        guardNameBrandChange(device, newName, newBrand);

        Optional.ofNullable(request.name())
                .ifPresent(device::setName);

        Optional.ofNullable(request.brand())
                .ifPresent(device::setBrand);

        Optional.ofNullable(request.state())
                .ifPresent(device::setState);

        return DeviceMapper.toResponse(device);
    }

    @Transactional
    public void delete(UUID id) {
        Device device = findOrThrow(id);
        if (device.getState() == DeviceState.IN_USE)
        {
            throw new DeviceInUseException("Cannot delete a device that is in use");
        }
        repository.delete(device);
    }

    private Device findOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new DeviceNotFoundException(id));
    }

    private void guardNameBrandChange(Device device, String newName, String newBrand) {
        if (device.getState() != DeviceState.IN_USE)
        {
            return;
        }
        boolean nameChanges = !Objects.equals(device.getName(), newName);
        boolean brandChanges = !Objects.equals(device.getBrand(), newBrand);
        if (nameChanges || brandChanges)
        {
            throw new DeviceInUseException("Cannot update name or brand of a device that is in use");
        }
    }
}
