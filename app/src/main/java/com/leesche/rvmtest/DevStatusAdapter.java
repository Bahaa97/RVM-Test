package com.leesche.rvmtest;

import android.graphics.Color;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.leesche.yyyiotlib.entity.UnitEntity;

import java.util.List;

public class DevStatusAdapter extends BaseQuickAdapter<UnitEntity, BaseViewHolder> {


    public DevStatusAdapter(int layoutResId, @Nullable List<UnitEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, UnitEntity item) {
        helper.setText(R.id.tv_unit_name, item.getUnit_name());
        helper.addOnClickListener(R.id.ll_type_root);
        if(item.getUnit_no() == 0){
            if(item.getUnit_type() == 0){
                helper.setBackgroundColor(R.id.tv_unit_name, Color.parseColor("#20D86E"));
            }else{
                helper.setBackgroundColor(R.id.tv_unit_name, Color.RED);
            }
        }
        if(item.getUnit_no() == 1){
            if(item.getUnit_type() == 1){
                helper.setBackgroundColor(R.id.tv_unit_name, Color.parseColor("#20D86E"));
            }else{
                helper.setBackgroundColor(R.id.tv_unit_name, Color.RED);
            }
        }
        if(item.getUnit_no() == 2){
            if(!item.getUnit_name().contains("ERR")){
                helper.setBackgroundColor(R.id.tv_unit_name, Color.parseColor("#20D86E"));
            }else{
                helper.setBackgroundColor(R.id.tv_unit_name, Color.RED);
            }
        }
    }
}
