package cc.coopersoft.keycloak.phone.providers.representations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.models.utils.KeycloakModelUtils;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenCodeRepresentation {

    private String id;
    private String phoneNumber;
    private String otpId;
    private String type;
    private Date createdAt;
    private Date expiresAt;
    private Boolean confirmed;

    public static TokenCodeRepresentation forPhoneNumber(String phoneNumber, String otpId) {

        TokenCodeRepresentation tokenCode = new TokenCodeRepresentation();

        tokenCode.id = KeycloakModelUtils.generateId();
        tokenCode.phoneNumber = phoneNumber;
        tokenCode.otpId = otpId;
        tokenCode.confirmed = false;

        return tokenCode;
    }
}
