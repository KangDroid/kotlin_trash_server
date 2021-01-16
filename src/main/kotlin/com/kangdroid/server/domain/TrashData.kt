package com.kangdroid.server.domain

import javax.persistence.*

@Entity
class TrashData(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 20,

    @Column(length = 500, nullable =  false)
    var cwdLocation: String,

    @Column(length = 500, nullable = false)
    var originalFileDirectory: String,

    @Column(length = 500, nullable = false)
    var trashFileDirectory: String
) : BaseTimeEntity()
