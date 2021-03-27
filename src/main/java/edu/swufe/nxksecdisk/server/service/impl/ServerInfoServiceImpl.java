package edu.swufe.nxksecdisk.server.service.impl;

import edu.swufe.nxksecdisk.server.service.ServerInfoService;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Administrator
 */
@Service
public class ServerInfoServiceImpl implements ServerInfoService
{
    @Override
    public String getOSName()
    {
        return System.getProperty("os.name");
    }

    @Override
    public String getServerTime()
    {
        final Date d = new Date();
        final DateFormat df = new SimpleDateFormat("YYYY\u5e74MM\u6708dd\u65e5 hh:mm");
        return df.format(d);
    }
}
