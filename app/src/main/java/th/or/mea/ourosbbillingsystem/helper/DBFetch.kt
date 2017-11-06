package th.or.mea.ourosbbillingsystem.helper

import android.content.Context
import th.or.mea.ourosbbillingsystem.model.MeterInfo

/**
 * Created by chaninsai on 10/17/2017.
 */
class DBFetch {

    companion object {
        @JvmStatic
        fun getMeterInfo(context: Context): MeterInfo {
            return MeterInfo();
        }
    }
}