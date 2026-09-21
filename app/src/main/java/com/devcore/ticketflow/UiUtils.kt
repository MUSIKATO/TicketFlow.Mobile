package com.devcore.ticketflow

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

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
                val lp = it.layoutParams
                if (lp is ViewGroup.MarginLayoutParams) {
                    lp.bottomMargin = navHeightPx + b.bottom
                    it.layoutParams = lp
                }
            }
            fab?.let {
                val lp = it.layoutParams
                if (lp is ViewGroup.MarginLayoutParams) {
                    lp.bottomMargin = navHeightPx + b.bottom + fabGapPx
                    it.layoutParams = lp
                }
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
                val lp = it.layoutParams
                if (lp is ViewGroup.MarginLayoutParams) {
                    lp.bottomMargin = b.bottom
                    it.layoutParams = lp
                }
                scroll?.let { s ->
                    val slp = s.layoutParams
                    if (slp is ViewGroup.MarginLayoutParams) {
                        slp.bottomMargin = pinnedH + gapPx + b.bottom
                        s.layoutParams = slp
                    }
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

// Badges prioridad/estado compartidos entre listas y detalle.
fun aplicarBadge(txt: TextView, badge: Triple<String, Int, Int>) {
    txt.text = badge.first
    txt.setTextColor(badge.second)
    txt.backgroundTintList = ColorStateList.valueOf(badge.third)
}

fun prioridadBadge(ctx: Context, prioridad: String): Triple<String, Int, Int> = when (prioridad) {
    "Baja" -> Triple(ctx.getString(R.string.ticket_priority_baja), color(ctx, R.color.green), color(ctx, R.color.green_bg))
    "Media" -> Triple(ctx.getString(R.string.ticket_priority_media), color(ctx, R.color.amber), color(ctx, R.color.amber_bg))
    "Alta" -> Triple(ctx.getString(R.string.ticket_priority_alta), color(ctx, R.color.orange), color(ctx, R.color.orange_bg))
    "Critica" -> Triple(ctx.getString(R.string.ticket_priority_critica), color(ctx, R.color.critical), color(ctx, R.color.critical_bg))
    else -> Triple(prioridad, color(ctx, R.color.text_secondary), color(ctx, R.color.card_stroke))
}

fun estadoBadge(ctx: Context, estado: String): Triple<String, Int, Int> = when (estado) {
    "Pendiente" -> Triple(ctx.getString(R.string.ticket_status_pendiente), color(ctx, R.color.pending_status), color(ctx, R.color.pending_status_bg))
    "En proceso" -> Triple(ctx.getString(R.string.ticket_status_en_proceso), color(ctx, R.color.info_blue), color(ctx, R.color.info_blue_bg))
    "Solucionado" -> Triple(ctx.getString(R.string.ticket_status_solucionado), color(ctx, R.color.green), color(ctx, R.color.green_bg))
    else -> Triple(estado, color(ctx, R.color.text_secondary), color(ctx, R.color.card_stroke))
}

private fun color(ctx: Context, id: Int) = ContextCompat.getColor(ctx, id)