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
define(['lib/knockout', 'citeck/utils/knockout.utils'], function(ko, koutils) {
    
    var koclass = koutils.koclass;
    
    var s = String,
        DocumentType = koclass('type-kind.DocumentType'),
        Site = koclass('type-kind.Site'),
        CreateVariant = koclass('type-kind.CreateVariant');
    
    CreateVariant
        .key('nodeRef', s)
        .property('title', s)
        .property('type', s)
        .property('formId', s)
        .property('destination', s)
        ;
    
    DocumentType
        .key('nodeRef', s)
        .property('title', s)
        .property('folder', s)
        .property('journal', s)
        .property('createVariant', CreateVariant)
        ;
    
    Site
        .key('name', s)
        .property('persistedTypes', [DocumentType])
        .property('selectedTypes', [DocumentType])
        .property('allTypes', [DocumentType])
        .computed('availableTypes', function() {
            return _.difference(this.allTypes(), this.selectedTypes());
        })
        .computed('typesToAdd', function() {
            return _.difference(this.selectedTypes(), this.persistedTypes());
        })
        .computed('typesToRemove', function() {
            return _.difference(this.persistedTypes(), this.selectedTypes());
        })
        
        .method('addType', function(type) {
            if(this.selectedTypes().indexOf(type) == -1) {
                this.selectedTypes.push(type);
            }
        })
        .method('removeType', function(type) {
            this.selectedTypes.remove(type);
        })
        
        .save(function(site) {
            
        })
        
        ;
    
})
