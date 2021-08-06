package mx.odelant.printorders.dataLayer

import androidx.sqlite.db.SimpleSQLiteQuery
import mx.odelant.printorders.entities.CartDao
import mx.odelant.printorders.entities.Client
import mx.odelant.printorders.entities.Product
import java.util.*

class AnalyticsDL {
    enum class DataType {
        Revenue,
        Quantity
    }

    enum class DateScope {
        Week,
        Month,
        Year
    }

    companion object {
        fun getBarChart(
            db: AppDatabase,
            mSelectedDataType: DataType,
            mSelectedDateScope: DateScope,
            mSelectedDate: Calendar,
            mSelectedProduct: Product?,
            mSelectedClient: Client?
        ): List<CartDao.IntPairForChart> {

            val (dateStart, dateEnd) = startAndEndDatesFromDateAndScope(
                mSelectedDateScope,
                mSelectedDate
            )

            val selectAggregate =
                when (mSelectedDataType) {
                    DataType.Revenue ->
                        """
                        Select SUM(cartItem.quantityInHundredths * cartItem.unitPriceInCents / 100) as amount,
                        """
                    DataType.Quantity ->
                        """
                        Select SUM(cartItem.quantityInHundredths) as amount,
                        """
                }

            val selectGranularity = when (mSelectedDateScope) {
                DateScope.Week ->
                    """
                    CAST(strftime('%d',datetime(cart.dateCreated/1000, 'unixepoch')) AS int) as selectGranularity,
                    """
                DateScope.Month ->
                    """
                    CAST(strftime('%d',datetime(cart.dateCreated/1000, 'unixepoch')) AS int) as selectGranularity,
                    """
                DateScope.Year ->
                    """
                    CAST(strftime('%m',datetime(cart.dateCreated/1000, 'unixepoch')) AS int) as selectGranularity,
                    """
            }

            val selectDate =
                """
                MAX(cart.dateCreated) as date
                """

            val from =
                """
                FROM Cart cart
                JOIN CartItem cartItem ON cartItem.cartId = cart.cart_id
                """

            val where =
                """
                WHERE cart.dateCreated BETWEEN ? AND ?
                    AND cart.isPending = 0
                    ${if (mSelectedProduct != null) "AND cartItem.productId = ${mSelectedProduct.id}" else ""}
                    ${if (mSelectedClient != null) "AND cart.clientId = ${mSelectedClient.id}" else ""}
                """

            val groupBy =
                """
                GROUP BY selectGranularity
                """

            val orderBy =
                """
                ORDER BY cart.dateCreated ASC
                """

            val queryString =
                selectAggregate + selectGranularity + selectDate + from + where + groupBy + orderBy
            val query =
                SimpleSQLiteQuery(queryString, arrayOf(dateStart.time.time, dateEnd.time.time))

            return db.cartDao().execIntPairListQuery(query)
        }

        fun getTotals(
            db: AppDatabase,
            mSelectedDateScope: DateScope,
            mSelectedDate: Calendar,
            mSelectedProduct: Product?,
            mSelectedClient: Client?
        ): CartDao.AnalyticsTotals {

            val (dateStart, dateEnd) = startAndEndDatesFromDateAndScope(
                mSelectedDateScope,
                mSelectedDate
            )

            val selectAggregate =
                """
                Select SUM(cartItem.quantityInHundredths * cartItem.unitPriceInCents / 100) as totalRevenue,
                    SUM(cartItem.quantityInHundredths) as totalAmount,
                    COUNT(cart.cart_id) as totalOrders
                """

            val from =
                """
                FROM Cart cart
                JOIN CartItem cartItem ON cartItem.cartId = cart.cart_id
                """

            val where =
                """
                WHERE cart.dateCreated BETWEEN ? AND ?
                    AND cart.isPending = 0
                    ${if (mSelectedProduct != null) "AND cartItem.productId = ${mSelectedProduct.id}" else ""}
                    ${if (mSelectedClient != null) "AND cart.clientId = ${mSelectedClient.id}" else ""}
                """

            val queryString = selectAggregate + from + where
            val query =
                SimpleSQLiteQuery(queryString, arrayOf(dateStart.time.time, dateEnd.time.time))

            return db.cartDao().execAnalyticsTotalsQuery(query)
        }

        fun getTotalReturns(
            db: AppDatabase,
            mSelectedDateScope: DateScope,
            mSelectedDate: Calendar,
            mSelectedProduct: Product?,
            mSelectedClient: Client?
        ): Int {

            val (dateStart, dateEnd) = startAndEndDatesFromDateAndScope(
                mSelectedDateScope,
                mSelectedDate
            )

            val selectAggregate =
                """
                Select SUM(cartReturnItem.quantityInHundredths) as totalAmount
                """

            val from =
                """
                FROM Cart cart
                JOIN CartReturnItem cartReturnItem ON cartReturnItem.cartId = cart.cart_id
                """

            val where =
                """
                WHERE cart.dateCreated BETWEEN ? AND ?
                    AND cart.isPending = 0
                    ${if (mSelectedProduct != null) "AND cartReturnItem.productId = ${mSelectedProduct.id}" else ""}
                    ${if (mSelectedClient != null) "AND cart.clientId = ${mSelectedClient.id}" else ""}
                """

            val queryString = selectAggregate + from + where
            val query =
                SimpleSQLiteQuery(queryString, arrayOf(dateStart.time.time, dateEnd.time.time))

            return db.cartDao().execIntQuery(query)
        }

        private fun startAndEndDatesFromDateAndScope(
            mSelectedDateScope: DateScope,
            mSelectedDate: Calendar
        ): Pair<Calendar, Calendar> {
            val dateStart: Calendar
            val dateEnd: Calendar
            when (mSelectedDateScope) {
                DateScope.Week -> {
                    val date: Calendar = mSelectedDate.clone() as Calendar
                    date.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day !
                    date.clear(Calendar.MINUTE)
                    date.clear(Calendar.SECOND)
                    date.clear(Calendar.MILLISECOND)
                    date.set(Calendar.DAY_OF_WEEK, date.firstDayOfWeek)
                    dateStart = date.clone() as Calendar
                    date.add(Calendar.WEEK_OF_YEAR, 1)
                    dateEnd = date.clone() as Calendar
                }

                DateScope.Month -> {
                    val date: Calendar = mSelectedDate.clone() as Calendar
                    date.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day !
                    date.clear(Calendar.MINUTE)
                    date.clear(Calendar.SECOND)
                    date.clear(Calendar.MILLISECOND)
                    date.set(Calendar.DAY_OF_MONTH, 1)
                    dateStart = date.clone() as Calendar
                    date.add(Calendar.MONTH, 1)
                    dateEnd = date.clone() as Calendar
                }
                DateScope.Year -> {
                    val date: Calendar = mSelectedDate.clone() as Calendar
                    date.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day !
                    date.clear(Calendar.MINUTE)
                    date.clear(Calendar.SECOND)
                    date.clear(Calendar.MILLISECOND)
                    date.set(Calendar.DAY_OF_MONTH, 1)
                    date.clear(Calendar.MONTH)
                    dateStart = date.clone() as Calendar
                    date.add(Calendar.YEAR, 1)
                    dateEnd = date.clone() as Calendar
                }
            }
            return Pair(dateStart, dateEnd)
        }
    }
}
