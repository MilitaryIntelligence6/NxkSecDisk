package edu.swufe.nxksecdisk.server.controller;

import edu.swufe.nxksecdisk.server.util.LogUtil;
import edu.swufe.nxksecdisk.printer.Out;
import edu.swufe.nxksecdisk.server.util.FileBlockUtil;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.Resource;

@ControllerAdvice
public class ErrorController
{
    @Resource
    private FileBlockUtil fbu;
    @Resource
    private LogUtil lu;

    @ExceptionHandler({Exception.class})
    public void handleException(final Exception e)
    {
        this.lu.writeException(e);
        this.fbu.checkFileBlocks();
        Out.println(String.format("处理请求时发生错误：\n\r------信息------\n\r%s\n\r------信息------",
						e.getMessage()));
    }
}
