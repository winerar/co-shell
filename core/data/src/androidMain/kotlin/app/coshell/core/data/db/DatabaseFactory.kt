package app.coshell.core.data.db

import android.content.Context
import androidx.room.Room

fun createCoShellDatabase(context: Context): CoShellDatabase {
    val appContext = context.applicationContext
    return Room.databaseBuilder<CoShellDatabase>(
        context = appContext,
        name = appContext.getDatabasePath(CoShellDatabase.FILE_NAME).absolutePath,
    ).configureCommon()
}
