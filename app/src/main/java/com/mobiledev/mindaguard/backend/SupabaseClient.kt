package com.mobiledev.mindaguard.backend

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Singleton Supabase client.
 *
 * Replace SUPABASE_URL and SUPABASE_ANON_KEY with your actual project values from:
 * https://supabase.com → Project Settings → API
 */
object SupabaseClient {

    private const val SUPABASE_URL = "https://YOUR_PROJECT_ID.supabase.co"
    private const val SUPABASE_ANON_KEY = "YOUR_ANON_KEY"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }
}

