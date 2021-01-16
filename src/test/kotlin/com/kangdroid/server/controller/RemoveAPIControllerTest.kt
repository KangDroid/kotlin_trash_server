package com.kangdroid.server.controller

import com.kangdroid.server.domain.TrashData
import com.kangdroid.server.domain.TrashDataRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class RemoveAPIControllerTest {

    @Autowired
    private lateinit var trashDataRepository: TrashDataRepository

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
}