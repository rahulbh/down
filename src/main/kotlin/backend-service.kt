// backend/src/main/kotlin/com/downdetector/service/ServiceMonitorService.kt
package com.downdetector.service

import com.downdetector.model.Service
import com.downdetector.model.ServiceStatus
import com.downdetector.repository.ServiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ServiceMonitorService(
    private val serviceRepository: ServiceRepository
) {
    @Scheduled(fixedDelay = 60000) // Every minute
    fun monitorServices() = runBlocking {
        val services = serviceRepository.findAll()
        
        services.map { service ->
            async(Dispatchers.IO) {
                val status = checkServiceStatus(service)
                service.currentStatus = status.status
                service.responseTime = status.responseTime
                service.lastCheckedAt = Instant.now()
                service
            }
        }.map { it.await() }
    }

    private fun checkServiceStatus(service: Service): ServiceStatusResult {
        return try {
            HttpClients.createDefault().use { client ->
                val startTime = System.currentTimeMillis()
                val request = HttpGet(service.url)
                
                val response = client.execute(request)
                val endTime = System.currentTimeMillis()
                
                ServiceStatusResult(
                    status = if (response.code in 200..299) ServiceStatus.ONLINE 
                             else ServiceStatus.DEGRADED,
                    responseTime = endTime - startTime
                )
            }
        } catch (e: Exception) {
            ServiceStatusResult(
                status = ServiceStatus.OFFLINE,
                responseTime = null
            )
        }
    }

    data class ServiceStatusResult(
        val status: ServiceStatus,
        val responseTime: Long?
    )
}
