package com.tristamisland

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tristamisland.domain.island.loadIsland
import com.tristamisland.presentation.IslandApp
import com.tristamisland.presentation.IslandViewModel
import com.tristamisland.presentation.IslandViewModelFactory
import com.tristamisland.presentation.theme.TristamTheme
import com.tristamisland.presentation.theme.GothicColors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(GothicColors.page.toArgb()))
        setContent {
            TristamTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = GothicColors.page) {
                    val app = application as TristamApp
                    val context = LocalContext.current
                    val story = remember {
                        loadIsland(context.assets.open(COAST_ASSET).bufferedReader().use { it.readText() })
                    }
                    val model: IslandViewModel = viewModel(factory = IslandViewModelFactory(story, app.islandGameStore))
                    IslandApp(model)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (application as TristamApp).backgroundMusic.onForeground(true)
    }

    override fun onStop() {
        if (!isChangingConfigurations) {
            (application as TristamApp).backgroundMusic.onForeground(false)
        }
        super.onStop()
    }

    private companion object {
        const val COAST_ASSET: String = "island/coast.json"
    }
}
