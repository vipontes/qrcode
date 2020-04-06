package br.net.easify.qrcode.view

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.net.easify.qrcode.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {
    private var bitmap: Bitmap? = null
    private var savedImage = ""

    companion object {
        const val qrCodeWidth = 512
        private val qrCodeDir = "qrcodeimgs"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Dexter.withActivity(this@MainActivity)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {

                }
            }).check()

        createQrCode!!.setOnClickListener {

            val inputMethodManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                qrCodeData.windowToken,
                InputMethodManager.RESULT_UNCHANGED_SHOWN
            )

            saveQrCode.visibility = View.GONE
            deleteImageFile()

            if (qrCodeData!!.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter String!", Toast.LENGTH_SHORT).show()
            } else {
                val qrCodeData = qrCodeData!!.text.toString()
                try {
                    bitmap = TextToImageEncode(qrCodeData)
                    qrCodeImage!!.setImageBitmap(bitmap)
                    savedImage = saveImage(bitmap)
                    if (savedImage.isNotEmpty()) {
                        saveQrCode.visibility = View.VISIBLE
                    }

                } catch (e: WriterException) {
                    e.printStackTrace()
                }
            }
        }

        saveQrCode!!.setOnClickListener {
            if (savedImage.isNotEmpty()) {
                shareImage(bitmap!!)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.scanMenuItem -> {
                var intent = Intent(this, ScannerActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    // Share image
    private fun shareImage(imageToShare: Bitmap) {
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"
        val bytes = ByteArrayOutputStream()
        imageToShare.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            contentResolver,
            imageToShare, "qrcode", null
        )
        val imageUri = Uri.parse(path)
        share.putExtra(Intent.EXTRA_STREAM, imageUri)
        startActivity(Intent.createChooser(share, "Compartilhar com"))
    }

    fun createImagesDirectory(): String? {
        val cw = ContextWrapper(this)
        val directory =
            cw.getDir(qrCodeDir, Context.MODE_PRIVATE)
        if (!directory.exists()) {
            directory.mkdir()
        }
        return directory.absolutePath
    }

    fun saveImage(myBitmap: Bitmap?): String {
        val bytes = ByteArrayOutputStream()
        myBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val imagesDir = createImagesDirectory()
        try {
            val f = File(imagesDir, Calendar.getInstance().timeInMillis.toString() + ".jpg")
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            MediaScannerConnection.scanFile(
                this,
                arrayOf(f.path),
                arrayOf("image/jpeg"), null
            )
            fo.close()
            return f.absolutePath
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

        return ""
    }

    private fun TextToImageEncode(Value: String): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(
                Value,
                BarcodeFormat.QR_CODE,
                qrCodeWidth,
                qrCodeWidth, null
            )

        } catch (Illegalargumentexception: IllegalArgumentException) {

            return null
        }

        val bitMatrixWidth = bitMatrix.getWidth()

        val bitMatrixHeight = bitMatrix.getHeight()

        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth

            for (x in 0 until bitMatrixWidth) {

                pixels[offset + x] = if (bitMatrix.get(x, y))
                    resources.getColor(R.color.black)
                else
                    resources.getColor(R.color.white)
            }
        }
        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)

        bitmap.setPixels(pixels, 0,
            qrCodeWidth, 0, 0, bitMatrixWidth, bitMatrixHeight)
        return bitmap
    }

    fun deleteImageFile() {
        if (savedImage.isNotEmpty()) {
            val fileToDelete = File(savedImage)
            if (fileToDelete.exists()) {
                if (fileToDelete.delete()) {
                    println("file Deleted :$savedImage")
                } else {
                    println("file not Deleted :$savedImage")
                }
            }
            savedImage = ""
        }
    }

    override fun onDestroy() {
        deleteImageFile()
        super.onDestroy()
    }
}
