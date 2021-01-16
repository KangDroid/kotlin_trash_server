package com.kangdroid.server.domain

import org.springframework.data.jpa.repository.JpaRepository

interface TrashDataRepository : JpaRepository<TrashData, Long> {
}