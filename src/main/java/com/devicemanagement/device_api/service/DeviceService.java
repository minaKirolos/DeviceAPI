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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;
import java.util.List;

@Service
public class DeviceService {
    private final DeviceRepository repository;

    public DeviceService(DeviceRepository repository, DeviceMapper mapper) {
        this.repository = repository;
    }

    @Transactional
    public DeviceResponse create(CreateDeviceRequest request) {
        Device device = DeviceMapper.toEntity(request);
        return DeviceMapper.toResponse( repository.save(device));
    }

    @Transactional(readOnly = true)
    public DeviceResponse getById(UUID id) {
        return DeviceMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getAll() {
        return repository.findAll().stream().map(DeviceMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getByBrand(String brand) {
        return repository.findByBrand(brand).stream().map(DeviceMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> getByState(DeviceState state) {
        return repository.findByState(state).stream().map(DeviceMapper::toResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<DeviceResponse> getByBrandAndState(String brand, DeviceState state) {
        return repository.findByBrandAndState(brand, state).stream()
                .map(DeviceMapper::toResponse)
                .toList();
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

        if (request.name() != null)
        {
            device.setName(request.name());
        }
        if (request.brand() != null)
        {
            device.setBrand(request.brand());
        }
        if (request.state() != null)
        {
            device.setState(request.state());
        }
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
