package th.or.mea.ourosbbillingsystem.model

import java.util.*

/**
 * Created by chaninsai on 10/17/2017.
 */
class Record(val readDate: Calendar) {

    var meterData = MeterData(readDate);
    var numOn = 1234
    var numOff = 1234
    var unitOn = 1000
    var unitOff = 1000
}