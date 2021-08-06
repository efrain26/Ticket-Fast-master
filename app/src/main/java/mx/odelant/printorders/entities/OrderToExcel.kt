package mx.odelant.printorders.entities

import java.util.*

class OrderToExcel {
    var clientName : String = ""
    var dataCreated : Date? = null
    var totalPrice : Double? = null
    var folio : Int = 0
    var isSelected : Boolean = false

    constructor(clientName: String, dataCreated: Date?, totalPrice: Double?, folio: Int, isSelected: Boolean) {
        this.clientName = clientName
        this.dataCreated = dataCreated
        this.totalPrice = totalPrice
        this.folio = folio
        this.isSelected = isSelected
    }
}