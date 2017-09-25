package ru.citeck.ecos.icase.levels.api;

/**
 * @author Alexander Nemerov
 * created on 17.03.2015.
 */
public interface CompleteLevelsCheckerGeneric<Node, Nodes> {

    Nodes checkCompletedLevels(Node caseNode);

    Nodes checkUncompletedLevels(Node caseNode);

    boolean isCompleteLevel(Node caseNode, Node level);

}
