package `in`.shrtl.app

import App
import ContextHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onStart() {
        super.onStart()
        ContextHelper.currentContext = this
    }

    override fun onStop() {
        super.onStop()
        ContextHelper.currentContext = null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
