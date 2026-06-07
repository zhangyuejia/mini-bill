package com.minibill.common.constant;

/**
 * 物件类型字典值枚举
 * 对应 sys_dict_type.code = 'item_type' 下的 sys_dict_data.value
 */
public enum ItemType {

    HOME_APPLIANCE("home_appliance", "家用电器"),
    ELECTRONIC("electronic", "电子设备"),
    VEHICLE("vehicle", "交通工具"),
    FURNITURE("furniture", "家具"),
    OTHER("other", "其他");

    private final String value;
    private final String label;

    ItemType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static ItemType fromValue(String value) {
        for (ItemType t : values()) {
            if (t.value.equals(value)) return t;
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
