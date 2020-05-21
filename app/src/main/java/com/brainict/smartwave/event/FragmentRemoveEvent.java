package com.brainict.smartwave.event;

import androidx.fragment.app.Fragment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * 해당 프래그먼트에서 프래그먼트를 지우고 다른 프래그먼트로 이동시 사용
 */

@Getter
@Setter
@AllArgsConstructor
public class FragmentRemoveEvent implements MessageEvent {
    private Fragment fragment;

}
