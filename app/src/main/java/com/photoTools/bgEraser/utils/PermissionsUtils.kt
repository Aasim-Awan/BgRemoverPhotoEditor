package com.photoTools.bgEraser

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi

object PermissionsUtils {

    var storage_permissions: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var storage_permissions_33: Array<String> = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )

    @RequiresApi(api = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    var getStorage_permissions_34: Array<String> = arrayOf(
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
        Manifest.permission.READ_MEDIA_IMAGES
    )

    fun getStoragePermission(): List<String> {
        val perm=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getStorage_permissions_34
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            storage_permissions_33
        } else {
            storage_permissions
        }
        return perm.toMutableList()
    }

    fun getCameraPermission(): List<String> {
        val perm=if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            storage_permissions_33
        } else {
            storage_permissions
        }
        val perms = listOf(*perm)
        val temp: MutableList<String> = ArrayList()
        temp.add(Manifest.permission.CAMERA)
        temp.addAll(perms)
        return temp
    }

}