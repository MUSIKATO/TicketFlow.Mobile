package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)

            val scrollLp = scroll.layoutParams as ViewGroup.MarginLayoutParams
            scrollLp.bottomMargin = navHeightPx + systemBars.bottom
            scroll.layoutParams = scrollLp

            val fabLp = fab.layoutParams as ViewGroup.MarginLayoutParams
            fabLp.bottomMargin = navHeightPx + systemBars.bottom + fabGapPx
            fab.layoutParams = fabLp

            insets
        }

        fab.setOnClickListener {
            Toast.makeText(this, "Crear nuevo ticket", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.imgProfileAvatar).setOnClickListener {
            lifecycleScope.launch {
                SupabaseClient.client.auth.signOut()
                val intent = Intent(this@AdminActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}