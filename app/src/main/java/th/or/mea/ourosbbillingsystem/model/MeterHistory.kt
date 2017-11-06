package th.or.mea.ourosbbillingsystem.model

import th.or.mea.ourosbbillingsystem.helper.Utilities
import java.util.*

/**
 * Created by chaninsai on 10/31/17.
 */
class MeterHistory(val readDate: Calendar) {
    val historyDate1: String
        get() = getHistoryDate(1)
    val historyDate2: String
        get() = getHistoryDate(2)
    val historyDate3: String
        get() = getHistoryDate(3)
    val historyDate4: String
        get() = getHistoryDate(4)
    val historyDate5: String
        get() = getHistoryDate(5)
    val historyDate6: String
        get() = getHistoryDate(6)

    val historyUnit1 = "123456"
    val historyUnit2 = "234567"
    val historyUnit3 = "345678"
    val historyUnit4 = "456789"
    val historyUnit5 = "567890"
    val historyUnit6 = "678901"

    fun getHistoryDate(prevMonth: Int): String {
        val tick = readDate.timeInMillis - prevMonth * (30 * 24 * 60 * 60 * 1000)
        val calc = Calendar.getInstance()
        calc.timeInMillis = tick
        return ((calc.get(Calendar.YEAR) + 543).toString()
                + Utilities.Add0(calc.get(Calendar.MONTH).toString(), 2)
                + Utilities.Add0(calc.get(Calendar.DAY_OF_MONTH).toString(), 2))

    }
}