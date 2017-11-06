package com.lehtek.zpp.zposprintingdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.lehtek.zpp.zpos.IZPosPrinter;
import com.lehtek.zpp.zpos.ZPosException;
import com.lehtek.zpp.zprint.ZPrintBarcode;
import com.lehtek.zpp.zprint.ZPrintDocument;
import com.lehtek.zpp.zprint.ZPrintPage;
import com.lehtek.zpp.zprint.ZPrintQRCode;
import com.lehtek.zpp.zprint.ZPrintText;
import com.platform.android.Font;
import com.platform.android.Log;
import com.platform.java.StringUtil;
import com.platform.java.SystemUtil;
import com.platform.java.TimeUtil;

import java.math.BigDecimal;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import th.or.mea.ourosbbillingsystem.BillCalculator;
import th.or.mea.ourosbbillingsystem.helper.DBFetch;
import th.or.mea.ourosbbillingsystem.helper.Utilities;
import th.or.mea.ourosbbillingsystem.model.ConfigData;
import th.or.mea.ourosbbillingsystem.model.MeterHistory;
import th.or.mea.ourosbbillingsystem.model.MeterInfo;
import th.or.mea.ourosbbillingsystem.model.Personal;
import th.or.mea.ourosbbillingsystem.model.ReceiptData;
import th.or.mea.ourosbbillingsystem.model.Record;
import th.or.mea.ourosbbillingsystem.RecordCommentNew;

/**
 * Created by chaninsai on 10/17/2017.
 */

public class BackgroundPrintZPP extends AsyncTask<String, Integer, String> {

    private static final String TAG = "BgPrintZpp";

    private static final String TEXT_AMT_OUT_BILL = "รายการค้างชำระอื่น ";
    private static final String TEXT_CREDIT_AMT = "เงินล่วงหน้าคงเหลือ";
    private static final String TEXT_BAHT = "บาท";
    private static final String TEXT_NO_BILL = "************ ใบแจ้งค่าไฟฟ้าแสดงอยู่ในหน้าแรก ************";
    private static final String[] TEXT_MEA = new String[]  {
            "การไฟฟ้านครหลวง",
            "30 ซ.ชิดลม ถ.เพลินจิต แขวงลุมพินี",
            "เขตปทุมวัน กทม. 10330"
        };
    private static final String TEXT_TAX_TITLE = "เลขประจำตัวผู้เสียภาษีอากร";
    private static final String TEXT_TITLE = "ใบเสร็จรับเงิน/ใบกำกับภาษี";
    private static final String TEXT_RCPT_NO = "เลขที่";
    private static final String TEXT_RCPT_DATE = "วันที่";
    private static final String TEXT_CA = "บัญชีแสดงสัญญา";
    private static final String TEXT_METER_NO = "รหัสเครื่องวัดฯ";
    private static final String TEXT_NAME = "ชื่อผู้ใช้ไฟฟ้า";
    private static final String TEXT_PAID_BY = "ชำระโดย";
    private static final String TEXT_ADDRESS = "สถานที่ใช้ไฟฟ้า";
    private static final String TEXT_TAX_ID = "Tax ID";
    private static final String TEXT_BRANCH_ID = "สาขาที่";
    private static final String TEXT_BRANCH_HQ = "สำนักงานใหญ่";
    private static final String TEXT_LAST_DATE = "วันที่จดเลขอ่าน";
    private static final String TEXT_BILL_NO = "เลขที่ใบแจ้งฯ";
    private static final String TEXT_TOTAL_KWH = "หน่วย";
    private static final String TEXT_PAY_AMOUNT = "ค่าไฟฟ้า";
    private static final String TEXT_VAT_PAY = "VAT";
    private static final String TEXT_TOTAL_AMOUNT = "จำนวนเงิน";
    private static final String TEXT_FT_AMOUNT = "Ft";
    private static final String TEXT_GRAND_PAY_AMOUNT = "รวมเงิน";
    private static final String TEXT_GRAND_VAT_PAY = "รวมภาษีมูลค่าเพิ่ม";
    private static final String TEXT_GRAND_TOTAL_AMOUNT = "รวมเงินทั้งสิ้น";
    private static final String TEXT_PAGE = "หน้า";
    private static final String TEXT_PAID_TYPE_DEBIT = "หักบัญชี";
    private static final String TEXT_PAID_TYPE_CREDIT = "หักบัตรเครดิต";
    private static final String TEXT_PAID_ACCOUNT = "เลขที่";
    private static final String TEXT_FICA_DOC = "FICA DOC";

    private static final String TEXT_PROCESS_MSG = "กำลังพิมพ์ข้อมูล...";

    private static final int NO_LINE_PER_PAGE = 2;

    public Context context;
    public BillCalculator billCalc;
    public RecordCommentNew commentNew;
    public ProgressDialog progressDialog;

    private MeterInfo meterInfo; // Model MeterInfo
    private Record rec; // ข้อมูลการจด

    public void setRec(Record oRec) {
        this.rec = oRec;
    }

    private ArrayList<ReceiptData> ArrayReceiptData; // ใบเสร็จที่เป็น Array List

    public void setRD(ArrayList<ReceiptData> oRD) {
        this.ArrayReceiptData = oRD;
    }

    private ArrayList<MeterHistory> ArrayHistory; // ข้อมูล History

    public void setHistory(ArrayList<MeterHistory> arrayHistory) {
        this.ArrayHistory = arrayHistory;
    }

    private ReceiptData receiptData; // ข้อมูลใบเสร็จที่เก็บเป็น Record
    private MeterHistory meterHistory; // ข้อมูล History

    private ConfigData arrConfigData;
    private Personal arrPersonalData;

    public ConfigData getArrConfigData() {
        return arrConfigData;
    }

    public void setArrConfigData(ConfigData arrConfigData) {
        this.arrConfigData = arrConfigData;
    }

    public Personal getArrPersonalData() {
        return arrPersonalData;
    }

    public void setArrPersonalData(Personal arrPersonalData) {
        this.arrPersonalData = arrPersonalData;
    }

    private BigDecimal sumPayment;
    private BigDecimal sumVat;
    private BigDecimal sumTotal;
    private DecimalFormat df = new DecimalFormat("00000000.00");
    private DecimalFormat dfBath = new DecimalFormat("#,###,##0.00");

    protected static ZPrintDocument zprintDoc = new ZPrintDocument("BgPrintZpp", "UTF-8");
    protected static IZPosPrinter lastPrinter = null;
    protected static int zprintCount = 0;

    protected ZPrintPage zpage;

    private static String font10 = null;
    private static String font10B = null;
    private static String font12 = null;
    private static String font12B = null;
    private static String font14 = null;
    private static String font14B = null;
    private static String font18B = null;

    private int[] medias = {1020, 2540};
    private int[] margins = {42, 95, 42, 105};
    private int[] adjMargins = {margins[0], margins[1], margins[2], margins[3]};
    private static final int PAGE_MAX = 2;
    private int adjLeft;
    private int adjTop;
    private int left;
    private int right;
    private int top;
    private int bottom;

    protected String printerModel = "ZPosPrinterTest";

    public String getPrinterModel() {
        return printerModel;
    }

//    public void setPrinterModel(String printerModel) {
//        this.printerModel = printerModel;
//    }


    public BackgroundPrintZPP(Context context, BillCalculator billCalc, RecordCommentNew commentNew) {
        this.context = context;
        this.billCalc = billCalc;
        this.commentNew = commentNew;
    }

    @Override
    protected void onPreExecute() {
        Log.debug(TAG, "onPreExecute...");

        super.onPreExecute();

        progressDialog = new ProgressDialog(context);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(TEXT_PROCESS_MSG);
        progressDialog.show();

        int pageIndex = (zprintCount - 1) % PAGE_MAX;
        zpage = zprintDoc.getPrintPage(pageIndex);
        while (zpage != null && zpage.getPageState() == ZPrintPage.PAGE_Printing) {
            SystemUtil.sleep(100);
        }
    }

    @Override
    protected void onPostExecute(String result) {

        Log.debug(TAG, "onPostExecute..." + result);
        if (zprintDoc != null) {
            if (zpage.getPageState() <= ZPrintPage.PAGE_Created) commentNew.sentFail();
//            try {
            if (zpage.getPageState() <= ZPrintPage.PAGE_Created) commentNew.sentFail();
            else {
//                    if (zpage.getPageState() == ZPrintPage.PAGE_Finished) {
//                        Log.debug(TAG, "Releasing printer...");
//                        zprintDoc.releasePrinter();
//                    }
                commentNew.sentFinish();
            }
//            }
//            catch (ZPosException e) {
//                e.printStackTrace();
//                commentNew.sentFail();
//            }
        }
        else {
            commentNew.sentFail();
        }
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        super.onPostExecute(result);
    }

    @Override
    protected String doInBackground(String... params) {
        Log.debug(TAG, "doInBackground..." + params[0]);
        String result = checkPrinter(params[0]);
        if (result != null) return result;

        meterInfo = DBFetch.getMeterInfo(context); // ดึงข้อมูล Meter Info


        if (ArrayReceiptData.size() > 0) {
            int pageNo = 1;
            int pageTot = 1;
            String lastRecpNo = "";
            for (int line = 0; line < ArrayReceiptData.size(); line++) {
                receiptData = ArrayReceiptData.get(line);
                // New receipt no. then find total page per receipt no.
                if (!lastRecpNo.equals(receiptData.getRecptno())) {
                    lastRecpNo = receiptData.getRecptno();
                    clearSumFooter();
                    pageNo = 1;
                    pageTot = 0;
                    for (int i = line; i < ArrayReceiptData.size(); i += NO_LINE_PER_PAGE) {
                        if (!lastRecpNo.equals(ArrayReceiptData.get(i).getRecptno())) break;
                        ++pageTot;
                    }
                }

                int lineBegin = line;
                for (int i = lineBegin + 1; i < ArrayReceiptData.size() && i < lineBegin + NO_LINE_PER_PAGE; ++i) {
                    if (!lastRecpNo.equals(ArrayReceiptData.get(i).getRecptno())) break;
                    ++line;
                }
                result = print(lineBegin, line, pageNo, pageTot);
                if (pageNo < pageTot) ++pageNo;
            }
        }
        else {
            result = print( -1, -1, 1, 1);
        }

        return result;
    }

    protected String print(int recLineBegin, int recLineEnd, int pageNo, int pageTot) {

        int pageIndex = zprintCount % PAGE_MAX;

        String result = startPrint(pageIndex);
        if (result != null) return result;
        ++zprintCount;

        String script = "";
        try {
            if (recLineBegin <= 0) {
                script += billForPrint(rec);
            }
            else {
                script += noBillForPrint();
            }
            if (ArrayReceiptData.size() > 0) {
                script +=  receiptForPrint(rec.getMeterData().getVatRate(), recLineBegin, recLineEnd, pageNo, pageTot);
            }

            result = finishPrint(pageIndex);
        }
        catch (Exception e) {
            Log.trace(TAG, e);
            return e.getMessage();
        }
        return result;
    }

    protected String checkPrinter(String printerModel) {
        long begin = TimeUtil.getTickMillis();
        if (Log.DEBUG) Log.debug(TAG, "Printer selecting...");
        if (printerModel == null || printerModel.length() == 0) printerModel = this.printerModel;
        if (zprintDoc.getPrinter() == null && zprintDoc.selectPrinter(null, printerModel, null) == null) {
            if (progressDialog != null) progressDialog.setMessage("");
            return "Cannot select printer: " + printerModel;
        }

        this.printerModel = printerModel;
        if (progressDialog != null) progressDialog.setMessage(TEXT_PROCESS_MSG + this.printerModel);
        IZPosPrinter currPrinter = zprintDoc.getPrinter();

        if (lastPrinter != currPrinter) {
            for (int i = 0; i < PAGE_MAX; i++) {
                zprintDoc.deletePrintPage(i);
            }
            zprintCount = 0;
            lastPrinter = currPrinter;
            if (Log.DEBUG) Log.debug(TAG, "Changed printer: " + lastPrinter.getLogicalName());
        }
        if (Log.DEBUG) Log.debug(TAG, "Print selected: " + (TimeUtil.getTickMillis() - begin));
        return null;
    }

    protected String startPrint(int zpageIndex) {
        long begin = TimeUtil.getTickMillis();

        if (Log.DEBUG) Log.debug(TAG, "Print starting..." + zpageIndex);

        float scale = 1.0F;
        if (font10 == null)
            font10 = new Font(Font.NORMAL_FAMILY, Font.Style.REGULAR, 7.0F / scale).getFullName();
        if (font10B == null)
            font10B = new Font(Font.NORMAL_FAMILY, Font.Style.BOLD, 7.0F / scale).getFullName();
        if (font12 == null)
            font12 = new Font(Font.NORMAL_FAMILY, Font.Style.REGULAR, 8.0F / scale).getFullName();
        if (font12B == null)
            font12B = new Font(Font.NORMAL_FAMILY, Font.Style.BOLD, 8.0F / scale).getFullName();
        if (font14 == null)
            font14 = new Font(Font.NORMAL_FAMILY, Font.Style.REGULAR, 9.5F / scale).getFullName();
        if (font14B == null)
            font14B = new Font(Font.NORMAL_FAMILY, Font.Style.BOLD, 9.5F / scale).getFullName();
        if (font18B == null)
            font18B = new Font(Font.NORMAL_FAMILY, Font.Style.BOLD, 12.5F / scale).getFullName();

        zpage = zprintDoc.getPrintPage(zpageIndex);

        if (zpage == null) {
            IZPosPrinter currPrinter = zprintDoc.getPrinter();
            int topM1 = currPrinter.verPxToUnit(currPrinter.getAdjustFromTop()) / 2;
            int errorM1 = currPrinter.verPxToUnit(currPrinter.getAdjustInDpi1());
            errorM1 = (medias[1] - margins[1] - margins[3]) * errorM1 / 254;
            errorM1 = (errorM1 + 5) / 10;
            //if (errorM1 < 0) errorM1 = 0;
            if (zpage != null) {
                zprintDoc.deletePrintPage(zpageIndex);
            }
            int[] printMedias = {currPrinter.getMediaWidth(), currPrinter.getMediaHeight()};
            int[] printMargins = currPrinter.getCapMargins();
            int zpageNo = zprintDoc.createPrintPage(printMedias[0], printMedias[1], printMargins[0], printMargins[1], printMargins[2], printMargins[3] - errorM1 - topM1);
            if (zpageIndex != zpageNo) {
                zprintDoc.deletePrintPage(zpageNo);
                return "Page no. mismatched";
            }
            zpage = zprintDoc.getPrintPage(zpageIndex);
        }

        zpage.setSinglePage(true);
        zpage.clearChildren();

        adjMargins[0] -= adjMargins[0] - zpage.getMarginLeftMm1() + (zpage.getWidthMm1() - medias[0]) / 2;
        adjMargins[1] -= adjMargins[1] - zpage.getMarginTopMm1() + (zpage.getHeightMm1() - medias[1]) / 2;
        adjMargins[2] -= adjMargins[2] - zpage.getMarginRightMm1() + (zpage.getWidthMm1() - medias[0]) / 2;
        adjMargins[3] -= adjMargins[3] - zpage.getMarginBottomMm1() + (zpage.getHeightMm1() - medias[1]) / 2;

        adjLeft = 0;
        adjTop = -3;
        left = -adjMargins[0] + adjLeft;
        right = medias[0] + left - (adjMargins[2] - adjMargins[0]) - 1;
        top = -adjMargins[1] + adjTop;
        bottom = medias[1] + top - (adjMargins[3] - adjMargins[1]) - 1;

        if (Log.DEBUG) Log.debug(TAG, "Print started: " + (TimeUtil.getTickMillis() - begin));

        return null;
    }


    public String finishPrint(int pageIndex) {
        long begin = TimeUtil.getTickMillis();
        if (Log.DEBUG) Log.debug(TAG, "Print finishing... " + pageIndex + "@" + zpage.hashCode());
        try {

            if (pageIndex >= 0) {

                int ret = zprintDoc.print(pageIndex, pageIndex);

                if (Log.DEBUG) Log.debug(TAG, "Print finished: " + (TimeUtil.getTickMillis() - begin));
            }

        } catch (Exception e) {
            Log.trace(e);
            return e.getMessage();
        }
        return null;
    }

    private String noBillForPrint() {
        top += 570 + 600;
        return PrintText(font18B, left + (left + right) / 2 + 125, top + 150, TEXT_NO_BILL, 1, 90);
    }

    private String billForPrint(Record rec) {
        if (zpage == null) return null;

        //      แปลงข้อมูลพร้อมพิมพ์ --------------------------->
        String strType = "";
        String strDiscout = "0.00";
        String strDiscName = "";
        String cpclData = "";

        DecimalFormat df = new DecimalFormat("#,###,##0.00");
        SimpleDateFormat sdf = new SimpleDateFormat("MMdd", Locale.US);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);

        meterHistory = ArrayHistory.get(0);

        String strDate = Integer.toString(cal.get(Calendar.YEAR) + 543) + sdf.format(cal.getTime());

        String strDateDue = getFormatDatePrint(strDate) + " - " + getFormatDatePrint(rec.getMeterData().getDueDate());

        String strDateTimeRecord = getFormatDatePrint(strDate) + " " + Utilities.nowTime().substring(0, 2) + ":" + Utilities.nowTime().substring(2);

        // พิพม์งานค้างชำระ
        BigDecimal dTotalForPay = billCalc.getBillDetail().getTotalAmt().add(rec.getMeterData().getAmtOfOutstandingBillsC());

        if (Integer.parseInt(rec.getMeterData().getType()) == 2) {
            strType = String.valueOf(Integer.parseInt(rec.getMeterData().getType())) + "." + rec.getMeterData().getTariff() + "." + rec.getMeterData().getTension();
        } else {
            strType = String.valueOf(Integer.parseInt(rec.getMeterData().getType())) + "." + rec.getMeterData().getTariff();
        }

        if (rec.getMeterData().getDiscCode() > 0 && !(billCalc.getBillDetail().isGetFree50Unit())) { // ถ้าส่ง 0 มาแสดงว่าไม่ได้ส่วนลด

            strDiscout = df.format(billCalc.getBillDetail().getDscAmount());
            strDiscout = (!strDiscout.equals("0.00") ? "-" + strDiscout : strDiscout);

            if (!strDiscout.equals("0.00")) {

                switch (rec.getMeterData().getDiscCode()) {
                    case 1:
                        strDiscName = "ทหารผ่านศึก " + billCalc.getBillDetail().getDscQtyHH() + " หน่วย";
                        break;
                    case 2:
                    case 3:
                        strDiscName = "ตามสิทธิ์ " + billCalc.getBillDetail().getDscQtyHH() + " หน่วย";
                        break;
                    default:
                        strDiscName = "";
                        break;
                }
            }
        }

//      Barcode
        String strBarcode = "|" + Utilities.getTaxId() + "\r"
                + rec.getMeterData().getContractAccountNo().substring(3)
                + Utilities.Add0(rec.getMeterData().getInvoiceNo(), 11) + "\r" // 11 หลัก
                + "11"
                + rec.getMeterData().getDueDate()
                + (rec.getMeterData().getNoOfOutstandingBillsC() > 0 ? "1" : "0")
                + Utilities.Add0(rec.getUnitOn() + "", 6) // 6 หลัก
                + "\r"
                + df.format(dTotalForPay).replace(".", "").replace(",", ""); // จำนวนเงิน

        // เลขอ่านครั้งหลัง
        String strShowRecordUnitOnOff = "";
        if (String.valueOf(rec.getNumOff()) != "0") {
            strShowRecordUnitOnOff = PrintText(font10, left + 350, top + 475, "N:" + String.valueOf(rec.getNumOn())
                    + " / F:" + String.valueOf(rec.getNumOff()), 0);
        } else {
            strShowRecordUnitOnOff = PrintText(font12, left + 350, top + 475, String.valueOf(rec.getNumOn()), 0);
        }

        // เลขอ่านครั้งก่อน
        String strShowPreviousOnOff = "";
        if (!String.valueOf(rec.getMeterData().getPreviousRead_Off()).equals("")) {
            strShowPreviousOnOff = PrintText(font10, left + 585, top + 475, "N:" + String.valueOf(Integer.parseInt(rec.getMeterData().getPreviousRead_On()))
                    + " / F:" + String.valueOf(Integer.parseInt(rec.getMeterData().getPreviousRead_Off())), 0);
        } else {
            strShowPreviousOnOff = PrintText(font12, left + 585, top + 475, String.valueOf(Integer.parseInt(rec.getMeterData().getPreviousRead_On())), 0);
        }

        // จำนวนหน่วย
        String strShowUnitOnOff = "";
        if (String.valueOf(rec.getUnitOff()) != "0") {
            strShowUnitOnOff = PrintText(font12, left + 800, top + 475, String.valueOf(rec.getUnitOn()) + " / " + String.valueOf(rec.getUnitOff()), 0);
        } else {
            strShowUnitOnOff = PrintText(font12, left + 800, top + 475, String.valueOf(rec.getUnitOn()), 0);
        }

        String strAddress = rec.getMeterData().getAddress().length() > 50 ? rec.getMeterData().getAddress().substring(0, 50) : rec.getMeterData().getAddress();

        String strMultiple = rec.getMeterData().getMultiple() + "";
        if (Integer.parseInt(strMultiple) > 0) //Integer.parseInt(MULTI)
        {
            //ตัวคูณ
            strMultiple = PrintText(font12, right - 40, top + 475, strMultiple, -1);
        }

// -------------------------------------------------->

        cpclData =
                PrintText(font12B, left + 495, top + 105, Utilities.getDistricName(meterInfo.getSection())) +
                        PrintText(font10, right - 45, top + 150, "An V" + Utilities.getPStaticVersion() + "-R" + rec.getMeterData().getRoute() + rec.getMeterData().getSubRoute(), -1) +
                        PrintText(font12B, left + 275, top + 212, rec.getMeterData().getName()) +
                        PrintText(font12B, left + 330, top + 260, strAddress) +

                        PrintText(font12B, left + 55, top + 365, rec.getMeterData().getContractAccountNo().substring(3)) +
                        PrintText(font12B, left + 350, top + 365, rec.getMeterData().getMeterNumber().substring(1), 0) +
                        PrintText(font12, left + 555, top + 365, rec.getMeterData().getMRU(), 0) +
                        PrintText(font12, left + 772, top + 365, Utilities.Add0(rec.getMeterData().getInvoiceNo(), 11), 0) +
                        PrintText(font12, right - 40, top + 365, strType, -1) +
                        PrintText(font12, left + 45, top + 475, strDateTimeRecord) +

                        strShowRecordUnitOnOff +
                        strShowPreviousOnOff +
                        strShowUnitOnOff +
                        strMultiple;

        top += 570;

        cpclData +=
                PrintText(font12B, left + 725, top + 0, df.format(billCalc.getBillDetail().getEc().doubleValue()), -1) +
                        PrintText(font12B, left + 725, top + 47, df.format(billCalc.getBillDetail().getSc().doubleValue()), -1) +
                        PrintText(font12, left + 365, top + 95, rec.getMeterData().getFTRate(), -1) +
                        PrintText(font12B, left + 725, top + 95, df.format(billCalc.getBillDetail().getEv().doubleValue()), -1) +

                        PrintText(font12, left + 145, top + 142, strDiscName) +
                        PrintText(font12B, left + 725, top + 142, strDiscout, -1) +
                        PrintText(font12B, left + 725, top + 190, df.format(billCalc.getBillDetail().getTotalExVat().doubleValue()), -1);


        if (billCalc.getBillDetail().isGetFree50Unit()) {
            cpclData +=
                    PrintText(font12B, left + 190, top + 197, "**************") +
                            PrintText(font12B, left + 65, top + 242, "****************");
        } else {
            cpclData +=
                    PrintText(font12B, left + 250, top + 237, String.valueOf(Integer.parseInt(rec.getMeterData().getVatRate())), -1) +
                            PrintText(font12B, left + 725, top + 237, df.format(billCalc.getBillDetail().getVat().doubleValue()), -1);
        }

        cpclData +=
                PrintText(font12B, left + 725, top + 285, df.format(billCalc.getBillDetail().getBillamt().doubleValue()), -1);

        // จะแสดง 1.ไฟฟรี 2.เงินล่วงหน้า
//        for (int i = 0; i < billCalc.getBillDetail().getArrShowDisName().size(); i++) {
//            cpclData +=
//                    PrintText(font12B, left + 65,  top + 340 + i * 45, billCalc.getBillDetail().getArrShowDisName().get(i).keySet().toString().replace("[", "").replace("]", ""), -1) +
//                    PrintText(font12B, left + 725,  top + 340 + i * 45, billCalc.getBillDetail().getArrShowDisName().get(i).values().toString().replace("[", "").replace("]", ""), 1);
//        }

        top += 600;
        // รหัสห้ามรับเช็ค
        String sReceiveCheckCode = "";
        if (rec.getMeterData().getReceiveCheckCode() > 0) {
            sReceiveCheckCode = PrintText(font12, right - 235, top + 42 - 0, "*ห้ามรับเช็ค", 1);
        }

        // เงินล่วงหน้าคงเหลือ
        String sTotalCredit = "";
        if (!df.format(billCalc.getBillDetail().getTotalCredit()).equals("0.00")) {
            sTotalCredit = PrintText(font12, left + 45, top + 100, "เงินล่วงหน้าคงเหลือ " + df.format(billCalc.getBillDetail().getTotalCredit()) + TEXT_BAHT);
        }

        // แสดงรายการค้างชำระอื่น ๆ
        String sAmtOutBillo = "";
        if (Double.parseDouble(rec.getMeterData().getAmtOfOutstandingBillsO()) > 0) {
            if (!df.format(billCalc.getBillDetail().getTotalCredit()).equals("0.00")) {
                sAmtOutBillo = PrintText(font12, right - 45, top + 100, "| " + TEXT_AMT_OUT_BILL + df.format(Double.parseDouble(rec.getMeterData().getAmtOfOutstandingBillsO())) + TEXT_BAHT, -1);
            } else {
                sAmtOutBillo = PrintText(font12, left + 45, top + 100, TEXT_AMT_OUT_BILL + df.format(Double.parseDouble(rec.getMeterData().getAmtOfOutstandingBillsO())) + TEXT_BAHT);
            }
        }

        cpclData +=
                PrintText(font12B, left + 400, top + 0, rec.getMeterData().getNoOfOutstandingBillsC() + "", 0) +
                        PrintText(font12B, left + 725, top + 0, df.format(rec.getMeterData().getAmtOfOutstandingBillsC()) + "", -1) +
                        PrintText(font14B, left + 745, top + 42 - 0, df.format(dTotalForPay.doubleValue()) + "", -1) +

                        sReceiveCheckCode +
                        sTotalCredit +
                        sAmtOutBillo;
        int i = 0;

        cpclData +=
                PrintText(font10, left + 258 + 127 * (++i - 1), top + 258, getFormatDatePrint(meterHistory.getHistoryDate1()), 0) +
                        PrintText(font10B, left + 258 + 127 * (i - 1), top + 300, meterHistory.getHistoryUnit1() + "", 0) +
                        PrintText(font10, left + 258 + 127 * (++i - 1), top + 258, getFormatDatePrint(meterHistory.getHistoryDate2()), 0) +
                        PrintText(font10B, left + 258 + 127 * (i - 1), top + 300, meterHistory.getHistoryUnit2() + "", 0) +
                        PrintText(font10, left + 258 + 127 * (++i - 1), top + 258, getFormatDatePrint(meterHistory.getHistoryDate3()), 0) +
                        PrintText(font10B, left + 258 + 127 * (i - 1), top + 300, meterHistory.getHistoryUnit3() + "", 0) +
                        PrintText(font10, left + 258 + 127 * (++i - 1), top + 258, getFormatDatePrint(meterHistory.getHistoryDate4()), 0) +
                        PrintText(font10B, left + 258 + 127 * (i - 1), top + 300, meterHistory.getHistoryUnit4() + "", 0) +
                        PrintText(font10, left + 258 + 127 * (++i - 1), top + 258, getFormatDatePrint(meterHistory.getHistoryDate5()), 0) +
                        PrintText(font10B, left + 258 + 127 * (i - 1), top + 300, meterHistory.getHistoryUnit5() + "", 0) +
                        PrintText(font10, left + 258 + 127 * (++i - 1), top + 258, getFormatDatePrint(meterHistory.getHistoryDate6()), 0) +
                        PrintText(font10B, left + 258 + 127 * (i - 1), top + 300, meterHistory.getHistoryUnit6() + "", 0) +

                        PrintText(font10, left + 310, top + 345, arrPersonalData.getOrgName() + " โทร." + arrPersonalData.getOrgTel()); // company name

        String[] Arr_sBarcode = strBarcode.split("\r");
        String showNumBarcode = Arr_sBarcode[0] + " " + Arr_sBarcode[1] + " " + Arr_sBarcode[2] + " " + Arr_sBarcode[3] + "\r";
        StringBuilder strBuilBarcode = new StringBuilder();

//      หากจำนวนเงินที่ต้องชำระเป็น 0 ไม่ต้องแสดง
        if (!df.format(dTotalForPay.doubleValue()).equals("0.00")) {
            if (rec.getMeterData().getPayMethod().isEmpty() || rec.getMeterData().getPayMethod().equals("")) {

                int iy = -adjMargins[1] + adjTop;
                strBuilBarcode.append(PrintText(font14, left + 470, top + 145, strDateDue));
                //CA BC
                String sCA = rec.getMeterData().getContractAccountNo().substring(3);
                strBuilBarcode.append(PrintBC128(80, right - 185, iy + 950, sCA, 320, 2, 90));
                strBuilBarcode.append(PrintText(font12, right - 185 + 70, iy + 950 - 80, sCA, 1, 90));
                //Payment QR
                strBuilBarcode.append(PrintQR(45, right - 55 - 190, iy + 995, strBarcode, 190, 3, 0));
                //Payment BC
                strBuilBarcode.append(PrintBC128(90, left + 45, top + 400, strBarcode, 940, 1, 0));
                strBuilBarcode.append(PrintText(font10, (left + right) / 2, top + 400 + 80, showNumBarcode, 0));
                cpclData += strBuilBarcode.toString();

            } else { // หักบัตรเครดิต
                cpclData +=
                        PrintText(font12B, 50, 1209, rec.getMeterData().getDdccdate()) +
                                PrintText(font12B, 50, 1239, rec.getMeterData().getShownum());
            }
        }

        return cpclData;
    }

    private String receiptForPrint(String vatRate, int recLineBegin, int recLineEnd, int pageNo, int pageTot) {

        receiptData = ArrayReceiptData.get(recLineBegin);
        String cpclData = getDataHeadReceipt(receiptData, vatRate);

        int row = recLineBegin;
        for (; row <= recLineEnd; row++) {
            cpclData += getDataItemReceipt(ArrayReceiptData.get(row), row - recLineBegin);
        }

        if (pageNo == pageTot) cpclData += getDataFooterReceipt(receiptData, NO_LINE_PER_PAGE, pageNo, pageTot);

        cpclData += PrintText(font12B, right - 45, top + 455 + ((NO_LINE_PER_PAGE + 5) * 40) - 5, TEXT_PAGE + " " + pageNo + "/" + pageTot, -1);

        return cpclData;
    }
    //    ส่วนหัวของใบเสร็จ
    private String getDataHeadReceipt(ReceiptData receiptData, String vatRate) {
        String strReturn = "";
        String addr1 = receiptData.getAddress(), addr2 = "";

        if (addr1.length() > 60) {

            int pos = addr1.indexOf(' ', 55);
            if (pos < 0) pos = addr1.indexOf('-', 55);
            if (pos < 0) pos = addr1.indexOf('.', 55);

            if (pos < 0) pos = addr1.lastIndexOf(' ');
            if (pos < 0) pos = addr1.lastIndexOf('-');
            if (pos < 0) pos = addr1.lastIndexOf('.');
            if (pos > 0) {
                addr2 = addr1.substring(pos, addr1.length() > 130 ? 130 : addr1.length());
                addr1 = addr1.substring(0, pos);
            }
        }

        top = -adjMargins[1] + adjTop + 1705;

        strReturn +=
                PrintText(font14B, left + 45, top + 0, TEXT_MEA[0]) +
                PrintText(font12, left + 45, top + 50, TEXT_MEA[1]) +
                PrintText(font12, left + 45, top + 90, TEXT_MEA[2]) +
                PrintText(font12, left + 45, top + 130, TEXT_TAX_TITLE + " " + Utilities.getTaxId()) +
                PrintText(font14B, right - 45, top + 0, TEXT_TITLE, -1) +
                PrintText(font12B, right - 45, top + 50, TEXT_RCPT_NO + " " + receiptData.getRecptno(), -1) +
                PrintText(font12B, right - 45, top + 90, TEXT_RCPT_DATE + " " + Utilities.dateThai(receiptData.getPaidondate()) + "", -1) +
                PrintText(font12, right - 45, top + 130, receiptData.getBranchname(), -1);

        strReturn +=
                PrintText(font12, left + 40, top + 180, TEXT_CA + " " + receiptData.getCA().substring(3)) +
                PrintText(font12, left + (left + right) / 2, top + 180, TEXT_METER_NO + " " + Integer.parseInt(rec.getMeterData().getMeterNumber())) +
                PrintText(font12, left + 40, top + 220, TEXT_NAME + " " + receiptData.getName()) +
                PrintText(font12, left + 40, top + 260, TEXT_PAID_BY + " " + receiptData.getPaidby()) +
                PrintText(font12, left + 40, top + 300, TEXT_ADDRESS + " " + addr1) +
                PrintText(font12, left + 65, top + 340, addr2);

        if (receiptData.getTaxid().length() > 0) {
            strReturn += PrintText(font12, left + 40, top + 380, TEXT_TAX_ID + " " + receiptData.getTaxid());
        }

        if (receiptData.getBranchpay().length() > 0) {

            if (receiptData.getBranchpay().equals("00000")) {
                strReturn += PrintText(font12, left + (left + right) / 2 + 75, top + 380, TEXT_BRANCH_HQ);
            }
            else {
                strReturn += PrintText(font12, left + (left + right) / 2 + 75, top + 380, TEXT_BRANCH_ID + " " + receiptData.getBranchpay());
            }
        }

        strReturn += PrintText(font10, left + 40, top + 420, TEXT_LAST_DATE) +
                PrintText(font10, left + 205, top + 420, TEXT_BILL_NO) +
                PrintText(font10, left + 440, top + 420, TEXT_TOTAL_KWH, -1) +
                PrintText(font10, left + 580, top + 420, TEXT_PAY_AMOUNT, -1) +
                PrintText(font10, left + 715, top + 420, TEXT_VAT_PAY + " " + Integer.parseInt(vatRate) + "%", -1) +
                PrintText(font10, right - 40 - 95, top + 420, TEXT_TOTAL_AMOUNT, -1) +
                PrintText(font10, right - 40, top + 420, TEXT_FT_AMOUNT, -1);

        return strReturn;
    }

    private void clearSumFooter() {
        sumPayment = new BigDecimal("0");
        sumVat = new BigDecimal("0");
        sumTotal = new BigDecimal("0");
    }

    //    ส่วนของ Dataitem
    private String getDataItemReceipt(ReceiptData receiptData, int row) {
        String strReturn = "";

        BigDecimal payAmt = new BigDecimal(receiptData.getPayamt());
        BigDecimal vatPay = new BigDecimal(receiptData.getVatpay());
        BigDecimal totAmt = new BigDecimal(receiptData.getTotamt());

        sumPayment = sumPayment.add(payAmt);
        sumVat = sumVat.add(vatPay);
        sumTotal = sumTotal.add(totAmt);

        int off = top + 455;

        strReturn += PrintText(font12, left + 40, off + (row * 40), Utilities.getFormatdate(receiptData.getLastdate())) +
                PrintText(font12, left + 180, off + (row * 40), receiptData.getBillno()) +
                PrintText(font12, left + 440, off + (row * 40), receiptData.getTotalkwh(), -1) +
                PrintText(font12, left + 600, off + (row * 40), dfBath.format(payAmt), -1) +
                PrintText(font12, left + 735, off + (row  * 40), dfBath.format(vatPay), -1) +
                PrintText(font12, right - 40 - 80, off + (row * 40), dfBath.format(totAmt), -1) +
                PrintText(font10, right - 40, off + (row * 40), receiptData.getFtamt(), -1);

        return strReturn;
    }

    //   ส่วนท้ายของใบเสร็จ
    private String getDataFooterReceipt(ReceiptData receiptData, int row, int pageNo, int pageTot) {
        String strReturn = "";

        String showPaidtype = "";
        if (receiptData.getPaidtype().equals("DD")) {
            showPaidtype = TEXT_PAID_TYPE_DEBIT;
        } else {
            showPaidtype = TEXT_PAID_TYPE_CREDIT;
        }

        int off = top + 455;

        strReturn += PrintText(font12B, right / 2 - 40, off + (row * 40) + 5, TEXT_GRAND_PAY_AMOUNT) +
                PrintText(font14B, right - 40, off + (row * 40) - 0, dfBath.format(sumPayment) + " " + TEXT_BAHT, -1);
        ++row;
        strReturn += PrintText(font12B, right / 2 - 40, off + (row * 40) + 5, TEXT_GRAND_VAT_PAY) +
                PrintText(font14B, right - 40, off + (row * 40) - 0, dfBath.format(sumVat) + " " + TEXT_BAHT, -1);
        ++row;
        strReturn += PrintText(font12B, right / 2 - 40, off + (row * 40) + 5, TEXT_GRAND_TOTAL_AMOUNT) +
                PrintText(font14B, right - 40, off + (row * 40) - 0, dfBath.format(sumTotal) + " " + TEXT_BAHT, -1);
        ++row;
        strReturn += PrintText(font12, right - 40, off + (row * 40) + 5, "(" + Utilities.ThaiBaht(df.format(sumTotal)) + ")", -1);
        ++row;

        strReturn += PrintText(font12, left + 45, off + (row * 40) - 5, showPaidtype + " " + receiptData.getBankname() + " " + receiptData.getAccid());
        ++row;

        strReturn += PrintText(font10, left + 45, off + (row * 40) - 5, TEXT_FICA_DOC + " " + receiptData.getFicadoc());

        return strReturn;
    }

    public String PrintText(String font, int xmm10, int ymm10, String text) {
        return PrintText(font, xmm10, ymm10, text, 1, 0);
    }

    public String PrintText(String font, int xmm10, int ymm10, String text, int align) {
        return PrintText(font, xmm10, ymm10, text, align, 0);
    }

    public String PrintText(String font, int xmm10, int ymm10, String text, int align, int rotate) {

        zpage.addChild(new ZPrintText(xmm10, ymm10, -1, -1, rotate * 10, text, align, font));
        return "TEXT " + text + "\r\n";
    }

    public String PrintBC128(int hmm10, int xmm10, int ymm10, String text, int wmm10, int scale, int rotate) {
        zpage.addChild(new ZPrintBarcode(xmm10, ymm10, wmm10, hmm10 - 10, rotate * 10, scale * 10, text, ZPrintBarcode.BarcodeFormat.CODE_128));
        return "BARCODE " + text + "\r\n";
    }

    public String PrintQR(int security, int xmm10, int ymm10, String text, int whmm10, int scale, int rotate) {
        zpage.addChild(new ZPrintQRCode(xmm10, ymm10, whmm10, whmm10, rotate * 10, scale * 10, text, 'M', 4));
        return "QR " + text + "\r\n";
    }


    private String getFormatDatePrint(String strDate) {
        String strReturn = strDate.substring(6, 8) + "/" + strDate.substring(4, 6) + "/" + strDate.substring(2, 4);
        return strReturn;
    }
}
