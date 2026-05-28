package com.devicemanagement.device_api.controller;

import com.devicemanagement.device_api.domain.DeviceState;
import com.devicemanagement.device_api.dto.CreateDeviceRequest;
import com.devicemanagement.device_api.dto.DeviceResponse;
import com.devicemanagement.device_api.dto.PatchDeviceRequest;
import com.devicemanagement.device_api.dto.UpdateDeviceRequest;
import com.devicemanagement.device_api.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody CreateDeviceRequest request, UriComponentsBuilder uriBuilder) {
        DeviceResponse created = service.create(request);
        URI location = uriBuilder.path("/api/devices/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public DeviceResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping
    public List<DeviceResponse> getDevices(@RequestParam(required = false) String brand,
                                           @RequestParam(required = false) DeviceState state) {
        if (brand != null && state != null)
        {
            return service.getByBrandAndState(brand, state);
        }
        if (brand != null) {
            return service.getByBrand(brand);
        }
        if (state != null) {
            return service.getByState(state);
        }
        return service.getAll();
    }
    @PutMapping("/{id}")
    public DeviceResponse fullUpdate(@PathVariable UUID id, @Valid @RequestBody UpdateDeviceRequest request){
        return service.fullUpdate(id, request);
    }
    @PatchMapping("/{id}")
    public DeviceResponse partialUpdate(@PathVariable UUID id, @Valid @RequestBody PatchDeviceRequest request){
        return service.partialUpdate(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
