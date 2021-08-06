package mx.odelant.printorders.entities

import androidx.room.ColumnInfo
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
data class Product(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "product_id")
    val id: Int,
    val name: String,
    val basePriceInCents: Int,
    val stockInHundredths: Int,
    val isDeleted: Boolean

) {
    override fun toString(): String {
        return name
    }
}

