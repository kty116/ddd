package com.brainict.smartwave.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * 플레이 리스트에 들려줄 항목이 있는지 체크하고 리스트값으로 주파수 설정 바꾸는 event
 */

@Getter
@Setter
@AllArgsConstructor
public class PlayMusicEvent implements MessageEvent {

}
