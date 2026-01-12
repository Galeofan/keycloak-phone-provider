package cc.coopersoft.keycloak.phone.providers.sender.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsRequestDto {
    private String phoneNumber;
}
