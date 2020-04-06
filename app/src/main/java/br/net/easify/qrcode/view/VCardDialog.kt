package br.net.easify.qrcode.view

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import br.net.easify.qrcode.R
import br.net.easify.qrcode.model.QRVCardModel

class VCardDialog(private var vcardData: QRVCardModel) : DialogFragment() {

    private lateinit var closeButton: ImageView
    private lateinit var nameText: TextView
    private lateinit var titleText: TextView
    private lateinit var emailText: TextView
    private lateinit var telText: TextView
    private lateinit var cellText: TextView
    private lateinit var urlText: TextView
    private lateinit var addressText: TextView
    private lateinit var orgText: TextView
    private lateinit var summaryText: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view: View = activity?.layoutInflater!!.inflate(R.layout.fragment_vcard, null)

        closeButton = view.findViewById(R.id.closeButton)
        emailText = view.findViewById(R.id.emailText)
        nameText = view.findViewById(R.id.nameText)
        titleText = view.findViewById(R.id.titleText)
        telText = view.findViewById(R.id.telText)
        cellText = view.findViewById(R.id.cellText)
        urlText = view.findViewById(R.id.urlText)
        addressText = view.findViewById(R.id.addressText)
        orgText = view.findViewById(R.id.orgText)
        summaryText = view.findViewById(R.id.summaryText)

        val alert = AlertDialog.Builder(activity)
        alert.setView(view)

        closeButton.setOnClickListener {
            dismiss()
        }

        if ( vcardData.fullName.isNullOrEmpty() ) {
            nameText.text = vcardData.name
        } else {
            nameText.text = vcardData.fullName
        }
        titleText.text = vcardData.title
        emailText.text = vcardData.email
        telText.text = vcardData.tel
        cellText.text = vcardData.cell
        urlText.text = vcardData.url
        addressText.text = vcardData.address
        orgText.text = vcardData.org
        summaryText.text = vcardData.summary

        return alert.create()
    }

    override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
        dialog!!.window!!.attributes.windowAnimations = R.style.AnimSlideUpBottom
    }
}