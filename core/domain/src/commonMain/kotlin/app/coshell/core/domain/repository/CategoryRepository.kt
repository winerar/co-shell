package app.coshell.core.domain.repository

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryId
import kotlinx.coroutines.flow.Flow

/** Implemented in `core:data` on top of Room. Soft-deleted categories are never emitted. */
interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun getCategory(id: CategoryId): Category?
    suspend fun upsert(category: Category)
    suspend fun delete(id: CategoryId)
}
