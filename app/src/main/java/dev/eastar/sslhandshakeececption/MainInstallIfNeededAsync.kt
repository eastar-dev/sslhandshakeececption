package dev.eastar.sslhandshakeececption

import android.content.*
import android.log.Log
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.security.ProviderInstaller
import dev.eastar.sslhandshakeececption.databinding.ActivityMainBinding


class MainInstallIfNeededAsync : AppCompatActivity(), ProviderInstaller.ProviderInstallListener {
    companion object {
        const val ERROR_DIALOG_REQUEST_CODE = 1
    }

    private var retryProviderInstall: Boolean = false

    //Update the security provider when the activity is created.
    private lateinit var bb: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ProviderInstaller.installIfNeededAsync(this, this)
        bb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bb.root)
    }


    /**
     * This method is only called if the provider is successfully updated
     * (or is already up-to-date).
     */
    override fun onProviderInstalled() {
        Log.toast(this, "onProviderInstalled")

        bb.button.setOnClickListener { run(this) }
    }

    /**
     * This method is called if updating fails; the error code indicates
     * whether the error is recoverable.
     */
    override fun onProviderInstallFailed(errorCode: Int, recoveryIntent: Intent?) {
        GoogleApiAvailability.getInstance().apply {
            if (isUserResolvableError(errorCode)) {
                // Recoverable error. Show a dialog prompting the user to
                // install/update/enable Google Play services.
                showErrorDialogFragment(this@MainInstallIfNeededAsync, errorCode, ERROR_DIALOG_REQUEST_CODE) {
                    // The user chose not to take the recovery action
                    onProviderInstallerNotAvailable()
                }
            } else {
                onProviderInstallerNotAvailable()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ERROR_DIALOG_REQUEST_CODE) {
            // Adding a fragment via GoogleApiAvailability.showErrorDialogFragment
            // before the instance state is restored throws an error. So instead,
            // set a flag here, which will cause the fragment to delay until
            // onPostResume.
            retryProviderInstall = true
        }
    }

    /**
     * On resume, check to see if we flagged that we need to reinstall the
     * provider.
     */
    override fun onPostResume() {
        super.onPostResume()
        if (retryProviderInstall) {
            // We can now safely retry installation.
            ProviderInstaller.installIfNeededAsync(this, this)
        }
        retryProviderInstall = false
    }

    private fun onProviderInstallerNotAvailable() {
        // This is reached if the provider cannot be updated for some reason.
        // App should consider all HTTP communication to be vulnerable, and take
        // appropriate action.
    }
}
