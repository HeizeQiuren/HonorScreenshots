package com.honorshots.screenshot.ui

data class PermissionState(
    val hasOverlayPermission: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val hasProjectionPermission: Boolean = false,
    val isServiceRunning: Boolean = false,
    val screenshotCount: Int = 0,
    val toastMessage: String? = null,
    val videoFolderPath: String? = null
) {
    val allPermissionsGranted: Boolean
        get() = hasOverlayPermission && hasStoragePermission && hasProjectionPermission

    val canStartService: Boolean
        get() = hasOverlayPermission && hasProjectionPermission
}
