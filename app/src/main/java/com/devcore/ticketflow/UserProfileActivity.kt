package com.devcore.ticketflow

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

// ponytail: Mi Perfil — datos del perfil en memoria + cerrar sesión real.
class UserProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        LayoutInsets.withBottomNav(
            root = findViewById(R.id.userProfileCoordinator),
            scroll = findViewById(R.id.userProfileScroll),
            fab = null,
            bottomNav = findViewById(R.id.bottomNav)
        )

        // Datos del perfil cargado en la sesión
        pintarPerfil()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile
        setupUserBottomNav(bottomNav, this, this::class.java)

        findViewById<android.view.View>(R.id.rowEditarInfo).setOnClickListener {
            startActivity(Intent(this, UserEditProfileActivity::class.java))
        }

        findViewById<android.view.View>(R.id.btnLogout).setOnClickListener {
            lifecycleScope.launch {
                SupabaseClient.client.auth.signOut()
                val intent = Intent(this@UserProfileActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    // ponytail: onResume refleja cambios de nombre/correo hechos en Editar Información sin volver a iniciar sesión.
    override fun onResume() {
        super.onResume()
        pintarPerfil()
    }

    private fun pintarPerfil() {
        SupabaseClient.perfil?.let {
            findViewById<TextView>(R.id.txtProfileName).text = it.nombre
            findViewById<TextView>(R.id.txtProfileEmail).text = it.correo
        }
    }
}