package diplom.openvifApiCamera.service;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

@Service
public class SnapshotCaptureService {

    private final File outputFile1 = new File("C:/Diplom/snapshots/camera1/latest.jpg");
    private final File outputFile2 = new File("C:/Diplom/snapshots/camera2/latest.jpg");
    private final File outputFile3 = new File("C:/Diplom/snapshots/camera3/latest.jpg");

    @PostConstruct
    public void init() {
        String rtspUrl1 = "rtsp://host.docker.internal:8554/mystream1";
        startStream(rtspUrl1, outputFile1);
        String rtspUrl2 = "rtsp://host.docker.internal:8554/mystream2";
        startStream(rtspUrl2, outputFile2);
        String rtspUrl3 = "rtsp://host.docker.internal:8554/mystream3";
        startStream(rtspUrl3, outputFile3);
    }

    private void startStream(String rtspUrl, File outputFile) {
        new Thread(() -> {
            while (true) {
                Java2DFrameConverter localConverter = new Java2DFrameConverter();
                int nullFrameCount = 0;
                final int NULL_FRAME_LIMIT = 20;

                try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(rtspUrl)) {
                    System.out.println("⏳ Стартуем поток: " + rtspUrl);

                    grabber.setOption("rtsp_transport", "tcp");
                    grabber.setOption("fflags", "nobuffer");
                    grabber.setOption("flags", "low_delay");
                    grabber.setOption("analyzeduration", "2000000");
                    grabber.setOption("probesize", "2000000");
                    grabber.setOption("rw_timeout", "300000");
                    grabber.setOption("stimeout", "500000");

                    grabber.start();
                    System.out.println("Поток запущен: " + rtspUrl);

                    while (true) {
                        Frame frame = grabber.grabImage();

                        if (frame == null) {
                            nullFrameCount++;
                            System.out.println("frame == null (" + nullFrameCount + ") — " + rtspUrl);

                            if (nullFrameCount >= NULL_FRAME_LIMIT) {
                                System.out.println("Слишком много пустых кадров. Перезапускаем поток: " + rtspUrl);
                                break; // выйти из внутреннего цикла — произойдёт перезапуск
                            }

                            Thread.sleep(100);
                            continue;
                        }

                        nullFrameCount = 0; // сброс если кадр норм

                        BufferedImage image = localConverter.convert(frame);
                        if (image != null && image.getWidth() > 16 && image.getHeight() > 16) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            if (ImageIO.write(image, "jpg", baos) && baos.size() > 0) {
                                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                    fos.write(baos.toByteArray());
                                }
                            }
                        }

                        Thread.sleep(80); // 10–12 FPS
                    }

                } catch (Exception e) {
                    System.err.println("Ошибка потока: " + rtspUrl);
                    e.printStackTrace();
                }

                // Пауза перед перезапуском
                try {
                    System.out.println("Повторный запуск через 1 сек: " + rtspUrl);
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }).start();
    }
}
