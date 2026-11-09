package app.coshell.rates.list

import app.coshell.rates.RatesRefresher
import app.coshell.rates.RefreshStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRatesRefresher : RatesRefresher {
    val status = MutableStateFlow(RefreshStatus.Idle)
    var runs = 0

    override fun refreshNow() {
        runs++
        status.value = RefreshStatus.Running
    }

    override fun observe(): Flow<RefreshStatus> = status
}
