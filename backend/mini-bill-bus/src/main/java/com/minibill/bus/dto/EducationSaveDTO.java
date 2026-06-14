package com.minibill.bus.dto;

import com.minibill.bus.entity.BusEducation;
import com.minibill.bus.entity.BusEducationItem;
import lombok.Data;

import java.util.List;

@Data
public class EducationSaveDTO {
    private BusEducation education;
    private List<BusEducationItem> items;
}
