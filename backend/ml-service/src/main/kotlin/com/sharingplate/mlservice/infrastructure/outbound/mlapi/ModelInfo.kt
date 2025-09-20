package com.sharingplate.mlservice.infrastructure.outbound.mlapi

data class ModelInfo(
    val input_name: String,
    val input_shape: List<Int>,
    val model_exists: Boolean,
    val model_loaded: Boolean,
    val model_path: String,
    val output_name: String,
    val output_shape: List<Int>
)