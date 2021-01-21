package com.kangdroid.server.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import com.kangdroid.server.dto.TrashDataResponseDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForObject
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoveAPIControllerTest {

    @LocalServerPort
    private var port: Int? = null

    @Autowired
    private lateinit var trashDataRepository: TrashDataRepository

    @Autowired
    private lateinit var mvc: MockMvc

    @Test
    fun getApiWorksWell() {
        // let
        val cwdLocation: String = "/home/kangdroid"
        val originalDirectory: String = "/home/kangdroid/test.txt"
        val trashFileDirectory: String = "/home/kangdroid/.Trash/test.txt"

        // Register
        trashDataRepository.save(
            TrashData(
                cwdLocation = cwdLocation,
                originalFileDirectory = originalDirectory,
                trashFileDirectory = trashFileDirectory
            )
        )

        // When
        val trashList: List<TrashData> = trashDataRepository.findAll()

        // then
        val trashData: TrashData = trashList.get(0)

        assertThat(trashData.id).isEqualTo(1)
        assertThat(trashData.cwdLocation).isEqualTo(cwdLocation)
        assertThat(trashData.originalFileDirectory).isEqualTo(originalDirectory)
        assertThat(trashData.trashFileDirectory).isEqualTo(trashFileDirectory)
    }

    @Test
    fun serverAliveChecker() {
        val url: String = "http://localhost:" + this.port + "/api/alive"
        val restTemplate: TestRestTemplate = TestRestTemplate()
        val response: String = restTemplate.getForObject(url, String::class.java)

        // check
        assertThat(response).isEqualTo("Server is Running!")
    }

    @Test
    fun getAllDataWorksWell() {
        // let
        val cwdLocation: String = "/home/kangdroid"
        val originalDirectory: String = "/home/kangdroid/test.txt"
        val trashFileDirectory: String = "/home/kangdroid/.Trash/test.txt"
        val url: String = "http://localhost:" + this.port + "/api/trash/data/all"

        // Register
        trashDataRepository.save(
            TrashData(
                cwdLocation = cwdLocation,
                originalFileDirectory = originalDirectory,
                trashFileDirectory = trashFileDirectory
            )
        )

        // Do work
        val restTemplate: TestRestTemplate = TestRestTemplate()
        val listData: Array<TrashData> = restTemplate.getForObject(url, Array<TrashData>::class.java)

        // Assert
        assertThat(listData[0].originalFileDirectory).isEqualTo(originalDirectory)
        assertThat(listData[0].cwdLocation).isEqualTo(cwdLocation)
        assertThat(listData[0].trashFileDirectory).isEqualTo(trashFileDirectory)
    }

//    @Test
//    fun postApiWorksWell() {
//        // let
//        val cwdLocation: String = "/home/kangdroid"
//        val originalDirectory: String = "/home/kangdroid/test.txt"
//        val trashFileDirectory: String = "/home/kangdroid/.Trash/test.txt"
//
//        val trashDataSaveRequestDto: TrashDataSaveRequestDto = TrashDataSaveRequestDto(
//            cwdLocation, originalDirectory
//        )
//
//        // when
//        val url: String = "http://localhost:" + this.port + "/api/trash/data"
//
//        mvc.perform(
//            post(url)
//                .contentType(MediaType.APPLICATION_JSON_UTF8)
//                .content(ObjectMapper().writeValueAsString(trashDataSaveRequestDto))
//        ).andExpect(status().isOk)
//
//        val allList: List<TrashData> = trashDataRepository.findAll()
//        assertThat(allList.get(0).cwdLocation).isEqualTo(cwdLocation)
//        assertThat(allList.get(0).originalFileDirectory).isEqualTo(originalDirectory)
//    }
}