package cc.coopersoft.keycloak.phone.providers.spi.phoneverify.impl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpResponseDto {

    private String result;

}
