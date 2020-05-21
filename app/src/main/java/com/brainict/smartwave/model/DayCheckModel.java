package com.brainict.smartwave.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DayCheckModel implements Serializable {
    private String day;
    private boolean checked;

    public DayCheckModel(String day) {
        this.day = day;
        checked = false;
    }
}
