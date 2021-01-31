package com.kangdroid.server.controller

import com.kangdroid.server.dto.SettingsResponseDto
import com.kangdroid.server.settings.Settings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForObject
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SettingsAPIControllerTest {
    @LocalServerPort
    private var port: Int? = null

    @Autowired
    private lateinit var settings: Settings

    @Test
    fun isSettingsGetWorkWell() {
        val errorValue: String = "ERROR"

        // URL/Port
        val url: String = "http://localhost:$port/api/settings/get"
        val restTemplate: TestRestTemplate = TestRestTemplate()
        val response: SettingsResponseDto = restTemplate.getForObject(url, SettingsResponseDto::class)
            ?: SettingsResponseDto(errorValue, errorValue)

        assertThat(response.serverVersion).isNotEqualTo(errorValue)
        assertThat(response.trashPath).isNotEqualTo(errorValue)

        assertThat(response.serverVersion).isEqualTo(settings.serverVersion)
        assertThat(response.trashPath).isEqualTo(settings.trashPath)
    }
}