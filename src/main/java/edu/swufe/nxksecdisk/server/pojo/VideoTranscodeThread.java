package edu.swufe.nxksecdisk.server.pojo;

import edu.swufe.nxksecdisk.server.util.ConfigReader;
import edu.swufe.nxksecdisk.system.AppSystem;
import org.apache.commons.codec.digest.DigestUtils;
import ws.schild.jave.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

/**
 * <h2>视频转码信息</h2>
 * <p>
 * 其中存放了视频的转码信息。
 * </p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public class VideoTranscodeThread {

    private String md5;

    private String progress;

    private Encoder encoder;

    private String outputFileName;

    private final ConfigReader config = ConfigReader.getInstance();

    public VideoTranscodeThread(File file,
                                EncodingAttributes encodingAttributes,
                                FFMPEGLocator ffmpegLocator)
            throws Exception {
        // 首先计算MD5值
        md5 = DigestUtils.md5Hex(new FileInputStream(file));
        progress = "0.0";
        MultimediaObject multimediaObject = new MultimediaObject(file, ffmpegLocator);
        encoder = new Encoder(ffmpegLocator);
        AppSystem.pool.execute(() -> run(multimediaObject, encodingAttributes));
    }

    public String getMd5() {
        return md5;
    }

    public String getProgress() {
        return progress;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    /**
     * <h2>终止当前转码过程</h2>
     * <p>执行该方法将中断正在进行的转码，并删除原有的输出文件。</p>
     *
     * @author 青阳龙野(kohgylw)
     */
    public void abort() {
        if (encoder != null) {
            encoder.abortEncoding();
        }
        File f = new File(config.requireTmpFilePath(), outputFileName);
        if (f.exists()) {
            f.delete();
        }
    }

    private void run(MultimediaObject multimediaObject,
                     EncodingAttributes encodingAttributes) {
        try {
            outputFileName = String.format("video_%s.mp4", UUID.randomUUID().toString());
            encoder.encode(multimediaObject, new File(config.requireTmpFilePath(), outputFileName),
                    encodingAttributes, new EncoderProgressListener() {
                        @Override
                        public void sourceInfo(MultimediaInfo arg0) {
                        }

                        @Override
                        public void progress(int arg0) {
                            progress = String.format("%s", arg0 / 10.00);
                        }

                        @Override
                        public void message(String arg0) {
                        }
                    });
            progress = "FIN";
        }
        catch (Exception e) {
            AppSystem.out.printf("警告：在线转码功能出现意外错误。详细信息：%s", e.getMessage());
        }
    }
}
