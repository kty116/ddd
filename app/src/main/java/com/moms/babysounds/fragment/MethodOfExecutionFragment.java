package com.moms.babysounds.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.moms.babysounds.R;
import com.moms.babysounds.activity.MainActivity;
import com.moms.babysounds.common.Constants;
import com.moms.babysounds.databinding.FragmentMethodOfExecutionBinding;
import com.moms.babysounds.model.AudioSetModel;
import com.moms.babysounds.service.MusicService;

import java.util.ArrayList;

public class MethodOfExecutionFragment extends DefaultFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private FragmentMethodOfExecutionBinding mBinding;

    public MethodOfExecutionFragment() {
    }

    public static MethodOfExecutionFragment newInstance(String param1, String param2) {
        MethodOfExecutionFragment fragment = new MethodOfExecutionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_method_of_execution, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.autoButton.setOnClickListener(this);
        mBinding.notAutoButton.setOnClickListener(this);
        mBinding.layoutDefault.homeButton.setOnClickListener(this);
        mBinding.layoutDefault.settingButton.setOnClickListener(this);
        mBinding.topLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {

            case R.id.auto_button:
                //자동
                ArrayList<AudioSetModel> audioSetModelList = new ArrayList<>();
                audioSetModelList.add(new AudioSetModel(0, 4 * Constants.SETTING_HZ_UP, Constants.MINUTE * 5, false));
                audioSetModelList.add(new AudioSetModel(0, 5 * Constants.SETTING_HZ_UP, Constants.MINUTE * 10, false));
                audioSetModelList.add(new AudioSetModel(0, 4 * Constants.SETTING_HZ_UP, Constants.MINUTE * 10, false));
                ((MainActivity) getActivity()).setFragment(InduceSleepFragment.newInstance(audioSetModelList, ""));
                break;

            case R.id.not_auto_button:
                //수동
                ((MainActivity) getActivity()).setFragment(NotAutoSleepFragment.newInstance("", ""));
                break;
        }
    }
}
