package cc.coopersoft.keycloak.phone.providers.spi.phoneverify.impl.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpRequestDto {

    private String code;
    private String session;

}
