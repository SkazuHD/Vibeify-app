package de.hsb.vibeify.services

import android.content.Context

object PlayerServiceLocator {
    private var instance: PlayerServiceV2? = null

    fun initialize(context: Context) {
        if (instance == null) {
            instance = PlayerServiceV2(context.applicationContext)
        }
    }

    fun get(): PlayerServiceV2? = instance
}
