package edu.swufe.nxksecdisk.server.config;

import edu.swufe.nxksecdisk.server.util.ConfigReader;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;

/**
 * <h2>服务器部分数据接入设置</h2>
 * <p>
 * 该配置类定义了服务器组件使用的MyBatis将如何链接数据库。如需更换其他数据库，请在此配置自己的数据源并替换原有数据源。
 * </p>
 *
 * @author 青阳龙野(kohgylw)
 * @version 1.0
 */
@Configurable
public class DataAccess
{
    private static Resource[] mapperFiles;

    private static Resource mybatisConfig;

    private static final ConfigReader config = ConfigReader.getInstance();

    @Bean
    public DataSource dataSource()
    {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(config.getFileNodePathDriver());
        dataSource.setUrl(config.getFileNodePathURL());
        dataSource.setUsername(config.getFileNodePathUserName());
        dataSource.setPassword(config.getFileNodePathPassWord());
        return dataSource;
    }

    @Bean(name = {"sqlSessionFactory"})
    @Autowired
    public SqlSessionFactoryBean sqlSessionFactoryBean(final DataSource ds)
    {
        final SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(ds);
        sqlSessionFactoryBean.setConfigLocation(DataAccess.mybatisConfig);
        sqlSessionFactoryBean.setMapperLocations(DataAccess.mapperFiles);
        return sqlSessionFactoryBean;
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer()
    {
        final MapperScannerConfigurer msf = new MapperScannerConfigurer();
        msf.setBasePackage("edu.swufe.nxksecdisk.server.mapper");
        msf.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return msf;
    }

    static
    {
        final String mybatisResourceFolder = String.format("%s%smybatisResource%s",
                config.requirePath(),
                File.separator, File.separator);
        final String mapperFilesFolder = mybatisResourceFolder + "mapperXML" + File.separator;
        DataAccess.mapperFiles = new Resource[]{new FileSystemResource(mapperFilesFolder + "NodeMapper.xml"),
                new FileSystemResource(mapperFilesFolder + "FolderMapper.xml"),
                new FileSystemResource(mapperFilesFolder + "PropertiesMapper.xml")};
        DataAccess.mybatisConfig = (Resource) new FileSystemResource(mybatisResourceFolder + "mybatis.xml");
    }
}
