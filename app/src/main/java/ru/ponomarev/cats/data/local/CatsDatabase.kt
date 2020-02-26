package ru.ponomarev.cats.data.local

import android.content.Context
import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable

@Database(
    entities = [CatEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CatsDatabase : RoomDatabase() {

    abstract fun catsDao(): CatsDao

    companion object {
        fun getInstance(context: Context) = Room
            .databaseBuilder(context, CatsDatabase::class.java, "cats-database")
            .build()
    }
}

@Entity(tableName = "cats")
data class CatEntity(
    @PrimaryKey
    val id: String,
    val url: String,
    val isFavorite: Boolean
)

@Dao
interface CatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCat(cat: CatEntity): Completable

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCats(map: List<CatEntity>): Completable

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCat(cat: CatEntity): Completable

    @Query("SELECT * FROM cats")
    fun getCats(): Flowable<List<CatEntity>>

    @Query("SELECT * FROM cats WHERE isFavorite = :isFavorite")
    fun getFavoriteCats(isFavorite: Boolean = true): Flowable<List<CatEntity>>
}
