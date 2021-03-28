package edu.swufe.nxksecdisk.server.mapper;

import edu.swufe.nxksecdisk.server.model.Property;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Administrator
 */
@Mapper
public interface PropertiesMapper {

    int insert(final Property p);

    int deleteByKey(final String propertyKey);

    Property selectByKey(final String propertyKey);

    int update(final Property p);
}
