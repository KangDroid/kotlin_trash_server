package com.kangdroid.server.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Column

@Entity
class TrashData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = Long.MAX_VALUE,

    @Column(length = 500, nullable = false)
    var cwdLocation: String,

    @Column(length = 500, nullable = false)
    var originalFileDirectory: String,

    @Column(length = 500, nullable = false)
    var trashFileDirectory: String
) : BaseTimeEntity()
