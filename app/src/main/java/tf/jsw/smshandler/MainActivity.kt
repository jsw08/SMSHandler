package tf.jsw.smshandler

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import android.Manifest.permission;
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import tf.jsw.smshandler.ui.theme.SMSHandlerTheme

class MainActivity : ComponentActivity() {
    private val requiredPermissions = arrayOf(
        permission.READ_SMS,
        permission.RECEIVE_SMS,
        permission.SEND_SMS,
        permission.READ_PHONE_STATE,
        permission.READ_CONTACTS,
        permission.ACCESS_FINE_LOCATION,
        permission.ACCESS_BACKGROUND_LOCATION,
        permission.FOREGROUND_SERVICE,
        permission.FOREGROUND_SERVICE_SPECIAL_USE,
        permission.POST_NOTIFICATIONS,
        permission.ACCESS_NOTIFICATION_POLICY,
        permission.CALL_PHONE
    );

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SMSHandlerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Toast.makeText(this, "Please set d&d settings.", Toast.LENGTH_SHORT).show()

            val intent = Intent(
                Settings
                    .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
            )
            startActivity(intent)
        }

        startService()

    }

    private fun startService() {
        val serviceIntent = Intent(this, ForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }
    @Composable

    fun App() {
        val hasPermissions = remember { checkPermissions() }
        if (hasPermissions) {
            Text(text = "HI")
        } else {
            RequestPermissions()
        }
    }


    @Composable
    fun RequestPermissions() {
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                Text(text = "HI")
            } else {
                Text(text = "NO PERMISSIONS")
            }
        }
        LaunchedEffect(Unit) {
            requestPermissionLauncher.launch(requiredPermissions)
        }
    }

    private fun checkPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SMSHandlerTheme {
        Greeting("Android")
    }
}