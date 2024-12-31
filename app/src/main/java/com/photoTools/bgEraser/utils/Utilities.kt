
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdSize
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

object Utilities {

    fun showDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String? = null,
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit) = {},
        icon: Drawable? = null,
    ) {
        val builder = AlertDialog.Builder(context)

        builder.setTitle(title)
        builder.setMessage(message)

        icon?.let {
            builder.setIcon(it)
        }

        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveClick()
            dialog.dismiss()
        }

        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText) { dialog, _ ->
                onNegativeClick()
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        dialog.show()
    }

    fun saveTempImageToFile(context: Context, bitmap: Bitmap): File? {
        return try {
            val fileName = "filtered_image${Calendar.getInstance().timeInMillis}.png"
            val file = File(context.getExternalFilesDir("Temp_filters"), fileName)

            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            file

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        } catch (e: java.lang.Exception) {
            Log.e("URI to RealPath", "getRealPathFromURI Exception : $e")
            return ""
        } finally {
            cursor?.close()
        }
    }

    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        val picturesDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Photo Editor"
        )
        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
        }

        val fileName = "image_${System.currentTimeMillis()}.png"
        val imageFile = File(picturesDir, fileName)

        return try {
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
            }

            MediaScannerConnection.scanFile(
                context,
                arrayOf(imageFile.absolutePath),
                null
            ) { path, uri ->
                Log.d("SaveBitmap", "Image saved and scanned: $path")
            }

            imageFile.absolutePath // Return the file path
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
            null
        }
    }


    fun captureScreenShot(viewToCapture: View): Bitmap {
        viewToCapture.background = null
        viewToCapture.destroyDrawingCache()
        viewToCapture.isDrawingCacheEnabled = true

        val bitmap = Bitmap.createBitmap(viewToCapture.drawingCache)
        viewToCapture.isDrawingCacheEnabled = false

        return Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888).apply {
            val canvas = Canvas(this)
            viewToCapture.draw(canvas)
        }
    }

    fun deleteAllFiles(context: Context) {
        val directory = context.getExternalFilesDir("Temp_filters")
        if (directory!!.exists()) {
            directory.deleteRecursively()
        }
    }

    fun getAdSize(activity: Activity): AdSize {
        val display: Display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

}
