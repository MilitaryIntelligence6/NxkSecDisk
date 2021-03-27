package edu.swufe.nxksecdisk.server.util;

import edu.swufe.nxksecdisk.printer.Out;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * @author Administrator
 */
public class RsaDecryptUtil
{
    private static Base64.Decoder decoder;

    private static KeyFactory keyFactory;

    private static Cipher cipher;

    static
    {
        RsaDecryptUtil.decoder = Base64.getDecoder();
        try
        {
            RsaDecryptUtil.keyFactory = KeyFactory.getInstance("RSA");
            RsaDecryptUtil.cipher = Cipher.getInstance("RSA");
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
    }

    public static String dncryption(final String context, final String privateKey)
    {
        final byte[] b = RsaDecryptUtil.decoder.decode(privateKey);
        final byte[] s = RsaDecryptUtil.decoder.decode(context.getBytes(StandardCharsets.UTF_8));
        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b);
        try
        {
            final PrivateKey key = RsaDecryptUtil.keyFactory.generatePrivate(spec);
            RsaDecryptUtil.cipher.init(2, key);
            final byte[] f = RsaDecryptUtil.cipher.doFinal(s);
            return new String(f);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Out.println(e.getMessage());
            Out.println("错误：RSA解密失败。");
        }
        return null;
    }
}
