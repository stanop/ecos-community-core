package org.activiti;

import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.GenericManagerFactory;

public class CustomHistoryManagerFactory extends GenericManagerFactory {


    @Override
    public Class<?> getSessionType() {
        return HistoryManager.class;
    }

    public CustomHistoryManagerFactory() {
        super(CustomHistoryManager.class);
    }

    @Override
    public Session openSession() {
        Session session = super.openSession();

        return session;
    }

}
