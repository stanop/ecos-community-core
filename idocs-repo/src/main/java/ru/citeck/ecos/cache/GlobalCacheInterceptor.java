package ru.citeck.ecos.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GlobalCacheInterceptor implements MethodInterceptor {

    private LoadingCache<Invocation, CacheEntry> cache;

    private long cacheAge = 600;
    private int cacheMaxSize = 100;

    private NodeService nodeService;

    @PostConstruct
    public void init() {
        cache = CacheBuilder.newBuilder()
                            .expireAfterWrite(cacheAge, TimeUnit.SECONDS)
                            .maximumSize(cacheMaxSize)
                            .build(CacheLoader.from(Invocation::proceed));
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {

        Invocation inv = new Invocation(methodInvocation);
        CacheEntry entry = cache.getUnchecked(inv);

        if (!entry.isValid()) {
            cache.invalidate(inv);
            entry = cache.getUnchecked(inv);
        }

        return entry.getValue();
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
    }

    public void clearCache() {
        cache.invalidateAll();
    }

    public void setCacheAge(long cacheAge) {
        this.cacheAge = cacheAge;
    }

    public void setCacheMaxSize(int cacheMaxSize) {
        this.cacheMaxSize = cacheMaxSize;
    }

    private class Invocation {

        private final MethodInvocation methodInvocation;

        public Invocation(MethodInvocation invocation) {
            methodInvocation = invocation;
        }

        public CacheEntry proceed() {
            Object result = AuthenticationUtil.runAsSystem(() -> {
                try {
                    return methodInvocation.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            return new CacheEntry(methodInvocation.getArguments(), result);
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Invocation that = (Invocation) o;

            return Objects.equals(methodInvocation.getMethod(), that.methodInvocation.getMethod())
                && Arrays.equals(methodInvocation.getArguments(), that.methodInvocation.getArguments());
        }

        @Override
        public int hashCode() {
            return 31 * Objects.hashCode(methodInvocation.getMethod()) +
                        Arrays.hashCode(methodInvocation.getArguments());
        }
    }

    private class CacheEntry {

        private final List<NodeKey> nodeRefParams;
        private final Object value;

        CacheEntry(Object[] args, Object value) {
            nodeRefParams = new ArrayList<>(args.length);
            for (Object arg : args) {
                if (arg instanceof NodeRef) {
                    NodeRef ref = (NodeRef) arg;
                    Date lastModified = (Date) nodeService.getProperty(ref, ContentModel.PROP_MODIFIED);
                    nodeRefParams.add(new NodeKey(ref, lastModified));
                }
            }
            this.value = value;
        }

        public boolean isValid() {
            return value != null && nodeRefParams.stream().allMatch(param -> {
                Date modified = (Date) nodeService.getProperty(param.nodeRef, ContentModel.PROP_MODIFIED);
                return Objects.equals(modified, param.lastModified);
            });
        }

        public Object getValue() {
            return value;
        }
    }

    private static class NodeKey {

        public final NodeRef nodeRef;
        public final Date lastModified;

        public NodeKey(NodeRef nodeRef, Date lastModified) {
            this.nodeRef = nodeRef;
            this.lastModified = lastModified;
        }
    }
}
