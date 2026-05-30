package com.devicemanagement.device_api;

import com.devicemanagement.device_api.repository.DeviceRepository;
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

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }
    @Test
    void createPersistsDeviceWithGeneratedIdAndTimestamp() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pixel 9\",\"brand\":\"Google\",\"state\":\"AVAILABLE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.creationTime").exists())
                .andExpect(jsonPath("$.name").value("Pixel 9"));
    }
    @Test
    void inUseDevice_cannotBeRenamedOrDeleted() throws Exception {
        String id = createDevice("Galaxy", "Samsung", "AVAILABLE");

        mockMvc.perform(patch("/api/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"state\":\"IN_USE\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/devices/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hacked\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/devices/{id}", id))
                .andExpect(status().isConflict());
    }
    @Test
    void deleteAvailableThenFetch_returns404() throws Exception {
        String id = createDevice("Device", "BrandX", "AVAILABLE");

        mockMvc.perform(delete("/api/devices/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/devices/{id}", id)).andExpect(status().isNotFound());
    }
    @Test
    void list_isPaged_andFiltersByBrand() throws Exception {
        createDevice("A", "Google", "AVAILABLE");
        createDevice("B", "Apple", "AVAILABLE");

        mockMvc.perform(get("/api/devices").param("brand", "Google"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].brand").value("Google"))
                .andExpect(jsonPath("$.page.size").value(10));
    }


    private String createDevice(String name, String brand, String state) throws Exception {
        String body = mockMvc.perform(post("/api/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"brand\":\"%s\",\"state\":\"%s\"}".formatted(name, brand, state)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

}
