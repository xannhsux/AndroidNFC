package com.salman.nfcreader

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog



import kotlinx.android.synthetic.main.activity_main.*

import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private var intentFiltersArray: Array<IntentFilter>? = null
    private val techListsArray = arrayOf(arrayOf(NfcF::class.java.name))
    private val nfcAdapter: NfcAdapter? by lazy {
        NfcAdapter.getDefaultAdapter(this)
    }
    private var pendingIntent: PendingIntent? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide();
        setContentView(R.layout.activity_main)

        try {

            //nfc process start
            pendingIntent = PendingIntent.getActivity(
                this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
            )
            val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            try {
                ndef.addDataType("text/plain")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }
            intentFiltersArray = arrayOf(ndef)
            if (nfcAdapter == null) {
                val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
                builder.setMessage("此设备不支持 NFC.")
                builder.setPositiveButton("Cancel", null)
                val myDialog = builder.create()
                myDialog.setCanceledOnTouchOutside(false)
                myDialog.show()
                txtviewshopid.setText("此设备不支持NFC，请使用其他设备尝试")
                txtviewmachineid.visibility = View.INVISIBLE

            } else if (!nfcAdapter!!.isEnabled) {
                val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
                builder.setTitle("NFC 已关闭")
                builder.setMessage("请开启 NFC")
                txtviewshopid.setText("NFC 已关闭. 请在设置-》NFC中打开")
                txtviewmachineid.visibility = View.INVISIBLE

                builder.setPositiveButton("Settings") { _, _ -> startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) }
                builder.setNegativeButton("Cancel", null)
                val myDialog = builder.create()
                myDialog.setCanceledOnTouchOutside(false)
                myDialog.show()
            }
        }
        catch (ex:Exception)
        {
            Toast.makeText(applicationContext, ex.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    var machineid="";
    var shopid="";
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val action = intent.action
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == action) {

            val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            with(parcelables) {
                try {
                    val inNdefMessage = this[0] as NdefMessage
                    val inNdefRecords = inNdefMessage.records
                    //if there are many records, you can call inNdefRecords[1] as array
                    var ndefRecord_0 = inNdefRecords[0]
                    var inMessage = String(ndefRecord_0.payload)
                    shopid = inMessage.drop(3);
                    txtviewshopid.setText("SHOP ID: " + shopid)
                    print(inNdefMessage)

                    ndefRecord_0 = inNdefRecords[1]
                    inMessage = String(ndefRecord_0.payload)
                    machineid = inMessage.drop(3);
                    txtviewmachineid.setText("MACHINE ID: " + machineid)


                        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action
                            || NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action
                        ) {

                            val tag =
                                intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
                            val ndef = Ndef.get(tag) ?: return



//
                    } else {
                        try {


                            ndefRecord_0 = inNdefRecords[2]
                            inMessage = String(ndefRecord_0.payload)

                        }
                        catch (ex:Exception){
                            Toast.makeText(applicationContext, "User ID not writted!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (ex: Exception) {
                    Toast.makeText(
                        applicationContext,
                        "There are no Machine and Shop information found!, please click write data to write those!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


        }

    }

    override fun onPause() {
        if (this.isFinishing) {
            nfcAdapter?.disableForegroundDispatch(this)
        }
        super.onPause()
    }
}
