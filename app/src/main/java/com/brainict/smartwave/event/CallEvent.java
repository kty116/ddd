package com.brainict.smartwave.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * 통화나 벨이 울린 후에도 소리 유지 할 수 있게 하는 event
 */

@Getter
@Setter
@AllArgsConstructor
public class CallEvent implements MessageEvent {

}
