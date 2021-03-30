package edu.swufe.nxksecdisk.server.service.impl;

import com.google.gson.Gson;
import edu.swufe.nxksecdisk.server.enumeration.VcLevel;
import edu.swufe.nxksecdisk.server.pojo.ChangePasswordInfoPojo;
import edu.swufe.nxksecdisk.server.pojo.LoginInfoPojo;
import edu.swufe.nxksecdisk.server.pojo.PublicKeyInfo;
import edu.swufe.nxksecdisk.server.pojo.SignUpInfoPojo;
import edu.swufe.nxksecdisk.server.service.AccountService;
import edu.swufe.nxksecdisk.server.util.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Administrator
 */
@Service
public class AccountServiceImpl implements AccountService {

    /**
     * 登录密钥有效期;
     */
    private static final long TIME_OUT = 30000L;

    /**
     * 关注账户，当任意一个账户登录失败后将加入至该集合中，登录成功则移除。登录集合中的账户必须进行验证码验证;
     */
    private static final Set<String> focusAccount = new HashSet<>();

    @Resource
    private RsaKeyUtil rsaKeyUtil;

    @Resource
    private LogUtil logUtil;

    @Resource
    private Gson gson;

    private VerificationCodeFactory verificationCodeFactory;

    private CharsetEncoder iso88591Encoder;

    private final ConfigReader config = ConfigReader.getInstance();

    {
        iso88591Encoder = Charset.forName("ISO-8859-1").newEncoder();
        if (!config.requireVcLevel().equals(VcLevel.CLOSE)) {
            int line = 0;
            int oval = 0;
            switch (config.requireVcLevel()) {
                case STANDARD: {
                    line = 6;
                    oval = 2;
                    break;
                }
                case SIMPLIFIED: {
                    line = 1;
                    oval = 0;
                    break;
                }
                default: {
                    break;
                }
            }
            // 验证码生成工厂，包含了一些不太容易误认的字符
            verificationCodeFactory = new VerificationCodeFactory(45, line, oval, 'a', 'b', 'c', 'd', 'e', 'f', 'g',
                    'h', 'j', 'k', 'm',
                    'n', 'p', 'q', 'r', 's', 't', 'w', 'x', 'y', 'z', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
                    'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z');
        }
    }

    @Override
    public String checkLoginRequest(final HttpServletRequest request, final HttpSession session) {
        final String encrypted = request.getParameter("encrypted");
        try {
            final String loginInfoStr = RsaDecryptUtil.dncryption(encrypted, rsaKeyUtil.getPrivateKey());
            final LoginInfoPojo info = gson.fromJson(loginInfoStr.replaceAll("\\\\", "\\\\\\\\"), LoginInfoPojo.class);
            if (System.currentTimeMillis() - Long.parseLong(info.getTime()) > TIME_OUT) {
                return "error";
            }
            final String accountId = info.getAccountId();
            if (!config.foundAccount(accountId)) {
                return "accountnotfound";
            }
            // 如果验证码开启且该账户已被关注，则要求提供验证码
            if (!config.requireVcLevel().equals(VcLevel.CLOSE)) {
                synchronized (focusAccount) {
                    if (focusAccount.contains(accountId)) {
                        String reqVerCode = request.getParameter("vercode");
                        String trueVerCode = (String) session.getAttribute("VERCODE");
                        // 确保一个验证码只会生效一次，无论对错
                        session.removeAttribute("VERCODE");
                        if (reqVerCode == null || trueVerCode == null
                                || !trueVerCode.equals(reqVerCode.toLowerCase())) {
                            return "needsubmitvercode";
                        }
                    }
                }
            }
            if (config.checkAccountPwd(accountId, info.getAccountPwd())) {
                session.setAttribute("ACCOUNT", (Object) accountId);
                // 如果该账户输入正确且是一个被关注的账户，则解除该账户的关注，释放空间
                if (!config.requireVcLevel().equals(VcLevel.CLOSE)) {
                    synchronized (focusAccount) {
                        focusAccount.remove(accountId);
                    }
                }
                return "permitlogin";
            }
            // 如果账户密码不匹配，则将该账户加入到关注账户集合，避免对方进一步破解
            synchronized (focusAccount) {
                if (!config.requireVcLevel().equals(VcLevel.CLOSE)) {
                    focusAccount.add(accountId);
                }
            }
            return "accountpwderror";
        }
        catch (Exception e) {
            return "error";
        }
    }

    @Override
    public void logout(final HttpSession session) {
        session.invalidate();
    }

    @Override
    public String getPublicKey() {
        PublicKeyInfo pki = new PublicKeyInfo();
        pki.setPublicKey(rsaKeyUtil.getPublicKey());
        pki.setTime(System.currentTimeMillis());
        return gson.toJson(pki);
    }

    @Override
    public void getNewLoginVerCode(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        try {
            if (config.requireVcLevel().equals(VcLevel.CLOSE)) {
                response.sendError(404);
            }
            else {
                VerificationCode vc = verificationCodeFactory.next(4);
                session.setAttribute("VERCODE", vc.getCode());
                response.setContentType("image/png");
                OutputStream out = response.getOutputStream();
                vc.saveTo(out);
                out.flush();
                out.close();
            }
        }
        catch (IOException e) {
            try {
                response.sendError(500);
            }
            catch (IOException e1) {

            }
        }
    }

    @Override
    public String doPong(HttpServletRequest request) {
        if (request.getSession().getAttribute("ACCOUNT") != null) {
            return "pong";// 只有登录了的账户才有必要进行应答
        }
        else {
            return "";// 未登录则不返回标准提示，但也做应答（此时，前端应停止后续应答以节省线程开支）
        }
    }

    @Override
    public String changePassword(HttpServletRequest request) {
        // 验证是否开启了用户修改密码功能
        if (!config.isAllowChangePassword()) {
            return "illegal";
        }
        // 必须登录了一个账户
        HttpSession session = request.getSession();
        final String account = (String) session.getAttribute("ACCOUNT");
        if (account == null) {
            return "mustlogin";
        }
        // 解析修改密码请求
        final String encrypted = request.getParameter("encrypted");
        try {
            final String changePasswordInfoStr = RsaDecryptUtil.dncryption(encrypted, rsaKeyUtil.getPrivateKey());
            final ChangePasswordInfoPojo info = gson.fromJson(changePasswordInfoStr.replaceAll("\\\\", "\\\\\\\\"),
                    ChangePasswordInfoPojo.class);
            if (System.currentTimeMillis() - Long.parseLong(info.getTime()) > TIME_OUT) {
                return "error";
            }
            // 如果验证码开启且该账户已被关注，则要求提供验证码
            if (!config.requireVcLevel().equals(VcLevel.CLOSE)) {
                synchronized (focusAccount) {
                    if (focusAccount.contains(account)) {
                        String reqVerCode = request.getParameter("vercode");
                        String trueVerCode = (String) session.getAttribute("VERCODE");
                        session.removeAttribute("VERCODE");// 确保一个验证码只会生效一次，无论对错
                        if (reqVerCode == null || trueVerCode == null
                                || !trueVerCode.equals(reqVerCode.toLowerCase())) {
                            return "needsubmitvercode";
                        }
                    }
                }
            }
            if (config.checkAccountPwd(account, info.getOldPwd())) {
                // 如果该账户输入正确且是一个被关注的账户，则解除该账户的关注，释放空间
                if (!config.requireVcLevel().equals(VcLevel.CLOSE)) {
                    synchronized (focusAccount) {
                        focusAccount.remove(account);
                    }
                }
                String newPassword = info.getNewPwd();
                // 新密码合法性检查
                if (newPassword != null && newPassword.length() >= 3 && newPassword.length() <= 32
                        && iso88591Encoder.canEncode(newPassword)) {
                    if (config.changePassword(account, newPassword)) {
                        logUtil.writeChangePasswordEvent(request, account, newPassword);
                        return "success";
                    }
                }
                return "invalidnewpwd";
            }
            else {
                // 如果账户密码不匹配，则将该账户加入到关注账户集合，避免对方进一步破解
                synchronized (focusAccount) {
                    if (!config.requireVcLevel().equals(VcLevel.CLOSE)) {
                        focusAccount.add(account);
                    }
                }
                return "oldpwderror";
            }
        }
        catch (Exception e) {
            logUtil.writeException(e);
            return "cannotchangepwd";
        }
    }

    @Override
    public String isAllowSignUp() {
        return config.isAllowSignUp() ? "true" : "false";
    }

    @Override
    public String doSignUp(HttpServletRequest request) {
        // 验证是否开启了注册功能
        if (!config.isAllowSignUp()) {
            return "illegal";
        }
        HttpSession session = request.getSession();
        // 如果已经登入一个账户了，必须先注销
        if (session.getAttribute("ACCOUNT") != null) {
            return "mustlogout";
        }
        // 如果开启了验证码则必须输入
        String reqVerCode = request.getParameter("vercode");
        if (!config.requireVcLevel().equals(VcLevel.CLOSE)) {
            String trueVerCode = (String) session.getAttribute("VERCODE");
            session.removeAttribute("VERCODE");// 确保一个验证码只会生效一次，无论对错
            if (reqVerCode == null || trueVerCode == null || !trueVerCode.equals(reqVerCode.toLowerCase())) {
                return "needvercode";
            }
        }
        // 解析注册请求
        final String encrypted = request.getParameter("encrypted");
        try {
            final String signUpInfoStr = RsaDecryptUtil.dncryption(encrypted, rsaKeyUtil.getPrivateKey());
            final SignUpInfoPojo info = gson.fromJson(signUpInfoStr.replaceAll("\\\\", "\\\\\\\\"),
                    SignUpInfoPojo.class);
            if (System.currentTimeMillis() - Long.parseLong(info.getTime()) > TIME_OUT) {
                return "error";
            }
            if (config.foundAccount(info.getAccount())) {
                return "accountexists";
            }
            String account = info.getAccount();
            String password = info.getPwd();
            // 新账户和密码的合法性检查
            if (account != null && account.length() >= 3 && account.length() <= 32
                    && iso88591Encoder.canEncode(account)) {
                if (account.indexOf("=") < 0 && account.indexOf(":") < 0 && account.indexOf("#") != 0) {
                    if (password != null && password.length() >= 3 && password.length() <= 32
                            && iso88591Encoder.canEncode(password)) {
                        if (config.createNewAccount(account, password)) {
                            logUtil.writeSignUpEvent(request, account, password);
                            session.setAttribute("ACCOUNT", account);
                            return "success";
                        }
                        else {
                            return "cannotsignup";
                        }
                    }
                    else {
                        return "invalidpwd";
                    }
                }
                else {
                    return "illegalaccount";
                }
            }
            else {
                return "invalidaccount";
            }
        }
        catch (Exception e) {
            logUtil.writeException(e);
            return "cannotsignup";
        }
    }
}
