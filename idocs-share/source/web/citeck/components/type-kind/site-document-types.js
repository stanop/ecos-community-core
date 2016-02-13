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
        .property('name', s)
        .property('folder', s)
        .property('journal', s)
        .property('createVariant', CreateVariant)
        ;
    
    Site
        .key('name', s)
        .property('allTypes', [DocumentType])
        .property('selectedTypes', [DocumentType])
        .property('currentTypes', [DocumentType])
        .computed('availableTypes', function() {
            return _.difference(this.allTypes(), this.currentTypes());
        })
        .computed('typesToAdd', function() {
            return _.difference(this.currentTypes(), this.selectedTypes());
        })
        .computed('typesToRemove', function() {
            return _.difference(this.selectedTypes(), this.currentTypes());
        })
        
        .method('addType', function(type) {
            if(this.currentTypes().indexOf(type) == -1) {
                this.currentTypes.push(type);
            }
        })
        .method('removeType', function(type) {
            this.currentTypes.remove(type);
        })
        
        .save(koutils.simpleSave({
            url: function(site) { 
                return Alfresco.constants.PROXY_URI + "citeck/site/document-types?site=" + site.name() 
            },
            toRequest: function(site) {
                return {
                    add: _.invoke(site.typesToAdd(), 'nodeRef'),
                    remove: _.invoke(site.typesToRemove(), 'nodeRef')
                } 
            }
        }))
        
        .method('save', function(callback) {
            Site.save(this, _.bind(function() {
                // we need to construct a copy of this array
                this.selectedTypes(this.currentTypes().concat([]));
                if(_.isFunction(callback)) callback(site);
                if(_.isObject(callback)) callback.fn.call(callback.scope, site);
            }, this));
        })
        
        ;
    
})
