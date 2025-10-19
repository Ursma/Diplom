package diplom.openvifApiCamera.controller;

import diplom.openvifApiCamera.dto.AuthRequest;
import diplom.openvifApiCamera.dto.CameraRequest;
import diplom.openvifApiCamera.dto.DeviceInfoResponse;
import diplom.openvifApiCamera.service.OnvifService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/api/camera")
@RequiredArgsConstructor
public class OnvifController {

    private final OnvifService onvifService;

    @PostMapping("/device-info")
    public DeviceInfoResponse getInfo(@RequestBody AuthRequest request) {
        return onvifService.getDeviceInfoResponse(request);
    }

//    @PostMapping("/snapshot")
//    public String takeSnapshot(@RequestBody CameraRequest request) {
//        return onvifService.takeSnapshot(request);
//    }

//    @GetMapping(value = "/mjpeg", produces = MediaType.MULTIPART_MIXED_VALUE)
//    public void streamCamera(HttpServletResponse response) throws IOException {
//        response.setContentType("multipart/x-mixed-replace; boundary=frame");
//        StreamingResponseBody body = onvifService.streamCamera();
//        body.writeTo(response.getOutputStream());
//    }

    @PostMapping("/snapshot/latest")
    public ResponseEntity<UrlResource> getLatestSnapshot(@RequestBody CameraRequest request) throws IOException {
        return onvifService.getLatestSnapshotByCamera(request);
    }
}
