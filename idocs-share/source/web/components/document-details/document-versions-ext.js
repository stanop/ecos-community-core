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
(function( $, undefined ) {

    $(document).ready(function(){
        //$('.ieOnly').hide();
    });

    var selectedVer = {
        vBase: null, // { version:'', documentUrl: '' },
        vNew: null // { version:'', documentUrl: '' }
    };

    function refreshUI() {
        // compare button state
        if (null == selectedVer['vBase'] || null == selectedVer['vNew']) {
            $('#versionCompareButton').prop("disabled", true);
            $('#versionCompareButtonNew').prop("disabled", true);
        } else {
            $('#versionCompareButton').prop("disabled", false);
            $('#versionCompareButtonNew').prop("disabled", false);
            $('#versionCompareButton').attr('baseDocUrl', selectedVer['vBase']['documentUrl']);
            $('#versionCompareButton').attr('newDocUrl', selectedVer['vNew']['documentUrl']);
            var buttonNew = "javascript:window.location='versions-difference?nodeRef="+selectedVer['vNew']['nodeRef']+"&versRef="+selectedVer['vBase']['nodeRef']+"'";
            $('#versionCompareButtonNew').attr('onclick', buttonNew);
        }
        //UI
        if (null == selectedVer['vBase']) {
            $('#baseVersion').hide();
        } else {
            document.getElementById('baseVersionLable').innerHTML = selectedVer['vBase']['version'];
//            $('#baseVersionLable').text(selectedVer['vBase']['version']);
            $('#baseVersion').show();
        }
        if (null == selectedVer['vNew']) {
            $('#newVersion').hide();
        } else {
            document.getElementById('newVersionLable').innerHTML = selectedVer['vNew']['version'];
//            $('#newVersionLable').text(selectedVer['vNew']['version']);
            $('#newVersion').show();
        }
        if (null == selectedVer['vBase'] && null == selectedVer['vNew']) {
            $('#emptyLable').show();
        } else {
            $('#emptyLable').hide();
        }
    }

    var methods = {

        init: function(options) {
            var settings = $.extend({}, { documentName: '' }, options);
            //if (!isSupported(settings['documentName']) || !isIE()) {
                //$('.ieOnly').hide();
                //return;
            //} else {
                $('.ieOnly').show();
            //}

            if (!isSupported(settings['documentName']) || !isIE()) {
                $('#versionCheckBoxDiv').hide();
                $('#versionCompareButton').hide();
                $('#versionCompareButtonNew').show();
            } else {            	
                $('#versionCheckBoxDiv').show();
                if ($('#versionCheckBox').checked==true) {
                	$('#versionCompareButton').show();
                	$('#versionCompareButtonNew').hide();
                } else {
                	$('#versionCompareButton').hide();
                	$('#versionCompareButtonNew').show();
                }
            }

            $('#baseVersion').hide();
            $('#newVersion').hide();

            $('#baseVersionButton').click(function() {
                selectedVer['vBase'] = null;
                refreshUI();
            });
            $('#newVersionButton').click(function() {
                selectedVer['vNew'] = null;
                refreshUI();
            });
            $('#versionCheckBox').click(function() {
                if (this.checked==true) {
                	$('#versionCompareButtonNew').hide();
                	$('#versionCompareButton').show();
                } else {
                	$('#versionCompareButton').hide();
                	$('#versionCompareButtonNew').show();
                }
            });
            refreshUI();
        },

        addToCompare: function(options) {
            var settings = $.extend({}, { version: '' }, options);
            var selector = $('#'+generateSelectorId(settings['version']));
            var vSelect = {
                version: settings['version'],
                documentName: selector.attr('documentName'),
                documentUrl: selector.attr('documentUrl'),
                nodeRef: selector.attr('nodeRef')
            }
//            if (1 == parseFloat(settings['version'])) {
//                Alfresco.util.PopupManager.displayMessage({
//                    text: 'В версии договора 1.0 нет прикреплённого документа'
//                });
//                return;
//            }
            if (null != selectedVer['vBase'] && null != selectedVer['vNew']) {
                Alfresco.util.PopupManager.displayMessage({
                    text: 'Вы уже указали версии для сравнения'
                });
                return;
            }
            function setVersions(v1, v2) {
                var last = Math.max(parseFloat(v1['version']), parseFloat(v2['version']));
                if (last == parseFloat(v1['version'])) {
                    selectedVer['vBase'] = v2;
                    selectedVer['vNew'] = v1;
                } else {
                    selectedVer['vBase'] = v1;
                    selectedVer['vNew'] = v2;
                }
            }
            function vlaidate(v1, v2) {
                if (parseFloat(v1['version']) == parseFloat(v2['version'])) {
                    Alfresco.util.PopupManager.displayMessage({
                        text: 'Данная версия уже выбранна'
                    });
                    return false;
                }
                return true;
            }
            if (null != selectedVer['vBase']) {
                if (!vlaidate(selectedVer['vBase'], vSelect)) {
                    return;
                }
                setVersions(selectedVer['vBase'], vSelect);
            } else if (null != selectedVer['vNew']) {
                if (!vlaidate(selectedVer['vNew'], vSelect)) {
                    return;
                }
                setVersions(selectedVer['vNew'], vSelect);
            } else {
                selectedVer['vBase'] = vSelect;
            }
            refreshUI();
        },

        generateSelectorHTML: function(options) {
            var settings = $.extend({}, { context: '', document: {}, documentUrl: '' }, options);
            var html = '';
            //if (isSupported(settings['document']['name']) && isIE()) {
            if (isSupported(settings['document']['name'])) {
                html = $('<div></div>').append(
                    $('<a>', {
                        'id': generateSelectorId(settings['document']['label']),
                        'class': settings['context'] + ' addToCompare ',
                        'href': '#',
                        'name': '.addToCompare',
                        'rel': settings['document']['label'],
                        'title': 'Добавить в сравнение',
                        'documentUrl': settings['documentUrl'],
                        'documentName': settings['document']['name'],
                        'nodeRef': settings['document']['nodeRef'],
                    })
                ).html();
            }
            return html;
        }

    };


    $.fn.extVersionsController = function(mHandle, options) {
        return methods[mHandle](options);
    };

    function isIE() {
        return true == $.browser.msie;
    }

    function getDocumentType(documentName) {
        var m = documentName.match(/\S+\.(\S+)$/);
        return (null == m)? null : m[1];
    }

    function isSupported(documentName) {
        var supportedTypes = {
            'doc': true,
            'docx': true,
            'rtf': true,
            'odt': true,
            'txt': true,
            'xml': true
        };
        var documentType = getDocumentType(documentName);
        return true == supportedTypes[documentType];
    }

    function generateSelectorId(version) {
        return 'addToCompare' + version.replace(/\./g,'_');
    }


})(jQuery);