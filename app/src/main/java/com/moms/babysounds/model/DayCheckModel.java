package com.moms.babysounds.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
