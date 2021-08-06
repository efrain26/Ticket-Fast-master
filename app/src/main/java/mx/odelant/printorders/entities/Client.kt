package mx.odelant.printorders.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(
            value = ["name"],
            unique = true
        )
    ]
)
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val isDeleted: Boolean

) {
    override fun toString(): String {
        return name
    }
}

