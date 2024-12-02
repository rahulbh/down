// backend/src/main/kotlin/com/downdetector/controller/ServiceController.kt
package com.downdetector.controller

import com.downdetector.model.Service
import com.downdetector.model.ServiceStatusResponse
import com.downdetector.repository.ServiceRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/services")
class ServiceController(
    private val serviceRepository: ServiceRepository
) {
    @PostMapping
    fun createService(@RequestBody service: Service): Service {
        return serviceRepository.save(service)
    }

    @GetMapping
    fun getAllServices(): List<ServiceStatusResponse> {
        return serviceRepository.findAll().map { service ->
            ServiceStatusResponse(
                id = service.id,
                name = service.name,
                url = service.url,
                status = service.currentStatus ?: ServiceStatus.OFFLINE,
                responseTime = service.responseTime,
                lastCheckedAt = service.lastCheckedAt
            )
        }
    }

    @DeleteMapping("/{id}")
    fun deleteService(@PathVariable id: Long): ResponseEntity<Void> {
        serviceRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}
