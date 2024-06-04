package com.leesche.rvmtest;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.google.gson.Gson;
import com.leesche.logger.Logger;
import com.leesche.yyyiotlib.UnitAdapter;
import com.leesche.yyyiotlib.entity.CmdResultEntity;
import com.leesche.yyyiotlib.entity.UnitEntity;
import com.leesche.yyyiotlib.serial.callback.ControlCallBack;
import com.leesche.yyyiotlib.serial.manager.Cmd2Constants;
import com.leesche.yyyiotlib.serial.manager.helper.RvmHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    boolean isInitSuccess = false;
    TextView tvTitle, tvDevId, tvInfo;
    ScrollView svInfo;
    RecyclerView rvTest, rvChannelA, rvChannelB;
    ImageView ivClear;
    Gson gson;
    String devId = "";
    String fwVer = "";
    StringBuilder sbInfo = new StringBuilder();

    UnitAdapter unitAdapter;
    DevStatusAdapter devStatusAdapterA, devStatusAdapterB;
    List<UnitEntity> devAList = new ArrayList<>();
    List<UnitEntity> devBList = new ArrayList<>();
    List<UnitEntity> unitEntities = new ArrayList<>();
    String curTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    ControlCallBack controlCallBack = new ControlCallBack() {
        @Override
        public void onResult(String cmdBackResult) {
            if (gson == null) gson = new Gson();
            CmdResultEntity cmdResultEntity = gson.fromJson(cmdBackResult, CmdResultEntity.class);
            if (cmdResultEntity.getFunc_code() != Cmd2Constants.CMD_StmToAndroid.CMD_OStar_StatusErr &&
                    cmdResultEntity.getFunc_code() != Cmd2Constants.CMD_StmToAndroid.CMD_OStar_OutNum &&
                    cmdResultEntity.getFunc_code() != Cmd2Constants.CMD_StmToAndroid.CMD2STM32_BackCtOtherMotor) {
                sbInfo.append("[").append(curTime).append("] ");
            }
            switch (cmdResultEntity.getFunc_code()) {
                case Cmd2Constants.CMD_StmToAndroid.CMD_OStar_BackMacInfo:
                    //dev id and FW version form main board
                    String[] values = cmdResultEntity.getValue().split("\\|");
                    devId = values[1];
                    fwVer = values[0];
                    tvDevId.setText("Dev Id: " + devId);
                    sbInfo.append("Dev Id: ").append(devId).append(" FW: V").append(fwVer).append("\n");
                    break;
                case Cmd2Constants.CMD_SCAN_StmToAndroid.CMD_OStar_BackMacInfo:
                    if (cmdResultEntity.getStatus() == 1 || cmdResultEntity.getStatus() == 2) {
                        sbInfo.append(cmdResultEntity.getBox_code() == 1 ? getString(R.string.scannerA_02) : getString(R.string.scannerB_02))
                                .append(getString(R.string.power_on)).append("\n");
                    }
                    if (cmdResultEntity.getStatus() == 3 || cmdResultEntity.getStatus() == 4) {
                        sbInfo.append(cmdResultEntity.getBox_code() == 1 ? getString(R.string.scannerA_02) : getString(R.string.scannerB_02))
                                .append(getString(R.string.power_off)).append("\n");
                    }
                    break;
                case Cmd2Constants.CMD_StmToAndroid.CMD_OStar_BackOpenCloseDoor:
                    if (cmdResultEntity.getStatus() == 2) {
                        sbInfo.append(getString(R.string.entrance_a_open_success));
                    }
                    if (cmdResultEntity.getStatus() == 4) {
                        sbInfo.append(getString(R.string.entrance_a_close_success));
                    }
                    if (cmdResultEntity.getStatus() == 3) {
                        sbInfo.append(getString(R.string.entrance_b_open_success));
                    }
                    if (cmdResultEntity.getStatus() == 5) {
                        sbInfo.append(getString(R.string.entrance_b_close_success));
                    }
                    if (cmdResultEntity.getStatus() == 12) {
                        sbInfo.append(getString(R.string.recycle_door_open_success));
                    }
                    if (cmdResultEntity.getStatus() == 13) {
                        sbInfo.append(getString(R.string.recycle_door_close_success));
                    }//open or close signal form door
                    break;
                case Cmd2Constants.CMD_StmToAndroid.CMD_OStar_OutNum:
                    String[] bottleCountStr = cmdResultEntity.getValue().split("\\|");
                    if (Integer.parseInt(bottleCountStr[0]) != 0) {
                        sbInfo.append(getString(R.string.entrance_a_collect)).append(bottleCountStr[0]).append("\n");
                    }
                    if (Integer.parseInt(bottleCountStr[1]) != 0) {
                        sbInfo.append(getString(R.string.entrance_b_collect)).append(bottleCountStr[1]).append("\n");
                    }//bottle count
                    break;
                case Cmd2Constants.CMD_StmToAndroid.CMD_OStar_WeightCheckRes:
                    int weight = Integer.parseInt(cmdResultEntity.getValue());
                    BottleChecker.getInstance().setBottleWeightStatus(cmdResultEntity.getBox_code(), weight);
                    String hintMsg = BottleChecker.getInstance().checkToSendCmdToStm(MainActivity.this, cmdResultEntity);//check the weight of bottle
                    sbInfo.append(getString(R.string.weight_hint)).append(cmdResultEntity.getBox_code() == 1 ?
                            getString(R.string.entrance_a) : getString(R.string.entrance_b)).append(cmdResultEntity.getValue() + "g. ").append(hintMsg).append("\n");
                    break;
                case Cmd2Constants.CMD_SCAN_StmToAndroid.CMD_OStar_BarCode:
                    BottleChecker.getInstance().setBottleCodeStatus(cmdResultEntity);
                    String hint2Msg = BottleChecker.getInstance().checkToSendCmdToStm(MainActivity.this, cmdResultEntity);//check the barcode of bottle
                    sbInfo.append(hint2Msg).append(cmdResultEntity.getBox_code() == 1 ? getString(R.string.scannerA) : getString(R.string.scannerB))
                            .append(" [").append(cmdResultEntity.getValue().trim()).append("]\n");
                    break;
                case Cmd2Constants.CMD_StmToAndroid.CMD_OStar_BackEnMT220V:
                    sbInfo.append((cmdResultEntity.getStatus() == 6) ? getString(R.string.shredder_on) : getString(R.string.shredder_off)); //Shredder
                    break;
                case Cmd2Constants.CMD_StmToAndroid.CMD_OStar_StatusErr:
                    DevStatusHandler.getInstance().updateEntranceAStatus(cmdResultEntity.getValue(), devAList, devBList);
                    devStatusAdapterA.setNewData(devAList);
                    devStatusAdapterB.setNewData(devBList);
                    break;
                case Cmd2Constants.CMD_SCAN_StmToAndroid.CMD_OStar_Distance:
                    sbInfo.append(getString(R.string.distance_show)).append(cmdResultEntity.getValue()).append("mm \n");//distance form the bottom of container
                    break;
                case Cmd2Constants.CMD_SCAN_StmToAndroid.CMD_OStar_LoadCode:
                case Cmd2Constants.CMD_StmToAndroid.CMD_OStar_QrCode:
                    sbInfo.append(getString(R.string.login_code_show)).append(cmdResultEntity.getValue()).append("\n");//received code to login
                    break;
                case Cmd2Constants.OtherSysStatus.ENTRANCE_STATUS:
                    BottleChecker.getInstance().init(cmdResultEntity.getBox_code());
                    sbInfo.append(getString(cmdResultEntity.getStatus() == 11 ? R.string.bottle_in_hint : R.string.bottle_out_hint));
                    break;
                default:
                    Logger.i("【RVM Result】 " + cmdBackResult);
                    break;
            }
            runOnUiThread(() -> {
                tvInfo.setText(sbInfo.toString());
                svInfo.fullScroll(ScrollView.FOCUS_DOWN);
            });
        }

        @Override
        public void onSaleResult(String saleCmdBackResult) {

        }

        @Override
        public void onDeviceStatusResult(int code, String otherResult) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
        initTestItem(-1);
        initRvm();
    }

    private void initView() {
        rvTest = findViewById(R.id.rvTest);
        rvChannelA = findViewById(R.id.rvChannelA);
        rvChannelB = findViewById(R.id.rvChannelB);
        tvTitle = findViewById(R.id.tvTitle);
        tvDevId = findViewById(R.id.tvDevId);
        svInfo = findViewById(R.id.svInfo);
        tvInfo = findViewById(R.id.tvInfo);
        ivClear = findViewById(R.id.ivClear);
        tvTitle.setText("RVMTest(1.0.1)");
        ivClear.setOnClickListener(view -> {
            sbInfo.delete(0, sbInfo.length());
            tvInfo.setText(sbInfo.toString());
        });
        unitAdapter = new UnitAdapter(R.layout.item_unit, unitEntities);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        devStatusAdapterA = new DevStatusAdapter(R.layout.item_dev_status, devAList);
        devStatusAdapterB = new DevStatusAdapter(R.layout.item_dev_status, devBList);
        rvChannelA.setLayoutManager(new GridLayoutManager(this, 4));
        rvChannelA.setAdapter(devStatusAdapterA);
        rvChannelB.setLayoutManager(new GridLayoutManager(this, 4));
        rvChannelB.setAdapter(devStatusAdapterB);
        rvTest.setLayoutManager(layoutManager);
        rvTest.setAdapter(unitAdapter);
        rvTest.addOnItemTouchListener(new OnItemChildClickListener() {
            @Override
            public void onSimpleItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                handlerTestItemByPosition(position);
            }
        });
    }

    private void initRvm() {
        ThreadManager.getThreadPollProxy().execute(() -> {
            do {
                isInitSuccess = RvmHelper.getInstance().initDev(MainActivity.this, 1);//0-->single entrance  1-->double entrance
            } while (!isInitSuccess);
            RvmHelper.getInstance().addControlCallBack(controlCallBack);
            while (TextUtils.isEmpty(devId)) {
                RvmHelper.getInstance().getDevIdAndEnableEntrance(true, true);
                SystemClock.sleep(6000);
            }
        });
    }

    private void handlerTestItemByPosition(int position) {
        UnitEntity unitEntity = unitEntities.get(position);
        if (unitEntity.getUnit_type() == 1) {
            RvmHelper.getInstance().openOrCloseEntrance(unitEntity.getUnit_no(), true);
        }
        if (unitEntity.getUnit_type() == 2) {
            RvmHelper.getInstance().openOrCloseEntrance(unitEntity.getUnit_no(), false);
        }
        if (unitEntity.getUnit_type() == 3) {
            //test the shredder,If shredder work normal, it will be work when the entrance door opening.
            RvmHelper.getInstance().testOpenOrCloseCrashMotor(unitEntity.getUnit_no() == 1);
        }
        if (unitEntity.getUnit_type() == 4) {
            RvmHelper.getInstance().uploadCheckResultToBoard(unitEntity.getUnit_no(), 0, true);
        }
        if (unitEntity.getUnit_type() == 5) {
            RvmHelper.getInstance().uploadCheckResultToBoard(unitEntity.getUnit_no(), 0, false);
        }
        if (unitEntity.getUnit_type() == 8) {
            RvmHelper.getInstance().openRecycleDoor();
        }
        if (unitEntity.getUnit_type() == 9) {
            initTestItem(unitEntity.getUnit_no());
        }
        if (unitEntity.getUnit_type() == 10) {
            RvmHelper.getInstance().sendOtherControlCmd(unitEntity.getUnit_no());
        }
    }

    private void initTestItem(int entranceNo) {
        if (unitEntities.size() > 0) unitEntities.clear();
        if (entranceNo == 1) {
            unitEntities.add(new UnitEntity(1, 1, getString(R.string.open_entrance_a)));
            unitEntities.add(new UnitEntity(2, 10, getString(R.string.belt_forward)));
            unitEntities.add(new UnitEntity(3, 10, getString(R.string.belt_back)));
            unitEntities.add(new UnitEntity(4, 10, getString(R.string.belt_stop)));
            unitEntities.add(new UnitEntity(5, 10, getString(R.string.turn_to_low)));
            unitEntities.add(new UnitEntity(6, 10, getString(R.string.turn_to_high)));
            unitEntities.add(new UnitEntity(7, 10, getString(R.string.roller_start)));
            unitEntities.add(new UnitEntity(8, 10, getString(R.string.roller_stop)));
            unitEntities.add(new UnitEntity(1, 2, getString(R.string.close_entrance_a)));
            unitEntities.add(new UnitEntity(-1, 9, getString(R.string.back)));
            unitAdapter.setNewData(unitEntities);
            return;
        }
        if (entranceNo == 2) {
            unitEntities.add(new UnitEntity(2, 1, getString(R.string.open_entrance_b)));
            unitEntities.add(new UnitEntity(22, 10, getString(R.string.belt_forward)));
            unitEntities.add(new UnitEntity(23, 10, getString(R.string.belt_back)));
            unitEntities.add(new UnitEntity(24, 10, getString(R.string.belt_stop)));
            unitEntities.add(new UnitEntity(25, 10, getString(R.string.turn_to_low)));
            unitEntities.add(new UnitEntity(26, 10, getString(R.string.turn_to_high)));
            unitEntities.add(new UnitEntity(27, 10, getString(R.string.roller_start)));
            unitEntities.add(new UnitEntity(28, 10, getString(R.string.roller_stop)));
            unitEntities.add(new UnitEntity(2, 2, getString(R.string.close_entrance_b)));
            unitEntities.add(new UnitEntity(-1, 9, getString(R.string.back)));
            unitAdapter.setNewData(unitEntities);
            return;
        }
        unitEntities.add(new UnitEntity(1, 1, getString(R.string.open_entrance_a)));
        unitEntities.add(new UnitEntity(1, 2, getString(R.string.close_entrance_a)));
        unitEntities.add(new UnitEntity(1, 4, getString(R.string.trans_in_a)));
        unitEntities.add(new UnitEntity(1, 5, getString(R.string.trans_out_a)));
        unitEntities.add(new UnitEntity(2, 1, getString(R.string.open_entrance_b)));
        unitEntities.add(new UnitEntity(2, 2, getString(R.string.close_entrance_b)));
        unitEntities.add(new UnitEntity(2, 4, getString(R.string.trans_int_b)));
        unitEntities.add(new UnitEntity(2, 5, getString(R.string.trans_out_b)));
        unitEntities.add(new UnitEntity(3, 1, getString(R.string.open_entrance_all)));
        unitEntities.add(new UnitEntity(3, 2, getString(R.string.close_entrance_all)));
        unitEntities.add(new UnitEntity(1, 3, getString(R.string.start_shredder)));
        unitEntities.add(new UnitEntity(0, 3, getString(R.string.stop_shredder)));
        unitEntities.add(new UnitEntity(1, 8, getString(R.string.open_recycle_door)));
        unitEntities.add(new UnitEntity(1, 9, getString(R.string.test_for_entrance_01)));
        unitEntities.add(new UnitEntity(2, 9, getString(R.string.test_for_entrance_02)));
        unitEntities.add(new UnitEntity(2, 11, getString(R.string.uvc_camera)));
        unitAdapter.setNewData(unitEntities);
    }
}