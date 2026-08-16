package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.navigation.NavGraph
import com.example.ui.theme.DastavejBoxTheme
import com.example.viewmodel.VaultViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: VaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DastavejBoxTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset system picker active flag when activity resumes
        viewModel.resetSystemPickerActive()
    }

    override fun onStop() {
        super.onStop()
        // If launching system file manager or camera, bypass auto-lock
        if (!viewModel.isLaunchingSystemPicker.value) {
            viewModel.lockVault()
        }
    }
}
