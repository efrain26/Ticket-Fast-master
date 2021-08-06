package mx.odelant.printorders.entities

import androidx.room.*

@Dao
interface ClientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertClient(client: Client)

    @Update
    suspend fun updateClient(client: Client)

    @Query("UPDATE client SET isDeleted = 1 WHERE id = :clientId")
    suspend fun deleteClient(clientId: Int)

    @Query("SELECT * FROM client WHERE isDeleted = 0")
    suspend fun getAllClients(): List<Client>

    @Query("SELECT * FROM client WHERE name LIKE :clientNameWithModifiers AND isDeleted = 0 ORDER BY name ASC")
    suspend fun getAllClientsLikeName(clientNameWithModifiers: String): List<Client>

    @Query("SELECT * FROM client where id = :clientId LIMIT 1")
    suspend fun getClient(clientId: Int): Client?

    @Query("SELECT COUNT(*) FROM client where name = :clientName")
    suspend fun getClientCountByName(clientName: String): Int

    @Query("SELECT COUNT(*) FROM client where name = :clientName and id = :clientId")
    suspend fun getClientCountByNameAndId(clientName: String, clientId: Int): Int
}