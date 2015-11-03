/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.lifecycle;

/**
 * LifeCycle Service is a service for managing of document life cycle.
 * Document is always in one and only one state. State changes according to the
 * transition table, which is bound to type of document. The table contains all
 * possible transitions for a given document type.
 *
 * @author: Alexander Nemerov
 * @date: 01.04.2014
 */
public interface LifeCycleServiceGeneric<Document, Transition, Transitions, Nodes, State> {

    /**
     * Calculates the right transition and do this transition. Changes document
     * status and do JavaScript actions.
     *
     * @param nodeRef   - document nodeRef
     * @param eventType - One of several types of events (automaticTransition,
     */
    boolean doTransition(Document nodeRef, String eventType);

    /**
     * Do transition. Changes document status and do JavaScript actions.
     *
     * @param nodeRef      - document nodeRef
     * @param transition   - Transition from transition table
     * @param fromStateDef
     * @param toStateDef
     */
    boolean doTransition(Document nodeRef, Transition transition, State fromStateDef, State toStateDef);

    /**
     * Get document state
     *
     * @param nodeRef - document nodeRef
     * @return document state
     */
    String getDocumentState(Document nodeRef);

    /**
     * Get all transitions with events available as user document actions at current state
     *
     * @param nodeRef - document nodeRef
     * @return set of transition from transition table
     */
    Transitions getAvailableUserEvents(Document nodeRef);

    /**
     * Get all transitions for document at current state
     *
     * @param nodeRef - document nodeRef
     * @return set of transition from transition table
     */
    Transitions getTransitionsByDocState(Document nodeRef);

    /**
     * Get all documents which wait a datetime
     *
     * @return set of documents
     */
    Nodes getDocumentsWithTimer();

}
