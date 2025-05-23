package diplom.openvifApiCamera.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AuthRequest {
    private Integer cameraId;
    private String username;
    private String password;
}
