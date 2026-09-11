package com.devcore.ticketflow

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

// ponytail: utilidades compartidas por las pantallas del rol usuario.
// Evita duplicar el manejo de insets (notch/gestos) y el enrutado del BottomNav.
object LayoutInsets {

    // Pantalla con BottomNavigation + scroll + FAB opcional:
    // top = barra de estado (notch), bottom = navegacion + barra de gestos.
    fun withBottomNav(root: View, scroll: View?, fab: View?, bottomNav: View?) {
        val navHeightPx = root.resources.getDimensionPixelSize(R.dimen.bottom_nav_height)
        val fabGapPx = root.resources.getDimensionPixelSize(R.dimen.fab_margin)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, b.top, v.paddingRight, v.paddingBottom)
            scroll?.let {
                val lp = it.layoutParams as ViewGroup.MarginLayoutParams
                lp.bottomMargin = navHeightPx + b.bottom
                it.layoutParams = lp
            }
            fab?.let {
                val lp = it.layoutParams as ViewGroup.MarginLayoutParams
                lp.bottomMargin = navHeightPx + b.bottom + fabGapPx
                it.layoutParams = lp
            }
            bottomNav?.setPadding(0, 0, 0, b.bottom)
            insets
        }
    }

    // Pantalla con boton fijo abajo (Crear Ticket):
    // top = barra de estado, bottom = solo barra de gestos.
    fun withPinnedButton(root: View, scroll: View?, pinned: View?) {
        val gapPx = root.resources.getDimensionPixelSize(R.dimen.fab_margin)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, b.top, v.paddingRight, v.paddingBottom)
            pinned?.let {
                val pinnedH = (it.layoutParams.height).takeIf { h -> h > 0 } ?: it.height
                val lp = it.layoutParams as ViewGroup.MarginLayoutParams
                lp.bottomMargin = b.bottom
                it.layoutParams = lp
                scroll?.let { s ->
                    val slp = s.layoutParams as ViewGroup.MarginLayoutParams
                    slp.bottomMargin = pinnedH + gapPx + b.bottom
                    s.layoutParams = slp
                }
            }
            insets
        }
    }

    // Pantalla de solo scroll (Detalle Ticket): solo necesita el top del notch.
    fun scrollOnly(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, b.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }
}

// Navegacion entre las 3 pestanas del usuario (una Activity por pestana).
fun setupUserBottomNav(nav: BottomNavigationView, activity: AppCompatActivity, current: Class<out Activity>) {
    nav.setOnItemSelectedListener { item ->
        val target = when (item.itemId) {
            R.id.nav_home -> UserDashboardActivity::class.java
            R.id.nav_tickets -> UserTicketsActivity::class.java
            R.id.nav_profile -> UserProfileActivity::class.java
            else -> return@setOnItemSelectedListener false
        }
        if (target != current) {
            activity.startActivity(Intent(activity, target))
            activity.finish()
        }
        true
    }
}