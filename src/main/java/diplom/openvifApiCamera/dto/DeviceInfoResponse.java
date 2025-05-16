package diplom.openvifApiCamera.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeviceInfoResponse {
    private String manufacturer;
    private String model;
    private String firmwareVersion;
    private String serialNumber;
    private String hardwareId;
}