package th.or.mea.ourosbbillingsystem.model

import th.or.mea.ourosbbillingsystem.helper.Utilities
import java.math.BigDecimal
import java.util.*

/**
 * Created by chaninsai on 10/18/2017.
 */
class MeterData(val readDate: Calendar) {

    val vatRate = "7"

    var contractAccountNo = "000123456789"
    var meterNumber = "0123456789"
    var mRU = "12345678"
    var route = "12345"
    var subRoute = "00"
    var name = "ชื่อ-นามสกุลผู้ใช้ไฟฟ้า (Fullname)"
    var address = "สถานที่ใช้ไฟฟ้า (Premise) " + "สถานที่ใช้ไฟฟ้า (Premise) " + "สถานที่ใช้ไฟฟ้า (Premise) "
    var fTRate = ".1234"
    var noOfOutstandingBillsC = 1
    var amtOfOutstandingBillsC = BigDecimal(123456.78)
    var amtOfOutstandingBillsO = (123456.78).toString()
    var type = "2"
    var tariff = "1"
    var tension = "0"
    var discCode = 1
    var previousRead_On = "0123"
    var previousRead_Off = "0123"

    var invoiceNo = "0123456789"
    var multiple = "10000"

    var receiveCheckCode = 1
    var payMethod = ""
    var ddccdate = "โปรดนำเงินเข้าบัญชีภายในวันที่ dd mmmmmmmm yyyy"
    var shownum = "เลขที่บัญชีธนาคาร 0123456789"

    fun getDueDate() : String {
        return ((readDate.get(Calendar.YEAR) + 543).toString()
                + Utilities.Add0(readDate.get(Calendar.MONTH).toString(), 2)
                + Utilities.Add0(readDate.get(Calendar.DAY_OF_MONTH).toString(), 2))
    }

}