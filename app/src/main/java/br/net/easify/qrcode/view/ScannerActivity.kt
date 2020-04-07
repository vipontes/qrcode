package br.net.easify.qrcode.view

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.net.easify.qrcode.R
import br.net.easify.qrcode.model.QRGeoModel
import br.net.easify.qrcode.model.QRURLModel
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import ezvcard.Ezvcard
import ezvcard.VCard
import kotlinx.android.synthetic.main.activity_scanner.*
import me.dm7.barcodescanner.zxing.ZXingScannerView


class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    override fun handleResult(rawResult: Result?) {
        processRawResult(rawResult!!.text)
        scanAgain.visibility = View.VISIBLE
    }

    private fun processRawResult(text: String?) {
        if (text!!.startsWith("BEGIN:")) {
            val vcard = Ezvcard.parse(text).first()
            txt_result.text = vcard.formattedName.value
            showVCardDialog(vcard)
        } else if (text!!.startsWith("http://") ||
            text!!.startsWith("https://") ||
            text!!.startsWith("www.")
        ) {
            var qrurlModel = QRURLModel()
            qrurlModel.url = text!!
            txt_result!!.text = qrurlModel.url
            startUrl(qrurlModel.url!!)
        } else if (text!!.startsWith("geo:")) {
            val qrGeoModel = QRGeoModel()
            val delims = "[ , ?q= ]+"
            val tokens = text.split(delims.toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            for (i in tokens.indices) {
                if (tokens[i].startsWith("geo:")) {
                    qrGeoModel.lat = tokens[i].substring("geo:".length)
                }
            }
            qrGeoModel.lat = tokens[0].substring("geo:".length)
            qrGeoModel.lng = tokens[1]
            qrGeoModel.geo_place = tokens[2]

            txt_result!!.text = qrGeoModel.lat + "/" + qrGeoModel.lng

            startUrl("https://www.google.com/maps?q=${qrGeoModel.lat},${qrGeoModel.lng}")
        } else {
            txt_result!!.text = text!!
        }
    }

    private fun startUrl(url: String) {
        val openURL = Intent(android.content.Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }

    fun showVCardDialog(card: VCard) {
        var dialog = VCardDialog(card)
        dialog.show(supportFragmentManager,"fragment_vcard")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        Dexter.withActivity(this@ScannerActivity)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    zxScan!!.setResultHandler(this@ScannerActivity)
                    zxScan!!.startCamera()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@ScannerActivity,
                        "You should enable this permission",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {

                }
            }).check()

        scanAgain!!.setOnClickListener {
            zxScan!!.resumeCameraPreview(this@ScannerActivity)
            scanAgain.visibility = View.GONE
        }
    }
}
