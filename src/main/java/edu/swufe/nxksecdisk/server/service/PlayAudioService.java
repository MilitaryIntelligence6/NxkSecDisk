package edu.swufe.nxksecdisk.server.service;

import javax.servlet.http.HttpServletRequest;

public interface PlayAudioService
{
    String getAudioInfoListByJson(final HttpServletRequest request);
}
