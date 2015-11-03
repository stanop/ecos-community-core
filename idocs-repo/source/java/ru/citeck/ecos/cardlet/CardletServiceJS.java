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
package ru.citeck.ecos.cardlet;

import org.alfresco.repo.jscript.ScriptNode;

import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;
import static ru.citeck.ecos.utils.JavaScriptImplUtils.*;

public class CardletServiceJS extends AlfrescoScopableProcessorExtension {

	private final CardletService cardletService;
	
	public CardletServiceJS(CardletService cardletService) {
		this.cardletService = cardletService;
	}
	
	public ScriptNode[] queryCardModes(ScriptNode node) {
		return wrapNodes(cardletService.queryCardModes(node.getNodeRef()), this);
	}
	
	public ScriptNode[] queryCardlets(ScriptNode node) {
		return wrapNodes(cardletService.queryCardlets(node.getNodeRef()), this);
	}
	
	public ScriptNode[] queryCardlets(ScriptNode node, String cardMode) {
		return wrapNodes(cardletService.queryCardlets(node.getNodeRef(), cardMode), this);
	}
	
}
