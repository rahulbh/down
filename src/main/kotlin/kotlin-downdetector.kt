// Build.gradle.kts (Module level)
plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
}

// Main Application File
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

data class ServiceStatus(
    val name: String,
    val url: String,
    val isAvailable: Boolean = false,
    val responseTime: Long = 0,
    val lastChecked: Long = System.currentTimeMillis()
)

class DowndetectorViewModel {
    private val httpClient = OkHttpClient()
    private val services = mutableStateListOf(
        ServiceStatus("GitHub", "https://github.com"),
        ServiceStatus("Google", "https://google.com"),
        ServiceStatus("AWS", "https://aws.amazon.com")
    )

    fun getServices(): List<ServiceStatus> = services

    suspend fun checkServiceStatus(service: ServiceStatus): ServiceStatus {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val request = Request.Builder().url(service.url).build()
                val response = httpClient.newCall(request).execute()
                val endTime = System.currentTimeMillis()

                service.copy(
                    isAvailable = response.isSuccessful,
                    responseTime = endTime - startTime,
                    lastChecked = System.currentTimeMillis()
                )
            } catch (e: IOException) {
                service.copy(
                    isAvailable = false,
                    responseTime = -1,
                    lastChecked = System.currentTimeMillis()
                )
            }
        }
    }

    suspend fun refreshAllServices() {
        services.indices.forEach { index ->
            val updatedService = checkServiceStatus(services[index])
            services[index] = updatedService
        }
    }
}

@Composable
fun DowndetectorApp(viewModel: DowndetectorViewModel) {
    val services by remember { mutableStateOf(viewModel.getServices()) }
    val coroutineScope = rememberCoroutineScope()

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Service Status", style = MaterialTheme.typography.h5)
            
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.refreshAllServices()
                    }
                }
            ) {
                Text("Refresh All Services")
            }

            Spacer(modifier = Modifier.height(16.dp))

            services.forEach { service ->
                ServiceStatusRow(service)
            }
        }
    }
}

@Composable
fun ServiceStatusRow(service: ServiceStatus) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(service.name, style = MaterialTheme.typography.subtitle1)
            Text(service.url, style = MaterialTheme.typography.caption)
        }

        val statusColor = if (service.isAvailable) Color.Green else Color.Red
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(statusColor)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = if (service.isAvailable) "Online" else "Offline",
            color = statusColor
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = if (service.responseTime > 0) "${service.responseTime} ms" else "N/A",
            style = MaterialTheme.typography.body2
        )
    }
}

fun main() = application {
    val viewModel = DowndetectorViewModel()

    Window(onCloseRequest = ::exitApplication) {
        DowndetectorApp(viewModel)
    }
}
