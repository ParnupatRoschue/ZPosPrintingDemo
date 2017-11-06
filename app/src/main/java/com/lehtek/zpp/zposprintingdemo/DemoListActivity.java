package com.lehtek.zpp.zposprintingdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import com.lehtek.zpp.zpos.ZPosDeviceFactory;
import com.platform.android.Font;
import com.platform.android.Log;
import com.platform.android.Rect;
import com.lehtek.zpp.zprint.ZPrintBarcode;
import com.lehtek.zpp.zprint.ZPrintDocument;
import com.lehtek.zpp.zprint.ZPrintPage;
import com.lehtek.zpp.zprint.ZPrintQRCode;
import com.lehtek.zpp.zprint.ZPrintRectangle;
import com.lehtek.zpp.zprint.ZPrintText;
import com.platform.java.ConvertUtil;
import com.platform.java.SystemUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import th.or.mea.ourosbbillingsystem.BillCalculator;
import th.or.mea.ourosbbillingsystem.RecordCommentNew;
import th.or.mea.ourosbbillingsystem.model.MeterData;
import th.or.mea.ourosbbillingsystem.model.MeterHistory;
import th.or.mea.ourosbbillingsystem.model.Personal;
import th.or.mea.ourosbbillingsystem.model.ReceiptData;
import th.or.mea.ourosbbillingsystem.model.Record;


public class DemoListActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    private static ZPrintDocument printDoc = new ZPrintDocument("TestPrint", "Cp874");

    protected IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_list);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(broadcastReceiver, filter);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.info("ACL_CONNECTED:" + device.getName() + "/" + device.getAddress());
                // Check if the connected device is one we had comm with
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                Log.info("ACL_DISCONNECTED:" + device.getName() + "/" + device.getAddress());
                // Check if the connected device is one we had comm with
                printDoc.checkPrinter(device.getAddress());
            }
        }
    };


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_demo_print_document);
                break;
            case 2:
                mTitle = getString(R.string.title_demo_print_form);
                break;
            case 3:
                mTitle = getString(R.string.title_demo_bitmap);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.demo_list, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static ArrayList<PlaceholderFragment> fragments = new ArrayList<PlaceholderFragment>();

        private Spinner spinTestPrintDoc;
        private Button btnTestPrintDoc;
        private Button btnTestPrintForm;
        private ImageView bmpTest;
        private int bitmapIndex = -1;
        private float ix;
        private float iy;
        private float mx;
        private float my;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            if (fragments.size() < sectionNumber) {
                fragments.add(fragment);
            }
            else {
                fragments.remove(sectionNumber - 1);
                fragments.add(sectionNumber - 1, fragment);
            }
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    rootView = inflater.inflate(R.layout.fragment_demo_print_doc, container, false);
                    updateSpinnerDeviceList(rootView);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.fragment_demo_print_form, container, false);
                    break;
                case 3:
                    rootView = inflater.inflate(R.layout.fragment_demo_bitmap, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.fragment_demo_list, container, false);
                    break;
            }
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((DemoListActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }

        public void updateSpinnerDeviceList(View view) {

            String[] deviceList = ZPosDeviceFactory.getDeviceList();
            if (view == null) view = this.getView();
            Spinner spinner = (Spinner)view.findViewById(R.id.demo_print_doc_spinner);

            if (spinner != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, deviceList);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                // Apply the adapter to the spinner
                spinner.setAdapter(adapter);
            }
        }

        @Override
        public void setUserVisibleHint(boolean visible){
            super.setUserVisibleHint(visible);
            if (visible && isResumed()){
                updateSpinnerDeviceList(this.getView());
            }
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            spinTestPrintDoc = (Spinner) view.findViewById(R.id.demo_print_doc_spinner);
            if (spinTestPrintDoc != null) {
                spinTestPrintDoc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            btnTestPrintDoc = (Button) view.findViewById(R.id.demo_print_doc_button);
            if (btnTestPrintDoc != null) {
                btnTestPrintDoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object item = spinTestPrintDoc.getSelectedItem();
                        if (item == null) return;
                        String logicalName = item.toString();
                        demoPrintDoc(v, logicalName);
                    }
                });
            }
            btnTestPrintForm = (Button) view.findViewById(R.id.demo_print_form_button);
            if (btnTestPrintForm != null) {
                btnTestPrintForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        demoPrintForm(v);
                    }
                });
            }

            bmpTest = (ImageView) view.findViewById(R.id.demo_bitmap);
            if (bmpTest != null) {

                bmpTest.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        float curX, curY;

                        switch (event.getAction()) {

                            case MotionEvent.ACTION_DOWN:
                                ix = event.getX();
                                mx = ix;
                                iy = event.getY();
                                my = iy;
                                break;
                            case MotionEvent.ACTION_MOVE:
                                curX = event.getX();
                                curY = event.getY();
                                bmpTest.scrollBy((int) (mx - curX), (int) (my - curY));
                                mx = curX;
                                my = curY;
                                break;
                            case MotionEvent.ACTION_UP:
                                curX = event.getX();
                                curY = event.getY();
//              bmpTest.scrollBy((int) (mx - curX), (int) (my - curY));
                                if (ix == curX && iy == curY) {
                                    ++bitmapIndex;
                                    if (bitmapIndex >= printDoc.getPrintPageSize()) {
                                        bitmapIndex = 0;
                                    }
                                    com.platform.android.Bitmap buff = printDoc.getPageBuffer(bitmapIndex);
                                    if (buff != null) {
                                        bmpTest.setScaleType(ImageView.ScaleType.CENTER);
                                        bmpTest.setImageBitmap(buff.getXBmp());
                                        bmpTest.setScrollX(0);
                                        bmpTest.setScrollY(0);
                                    }
                                }
                                break;
                        }

                        return true;
                    }
                });
            }
        }

        private void demoPrintDoc(View v, String logicalName) {
            if (printDoc.selectPrinter(logicalName, null, null) != null) {
                int pageNo = 0;
                ZPrintPage page = printDoc.getPrintPage(pageNo);
                if (page == null) {
                    pageNo = printDoc.createPrintPage(40, 90, 40, 45);
                    page = printDoc.getPrintPage(pageNo);
                }
                page.setSinglePage(true);
                page.clearChildren();
                Rect area = page.getPrintAreaMm1();

                page.addChild(new ZPrintRectangle(0, 0, area.width(), area.height(), 0xFF7F7F7F, 10, 0));
                Font font = Font.getNormalFont();
                int y = 0;
                int fh = font.getFontHeight();
                String testString = "Testing กำลังทดสอบ 123456790 $.+-*/";
                page.addChild(new ZPrintText(0, y, testString));
                y += fh + 1;
                page.addChild(new ZPrintText(0, y, "จ๋ำกันได้ป๋าว พี่ฎูนู๋เป่าปี่ อี่อูอู๋เอ่าอี่"));
                StringBuffer sb = new StringBuffer(32);
                for (int i = 0x020; i <= 0x080; i++) {
                    sb.append((char) i);
                    if (i % 16 == 0 && i != 32) {
                        y += fh + 1;
                        page.addChild(new ZPrintText(5, y, sb.toString()));
                        sb.delete(0, sb.length() - 1);
                    }
                }
                for (int i = 0x0E00; i <= 0x0E60; i++) {
                    sb.append((char) i);
                    if (i % 16 == 0 && i != 0x0E00) {
                        y += fh + 1;
                        page.addChild(new ZPrintText(5, y, sb.toString()));
                        sb.delete(0, sb.length() - 1);
                    }
                }
                int x = area.width() / 2;
                y += fh + 1;
                page.addChild(new ZPrintText(x, y, -1, -1, 0, testString, 0, "-BOLD-UNDER-"));
                y += fh + 1;
                page.addChild(new ZPrintText(x, y, -1, -1, 0, testString, -1, "-ITALIC-STRIKE-" + ConvertUtil.toString(Font
                        .NORMAL_SIZE + 3F)));
                y += fh + 1;
                page.addChild(new ZPrintText(x, y, -1, -1, 0, testString, 1, "-BOLD_ITALIC--" + ConvertUtil.toString(Font
                        .NORMAL_SIZE - 3F)));
                y += fh + 1;
                page.addChild(new ZPrintText(x, y, -1, -1, -900, testString, 1, null));
                y += fh + 1;
                page.addChild(new ZPrintBarcode(x - 700 / 2, y, 700, 90,
                        "|123456789012345\n12345678901234567890\n12345678901",
                        ZPrintBarcode.BarcodeFormat.CODE_128));
                y += 90 + 1;
                page.addChild(new ZPrintBarcode(x - 90 / 2, y + 650, 650, 90, "|123456789012345\n1234567890",
                        ZPrintBarcode.BarcodeFormat.CODE_128));
                y += 650 + 1;
                page.addChild(new ZPrintQRCode(x - 190 / 2, y, 190, 190, 0, 1,
                        "|123456789012345\n12345678901234567890\n12345678901"
                        , 'M', 2));
                y += 150 + 1;
                try {
                    if (pageNo >= 0) {
                        int pageCount = printDoc.print(pageNo, pageNo);
                    }

                }
                catch (Exception e) {
                    Log.trace(e);
                    AlertDialog.Builder abuilder = new AlertDialog.Builder((Activity) v.getContext());
                    abuilder.setMessage(e.getMessage())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            }
        }

        BackgroundPrintZPP bgprint;
        boolean bgprinting = false;

        private void demoPrintForm(View v) {
            BillCalculator billCalc = new BillCalculator(123456.78);
            RecordCommentNew commNew = new RecordCommentNew() {
                @Override
                public void sentFinish() {
                    super.sentFinish();

                    bgprinting = false;
                    try {
                        Log.info(bgprint.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void sentFail() {
                    super.sentFail();
                    bgprinting = false;
                    try {
                        Log.info(bgprint.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }
            };
            if (bgprinting) {
            }
            Record record = new Record(Calendar.getInstance());
            bgprint = new BackgroundPrintZPP(getActivity(), billCalc, commNew);
            bgprint.setArrPersonalData(new Personal("จดหน่วย-แจ้งค่าไฟฟ้าโดย บริษัทตัวsแทน", "01-234-5678"));
            bgprint.setRec(record);
            ArrayList<MeterHistory> histories = new ArrayList<>();
            histories.add(new MeterHistory(record.getReadDate()));
            bgprint.setHistory(histories);
            ArrayList<ReceiptData> receipts = new ArrayList<>();
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(record.getReadDate().getTimeInMillis() - (1 * 30 * 24 * 60 * 60 * 1000));
            receipts.add(new ReceiptData("0123456789012345", record.getReadDate(), date));
            date.setTimeInMillis(record.getReadDate().getTimeInMillis() - (2 * 30 * 24 * 60 * 60 * 1000));
            receipts.add(new ReceiptData("0123456789012345", record.getReadDate(), date));
            date.setTimeInMillis(record.getReadDate().getTimeInMillis() - (3 * 30 * 24 * 60 * 60 * 1000));
            receipts.add(new ReceiptData("0123456789012345", record.getReadDate(), date));
            date.setTimeInMillis(record.getReadDate().getTimeInMillis() - (4 * 30 * 24 * 60 * 60 * 1000));
            receipts.add(new ReceiptData("1234567890123456", record.getReadDate(), date));
            date.setTimeInMillis(record.getReadDate().getTimeInMillis() - (5 * 30 * 24 * 60 * 60 * 1000));
            receipts.add(new ReceiptData("1234567890123456", record.getReadDate(), date));
            bgprint.setRD(receipts);
            String result = "";
            // Print to device DPP-450-R
            bgprint.execute("DPP-450R", "DemoPrintForm");
            // For testing print out to bitmap in cache
//                bgprint.execute("ZPosPrinterTest", "DemoPrintForm");
            bgprinting = true;
        }

    }

}
