package ru.citeck.ecos.cardlet;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.cardlet.xml.Cardlet;

import java.util.List;

public class CardletsWithModes {

    private List<Cardlet> cardlets;
    private List<NodeRef> modes;

    public CardletsWithModes(List<Cardlet> cardlets, List<NodeRef> modes) {
        this.cardlets = cardlets;
        this.modes = modes;
    }

    public List<Cardlet> getCardlets() {
        return cardlets;
    }

    public void setCardlets(List<Cardlet> cardlets) {
        this.cardlets = cardlets;
    }

    public List<NodeRef> getModes() {
        return modes;
    }

    public void setModes(List<NodeRef> modes) {
        this.modes = modes;
    }
}
