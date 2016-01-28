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
package ru.citeck.ecos.search;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Anton Fateev <anton.fateev@citeck.ru>
 */
public enum SearchPredicate {
	
	//without match method
	STRING_CONTAINS("string-contains"),  STRING_NOT_EQUALS("string-not-equals"),
    STRING_STARTS_WITH("string-starts-with"), STRING_ENDS_WITH("string-ends-with"),
    STRING_EMPTY("string-empty"), 

    NUMBER_EQUALS("number-equals"), NUMBER_NOT_EQUALS("number-not-equals"),
    NUMBER_LESS_THAN("number-less-than"), NUMBER_LESS_OR_EQUAL("number-less-or-equal"),
    NUMBER_GREATER_THAN("number-greater-than"), NUMBER_GREATER_OR_EQUAL("number-greater-or-equal"),

    DATE_EQUALS("date-equals"), DATE_NOT_EQUALS("date-not-equals"),
    DATE_LESS_OR_EQUAL("date-less-or-equal"), DATE_GREATER_OR_EQUAL("date-greater-or-equal"), DATE_LESS_THAN("date-less-than"),
    DATE_EMPTY("date-empty"), 

    BOOLEAN_EMPTY("boolean-empty"), 
    BOOLEAN_TRUE("boolean-true"), BOOLEAN_FALSE("boolean-false"),

    ANY("any"), LIST_EQUALS("list-equals"), LIST_NOT_EQUALS("list-not-equals"),

    NODEREF_CONTAINS("noderef-contains"), NODEREF_NOT_CONTAINS("noderef-not-contains"),
    NODEREF_EMPTY("noderef-empty"), NODEREF_NOT_EMPTY("noderef-not-empty"),
    ASSOC_CONTAINS("assoc-contains"), ASSOC_NOT_CONTAINS("assoc-not-contains"),
    ASSOC_EMPTY("assoc-empty"),

    QNAME_CONTAINS("qname-contains"), QNAME_NOT_CONTAINS("qname-not-contains"),

    ASPECT_EQUALS("aspect-equals"), PARENT_EQUALS("parent-equals"),
    PATH_EQUALS("path-equals"), PATH_CHILD("path-child"), PATH_DESCENDANT("path-descendant"),
    TYPE_NOT_EQUALS("type-not-equals"), ASPECT_NOT_EQUALS("aspect-not-equals"),
	
    //with match method
	STRING_EQUALS("string-equals") {
		public boolean match(Serializable nodeValue, Serializable criterionValue) {
			return String.valueOf(nodeValue).equals(String.valueOf(criterionValue));
		}
	},

	ID_EQUALS("id-equals") {
		public boolean match(Serializable nodeValue, Serializable criterionValue) {
			if (nodeValue == criterionValue)
				return true;
			if (nodeValue == null || criterionValue == null)
				return false;
			if (!NodeRef.isNodeRef(nodeValue.toString()) || !NodeRef.isNodeRef(nodeValue.toString()))
				return false;
			if (new NodeRef(nodeValue.toString()).equals(new NodeRef(criterionValue.toString())))
				return true;
			return false;

		}
	},
	STRING_NOT_EMPTY("string-not-empty") {
		public boolean match(Serializable nodeValue, Serializable criterionValue) {
			if (nodeValue == null || String.valueOf(nodeValue).isEmpty()) {
				return false;
			}
			return true;

		}
	},
	TYPE_EQUALS("type-equals") {
		public boolean match(Serializable nodeValue, Serializable criterionValue) {
			if (nodeValue == null || criterionValue == null) {
				return false;
			}
			
			if (!(nodeValue instanceof QName)) {
				nodeValue = QName.createQName(String.valueOf(nodeValue));
			}
			if (!(criterionValue instanceof QName)) {
				criterionValue = QName.createQName(String.valueOf(criterionValue));
			}
			
			if (nodeValue instanceof QName && criterionValue instanceof QName) {
				if (nodeValue.equals(criterionValue)) {
					return true;
				}
			}
			return false;

		}
	},
	DATE_NOT_EMPTY("date-not-empty") {
		public boolean match(Serializable nodeValue, Serializable criterionValue) {
			if (nodeValue == null) {
				return false;
			}

			if (nodeValue instanceof Date) {
				return true;
			}

			return false;

		}

	},
	FLOAT_NOT_EMPTY("float-not-empty") {
		public boolean match(Serializable nodeValue, Serializable criterionValue) {
			if (nodeValue == null) {
				return false;
			}

			if (nodeValue instanceof Float) {
				return true;
			}
			return false;

		}
	},
	ASSOC_NOT_EMPTY("assoc-not-empty") {
		public boolean match(Serializable nodeValue, Serializable criterionValue) {
			if (nodeValue == null) {
				return false;
			}
			if (nodeValue instanceof List) {
				List asList = (List) nodeValue;
				if (asList.isEmpty()) {
					return false;
				} else {
					return true;
				}
			}
			return false;

		}
	},
	INT_NOT_EMPTY("int-not-empty") {
		public boolean match(Serializable nodeValue, Serializable criterionValue) {
			if (nodeValue == null) {
				return false;
			}

			if (nodeValue instanceof Integer) {
				return true;
			}
			return false;

		}
	},
	BOOLEAN_NOT_EMPTY("boolean-not-empty") {
		public boolean match(Serializable nodeValue, Serializable criterionValue) {
			if (nodeValue == null) {
				return false;
			}
			if (nodeValue instanceof Boolean) {
				return true;
			}
			return false;

		}
	};

	private final String value;

	public boolean match(Serializable nodeValue, Serializable criterionValue) {

		throw new UnsupportedOperationException();
	}

	private SearchPredicate(String value) {
		this.value = value;
	}

	public static SearchPredicate forName(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Null enum argument");
		}
		for (SearchPredicate searchPredicate : values()) {
			if (value.equalsIgnoreCase(searchPredicate.getValue())) {
				return searchPredicate;
			}
		}
		throw new IllegalArgumentException("Unknown enum argument");
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return value;
	}
}
