package ru.citeck.ecos.workflow.mirror;

/*package*/ class MirrorTaskWork implements Runnable {
    
    private WorkflowMirrorService service;
    private String taskId;
    
    public MirrorTaskWork(WorkflowMirrorService service, String taskId) {
        this.service = service;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        service.mirrorTask(taskId);
    }
}
