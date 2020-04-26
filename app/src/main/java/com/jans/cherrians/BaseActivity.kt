package com.jans.cherrians

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {
    /*internal interface SnackbarCallback {
        fun show()
        fun dismiss()
    }
*/
    private var mSnackBar: Snackbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(ConnectivityReceiver(),
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    protected fun showMessage(isConnected: Boolean) {
        if (!isConnected) {
            val messageToUser = "You are offline now."
            mSnackBar = Snackbar.make(findViewById(R.id.rootLayout), messageToUser, Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
            mSnackBar?.duration = Snackbar.LENGTH_INDEFINITE
            mSnackBar?.show()
        } else {
            mSnackBar?.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showMessage(isConnected)
    }
}