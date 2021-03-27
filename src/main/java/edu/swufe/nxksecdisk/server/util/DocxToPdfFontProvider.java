package edu.swufe.nxksecdisk.server.util;

import com.lowagie.text.FontFactory;
import fr.opensagres.xdocreport.itext.extension.font.AbstractFontRegistry;

/**
 * <h2>Docx转PDF字体格式封装类</h2>
 * <p>该类用于提供docx转PDF时所需的字体格式封装对象，该类被设计为单例模式，请使用getInstance()方法获取唯一实例。</p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
public class DocxToPdfFontProvider extends AbstractFontRegistry
{
    private volatile static DocxToPdfFontProvider instance;

    private DocxToPdfFontProvider()
    {
        FontFactory.setFontImp(new DocxToPdfFontFactory());
    }

    @Override
    protected String resolveFamilyName(String arg0, int arg1)
    {
        return arg0;
    }

    /**
     * <h2>取得字体封装类的唯一实例</h2>
     * <p>请调用该方法获取docx转PDF功能所需的字体封装对象。</p>
     *
     * @return fr.opensagres.xdocreport.itext.extension.font.AbstractFontRegistry 字体封装对象
     * @author 青阳龙野(kohgylw)
     */
    public static DocxToPdfFontProvider getInstance()
    {
        if (instance == null)
        {
			synchronized (DocxToPdfFontProvider.class)
			{
				if (instance == null)
				{
					instance = new DocxToPdfFontProvider();
				}
			}
        }
        return instance;
    }

}
