package com.afcrm.server.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CalendarFormDataDto {
    private Long id;
    private String serviceName;
    private String address;
    private String client;
    private String groupBrand;
    private String groupModel;
}
