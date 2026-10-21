package com.afcrm.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfigDto {
    private String theme;
    private Boolean oauthEnabled;
    private String customConfiguration;
}
