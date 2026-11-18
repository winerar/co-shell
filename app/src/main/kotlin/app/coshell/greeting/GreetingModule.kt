package app.coshell.greeting

import app.coshell.core.data.family.FamilyService
import app.coshell.core.data.family.FamilyState
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow

/** Who is signed in to the family: the only thing the greeting needs from [FamilyService]. */
fun interface GreetingFamily {
    fun state(): Flow<FamilyState>
}

@Module
@InstallIn(SingletonComponent::class)
interface GreetingModule {
    @Binds fun bindGreetingStore(impl: PrefsGreetingStore): GreetingStore
    @Binds fun bindGreetingNotices(impl: GreetingNotifier): GreetingNotices

    companion object {
        @Provides
        fun provideGreetingFamily(family: FamilyService) = GreetingFamily { family.state }
    }
}
