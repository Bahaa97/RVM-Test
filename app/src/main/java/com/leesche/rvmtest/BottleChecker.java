package com.leesche.rvmtest;

import android.content.Context;

import com.leesche.logger.Logger;
import com.leesche.yyyiotlib.entity.CmdResultEntity;
import com.leesche.yyyiotlib.serial.manager.helper.RvmHelper;

public class BottleChecker {

    boolean bottleWeightHaveChecked = false;
    boolean bottleCodeHaveChecked = false;
    boolean bottleWeightCheckedIsValid = false;
    boolean bottleCodeCheckedIsValid = false;

    boolean bottleBWeightHaveChecked = false;
    boolean bottleBCodeHaveChecked = false;
    boolean bottleBWeightCheckedIsValid = false;
    boolean bottleBCodeCheckedIsValid = false;
    static BottleChecker bottleChecker;

    public static BottleChecker getInstance() {
        synchronized (BottleChecker.class) {
            if (bottleChecker == null) {
                bottleChecker = new BottleChecker();
            }
        }
        return bottleChecker;
    }

    public void init(int boxCode) {
        if (boxCode == 1) {
            this.bottleCodeHaveChecked = false;
            this.bottleWeightHaveChecked = false;
            this.bottleCodeCheckedIsValid = false;
            this.bottleWeightCheckedIsValid = false;
        } else {
            this.bottleBCodeHaveChecked = false;
            this.bottleBWeightHaveChecked = false;
            this.bottleBCodeCheckedIsValid = false;
            this.bottleBWeightCheckedIsValid = false;
        }
    }

    public void setBottleWeightStatus(int boxCode, int weight) {
        Logger.i("【Bottle Checker】weight " + boxCode);
        if (boxCode == 1) {
            this.bottleWeightHaveChecked = true;
            this.bottleWeightCheckedIsValid = weight < 50;
        } else {
            this.bottleBWeightHaveChecked = true;
            this.bottleBWeightCheckedIsValid = weight < 50;
        }
    }

    public boolean isCanSendCmdToStm(int boxCode) {
        Logger.i("【Bottle Checker】check " + boxCode);
        if (boxCode == 1) {
            if (bottleWeightHaveChecked && !bottleWeightCheckedIsValid) return true;
            if (bottleCodeHaveChecked && !bottleCodeCheckedIsValid) return true;
            return bottleWeightHaveChecked && bottleCodeHaveChecked;
        } else {
            if (bottleBWeightHaveChecked && !bottleBWeightCheckedIsValid) return true;
            if (bottleBCodeHaveChecked && !bottleBCodeCheckedIsValid) return true;
            return bottleBWeightHaveChecked && bottleBCodeHaveChecked;
        }
    }

    public void setBottleCodeStatus(CmdResultEntity cmdResultEntity) {
        Logger.i("【Bottle Checker】code " + cmdResultEntity.getValue());
        if (cmdResultEntity.getBox_code() == 1) {
            this.bottleCodeHaveChecked = true;
            this.bottleCodeCheckedIsValid = !cmdResultEntity.getValue().trim().equals("123456");
        } else {
            this.bottleBCodeHaveChecked = true;
            this.bottleBCodeCheckedIsValid = !cmdResultEntity.getValue().trim().equals("123456");
        }
    }

    public void setBottleCodeStatus(int boxCode, boolean isValid) {
        if (boxCode == 1) {
            this.bottleCodeHaveChecked = true;
            this.bottleCodeCheckedIsValid = isValid;
        } else {
            this.bottleBCodeHaveChecked = true;
            this.bottleBCodeCheckedIsValid = isValid;
        }
    }

    public boolean isBottleValid(int boxCode) {
        if (boxCode == 1) {
            return bottleWeightCheckedIsValid && bottleCodeCheckedIsValid;
        } else {
            return bottleBWeightCheckedIsValid && bottleBCodeCheckedIsValid;
        }

    }

    public int checkIsPass(CmdResultEntity cmdResultEntity) {
        if (isCanSendCmdToStm(cmdResultEntity.getBox_code())) {
            boolean isPass = BottleChecker.getInstance().isBottleValid(cmdResultEntity.getBox_code());
            RvmHelper.getInstance().uploadCheckResultToBoard(cmdResultEntity.getBox_code(), cmdResultEntity.getStatus(), isPass);
            int statusCode = 2;
            if (!isPass) {
                if (cmdResultEntity.getBox_code() == 1) {
                    if (bottleCodeHaveChecked && bottleWeightHaveChecked) {
                        if (!bottleCodeCheckedIsValid && !bottleWeightCheckedIsValid)
                            statusCode = 3;
                        if (bottleCodeCheckedIsValid && !bottleWeightCheckedIsValid) statusCode = 4;
                        if (!bottleCodeCheckedIsValid && bottleWeightCheckedIsValid) statusCode = 5;
                    }
                    if (bottleCodeHaveChecked && !bottleWeightHaveChecked) {
                        return 6;
                    }
                    if (!bottleCodeHaveChecked && bottleWeightHaveChecked) {
                        return 7;
                    }
                }
                if (cmdResultEntity.getBox_code() == 2) {
                    if (bottleBCodeHaveChecked && bottleBWeightHaveChecked) {
                        if (!bottleBCodeCheckedIsValid && !bottleBWeightCheckedIsValid)
                            statusCode = 3;
                        if (bottleBCodeCheckedIsValid && !bottleBWeightCheckedIsValid)
                            statusCode = 4;
                        if (!bottleBCodeCheckedIsValid && bottleBWeightCheckedIsValid)
                            statusCode = 5;
                    }
                    if (bottleBCodeHaveChecked && !bottleBWeightHaveChecked) {
                        return 6;
                    }
                    if (!bottleBCodeHaveChecked && bottleBWeightHaveChecked) {
                        return 7;
                    }
                }
            }
            init(cmdResultEntity.getBox_code());
            return statusCode;
        } else {
            if (cmdResultEntity.getBox_code() == 1) {
                if (bottleCodeHaveChecked) {
                    Logger.i("【Bottle Checker】 等待检查重量 (" + cmdResultEntity.getBox_code() + ")");
                    return 1;
                }
                if (bottleWeightHaveChecked) {
                    Logger.i("【Bottle Checker】 等待检查条码(" + cmdResultEntity.getBox_code() + ")");
                    return 0;
                }
            }
            if (cmdResultEntity.getBox_code() == 2) {
                if (bottleBCodeHaveChecked) {
                    Logger.i("【Bottle Checker】 等待检查重量 (" + cmdResultEntity.getBox_code() + ")");
                    return 1;
                }
                if (bottleBWeightHaveChecked) {
                    Logger.i("【Bottle Checker】 等待检查条码(" + cmdResultEntity.getBox_code() + ")");
                    return 0;
                }
            }
            return -1;
        }
    }

    public String getMsgHintByStatus(int boxCode, int status) {
        String hintMsg = "  waiting for check...";
        switch (status) {
            case 0:
                hintMsg = "  waiting for check barcode...";
                break;
            case 1:
                hintMsg = "waiting for check weight...";
                break;
            case 2:
                hintMsg = "barcode and weight are all match together.";
                break;
            case 3:
                hintMsg = "  barcode and weight aren't match together.";
                break;
            case 4:
            case 7:
                hintMsg = "weight more than 50g!";
                break;
            case 5:
            case 6:
                hintMsg = "  Invalid barcode!";
                break;
            case -1:
                if (boxCode == 1) {
                    if ((bottleCodeHaveChecked && bottleCodeCheckedIsValid && !bottleWeightHaveChecked) ||
                            (!bottleCodeHaveChecked && bottleWeightHaveChecked && bottleWeightCheckedIsValid)) {
                        hintMsg = "checked overtime!\n";
                    } else {
                        hintMsg = "pls deposit standardize!\n";
                    }
                }
                if (boxCode == 2) {
                    if ((bottleBCodeHaveChecked && bottleBCodeCheckedIsValid && !bottleBWeightHaveChecked) ||
                            (!bottleBCodeHaveChecked && bottleBWeightHaveChecked && bottleBWeightCheckedIsValid)) {
                        hintMsg = "checked overtime!\n";
                    } else {
                        hintMsg = "pls deposit standardize!\n";
                    }
                }
                break;
        }
        return hintMsg;
    }

    public String checkToSendCmdToStm(Context context, CmdResultEntity cmdResultEntity) {
        int status = checkIsPass(cmdResultEntity);
        return getMsgHintByStatus(cmdResultEntity.getBox_code(), status);
    }
}
