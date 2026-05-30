package com.devicemanagement.device_api;

import com.devicemanagement.device_api.domain.DeviceState;
import com.devicemanagement.device_api.dto.CreateDeviceRequest;
import com.devicemanagement.device_api.dto.PatchDeviceRequest;
import com.devicemanagement.device_api.repository.DeviceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class DeviceApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository repository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void createPersistsDeviceWithGeneratedIdAndTimestamp() throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest("Pixel 9", "Google", DeviceState.AVAILABLE);

        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.creationTime").exists())
                .andExpect(jsonPath("$.name").value("Pixel 9"));
    }

    @Test
    void inUseDevice_cannotBeRenamedOrDeleted() throws Exception {
        String id = createDevice("Galaxy", "Samsung", DeviceState.AVAILABLE);

        mockMvc.perform(patch("/api/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PatchDeviceRequest(null, null, DeviceState.IN_USE))))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PatchDeviceRequest("Hacked", null, null))))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/devices/{id}", id))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteAvailableThenFetch_returns404() throws Exception {
        String id = createDevice("Device", "BrandX", DeviceState.AVAILABLE);

        mockMvc.perform(delete("/api/devices/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/devices/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void list_isPaged_andFiltersByBrand() throws Exception {
        createDevice("A", "Google", DeviceState.AVAILABLE);
        createDevice("B", "Apple", DeviceState.AVAILABLE);

        mockMvc.perform(get("/api/devices").param("brand", "Google"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].brand").value("Google"))
                .andExpect(jsonPath("$.page.size").value(10));
    }

    @Test
    void unknownSortProperty_returns400() throws Exception {
        mockMvc.perform(get("/api/devices").param("sort", "doesNotExist"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Unknown sort/filter property 'doesNotExist'")));
    }

    private String createDevice(String name, String brand, DeviceState state) throws Exception {
        CreateDeviceRequest request = new CreateDeviceRequest(name, brand, state);
        String body = mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }
}