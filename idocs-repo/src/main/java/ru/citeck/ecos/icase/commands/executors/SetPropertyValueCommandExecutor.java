package ru.citeck.ecos.icase.commands.executors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.commands.CommandExecutor;
import ru.citeck.ecos.commands.CommandsService;
import ru.citeck.ecos.icase.commands.dto.SetPropertyValueCommand;
import ru.citeck.ecos.records.RecordsUtils;

import javax.annotation.PostConstruct;
import java.io.Serializable;

@Component
public class SetPropertyValueCommandExecutor implements CommandExecutor<SetPropertyValueCommand> {

    public static final String TYPE = "set-property-value";

    private CommandsService commandsService;
    private NamespaceService namespaceService;
    private NodeService nodeService;

    @Autowired
    public SetPropertyValueCommandExecutor(CommandsService commandsService,
                                           NamespaceService namespaceService,
                                           NodeService nodeService) {
        this.commandsService = commandsService;
        this.namespaceService = namespaceService;
        this.nodeService = nodeService;
    }

    @PostConstruct
    public void init() {
        commandsService.addExecutor(this);
    }

    @Nullable
    @Override
    public Object execute(SetPropertyValueCommand command) {
        NodeRef caseRef = RecordsUtils.toNodeRef(command.getCaseRef());
        QName propertyQName = QName.resolveToQName(namespaceService, command.getPropertyQName());
        Serializable propertyValue = command.getPropertyValue();
        nodeService.setProperty(caseRef, propertyQName, propertyValue);
        return null;
    }
}
