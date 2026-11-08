package app.coshell.core.data.db

import androidx.room.Room

/** In-memory database for JVM tests. */
fun createInMemoryCoShellDatabase(): CoShellDatabase =
    Room.inMemoryDatabaseBuilder<CoShellDatabase>().configureCommon()
