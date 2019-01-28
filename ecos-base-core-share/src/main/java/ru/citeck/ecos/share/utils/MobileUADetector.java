package ru.citeck.ecos.share.utils;

import au.com.flyingkite.mobiledetect.UAgentInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;

import java.util.concurrent.ConcurrentHashMap;

public class MobileUADetector {

    private static final Log logger = LogFactory.getLog(MobileUADetector.class);

    private final static String HEADER_USER_AGENT = "user-agent";

    private static ConcurrentHashMap<String, UAgentInfo> agentCache = new ConcurrentHashMap<>();

    private MobileUADetector() {}

    public static boolean isMobile() {
        RequestContext context = ThreadLocalRequestContext.getRequestContext();
        if (context == null) {
            return false;
        }
        return isMobile(context.getHeader(HEADER_USER_AGENT));
    }

    public static boolean isMobile(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return false;
        }
        try {
            return agentCache.computeIfAbsent(userAgent, MobileUADetector::getInfoImpl).detectMobileQuick();
        } catch (Exception e) {
            logger.error("User agent: " + userAgent, e);
            return false;
        } finally {
            if (agentCache.size() > 200) {
                agentCache.clear();
            }
        }
    }

    private static UAgentInfo getInfoImpl(String userAgent) {
        return new UAgentInfo(userAgent, null);
    }
}
