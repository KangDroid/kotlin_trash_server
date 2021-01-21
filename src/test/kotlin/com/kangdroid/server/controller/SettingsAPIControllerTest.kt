package com.kangdroid.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.server.dto.SettingsRequestDto
import com.kangdroid.server.dto.SettingsResponseDto
import com.kangdroid.server.settings.Settings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SettingsAPIControllerTest {

    @LocalServerPort
    private var port: Int? = null

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun isGetApiWorksWell() {
        // Let
        val serverSettings: Settings = Settings
        with (serverSettings) {
            trashCanPath = "/Users/kangdroid/Desktop/test_trashcan"
        }

        // when
        val url: String = "http://localhost:" + this.port + "/api/settings/get"

        // do
        val restTemplate: TestRestTemplate = TestRestTemplate()
        val response: SettingsResponseDto = restTemplate.getForObject(url, SettingsResponseDto::class.java)

        assertThat(response.serverVersion).isEqualTo(serverSettings.serverVersion)
        assertThat(response.trashCanPath).isEqualTo(serverSettings.trashCanPath)
    }

    @Test
    fun isPostSettingsWorksWell() {
        // Let
        val serverSettings: Settings = Settings
        with (serverSettings) {
            trashCanPath = "/Users/kangdroid/Desktop/test_trashcan"
        }

        val settingsRequest: SettingsRequestDto = SettingsRequestDto()
        settingsRequest.trashCanPath = "/tmp"

        // When
        val url: String = "http://localhost:" + this.port + "/api/settings/set"

        // do
        mvc.perform(put(url)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(ObjectMapper().writeValueAsString(settingsRequest))

        ).andExpect(status().isOk)

        // Assert
        assertThat(serverSettings.trashCanPath).isEqualTo(settingsRequest.trashCanPath)
    }
}