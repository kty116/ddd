package com.moms.babysounds.event;

import androidx.fragment.app.Fragment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * 위치데이터 이벤트
 */

@Getter
@Setter
@AllArgsConstructor
public class FragmentRemoveEvent implements MessageEvent {
    private Fragment fragment;

}
