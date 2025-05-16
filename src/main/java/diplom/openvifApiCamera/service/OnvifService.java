package diplom.openvifApiCamera.service;

import diplom.openvifApiCamera.dto.AuthRequest;
import diplom.openvifApiCamera.dto.CameraRequest;
import diplom.openvifApiCamera.dto.DeviceInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
@Service
public class OnvifService {

    public DeviceInfoResponse getDeviceInfoResponse(AuthRequest request) {
        try {
            // 1. Открываем SOAP-соединение
            SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();
            SOAPConnection connection = connectionFactory.createConnection();
            String deviceUrl = "http://192.168.3.22:10000/onvif/device_service";
            switch (request.getCameraId()) {
                case 1: deviceUrl = "http://192.168.3.22:10000/onvif/device_service";
                        break;
                case 2: deviceUrl = "http://192.168.3.22:10001/onvif/device_service";
                        break;
                case 3: deviceUrl = "http://192.168.3.22:10001/onvif/device_service";
                    break;
            }
            URL endpoint = new URL(deviceUrl);
            SOAPMessage soapRequest = createGetDeviceInformationRequest(request.getUsername(), request.getPassword());

            MimeHeaders headers = soapRequest.getMimeHeaders();
            headers.addHeader("SOAPAction", "\"http://www.onvif.org/ver10/device/wsdl/GetDeviceInformation\"");

            SOAPMessage soapResponse = connection.call(soapRequest, endpoint);

            // 2. Сохраняем ответ в поток
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            soapResponse.writeTo(out);
            connection.close();

            // 3. Читаем поток как XML-документ
            ByteArrayInputStream inputStream = new ByteArrayInputStream(out.toByteArray());
            return parseDeviceInfo(inputStream);

        } catch (Exception e) {
            throw new RuntimeException("Failed to get device information", e);
        }
    }

    private DeviceInfoResponse parseDeviceInfo(InputStream soapInputStream) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(soapInputStream);

            DeviceInfoResponse deviceInfo = new DeviceInfoResponse();

            NodeList responseList = doc.getElementsByTagNameNS("*", "GetDeviceInformationResponse");
            if (responseList.getLength() == 0) {
                throw new RuntimeException("No GetDeviceInformationResponse found");
            }

            org.w3c.dom.Node responseNode = responseList.item(0);
            NodeList fields = responseNode.getChildNodes();
            for (int i = 0; i < fields.getLength(); i++) {
                org.w3c.dom.Node field = fields.item(i);
                if (field.getNodeType() == Node.ELEMENT_NODE) {
                    switch (field.getLocalName()) {
                        case "Manufacturer":
                            deviceInfo.setManufacturer(field.getTextContent());
                            break;
                        case "Model":
                            deviceInfo.setModel(field.getTextContent());
                            break;
                        case "FirmwareVersion":
                            deviceInfo.setFirmwareVersion(field.getTextContent());
                            break;
                        case "SerialNumber":
                            deviceInfo.setSerialNumber(field.getTextContent());
                            break;
                        case "HardwareId":
                            deviceInfo.setHardwareId(field.getTextContent());
                            break;
                    }
                }
            }
            return deviceInfo;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SOAP response", e);
        }
    }


//    public String takeSnapshot(CameraRequest request) {
//        String rtspUrl;
//
//        switch (request.getCameraId()) {
//            case 1:
//            case 2:
//            case 3:
//                rtspUrl = "rtsp://host.docker.internal:8554/mystream";
//                break;
//            default:
//                rtspUrl = "rtsp://host.docker.internal:8554";
//                break;
//        }
//
//        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl)) {
//            // ⚡ Ключевые параметры для ускорения
//            grabber.setOption("rtsp_transport", "tcp");
//            grabber.setOption("fflags", "nobuffer");
//            grabber.setOption("flags", "low_delay");
//            grabber.setOption("analyzeduration", "500000"); // 0.5 сек
//            grabber.setOption("probesize", "500000");        // 0.5 МБ
//            grabber.setOption("rw_timeout", "300000");       // 0.3 сек
//            grabber.setOption("stimeout", "500000");         // 0.5 сек
//
//            long start = System.currentTimeMillis();
//            grabber.start();
//
//            Frame frame = grabber.grabImage();
//            if (frame != null) {
//                Java2DFrameConverter converter = new Java2DFrameConverter();
//                BufferedImage image = converter.convert(frame);
//
//                File outputDir = new File("C:/Diplom/snapshots");
//                if (!outputDir.exists()) {
//                    outputDir.mkdirs();
//                }
//                File outputFile = new File(outputDir, "latest.jpg");
//                ImageIO.write(image, "jpg", outputFile);
//
//                grabber.stop();
//
//                long duration = System.currentTimeMillis() - start;
//                System.out.println("Снимок сделан за: " + duration + " мс. Путь: " + outputFile.getAbsolutePath());
//
//                return "Снимок успешно сохранён.";
//            } else {
//                return "Не удалось получить изображение.";
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "Ошибка при снимке: " + e.getMessage();
//        }
//    }


//    public StreamingResponseBody streamCamera() {
//        return outputStream -> {
//            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl)) {
//                grabber.setOption("rtsp_transport", "tcp");
//                grabber.start();
//
//                Java2DFrameConverter converter = new Java2DFrameConverter();
//
//                while (!Thread.currentThread().isInterrupted()) {
//                    Frame frame = grabber.grabImage();
//                    if (frame == null) continue;
//
//                    BufferedImage image = converter.convert(frame);
//                    BufferedImage resized = new BufferedImage(480, 270, BufferedImage.TYPE_3BYTE_BGR);
//                    Graphics2D g = resized.createGraphics();
//                    g.drawImage(image, 0, 0, 480, 270, null);
//                    g.dispose();
//
//                    outputStream.write("--frame\r\n".getBytes());
//                    outputStream.write("Content-Type: image/jpeg\r\n\r\n".getBytes());
//                    ImageIO.write(resized, "jpg", outputStream);
//                    outputStream.write("\r\n".getBytes());
//                    outputStream.flush();
//
//                    Thread.sleep(200);
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                log.info("Поток прерван");
//            } catch (Exception e) {
//                log.error("Ошибка стриминга: {}", e.getMessage());
//                throw new RuntimeException(e);
//            }
//        };
//    }

    private SOAPMessage createGetDeviceInformationRequest(String username, String password) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage message = messageFactory.createMessage();

        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        envelope.addNamespaceDeclaration("dev", "http://www.onvif.org/ver10/device/wsdl");

        SOAPHeader header = envelope.getHeader();
        SOAPElement security = header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
        usernameToken.addChildElement("Username", "wsse").addTextNode(username);
        usernameToken.addChildElement("Password", "wsse").addTextNode(password);

        SOAPBody body = envelope.getBody();
        body.addChildElement("GetDeviceInformation", "dev");

        message.saveChanges();
        return message;
    }

    public ResponseEntity<UrlResource> getLatestSnapshotByCamera(CameraRequest cameraRequest) throws MalformedURLException {
        File file = getSnapshotPathAndCreateFile(cameraRequest);
        if (!file.exists() || file.length() == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        UrlResource resource = new UrlResource(file.toURI());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    private static File getSnapshotPathAndCreateFile(CameraRequest cameraRequest) {
        int id = cameraRequest.getCameraId();
        System.out.println(">>> cameraId: " + id);

        String snapshotPath;
        switch (id) {
            case 1:
                snapshotPath = "C:/Diplom/snapshots/camera1/latest.jpg";
                break;
            case 2:
                snapshotPath = "C:/Diplom/snapshots/camera2/latest.jpg";
                break;
            case 3:
                snapshotPath = "C:/Diplom/snapshots/camera3/latest.jpg";
                break;
            default:
                snapshotPath = "C:/Diplom/snapshots/camera1/latest.jpg";
        }

        System.out.println(">>> resolved snapshotPath: " + snapshotPath);
        return new File(snapshotPath);
    }
}