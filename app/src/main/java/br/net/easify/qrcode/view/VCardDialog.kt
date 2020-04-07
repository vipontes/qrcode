package br.net.easify.qrcode.view

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import br.net.easify.qrcode.R
import ezvcard.VCard
import ezvcard.parameter.TelephoneType

class VCardDialog(private var vcardData: VCard) : DialogFragment() {

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

        nameText.text = vcardData.formattedName.value
        orgText.text = vcardData.organization.values.first()
        if (vcardData.titles.size > 0) titleText.text = vcardData.titles.first().value
        if (vcardData.emails.size > 0) emailText.text = vcardData.emails.first().value
        if (vcardData.telephoneNumbers.size > 0) {
            val phoneList = vcardData.telephoneNumbers
            for (item in phoneList) {
                val types = item.types
                if (types.contains(TelephoneType.CELL) || types.contains(TelephoneType.TEXTPHONE)) {
                    cellText.text = item.text
                }
                if (types.contains(TelephoneType.HOME) || types.contains(TelephoneType.WORK)) {
                    telText.text = item.text
                }
            }
        }
        urlText.text = vcardData.urls.first().value
        if (vcardData.addresses.size > 0) addressText.text =
            vcardData.addresses.first().streetAddressFull
        if (vcardData.notes.size > 0) summaryText.text = vcardData.notes.first().value

        return alert.create()
    }

    override fun onActivityCreated(arg0: Bundle?) {
        super.onActivityCreated(arg0)
        dialog!!.window!!.attributes.windowAnimations = R.style.AnimSlideUpBottom
    }
}