package th.or.mea.ourosbbillingsystem.model

import th.or.mea.ourosbbillingsystem.helper.Utilities
import java.util.*

/**
 * Created by chaninsai on 10/17/2017.
 */
class ReceiptData(val recptno: String, val paidDate: Calendar, val ldate: Calendar) {

    val paidondate: String
    val lastdate: String
    val address = "หมู่บ้านของเรา บ้านเลขที่ 99/999 หมูที่ 9 ถนนหนทางอันยาวไกล ตำบลบ้านดอนกว้าง อำเภอบ้านเมืองเรา จังหวัดของเรา 99999"
    val branchname = "สำนักงานใหญ่"
    val cA = "000123456789"
    val name = "ชื่อ-นามสกุลผู้ใช้ไฟฟ้า (Fullname)"
    val paidtype = "CC"
    val bankname = "บ.เทสโก้คาร์ดเซอร์วิสเซส จก"
    val accid = "123456789012XXXX"
    val paidby = bankname + " " + accid
    val taxid = "012345678901234"
    val branchpay = "00000"
    val ficadoc = "0123456789012"

    val billno = "0123456789"
    val totalkwh = "123456"
    val payamt = (123456.78).toString()
    val Vatpay = (Math.round(123456.78 * 0.07 * 100) / 100.0).toString()
    val totamt = (Math.round(123456.78 * 1.07 * 100) / 100.0).toString()
    val ftamt =".1234"

    init {
        paidondate = Utilities.toBEDate(paidDate)
        lastdate = Utilities.toBEDate(ldate)
    }
}