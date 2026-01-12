package cc.coopersoft.keycloak.phone.providers.spi.phoneverify.impl.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpResponseDto {
    private String otpId;
    private Integer errorCode;
}
