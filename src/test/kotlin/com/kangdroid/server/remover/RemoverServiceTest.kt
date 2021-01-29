package com.kangdroid.server.remover

import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import com.kangdroid.server.dto.TrashDataRestoreRequestDto
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
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoverServiceTest {

    @Autowired
    private lateinit var trashDataRepository: TrashDataRepository

    @Autowired
    private lateinit var dataService: TrashDataService

    @Autowired
    private lateinit var settings: Settings

    @Autowired
    private lateinit var removerService: RemoverService

    @Before
    fun initTest() {
        removerService.trashList.clear()
    }

    @Test
    fun initMapWorksWell() {
        // Let
        val testCwdLocation: String = "/tmp"
        val testOriginalFileDirectory: String = "/tmp/test.txt"
        val testTrashFileDirectory: String = "${settings.trashPath}/test.txt"
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
        val targetTestFile: String = "${settings.trashPath}/KDRTestFileAfter.txt"
        val fileObject: File = File(targetTestFile)

        if (!fileObject.exists()) {
            fileObject.createNewFile()
        }

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
        // make sure there is no test.txt on target location.
        val fileObject: File = File(settings.trashPath, File(testFileLocation).name)
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
        // Add testfileLocation to hashMap
        val fileObject: File = File(settings.trashPath, File(testFileLocation).name)
        removerService.trashList[fileObject.absolutePath.toString()] = TrashDataSaveRequestDto() // empty should work.

        // test it.
        assertThat(removerService.checkTrashCan(testFileLocation)).isNotEqualTo(fileObject.absolutePath.toString())
    }

    @Test
    fun isRestoreWorkingWellTrue() {
        // Let
        val testFileObject: File = File(settings.trashPath, "KDRtest.txt").also {
            if (!it.exists()) {
                it.createNewFile()
            }
        }

        removerService.trashList[testFileObject.absolutePath] = TrashDataSaveRequestDto(
            id = 0,
            cwdLocation = "TEST",
            originalFileDirectory = "/tmp/Test2.txt",
            trashFileDirectory = testFileObject.absolutePath
        )

        val innerCount: Int = removerService.trashList.size

        val returnMessage: String = removerService.restore(TrashDataRestoreRequestDto(testFileObject.absolutePath))

        // Assert
        val targetFileObject: File = File("/tmp/Test2.txt")
        assertThat(returnMessage).isEqualTo(removerService.RESTORE_FULL_SUCCESS)
        assertThat(removerService.trashList.size).isEqualTo(innerCount-1)
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

    @Test
    fun isRestoreWorkingWellFalse() {
        // Let
        val testFileObject: File = File(settings.trashPath, "KDRtest.txt").also {
            if (!it.exists()) {
                it.createNewFile()
            }
        }

        val targetFileAddition: File = File("/tmp/Test2.txt").also {
            if (!it.exists()) {
                it.createNewFile()
            }
        }

        removerService.trashList[testFileObject.absolutePath] = TrashDataSaveRequestDto(
            id = 0,
            cwdLocation = "TEST",
            originalFileDirectory = "/tmp/Test2.txt",
            trashFileDirectory = testFileObject.absolutePath
        )

        val innerCount: Int = removerService.trashList.size

        var returnMessage: String = removerService.restore(TrashDataRestoreRequestDto(testFileObject.absolutePath))

        // Assert - TARGET_EXISTS
        assertThat(returnMessage).isEqualTo(removerService.RESTORE_TARGET_EXISTS)
        assertThat(removerService.trashList.size).isEqualTo(innerCount)
        assertThat(targetFileAddition.exists()).isEqualTo(true)
        assertThat(testFileObject.exists()).isEqualTo(true)

        // Assert - TARGET NOT ON MAP
        removerService.trashList.clear()
        returnMessage = removerService.restore(TrashDataRestoreRequestDto(testFileObject.absolutePath))
        assertThat(returnMessage).isEqualTo(removerService.RESTORE_TARGET_NOT_ON_MAP)
        assertThat(removerService.trashList.size).isEqualTo(0)
        assertThat(targetFileAddition.exists()).isEqualTo(true)
        assertThat(testFileObject.exists()).isEqualTo(true)


        // Cleanup
        if (testFileObject.exists()) {
            testFileObject.delete()
        }

        if (targetFileAddition.exists()) {
            targetFileAddition.delete()
        }
    }
}