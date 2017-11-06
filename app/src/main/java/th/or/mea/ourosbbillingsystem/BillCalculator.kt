package th.or.mea.ourosbbillingsystem

import java.math.BigDecimal
import java.util.*

/**
 * Created by chaninsai on 10/17/2017.
 */

class BillCalculator(var energyCost: Double) {

    class BillDetail(var cost: Double) {

        var ec: BigDecimal
        var sc: BigDecimal
        var ev: BigDecimal
        var dscAmount: BigDecimal
        var dscQtyHH = 999
        var free50Unit = false;
        var totalExVat: BigDecimal
        var vat: BigDecimal
        var billamt: BigDecimal
//        var arrShowDisName: Dictionary<String, String>
        var totalCredit: BigDecimal
        var totalAmt: BigDecimal

        init {
            var sum = cost
            ec = BigDecimal(sum)
            var div10 = Math.round(cost * 10) / 100.0
            var div100 = Math.round(cost * 1) / 100.0
            sc = BigDecimal(div100)
            sum += div100
            ev = BigDecimal(div10)
            sum += div10
            dscAmount = BigDecimal(div100)
            sum -= div100
            totalExVat = BigDecimal(sum)
            var cal = sum * 0.07
            vat = BigDecimal(cal)
            sum += cal
            billamt = BigDecimal(sum)
            totalCredit = BigDecimal(div10)
            sum -= div10
            totalAmt = BigDecimal(sum)
        }

        fun isGetFree50Unit(): Boolean {
            return this.free50Unit
        }
    }

    val billDetail = BillDetail(energyCost);
}
