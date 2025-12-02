package com.princelumpy.breakvault.data

import kotlinx.serialization.Serializable

@Serializable
enum class EnergyLevel {
    LOW, MEDIUM, HIGH, NONE
}

@Serializable
enum class TrainingStatus {
    READY,    // Fire icon
    TRAINING  // Hammer icon
}
