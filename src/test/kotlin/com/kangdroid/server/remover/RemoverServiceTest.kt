package com.kangdroid.server.remover

import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.service.TrashDataService
import com.kangdroid.server.settings.Settings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoverServiceTest {

    @Autowired
    private lateinit var trashDataRepository: TrashDataRepository

    @Autowired
    private lateinit var dataService: TrashDataService

    @Before
    fun initTest() {
        Settings.trashCanPath = "/tmp"
    }

    @Test
    fun initMapWorksWell() {
        // Let
        val testCwdLocation: String = "/tmp"
        val testOriginalFileDirectory: String = "/tmp/test.txt"
        val testTrashFileDirectory: String = "${Settings.trashCanPath}/test.txt"
        val fileObject: File = File(testTrashFileDirectory)
        if (!fileObject.exists()) {
            fileObject.createNewFile()
        }
        trashDataRepository.save(
            TrashData(
                id = 0,
                cwdLocation = testCwdLocation,
                originalFileDirectory = testOriginalFileDirectory,
                trashFileDirectory = testTrashFileDirectory
            )
        )

        val removerService: RemoverService = RemoverService(dataService)
        val methodToTest: Method = removerService.javaClass.getDeclaredMethod("initMap")
        methodToTest.isAccessible = true

        // Execute
        methodToTest.invoke(removerService)

        // Get Values
        val trashList: ConcurrentHashMap<String, TrashDataSaveRequestDto> = removerService.trashList

        // Assert!
        assertThat(trashList.containsKey(testTrashFileDirectory)).isEqualTo(true)

        val valueToCheck: TrashDataSaveRequestDto = trashList[testTrashFileDirectory]!! // assert above.
        assertThat(valueToCheck.trashFileDirectory).isEqualTo(testTrashFileDirectory)
        assertThat(valueToCheck.cwdLocation).isEqualTo(testCwdLocation)
        assertThat(valueToCheck.originalFileDirectory).isEqualTo(testOriginalFileDirectory)

        if (fileObject.exists()) {
            fileObject.delete()
        }
    }

    @Test
    fun initDataWorksWell() {
        // after init map, initdata looks for any externally-new added file.
        val targetTestFile: String = "${Settings.trashCanPath}/KDRTestFileAfter.txt"
        val fileObject: File = File(targetTestFile)

        if (!fileObject.exists()) {
            fileObject.createNewFile()
        }

        val removerService: RemoverService = RemoverService(dataService)
        val methodToTest: Method = removerService.javaClass.getDeclaredMethod("initData")
        methodToTest.isAccessible = true

        // Execute
        methodToTest.invoke(removerService)

        val trashList: ConcurrentHashMap<String, TrashDataSaveRequestDto> = removerService.trashList

        //Assert
        assertThat(trashList.containsKey(targetTestFile)).isEqualTo(true)

        val testObject: TrashDataSaveRequestDto = trashList[targetTestFile]!!
        assertThat(testObject.trashFileDirectory).isEqualTo(targetTestFile)
        assertThat(testObject.cwdLocation).isEqualTo("EXTERNAL")
        assertThat(testObject.originalFileDirectory).isEqualTo("EXTERNAL")

        if (fileObject.exists()) {
            fileObject.delete()
        }
    }

    @Test
    fun checkTrashCanWorksWellNormal() {
        // Let
        val testFileLocation: String = "/tmp/test.txt"
        val removerService: RemoverService = RemoverService(dataService)
        // make sure there is no test.txt on target location.
        val fileObject: File = File(Settings.trashCanPath, File(testFileLocation).name)
        if (fileObject.exists()) {
            fileObject.delete()
        }

        // test it.
        assertThat(removerService.checkTrashCan(testFileLocation)).isEqualTo(fileObject.absolutePath.toString())
    }

    @Test
    fun checkTrashCnWorksWellDuplicate() {
        // Let
        val testFileLocation: String = "/tmp/test.txt"
        val removerService: RemoverService = RemoverService(dataService)
        // Add testfileLocation to hashMap
        val fileObject: File = File(Settings.trashCanPath, File(testFileLocation).name)
        removerService.trashList[fileObject.absolutePath.toString()] = TrashDataSaveRequestDto() // empty should work.

        // test it.
        assertThat(removerService.checkTrashCan(testFileLocation)).isNotEqualTo(fileObject.absolutePath.toString())
    }
}