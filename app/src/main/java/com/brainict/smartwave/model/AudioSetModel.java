package com.brainict.smartwave.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class AudioSetModel implements Serializable {
    private int audioWaveForm;
    private double hz;
    private long time;
    private boolean alreadyPlayed;

}
