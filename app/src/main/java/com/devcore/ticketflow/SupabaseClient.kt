package com.devcore.ticketflow

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://rsqayszvxrcgvylquzog.supabase.co",
        supabaseKey = "sb_publishable_cCCulG6WfHEr-BRaG36bBQ_3rTbTL3Z"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
        install(Realtime)
    }
}