package edu.swufe.nxksecdisk.server.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.swufe.nxksecdisk.server.util.ConfigReader;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.MultipartConfigElement;
import java.io.File;

/**
 * <h2>Web功能-MVC相关配置类</h2>
 * <p>
 * 该Spring配置类主要负责配置kiftd网页服务器的基本处理行为，并在IOC容器中生成所需的工具实例。
 * </p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
@Configurable
@ComponentScan({"edu.swufe.nxksecdisk.server.controller", "edu.swufe.nxksecdisk.server.service.impl", "edu.swufe" +
        ".nxksecdisk.server.util"})
@ServletComponentScan({"edu.swufe.nxksecdisk.server.listener", "edu.swufe.nxksecdisk.server.filter"})
@Import({DataAccess.class})
public class Mvc extends ResourceHttpRequestHandler implements WebMvcConfigurer {

    private final ConfigReader config = ConfigReader.getInstance();

    /**
     * 启用DefaultServlet用以处理可直接请求的静态资源;
     *
     * @param configurer
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /**
     * 设置Web静态资源映射路径;
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        // 将静态页面资源所在文件夹加入至资源路径中
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        String.format("file:%s%swebContext%s",
                                config.getPath(),
                                File.separator,
                                File.separator));
    }

    /**
     * 生成上传管理器，用于接收/缓存上传文件;
     *
     * @return
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        final MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(-1L);
        factory.setLocation(config.getTemporaryfilePath());
        return factory.createMultipartConfig();
    }

    /**
     * 生成Gson实例，用于服务Json序列化和反序列化;
     *
     * @return
     */
    @Bean
    public Gson gson() {
        return new GsonBuilder().create();
    }
}
