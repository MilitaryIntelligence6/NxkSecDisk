package edu.swufe.nxksecdisk.server.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;

/**
 * <h2>Txt转PDF工具</h2>
 * <p>
 * 该工具是实现txt在线预览功能的核心，能将txt格式的文档以流形式转化为PDF。文档内容将使用开源免费字体“文泉驿正黑”显示。该工具应由Spring
 * IOC容器管理。
 * </p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
@Component
public class TxtToPdfUtil {

    @Resource
    private TxtCharsetGetter tcg;

    /**
     * <h2>执行word格式转换（docx）</h2>
     * <p>
     * 将输入流中的word文件转换为PDF格式并输出至指定输出流，该方法线程阻塞。
     * </p>
     *
     * @param in  java.io.InputStream 输入流，输入docx格式
     * @param out java.io.OutputStream 输出流，输出PDF格式
     * @throws Exception 无法完成转码
     * @author 青阳龙野(kohgylw)
     */
    public void convertPdf(File in, OutputStream out) throws Exception {
        // 以A4页面显示文本;
        Rectangle rect = new Rectangle(PageSize.A4);
        Document doc = new Document(rect);
        // 开始转换
        PdfWriter pw = PdfWriter.getInstance(doc, out);
        doc.open();
        BaseFont songFont = BaseFont.createFont(
                ConfigureReader.getInstance().getPath() + File.separator + "fonts/wqy-zenhei.ttc,0",
                BaseFont.IDENTITY_H,
                BaseFont.NOT_EMBEDDED);
        // 设置字体格式;
        Font font = new Font(songFont, 12, Font.NORMAL);
        Paragraph paragraph = new Paragraph();
        paragraph.setFont(font);
        // 判断编码格式;
        String charset = tcg.getTxtCharset(new FileInputStream(in));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(in), charset));
        String line = null;
        while ((line = reader.readLine()) != null) {
            // 将文本逐行写入PDF段落;
            paragraph.add(line + "\n");
        }
        reader.close();
        // 避免因打开空文档可能造成的打开失败
        if (paragraph.isEmpty()) {
            paragraph.add("");
        }
        // 写入段落至文档;
        doc.add(paragraph);
        // 关闭文档;
        doc.close();
        pw.flush();
        pw.close();
    }

}
