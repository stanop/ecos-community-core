package ru.citeck.ecos.action.group.impl;

import ru.citeck.ecos.action.group.ActionResult;

import java.util.List;

public interface ResultsListener<T> {

    void onProcessed(List<ActionResult<T>> results);

}
