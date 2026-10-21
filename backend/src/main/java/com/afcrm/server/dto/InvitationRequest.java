package com.afcrm.server.dto;

import com.afcrm.server.model.Role;
import lombok.Data;

@Data
public class InvitationRequest {
    private Role role;
}
