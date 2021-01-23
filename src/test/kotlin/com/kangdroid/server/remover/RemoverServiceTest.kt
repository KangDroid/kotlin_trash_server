package com.kangdroid.server.remover

import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import com.kangdroid.server.dto.TrashDataSaveRequestDto
import com.kangdroid.server.service.TrashDataService
import com.kangdroid.server.settings.Settings
import org.assertj.core.api.Assertions.assertThat
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

    @Test
    fun initMapWorksWell() {
        // Let
        Settings.trashCanPath = "/tmp"
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
        val filedToGet: Field = removerService.javaClass.getDeclaredField("trashList")
        filedToGet.isAccessible = true
        val trashList: ConcurrentHashMap<String, TrashDataSaveRequestDto> = filedToGet.get(removerService) as ConcurrentHashMap<String, TrashDataSaveRequestDto>

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
}