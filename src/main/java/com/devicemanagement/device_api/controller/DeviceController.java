package com.devicemanagement.device_api.controller;

import com.devicemanagement.device_api.domain.DeviceState;
import com.devicemanagement.device_api.dto.CreateDeviceRequest;
import com.devicemanagement.device_api.dto.DeviceResponse;
import com.devicemanagement.device_api.dto.PatchDeviceRequest;
import com.devicemanagement.device_api.dto.UpdateDeviceRequest;
import com.devicemanagement.device_api.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Devices", description = "Create, query, update and delete device resources")
public class DeviceController {
    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }
    @Operation(summary = "Create a new device")
    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody CreateDeviceRequest request, UriComponentsBuilder uriBuilder) {
        DeviceResponse created = service.create(request);
        URI location = uriBuilder.path("/api/devices/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }
    @Operation(summary = "Fetch a single device by id")
    @GetMapping("/{id}")
    public DeviceResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }
    @Operation(summary = "Fetch all devices, optionally filtered by brand and/or state")
    @GetMapping
    public PagedModel<DeviceResponse> getDevices(@RequestParam(required = false) String brand,
                                                 @RequestParam(required = false) DeviceState state,
                                                 @PageableDefault(size = 10) Pageable pageable) {
        Page<DeviceResponse> page;
        if (brand != null && state != null) {
            page = service.getByBrandAndState(brand, state, pageable);
        } else if (brand != null) {
            page = service.getByBrand(brand, pageable);
        } else if (state != null) {
            page = service.getByState(state, pageable);
        } else {
            page = service.getAll(pageable);
        }
        return new PagedModel<>(page);
    }
    @Operation(summary = "Fully update a device (replaces all fields)")
    @PutMapping("/{id}")
    public DeviceResponse fullUpdate(@PathVariable UUID id, @Valid @RequestBody UpdateDeviceRequest request){
        return service.fullUpdate(id, request);
    }
    @Operation(summary = "Partially update a device (only provided fields change)")
    @PatchMapping("/{id}")
    public DeviceResponse partialUpdate(@PathVariable UUID id, @Valid @RequestBody PatchDeviceRequest request){
        return service.partialUpdate(id, request);
    }
    @Operation(summary = "Delete a device")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
