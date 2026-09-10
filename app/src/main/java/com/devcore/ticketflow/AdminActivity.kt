package com.devcore.ticketflow

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge: el contenido se dibuja hasta los bordes (barra de estado,
        // cutout y barra de navegación).
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val root = findViewById<View>(R.id.adminCoordinator)
        val scroll = findViewById<View>(R.id.adminScroll)
        val fab = findViewById<View>(R.id.fabNewTicket)
        val navHeightPx = resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        val fabGapPx = resources.getDimensionPixelSize(R.dimen.fab_margin)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Inset superior (barra de estado + cutout) -> padding del contenedor
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)

            val scrollLp = scroll.layoutParams as ViewGroup.MarginLayoutParams
            scrollLp.bottomMargin = navHeightPx + systemBars.bottom
            scroll.layoutParams = scrollLp

            val fabLp = fab.layoutParams as ViewGroup.MarginLayoutParams
            fabLp.bottomMargin = navHeightPx + systemBars.bottom + fabGapPx
            fab.layoutParams = fabLp

            insets
        }
    }
}