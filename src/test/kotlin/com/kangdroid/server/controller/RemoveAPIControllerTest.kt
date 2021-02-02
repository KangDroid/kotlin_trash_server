package com.kangdroid.server.controller

import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import com.kangdroid.server.dto.TrashDataRestoreRequestDto
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.remover.RemoverService
import com.kangdroid.server.service.TrashDataService
import com.kangdroid.server.settings.Settings
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForObject
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import java.io.File

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

    @Autowired
    private lateinit var removerService: RemoverService

    @Autowired
    private lateinit var settings: Settings

    @Autowired
    private lateinit var dataService: TrashDataService

    @Before
    fun cleanDb() {
        File(settings.trashPath).mkdir()
    }

    @After
    fun cleanDirectory() {
        File(settings.trashPath).deleteRecursively()
        dataService.deleteAll()
    }

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
        dataService.saveData(
            TrashDataSaveRequestDto(
                id = 0,
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

    @Test
    fun isRestoreWorking() {
        // Let
        val testFileObject: File = File(settings.trashPath, "KDRtest.txt").also {
            if (!it.exists()) {
                it.createNewFile()
            }
        }

        dataService.saveData(
            TrashDataSaveRequestDto(
                id = 0,
                cwdLocation = "TEST",
                originalFileDirectory = File(System.getProperty("java.io.tmpdir"), "Test2.txt").absolutePath,
                trashFileDirectory = testFileObject.absolutePath
            )
        )

        val innerCount: Int = dataService.size()

        // Do work
        val url: String = "http://localhost:" + this.port + "/api/trash/data/restore"
        val restTemplate: TestRestTemplate = TestRestTemplate()
        val responseMessage: String = restTemplate.postForObject(url, TrashDataRestoreRequestDto(testFileObject.absolutePath), String::class)
            ?: "Error"


//        val returnMessage: String = removerService.restore(TrashDataRestoreRequestDto(testFileObject.absolutePath))

        // Assert
        val targetFileObject: File = File(System.getProperty("java.io.tmpdir"), "Test2.txt")
        assertThat(responseMessage).isEqualTo(removerService.RESTORE_FULL_SUCCESS)
        assertThat(dataService.size()).isEqualTo(innerCount-1)
        assertThat(targetFileObject.exists()).isEqualTo(true)
        assertThat(testFileObject.exists()).isEqualTo(false)

        // Cleanup
        if (testFileObject.exists()) {
            testFileObject.delete()
        }

        if (targetFileObject.exists()) {
            targetFileObject.delete()
        }
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