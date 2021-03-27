package edu.swufe.nxksecdisk.server.service;

import javax.servlet.http.*;

public interface PlayAudioService
{
    String getAudioInfoListByJson(final HttpServletRequest request);
}
