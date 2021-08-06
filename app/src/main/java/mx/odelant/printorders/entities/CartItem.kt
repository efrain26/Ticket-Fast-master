package mx.odelant.printorders.entities

import androidx.room.*

@Entity
    (
    indices = [Index("cartId"), Index("productId")]
    , foreignKeys =
[ForeignKey
    (
    entity = Cart::class
    , parentColumns = ["cart_id"]
    , childColumns = ["cartId"]
)
    , ForeignKey
    (
    entity = Product::class
    , parentColumns = ["product_id"]
    , childColumns = ["productId"]
)
]
)
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "cart_item_id")
    val id: Int,
    val cartId: Int,
    val productId: Int,
    val quantityInHundredths: Int,
    val unitPriceInCents: Int
)