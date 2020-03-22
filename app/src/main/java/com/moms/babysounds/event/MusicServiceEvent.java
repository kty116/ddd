package com.moms.babysounds.event;

import android.location.Location;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * 위치데이터 이벤트
 */

@Getter
@Setter
@AllArgsConstructor
public class MusicServiceEvent implements MessageEvent {
    private boolean live;

}
