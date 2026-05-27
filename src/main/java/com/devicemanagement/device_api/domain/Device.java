package com.devicemanagement.device_api.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeviceState state;

    @CreationTimestamp
    @Column(name = "creation_time", nullable = false, updatable = false)
    private Instant creationTime;

    protected Device() {
        // JPA requires a no-arg constructor
    }

    public Device(String name, String brand, DeviceState state) {
        this.name = name;
        this.brand = brand;
        this.state = state;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }
}