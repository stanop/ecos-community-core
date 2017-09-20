package ru.citeck.ecos.action;

import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Support saving actions and conditions in repository and further retrieving them back.
 * 
 * The schema differs from used by ActionService in such a way, 
 * that it should be convenient to create and show actions and conditions in UI.
 * 
 * @author Sergey Tiunov
 *
 */
public interface ActionDAO {

    NodeRef save(Action action, NodeRef parent, QName childAssocType);
    
    void save(Action action, NodeRef nodeRef);
    
    Action readAction(NodeRef nodeRef);
    
    List<Action> readActions(NodeRef parent, QName childAssocType);
    
}
