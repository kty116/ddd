package com.moms.babysounds.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.moms.babysounds.R;
import com.moms.babysounds.activity.MainActivity;
import com.moms.babysounds.common.TinyDB;
import com.moms.babysounds.databinding.FragmentAwakeningBinding;
import com.moms.babysounds.databinding.FragmentMainBinding;
import com.moms.babysounds.model.DayCheckModel;

import java.util.ArrayList;

public class MainFragment extends DefaultFragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private FragmentMainBinding mBinding;
    public static final String TAG = MainFragment.class.getSimpleName();
    private ArrayList<View> mDayGroupChildList;
    private DayCheckModel[] mDayCheckModels;
    private TinyDB mTinyDB;
    private Fragment fragment;

    public MainFragment() {
    }

    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding.m01.setOnClickListener(this);
        mBinding.m02.setOnClickListener(this);
        mBinding.m03.setOnClickListener(this);
        mBinding.m04.setOnClickListener(this);
        mBinding.m05.setOnClickListener(this);
        mBinding.m06.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.m01:
                fragment = MethodOfExecutionFragment.newInstance("D", "d");
                ((MainActivity)getActivity()).setFragment(fragment);

                break;
            case R.id.m02:
                fragment = DeepSleepFragment.newInstance("D", "d");
                ((MainActivity)getActivity()).setFragment(fragment);
                break;
            case R.id.m03:
                fragment = AwakeningFragment.newInstance("D", "d");
                ((MainActivity)getActivity()).setFragment(fragment);
                break;
            case R.id.m04:

                break;
            case R.id.m05:
                fragment = RestFragment.newInstance("D", "d");
                ((MainActivity)getActivity()).setFragment(fragment);
                break;
            case R.id.m06:
                fragment = NotAutoSleepFragment2.newInstance("D", "d");
                ((MainActivity)getActivity()).setFragment(fragment);
                break;

        }

    }
}
