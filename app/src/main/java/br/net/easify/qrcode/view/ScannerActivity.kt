package br.net.easify.qrcode.view

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.net.easify.qrcode.R
import br.net.easify.qrcode.model.QRGeoModel
import br.net.easify.qrcode.model.QRURLModel
import br.net.easify.qrcode.model.QRVCardModel
import com.google.zxing.Result
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_scanner.*
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    override fun handleResult(rawResult: Result?) {
        processRawResult(rawResult!!.text)
        scanAgain.visibility = View.VISIBLE
    }

    private fun processRawResult(text: String?) {
        if (text!!.startsWith("BEGIN:")) {
            val tokens = text?.split("\n".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            val qrVCardModel = QRVCardModel()
            for (i in tokens.indices) {
                if (tokens[i].startsWith("BEGIN:")) {
                    qrVCardModel.type = tokens[i].substring("BEGIN:".length)
                } else if (tokens[i].startsWith("N:")) {
                    qrVCardModel.name = tokens[i].substring("N:".length)
                } else if (tokens[i].startsWith("ORG:")) {
                    qrVCardModel.org = tokens[i].substring("ORG:".length)
                } else if (tokens[i].startsWith("TEL:")) {
                    qrVCardModel.tel = tokens[i].substring("TEL:".length)
                } else if (tokens[i].startsWith("EMAIL:")) {
                    qrVCardModel.email = tokens[i].substring("EMAIL:".length)
                } else if (tokens[i].startsWith("NOTE:")) {
                    qrVCardModel.note = tokens[i].substring("NOTE:".length)
                } else if (tokens[i].startsWith("SUMMARY:")) {
                    qrVCardModel.summary = tokens[i].substring("SUMMARY:".length)
                } else if (tokens[i].startsWith("DTSTART:")) {
                    qrVCardModel.dtstart = tokens[i].substring("DTSTART:".length)
                } else if (tokens[i].startsWith("DTEND:")) {
                    qrVCardModel.dtend = tokens[i].substring("DTEND:".length)
                }
            }
            if ( qrVCardModel.type.equals("VCARD"))
                txt_result.text = qrVCardModel.name
            else
                txt_result.text = qrVCardModel.type

        } else if (text!!.startsWith("http://") ||
            text!!.startsWith("https://") ||
            text!!.startsWith("www.")
        ) {
            var qrurlModel = QRURLModel()
            qrurlModel.url = text!!
            txt_result!!.text = qrurlModel.url
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
        } else {
            txt_result!!.text = text!!
        }
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
