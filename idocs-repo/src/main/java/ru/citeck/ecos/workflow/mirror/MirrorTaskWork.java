package ru.citeck.ecos.workflow.mirror;

import org.alfresco.repo.security.authentication.AuthenticationUtil;

/*package*/ class MirrorTaskWork implements Runnable {
    
    private WorkflowMirrorService service;
    private String taskId;
    
    public MirrorTaskWork(WorkflowMirrorService service, String taskId) {
        this.service = service;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        AuthenticationUtil.runAsSystem(() -> {
            service.mirrorTask(taskId);
            return null;
        });
    }
}
