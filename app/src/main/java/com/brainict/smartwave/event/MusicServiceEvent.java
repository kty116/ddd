package com.brainict.smartwave.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


/**
 * MusicService 종료 event
 */

@Getter
@Setter
@AllArgsConstructor
public class MusicServiceEvent implements MessageEvent {
    private boolean live;

}
