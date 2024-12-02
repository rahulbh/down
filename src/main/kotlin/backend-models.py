// backend/src/main/kotlin/com/downdetector/model/Service.kt
package com.downdetector.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

enum class CheckType {
    HTTP, TCP, PING
}

enum class ServiceStatus {
    ONLINE, OFFLINE, DEGRADED
}

@Entity
@Table(name = "services")
data class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(unique = true)
    val name: String,

    @NotBlank
    val url: String,

    @Enumerated(EnumType.STRING)
    val checkType: CheckType = CheckType.HTTP,

    val timeout: Int = 5000,

    @Transient
    var currentStatus: ServiceStatus? = null,

    @Transient
    var responseTime: Long? = null,

    @Transient
    var lastCheckedAt: Instant? = null
)

data class ServiceStatusResponse(
    val id: Long?,
    val name: String,
    val url: String,
    val status: ServiceStatus,
    val responseTime: Long?,
    val lastCheckedAt: Instant?
)
