package edu.swufe.nxksecdisk.server.controller;

import edu.swufe.nxksecdisk.server.util.FileBlockUtil;
import edu.swufe.nxksecdisk.server.util.LogUtil;
import edu.swufe.nxksecdisk.system.AppSystem;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.Resource;

/**
 * @author Administrator
 */
@ControllerAdvice
public class ErrorController {

    @Resource
    private FileBlockUtil fileBlockUtil;

    @Resource
    private LogUtil logUtil;

    @ExceptionHandler({Exception.class})
    public void handleException(final Exception e) {
        this.logUtil.writeException(e);
        this.fileBlockUtil.checkFileBlocks();
        e.printStackTrace();
        AppSystem.out.printf("处理请求时发生错误：\n\r------信息------\n\r%s\n\r------信息------",
                e.getMessage());
    }
}
