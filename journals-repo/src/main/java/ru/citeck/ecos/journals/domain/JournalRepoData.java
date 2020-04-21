package ru.citeck.ecos.journals.domain;

import lombok.Data;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.List;

@Data
public class JournalRepoData {

    private NodeRef nodeRef;
    private List<NodeRef> criteria;
}
