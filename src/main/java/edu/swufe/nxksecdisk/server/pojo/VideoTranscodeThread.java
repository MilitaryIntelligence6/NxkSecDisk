package edu.swufe.nxksecdisk.server.pojo;

import edu.swufe.nxksecdisk.system.AppSystem;
import edu.swufe.nxksecdisk.server.util.ConfigureReader;
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
public class VideoTranscodeThread
{
    private String md5;

    private String progress;

    private Encoder encoder;

    private String outputFileName;

    public VideoTranscodeThread(File f, EncodingAttributes ea, FFMPEGLocator fl) throws Exception
    {
        // 首先计算MD5值
        md5 = DigestUtils.md5Hex(new FileInputStream(f));
        progress = "0.0";
        MultimediaObject mo = new MultimediaObject(f, fl);
        encoder = new Encoder(fl);
        Thread t = new Thread(() ->
        {
            try
            {
                outputFileName = String.format("video_%s.mp4", UUID.randomUUID().toString());
                encoder.encode(mo, new File(ConfigureReader.getInstance().getTemporaryfilePath(), outputFileName),
                        ea, new EncoderProgressListener()
                        {
                            @Override
                            public void sourceInfo(MultimediaInfo arg0)
                            {
                            }

                            @Override
                            public void progress(int arg0)
                            {
                                progress = String.format("%s", arg0 / 10.00);
                            }

                            @Override
                            public void message(String arg0)
                            {
                            }
                        });
                progress = "FIN";
            }
            catch (Exception e)
            {
                AppSystem.out.printf("警告：在线转码功能出现意外错误。详细信息：%s", e.getMessage());
            }
        });
        t.start();
    }

    public String getMd5()
    {
        return md5;
    }

    public String getProgress()
    {
        return progress;
    }

    public String getOutputFileName()
    {
        return outputFileName;
    }

    /**
     * <h2>终止当前转码过程</h2>
     * <p>执行该方法将中断正在进行的转码，并删除原有的输出文件。</p>
     *
     * @author 青阳龙野(kohgylw)
     */
    public void abort()
    {
        if (encoder != null)
        {
            encoder.abortEncoding();
        }
        File f = new File(ConfigureReader.getInstance().getTemporaryfilePath(), outputFileName);
        if (f.exists())
        {
            f.delete();
        }
    }

}
