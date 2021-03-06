package com.kangdroid.server.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

interface TrashDataRepository : JpaRepository<TrashData, Long> {
    @Query("SELECT t FROM TrashData t ORDER BY t.id DESC")
    fun findAllDesc(): List<TrashData>

    @Transactional(readOnly = true)
    fun findByTrashFileDirectoryEquals(input: String): TrashData?

    @Transactional
    fun deleteByTrashFileDirectory(input: String)
}