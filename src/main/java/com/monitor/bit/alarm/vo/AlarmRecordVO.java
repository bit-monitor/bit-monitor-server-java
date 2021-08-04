package com.monitor.bit.alarm.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AlarmRecordVO {

    private Long id;

    /**
     * 预警规则id
     */
    private Long alarmId;

    /**
     * 报警内容，格式为JSON字符串
     */
    private String alarmData;

    /**
     * 创建时间
     */
    private Date createTime;
}
