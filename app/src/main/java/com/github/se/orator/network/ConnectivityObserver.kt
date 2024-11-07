package com.github.se.orator.network
import kotlinx.coroutines.flow.Flow

// Interface to observe connectivity status changes
interface ConnectivityObserver {

    // Observes and emits network statuses as a Flow
    fun observe(): Flow<Status>

    // Enum to represent possible network statuses
    enum class Status {
        Available, Unavailable
    }
}
