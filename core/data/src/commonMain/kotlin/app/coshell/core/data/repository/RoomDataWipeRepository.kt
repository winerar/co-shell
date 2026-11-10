package app.coshell.core.data.repository

import app.coshell.core.data.db.WipeDao
import app.coshell.core.domain.DataCounts
import app.coshell.core.domain.DataWipeRepository

class RoomDataWipeRepository(private val dao: WipeDao, private val stamper: WriteStamper) : DataWipeRepository {
    override suspend fun counts() = DataCounts(dao.accounts(), dao.transactions(), dao.categories(), dao.manualRates())

    override suspend fun wipeAll() {
        val stamp = stamper.stamp()
        dao.wipeAll(stamp.now, stamp.by)
    }
}
