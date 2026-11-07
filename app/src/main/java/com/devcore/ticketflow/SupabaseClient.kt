package com.devcore.ticketflow

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client = try {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth) {
                scheme = "ticketflow"
                host = "auth"
            }
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    } catch (e: Exception) {
        Log.e("SupabaseClient", "Error al inicializar Supabase client", e)
        throw e
    }

    // Variable global con el perfil del usuario con sesión activa (persiste por sesión en memoria)
    var perfil: Profile? = null
}