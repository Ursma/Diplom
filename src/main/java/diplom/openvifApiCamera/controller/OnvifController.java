package diplom.openvifApiCamera.controller;

import diplom.openvifApiCamera.dto.AuthRequest;
import diplom.openvifApiCamera.dto.DeviceInfoResponse;
import diplom.openvifApiCamera.service.OnvifService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/api/camera")
@RequiredArgsConstructor
public class OnvifController {

    private final OnvifService onvifService;

    @PostMapping("/info")
    public DeviceInfoResponse getInfo(@RequestBody AuthRequest request) {
        return onvifService.getDeviceInfoResponse(request);
    }

    @PostMapping("/snapshot")
    public String takeSnapshot() {
        return onvifService.takeSnapshot();
    }

    @GetMapping(value = "/mjpeg", produces = MediaType.MULTIPART_MIXED_VALUE)
    public void streamCamera(HttpServletResponse response) throws IOException {
        response.setContentType("multipart/x-mixed-replace; boundary=frame");
        StreamingResponseBody body = onvifService.streamCamera();
        body.writeTo(response.getOutputStream());
    }
}
