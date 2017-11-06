package th.or.mea.ourosbbillingsystem.helper

import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by chaninsai on 10/31/17.
 */
class Utilities {

    companion object {
        @JvmStatic
        val taxId = "099400016224300"

        @JvmStatic
        val pStaticVersion = "1.23.456"

        @JvmStatic
        fun getDistricName(section: String): String {
            var strReturn =  section
            when (strReturn) {
                "55" -> strReturn = "เขตลาดพร้าว"
                "56" -> strReturn = "เขตสามเสน"
                "57" -> strReturn = "เขตนนทบุรี"
                "58" -> strReturn = "เขตธนบุรี"
                "59" -> strReturn = "เขตบางใหญ่"
                "60" -> strReturn = "เขตลาดกระบัง"
                "65" -> strReturn = "เขตประเวศ"
                "66" -> strReturn = "เขตวัดเลียบ"
                "67" -> strReturn = "เขตคลองเตย"
                "68" -> strReturn = "เขตราษฎร์บูรณะ"
                "69" -> strReturn = "เขตยานนาวา"
                "70" -> strReturn = "เขตบางขุนเทียน"
                "75" -> strReturn = "เขตบางบัวทอง"
                "76" -> strReturn = "เขตบางกะปิ"
                "77" -> strReturn = "เขตสมุทรปราการ"
                "78" -> strReturn = "เขตบางพลี"
                "79" -> strReturn = "เขตมีนบุรี"
                "80" -> strReturn = "เขตบางเขน"
            }
			return strReturn;
        }

        fun toBEDate(date: Calendar): String {
            val sdf = SimpleDateFormat("MMdd", Locale.US)
            return Integer.toString(date.get(Calendar.YEAR) + 543) + sdf.format(date.getTime())
        }

        @JvmStatic
        fun nowTime(): String {
            val now = Calendar.getInstance()
            return now.get(Calendar.HOUR).toString() + now.get(Calendar.MINUTE).toString()
        }

        @JvmStatic
        fun dateThai(sBefore: String) // รับมาเป็น YYYY/mm/dd
                : String {
            var sReturn: String = ""
            var sTemp1: String = ""

            sReturn = sBefore.substring(4, 6)

            when (sReturn) {
                "01" -> sTemp1 = " มกราคม "
                "02" -> sTemp1 = " กุมภาพันธ์ "
                "03" -> sTemp1 = " มีนาคม "
                "04" -> sTemp1 = " เมษายน "
                "05" -> sTemp1 = " พฤษภาคม "
                "06" -> sTemp1 = " มิถุนายน "
                "07" -> sTemp1 = " กรกฎาคม "
                "08" -> sTemp1 = " สิงหาคม "
                "09" -> sTemp1 = " กันยายน "
                "10" -> sTemp1 = " ตุลาคม "
                "11" -> sTemp1 = " พฤศจิกายน "
                "12" -> sTemp1 = " ธันวาคม "

                else -> sTemp1 = sReturn
            }


            sReturn = Integer.parseInt(sBefore.substring(6, 8)).toString() + " " + sTemp1 + " " + sBefore.substring(0, 4)


            return sReturn
        }

        @JvmStatic
        fun getFormatdate(yyyymmdd: String): String {
            return yyyymmdd.substring(6, 8) + "/" + yyyymmdd.substring(4, 6) + "/" + yyyymmdd.substring(2, 4)
        }

        @JvmStatic
        fun ThaiBaht(bahtTxt: String): String {
            var n: String
            var bahtTH: String = ""
            val num = arrayOf<String>("ศูนย์", "หนึ่ง", "สอง", "สาม", "สี่", "ห้า", "หก", "เจ็ด", "แปด", "เก้า", "สิบ")
            val rank = arrayOf<String>("", "สิบ", "ร้อย", "พัน", "หมื่น", "แสน", "ล้าน")
            val temp = bahtTxt.split('.')
            var intVal = temp[0]
            var off: Int = intVal.length - 1;
            for (i in 0 until intVal.length) {
                if (intVal[i] != '0') {
                    off = i
                    break
                }
            }
            intVal = intVal.substring(off);
            var decVal = temp[1]
            off = decVal.length - 1;
            for (i in 0 until decVal.length) {
                if (decVal[i] != '0') {
                    off = i
                    break
                }
            }
            decVal = decVal.substring(off);
            if (bahtTxt.toDouble() == 0.0)
                bahtTH = "ศูนย์บาทถ้วน"
            else {
                for (i in 0 until intVal.length) {
                    n = intVal.substring(i, i + 1)
                    if (n != "0") {
                        if (i == intVal.length - 1 && n === "1")
                            bahtTH += "เอ็ด"
                        else if (i == intVal.length - 2 && n === "2")
                            bahtTH += "ยี่"
                        else if (i == intVal.length - 2 && n === "1")
                            bahtTH += ""
                        else
                            bahtTH += num[n.toInt()]
                        bahtTH += rank[intVal.length - i - 1]
                    }
                }
                bahtTH += "บาท"
                if (decVal == "00" || decVal == "0")
                    bahtTH += "ถ้วน"
                else {
                    for (i in 0 until decVal.length) {
                        n = decVal.substring(i, i + 1)
                        if (n !== "0") {
                            if (i == decVal.length - 1 && n === "1")
                                bahtTH += "เอ็ด"
                            else if (i == decVal.length - 2 && n === "2")
                                bahtTH += "ยี่"
                            else if (i == decVal.length - 2 && n === "1")
                                bahtTH += ""
                            else
                                bahtTH += num[n.toInt()]
                            bahtTH += rank[decVal.length - i - 1]
                        }
                    }
                    bahtTH += "สตางค์"
                }
            }
            return bahtTH
        }

        @JvmStatic
        fun Add0(str: String, digit: Int): String {
            var s = str;
            while (s.length < digit) {
                s = "0" + s
            }
            return s
        }

        @JvmStatic
        fun round(value: Int, roundUp: Int) : Int {
            return value
        }

    }
}