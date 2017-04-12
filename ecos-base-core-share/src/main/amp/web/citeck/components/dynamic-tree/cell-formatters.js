/*
 * Copyright (C) 2008-2017 Citeck LLC.
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
/**
 * Default cell formatters for YUI DataTable.
 */
(function() {

	Citeck = typeof Citeck != "undefined" ? Citeck : {};
	Citeck.format = Citeck.format || {};

	var typeNameCache = {};
	var repoMessageCache = {};
	var loadedFormattersCache = {};

	YAHOO.lang.augmentObject(Citeck.format, {

		empty: function() {
			return function(elCell, oRecord, oColumn, sData) {
				elCell.innerHTML = "";
			};
		},

		evalFormatter: function(formatterExpr) {
			try {
				if(!formatterExpr.match(/[)]$/)) {
					formatterExpr += '()';
				}
				with(Citeck.format) {
					return eval(formatterExpr);
				}
			} catch(e) {
				return null;
			}
		},

		loadedFormatter: function(formatterExpr) {
			if(loadedFormattersCache[formatterExpr]) {
				return loadedFormattersCache[formatterExpr];
			}
			return loadedFormattersCache[formatterExpr] = function(elCell, oRecord, oColumn, oData) {
				var formatter = Citeck.format.evalFormatter(formatterExpr);
				if(formatter != null) {
					loadedFormattersCache[formatterExpr] = formatter;
					return formatter.apply(this, arguments);
				} else {
					elCell.innerHTML = "";
					_.delay(loadedFormattersCache[formatterExpr], 100, elCell, oRecord, oColumn, oData);
				}
			};
		},

		checkbox: function(callbackName) {
			return function(elCell, oRecord, oColumn, sData) {
				elCell.innerHTML = '<input type="checkbox" data-bind="checked: '+callbackName+'" />';
			};
		},

		encodeHTML: function() {
			return function(elCell, oRecord, oColumn, sData) {
				elCell.innerHTML = Alfresco.util.encodeHTML(sData, true);
			};
		},

		fileSize: function(contentKey) {
			return function(elCell, oRecord, oColumn, oData) {
				var content = oRecord.getData(contentKey);
				if(!content) {
					elCell.innerHTML = '';
					return;
				}
				elCell.innerHTML = Alfresco.util.formatFileSize(content.size);
			}
		},

		contentFileSize: function(contentKey) {
			return function(elCell, oRecord, oColumn, oData) {
				var content = oRecord.getData(contentKey);
				if(!content) {
					elCell.innerHTML = '';
					return;
				}
				elCell.innerHTML = Alfresco.util.formatFileSize(content);
			}
		},

		fileType : function(contentKey) {
			return function(elCell, oRecord, oColumn, oData) {
				var content = oRecord.getData(contentKey);
				if (!content) {
					elCell.innerHTML = '';
					return;
				}
				elCell.innerHTML = content.mimetype;
			}
		},

		bool: function(trueLabel, falseLabel) {
			return function(elCell, oRecord, oColumn, sData) {
				if(!sData) {
					elCell.innerHTML = falseLabel;
					return;
				}
				elCell.innerHTML = sData === "true" || sData === true ? trueLabel : falseLabel;
			};
		},

		date: function Citeck_format_date(pattern) {
			if (!pattern) {
				pattern = 'dd.MM.yyyy';
			}

			return function(elCell, oRecord, oColumn, sData) {
				if (!sData) {
					elCell.innerHTML = '';
					return;
				}

				var date = Alfresco.util.fromISO8601(sData);
				elCell.innerHTML = date.toString(pattern);
			};
		},

		datetime: function(pattern) {
			return Citeck.format.date(pattern || 'dd.MM.yyyy HH:mm:ss');
		},

		date_iso: function(pattern) {
			if (!pattern) {
				pattern = 'dd.MM.yyyy';
			}
			return function(elCell, oRecord, oColumn, sData) {
				if (!sData) {
					elCell.innerHTML = '';
					return;
				}
				var date = Alfresco.util.fromISO8601(sData.iso8601);
				elCell.innerHTML = date.toString(pattern);
			};
		},

		// alias for backwards compatibility
		dateFormat: function(pattern) {
			return Citeck.format.date(pattern);
		},

		// alias for backwards compatibility
		dateFormatter: function(pattern) {
			return Citeck.format.date(pattern);
		},

		qname: function(full) {
			return function(elCell, oRecord, oColumn, sData) {
				if (!sData) {
					elCell.innerHTML = '';
					return;
				}
				elCell.innerHTML = full ? sData.fullQName : sData.shortQName;
			};
		},

		icon: function(iconSize, namePath) {
			return function(elCell, oRecord, oColumn, sData) {
				var name = sData || oRecord.getData(namePath);
				elCell.innerHTML = '<span class="icon16"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util.getFileIcon(name, null, iconSize) + '"/></span>';
			};
		},

		iconName: function(iconSize, namePath) {
			return function(elCell, oRecord, oColumn, sData) {
				var name = sData || oRecord.getData(namePath) || '';
				elCell.innerHTML = '<span class="icon16"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util.getFileIcon(name, null, iconSize) + '"/>&nbsp;' + name + '</span>';
			};
		},

		iconContentName: function(iconSize, contentPath, namePath) {
			return function(elCell, oRecord, oColumn, sData) {
			    var name = sData || oRecord.getData(namePath) || '';
				var content = oRecord.getData(contentPath);
				if (!content || !content.url) {
				    elCell.innerHTML = name;
				} else {
				    elCell.innerHTML = '<span class="icon16"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util.getFileIcon(content.url, null, iconSize) + '"/>&nbsp;' + name + '</span>';
				}
			};
		},

		avatar: function(namePath) {
			return function(elCell, oRecord, oColumn, sData) {
				var name = sData || oRecord.getData(namePath);
				elCell.innerHTML = '<span class="icon"><img src="' + Alfresco.constants.PROXY_URI + 'slingshot/profile/avatar/' + name + '"/></span>';
			};
		},

		userName: function(userNamePath, firstNamePath, lastNamePath, plainText) {
			return function(elCell, oRecord, oColumn, sData) {
				var userName = oRecord.getData(userNamePath),
					firstName = oRecord.getData(firstNamePath),
					lastName = oRecord.getData(lastNamePath);
				elCell.innerHTML = Citeck.utils.formatUserName({
					userName: userName,
					firstName: firstName,
					lastName: lastName
				}, plainText);
			};
		},

		node: function(plainText) {
			return function(elCell, oRecord, oColumn, sData) {
				if(!sData) {
					elCell.innerHTML = "";
					return;
				}
				if(sData.type == "cm:person") {
					elCell.innerHTML = Citeck.utils.formatUserName({
						userName: sData["cm:userName"],
						firstName: sData["cm:firstName"],
						lastName: sData["cm:lastName"]
					}, plainText);
					return;
				}
				if(sData.type == "cm:authorityContainer") {
					elCell.innerHTML = sData["cm:authorityDisplayName"] || sData["cm:authorityName"] || sData.displayName || "";
					return;
				}
				elCell.innerHTML = sData.displayName; 
			};
		},

		nodeRef: function(props) {
			return function(elCell, oRecord, oColumn, sData) {
				if(!sData) {
					elCell.innerHTML = "";
					return;
				}
				else {
					Alfresco.util.Ajax.request({
						url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=" + sData[0] + (props ? '&props=' + props + '&replaceColon=_' : ''),
						successCallback: {
							scope: this,
							fn: function(response) {
								if (response.json && response.json.props) {
									props = props ? ('' + props).split(',') : ["cm:name"];
									var value = '';
									for (var i = 0; i < props.length; i++) {
										var v1 = response.json.props[props[i]];
										var v2 = response.json.props[props[i].replace(':', '_')];
										value += (v1 || v2 || ' ') + ( i < props.length - 1 ? ', ' : '');
									}
									elCell.innerHTML = value;
								}
							}
						},
						failureCallback: {
							scope: this,
							fn: function(response) {}
						},
						execScripts: true
					});
				}
			};
		},

		userOrGroup: function(plainText) {
			return function(elCell, oRecord, oColumn, sData) {
				if(!sData) {
					elCell.innerHTML = "";
					return;
				}
				else {
					var nodeRefs = [];
					if (typeof sData === 'object' && sData.length) {
						nodeRefs = sData;
					}
					else {
						nodeRefs.push(sData);
					}
					for(var i = 0; i < nodeRefs.length; i++) {
						Alfresco.util.Ajax.request({
							url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=" + nodeRefs[i],
							successCallback: {
								scope: this,
								fn: function(response) {
									if (response.json && response.json.props) {
										var displayName ='';
										if(response.json.props["cm:authorityDisplayName"])
										{
											displayName = response.json.props["cm:authorityDisplayName"];
										}
										else if(response.json.props["cm:firstName"] || response.json.props["cm:lastName"])
										{
											displayName = response.json.props["cm:lastName"] +" "+response.json.props["cm:firstName"];
										}
										if (elCell.innerHTML)
											elCell.innerHTML += '<br />';
										elCell.innerHTML += displayName;
									}
								}
							}, failureCallback: { scope: this, fn: function(response) {} }, execScripts: true
						});
					}
				}
			};
		},
		
		user: function(plainText) {
			return function(elCell, oRecord, oColumn, sData) {
				if(!sData) {
					elCell.innerHTML = "";
					return;
				}
                else
                {
                    Alfresco.util.Ajax.request({
                        url: Alfresco.constants.PROXY_URI + "api/people/" + sData,
                        successCallback: {
                            scope: this,
                            fn: function(response) {
                                if (response.json && response.json) {
                                    elCell.innerHTML = response.json["lastName"]+" "+response.json["firstName"];
                                }
                            }
                        }, failureCallback: { scope: this, fn: function(response) {} }, execScripts: true
                    });
                }
			};
		},

		code: function(labels, tdClassPrefix, trClassPrefix) {
			return function(el, oRecord, oColumn, sData) {
				var td = el.parentElement,
					tr = td.parentElement;
				el.innerHTML = labels[sData] || sData || "";
				if(tdClassPrefix) {
					Dom.addClass(td, tdClassPrefix + sData);
				}
				if(trClassPrefix) {
					Dom.addClass(tr, trClassPrefix + sData);
				}
			}
		},

		multiple: function(singleFormatter) {
			return function(elCell, oRecord, oColumn, sData) {
				if(YAHOO.lang.isArray(sData)) {
					var texts = [];
					for(var i = 0, ii = sData.length; i < ii; i++) {
						singleFormatter(elCell, oRecord, oColumn, sData[i]);
						texts[i] = elCell.innerHTML;
					}
                    elCell.innerHTML = texts.reduce(function (resultStr, text) {
                        return resultStr += (resultStr && text ? ", " : "") + text;
                    }, "");
				} else {
					singleFormatter(elCell, oRecord, oColumn, sData);
				}
			};
		},

		loading: function() {
			return function(elCell, oRecord, oColumn, sData) {
				elCell.innerHTML = '<span class="column-loading"></span>';
			};
		},

		message: function(prefix) {
			return function(elCell, oRecord, oColumn, sData) {
				elCell.innerHTML = Alfresco.util.message(prefix + sData);
			};
		},

		repoMessage: function(prefix) {
			var cache = repoMessageCache;
			return function(elCell, oRecord, oColumn, sData) {
				if (!sData) { return; }
				var key = (prefix || "") + sData;
				if(cache[key]) {
					elCell.innerHTML = cache[key];
				} else {
					Alfresco.util.Ajax.jsonGet({
						url: Alfresco.constants.PROXY_URI + "citeck/message?key=" + key,
						successCallback: {
							fn: function(response) {
								elCell.innerHTML = cache[key] = response.serverResponse.responseText;
							}
						},
                        failureCallback: {
                            fn: function(response) {
                                elCell.innerHTML = cache[key] = key;
                            }
                        }
					});
					elCell.innerHTML = '<span class="column-loading"></span>';
				}
			};
		},

		workflowPriority: function() {
			return function(elCell, oRecord, oColumn, sData) {
				var codes = {
						"1": "high",
						"2": "medium",
						"3": "low"
					},
					priority = codes[sData] || null;
				if(priority) {
					elCell.innerHTML = '<span class="priority-' + priority + '" title="' + Alfresco.util.message('priority.' + priority) + '"></span>';
				} else {
					elCell.innerHTML = '';
				}
			};
		},

		workflowName: function() {
			return function(elCell, oRecord, oColumn, sData) {
				var workflowDefinitionName = '';
				Alfresco.util.Ajax.jsonGet({
					url: Alfresco.constants.PROXY_URI + "api/workflow-definitions",
					successCallback: {
						fn: function(response) {
							var data = response.json.data;
							for(var i=0; i<data.length; i++)
							{
								if(data[i].name==sData)
								{
									elCell.innerHTML = data[i].title;
									break;
								}
							}
						}
					},
					failureCallback: {
						fn: function(response) {
							elCell.innerHTML = '';
						}
					}
				});
			};
		},
		typeName: function(key) {
			var cache = typeNameCache;
			return function(elCell, oRecord, oColumn, sData) {
				if (!sData) { return; }
				var typeQName = key ? sData[key] : sData;
				if(cache[typeQName]) {
					elCell.innerHTML = cache[typeQName];
				} else {
					Alfresco.util.Ajax.jsonGet({
						url: Alfresco.constants.PROXY_URI + "api/classes/" + typeQName.replace(':','_'),
						successCallback: {
							fn: function(response) {
								elCell.innerHTML = cache[typeQName] = response.json.title;
							}
						},
                        failureCallback: {
                            fn: function(response) {
                                elCell.innerHTML = cache[typeQName] = typeQName;
                            }
                        }
					});
					elCell.innerHTML = '<span class="column-loading"></span>';
				}
			};
		},

		taskHistoryOutcome: function() {
			var cache = repoMessageCache;
			return function(elCell, oRecord, oColumn, sData) {
				if (!sData) { return; }

				var getMessages = function(keys, onSuccess, onFailure) {
					var result = {};
					var notCachedKeys = [];
					for (var k = 0; k < keys.length; k++) {
						var key = keys[k];
						var cachedValue = cache[key];
						if (cachedValue) {
							result[key] = cachedValue;
						} else {
							notCachedKeys.push(key);
						}
					}
					if (notCachedKeys.length > 0) {
						Alfresco.util.Ajax.jsonPost({
							url: Alfresco.constants.PROXY_URI + "citeck/util/messages",
							dataObj: {"keys" : notCachedKeys},
							successCallback: {
								fn: function(response) {
									for (var i = 0; i < notCachedKeys.length; i++) {
										var key = notCachedKeys[i];
										result[key] = response.json[key];
										cache[key] = response.json[key];
									}
									onSuccess(result);
								}
							},
							failureCallback: {
								fn: function() {
									for (var i = 0; i < notCachedKeys.length; i++) {
										result[notCachedKeys[i]] = notCachedKeys[i];
									}
									onFailure(result);
								}
							}
						});
					} else {
						onSuccess(result);
					}
				};

				var typeQName = oRecord.getData()['attributes["event:taskType"]']["shortQName"];
				var outcome = sData;

				elCell.innerHTML = '<span class="column-loading"></span>';

				var keyByType = "workflowtask." + typeQName.replace(/:/g, "_") + ".outcome." + outcome;
				var globalKey = "workflowtask.outcome." + outcome;

				getMessages([keyByType, globalKey],
					function(msgs) {
						if (msgs[keyByType] != keyByType) {
							elCell.innerHTML = msgs[keyByType];
						} else if (msgs[globalKey] != globalKey) {
							elCell.innerHTML = msgs[globalKey];
						} else {
							elCell.innerHTML = outcome;
						}
					},
					function (msgs) {
						elCell.innerHTML = outcome;
					});
			};
		},

        documentDetailsLink: function (target) {
            return Citeck.format.siteURL('document-details?nodeRef={nodeRef}', '{displayName}', target)
        },

        folderDetailsLink: function (target) {
            return Citeck.format.siteURL('folder-details?nodeRef={nodeRef}', '{displayName}', target)
        },

        documentLink: function () {
            return Citeck.format.siteURL('document-details?nodeRef={nodeRef}', '{displayName}', null)
        },

        folderLink: function () {
            return Citeck.format.siteURL('document-details?nodeRef={nodeRef}', '{displayName}', null)
        },

        caseLink: function () {
            return Citeck.format.siteURL('document-details?nodeRef={nodeRef}', '{displayName}', null)
        },

        siteURL: function (urlTemplate, labelTemplate, target) {
            if (!target) target = '_self';
            if (!urlTemplate) urlTemplate = '';
            return function (elCell, oRecord, oColumn, sData) {
                if (sData) {
                    var url = Alfresco.util.siteURL(YAHOO.lang.substitute(urlTemplate, sData));
                    var label = YAHOO.lang.substitute(labelTemplate, sData);
                    elCell.innerHTML = '<a class="document-link" onclick="event.stopPropagation()" '
                                     + 'href="' + url + '" target="' + target + '">' + label + '</a>';
                }
            }
        },

        downloadContent: function (keyToNodeRef) {
            var downloadUrl = Alfresco.constants.PROXY_URI + "/citeck/print/content?nodeRef=",
                downloadImage = Alfresco.constants.URL_RESCONTEXT + "/components/documentlibrary/actions/document-download-16.png",
				title = Alfresco.util.message("actions.document.download");
            return function (elCell, oRecord) {
                var nodeRefToDownload = oRecord.getData(keyToNodeRef);
                downloadUrl = downloadUrl + nodeRefToDownload;
                console.log(downloadUrl);
                elCell.innerHTML = '<div class="document-download">' + '<a class="simple-link" onclick="event.stopPropagation()" '
                    + 'href="' + downloadUrl + '" style="background-image: url(' + downloadImage + ')" ' +
					'title="' + title +'"/>' + '</div>';
            }
        },
        
        doubleClickLink: function(urlTemplate, fieldId, formatter, target) {
            if (!target) target = '_self';
            if (!urlTemplate) urlTemplate = '';
            return function (elCell, oRecord, oColumn, sData) {
                var label = formatter && (formatter.apply(this, arguments), elCell.innerHTML) || sData || Alfresco.util.message("label.none");
                var url = Alfresco.util.siteURL(YAHOO.lang.substitute(urlTemplate, {
                    id: oRecord.getData(fieldId)
                }));
                elCell.innerHTML = '<a class="document-link" onclick="event.stopPropagation()" '
                                 + 'href="' + url + '" target="' + target + '">' + label + '</a>';
            }
        },

        /**
         * Actions: Edit & Remove
         * Event: "actionNonContentButtonClicked"
         * Control: table-children [dynamic-table.js]
         * This action contains formatter, which creates 2 buttons: edit & remove.
         * */
		actionsNonContent: function(params) {
            /**
             * Hard Code!
             * @oRecord mandatory must contain parameter 'nodeRef' !!! NodeRef will be sent with fire-event.
             * */
			return function(elCell, oRecord, oColumn, sData) {
                var nodeRef = oRecord.getData("nodeRef");
                var panelId = "yui-actions-non-content-buttonsPanel-" + oRecord.getCount();
                if (params && params.panelID)
                    panelId = panelId + "-" + params.panelID;
                var evnBtn = { "eventType": "", "nodeRef": nodeRef, "_item_name_": nodeRef, "source": {} };
                elCell.innerHTML = '';
                var div = document.createElement("div");
                div.id = panelId;
                elCell.appendChild(div);

                /**
                 * @containerId is html-identifier of the buttons container.
                 * @type_evn type_evn is type of event. It will be sent through fire-event and it will be a part of
                 *  the css-class name.
                 * */
                var createButton = function(pnl, type_evn) {
                    // creating button
                    var btnTag = document.createElement('div');
                    btnTag.className = "btn-" + type_evn;
                    btnTag.onclick = function() {
                        evnBtn.eventType = type_evn;
                        evnBtn.elementId = this.id;
                        evnBtn.elementTag = this.tagName;
                        YAHOO.Bubbling.fire("actionNonContentButtonClicked", evnBtn);
                    };
                    pnl.appendChild(btnTag);
                    return btnTag;
                };
                // creating buttons
                createButton(div, "edit").setAttribute("title", Alfresco.util.message('title.table-children.editItem'));
                createButton(div, "remove").setAttribute("title", Alfresco.util.message('title.table-children.removeItem'));
			}
		},

        /**
         * Actions: Start workflow
         * Event: "actionNonContentButtonClicked"
         * Control: table-children [dynamic-table.js]
         * This action contains formatter, which creates 2 buttons: edit & remove.
         * */
		actionStartWorkflow: function(params) {
            /**
             * Hard Code!
             * @oRecord mandatory must contain parameter 'nodeRef' !!! NodeRef will be sent with fire-event.
             * */
			return function(elCell, oRecord, oColumn, sData) {
                var nodeRef = oRecord.getData("nodeRef");
                var actionTitle;
                var workflowId = "";
                var formId = "";
                var wf_params = "";
                var panelId = "yui-actions-non-content-buttonsPanel-" + oRecord.getCount();
                if (params && params.panelID)
                    panelId = panelId + "-" + params.panelID;
                if (params && params.actionTitle)
                    actionTitle = params.actionTitle;
                if (params && params.workflowId)
                {
                    wf_params = wf_params+"workflowId="+params.workflowId;
                }
                if (params && params.formId)
                {
                    wf_params = wf_params+"&formId="+params.formId;
                }
                if (params && params.packageItems)
                {
                    wf_params = wf_params+"&packageItems="+params.packageItems;
                }
                else
                {
                    wf_params = wf_params+"&packageItems="+nodeRef;
                }
                var evnBtn = { "eventType": "", "nodeRef": nodeRef, "_item_name_": nodeRef, "source": {}, "wf_params": wf_params};
                elCell.innerHTML = '';
                var div = document.createElement("div");
                div.id = panelId;
                elCell.appendChild(div);

                /**
                 * @containerId is html-identifier of the buttons container.
                 * @type_evn type_evn is type of event. It will be sent through fire-event and it will be a part of
                 *  the css-class name.
                 * */
                var createButton = function(pnl, type_evn) {
                    // creating button
                    var btnTag = document.createElement('div');
                    btnTag.className = "btn-" + type_evn;
                    btnTag.innerText = actionTitle ? actionTitle:"Начать бизнес процесс";
                    btnTag.onclick = function() {
                        evnBtn.eventType = type_evn;
                        evnBtn.elementId = this.id;
                        evnBtn.elementTag = 'DIV';
                        YAHOO.Bubbling.fire("actionNonContentButtonClicked", evnBtn);
                    };
                    pnl.appendChild(btnTag);
                    return btnTag;
                };
                // creating buttons
                createButton(div, "start-workflow").setAttribute("title", actionTitle ? actionTitle:"Начать бизнес процесс");
			}
		},

        dynamicTablePredicate: function(params) {
            return function(elCell, oRecord, oColumn, sData) {
                var msgId = "predicate." + oRecord.getData("journal_predicate");
                var res = Alfresco.util.message.call(this, msgId, "", null);
                if (msgId === res)
                    elCell.innerHTML = oRecord.journal_predicate;
                else
                    elCell.innerHTML = res;
            }
        },

        dynamicTableShortQName: function(params) {
            //sle
            return function(elCell, oRecord, oColumn, sData) {
                var QName = oRecord.getData("journal_fieldQName");
                var attrFullName = QName.replace('{', '%7B').replace('}', '%7D');
                elCell.innerHTML = QName;
                Alfresco.util.Ajax.request({
                    url: "/share/proxy/alfresco/search/search-attributes?attrFull=" + attrFullName,
                    successCallback: {
                        scope: this,
                        fn: function(response) {
                            if (response.json && response.json.attributes && response.json.attributes.length > 0) {
                                elCell.innerHTML = response.json.attributes[0].shortName;
                            }
                        }
                    }, failureCallback: { scope: this, fn: function(response) {} }, execScripts: true
                });
            }
        },

        taskOutcome: function() {
            var formatterScope = this;
            return function (elCell, oRecord, oColumn, sData) {
                var getParentElement = function (item, parentNodeName) {
                    var parent = item.parent().get(0);
                    return parent.nodeName == parentNodeName ? parent : getParentElement(item.parent(), parentNodeName);
                };

                var checkMirror = function () {
                    Alfresco.util.Ajax.jsonGet({
                        url: Alfresco.constants.PROXY_URI + "citeck/mirror-task/status?taskId=" + oRecord._oData.taskId,
                        successCallback: {
                            fn: function (response) {
                                if (response.json.status == "Completed" && response.json.completionDate) {
                                    YAHOO.Bubbling.fire("metadataRefresh");
                                } else {
                                    setTimeout(function () { checkMirror() }, 1000);
                                    console.dir("WorkflowMirrorService could not find the mirror for given task!");
                                }
                            }
                        }
                    });
                };

                var th = this.getThEl(oColumn),
                    td = this.getTdEl(elCell),
                    tr = this.getTrEl(elCell);
                Dom.addClass(th, "hide-column");
                Dom.addClass(td, "hide-column");
                
                var move = function() {
                    var ntr = document.createElement("TR"),
                        ntd = document.createElement("TD");
                    ntd.colSpan = this.getColumnSet().getDefinitions().length;
                    ntr.appendChild(ntd);
                    ntd.appendChild(elCell);
                    elCell.innerHTML = '<div class="loading-form"></div>';
                    Dom.insertAfter(ntr, tr);
                    this.unsubscribe('renderEvent', move, this);
                };
                this.subscribe('renderEvent', move, this, true);

                var htmlid = _.uniqueId("inline-form-");
                Alfresco.util.Ajax.request({
                    url: Alfresco.constants.URL_SERVICECONTEXT + "citeck/form/inline?itemKind=task&itemId=" +
                        oRecord._oData.taskId + "&formId=inline&submitType=json&htmlid=" + htmlid + "&showSubmitButton=false",
                    execScripts: true,
                    successCallback: {
                        fn: function (response) {
                            elCell.innerHTML = response.serverResponse.responseText;
                            YAHOO.Bubbling.on("beforeFormRuntimeInit", function(layer, args) {
                                if (Alfresco.util.hasEventInterest(htmlid + "-form", args))
                                {
                                    args[1].runtime.setAJAXSubmit(true, {
                                        successCallback: {
                                            scope: this,
                                            fn: function(response) {
                                                //setTimeout(function () { checkMirror() }, 1000);
                                                YAHOO.Bubbling.fire("metadataRefresh");
                                            }
                                        },
                                        failureCallback: {
                                            scope: this,
                                            fn: formatterScope.onFailure
                                        }
                                    });
                                }
                            });
                        }
                    },
                    failureCallback: {
                        scope: this,
                        fn: formatterScope.onFailure
                    }
                });
            }
        },
        
		taskButtons: function() {
			var formatterScope = this;
			return function (elCell, oRecord, oColumn, sData) {
				var th = this.getThEl(oColumn),
					td = this.getTdEl(elCell),
					tr = this.getTrEl(elCell);
				Dom.addClass(th, "hide-column");
				Dom.addClass(td, "hide-column");
				
				var move = function() {
					this.unsubscribe('renderEvent', move, this);
					var ntr = document.createElement("TR"),
						ntd = document.createElement("TD");
					ntd.colSpan = this.getColumnSet().getDefinitions().length;
					ntr.appendChild(ntd);
					ntd.appendChild(elCell);
					Dom.insertAfter(ntr, tr);
					var taskId = oRecord._oData.taskId;
					var htmlid = _.uniqueId("task-buttons-");
				
					var disableActionButtons = function(disabled)
					{
						if (reassignButton)
						{
							reassignButton.set("disabled", disabled)
						}
						if (releaseButton)
						{
							releaseButton.set("disabled", disabled)
						}
						if (claimButton)
						{
							claimButton.set("disabled", disabled)
						}
					}
					var updateTaskProperties = function (properties, action)
					{
						disableActionButtons(true);
						YAHOO.lang.later(2000, this, function()
						{
							if (this.isRunning)
							{
									var feedbackMessage = Alfresco.util.PopupManager.displayMessage(
									{
										text: Alfresco.util.message("message." + action),
										spanClass: "wait",
										displayTime: 0
									});
							}
						}, []);

						 // Run rules for folder (and sub folders)
						if (!this.isRunning)
						{
							this.isRunning = true;

							// Start/stop inherit rules from parent folder
							Alfresco.util.Ajax.jsonPut(
							{
								url: Alfresco.constants.PROXY_URI_RELATIVE + "api/task-instances/" + taskId,
								dataObj: properties,
								successCallback:
								{
									fn: function(response, action)
									{
										//this.isRunning = false;
										var data = response.json.data;
										if (data)
										{
											Alfresco.util.PopupManager.displayMessage(
											{
												text: Alfresco.util.message("message." + action + ".success")
											});

											YAHOO.lang.later(3000, this, function(data)
											{
												if (data.owner && data.owner.userName == Alfresco.constants.USERNAME)
												{
													// Let the user keep working on the task since he claimed it
													document.location.reload();
												}
												else
												{
													// Check referrer and fall back to user dashboard if unavailable.
													if(this.referrerValue) 
													{
														// Take the user to the most suitable place
														this.navigateForward(true);
													} else {
													 // ALF-20001. If referrer isn't available, either because there was no previous page 
													 // (because the user navigated directly to the page via an emailed link)
													 // or because the referrer header has been blocked, fall back to user dashboard.
													 document.location.href = Alfresco.constants.URL_CONTEXT;
													}
												}
											}, data);
										}
									},
									obj: action,
									scope: this
								},
								failureCallback:
								{
									fn: function(response)
									{
										this.isRunning = false;
										disableActionButtons(false);
										Alfresco.util.PopupManager.displayPrompt(
										{
											title: Alfresco.util.message("message.failure"),
											text: Alfresco.util.message("message." + action + ".failure")
										});
									},
									scope: this
								}
							});
						}
					}
					

					var onClaimButtonClick = function ()
					{
						updateTaskProperties(
						{
							"cm_owner": Alfresco.constants.USERNAME
						}, "claim");
					};
					  
					var onReleaseButtonClick = function ()
					{
						updateTaskProperties(
						{
							"cm_owner": null
						}, "release");
					};
					
					var reassignPanel;
					
					var onReassignButtonClick = function (layer, args)
					{
						var peopleFinder = Alfresco.util.ComponentManager.get(htmlid + "-peopleFinder");
						//var reassignPanel = Alfresco.util.ComponentManager.get(htmlid + "-reassignPanel");
						peopleFinder.clearResults();
						reassignPanel.show();
					};
					  
					elCell.innerHTML='';
					var actionsDiv = document.createElement('div');
					actionsDiv.className ="actions";
					
					elCell.appendChild(actionsDiv)
					
					//var buttonsHTML = '<div class="actions" id="'+htmlid+'">'
					var claimable = oRecord._oData.claimable;
					
					actionsDiv.id=htmlid;
					if (claimable=="true") {
						var claimButton = new YAHOO.widget.Button({
							type: "button",
							container: htmlid,
							label: Alfresco.util.message("button.claim"),
							onclick: {
								fn: onClaimButtonClick,
							},
						});
						//Alfresco.util.createYUIButton(actionsDiv, "claim", this.onClaimButtonClick1, [], button);
						Dom.removeClass(Selector.query(".actions .claim", htmlid), "hidden");
						
						//buttonsHTML+='<span class="claim" id="'+htmlid+'-claim-span"><button id="'+htmlid+'-claim">'+Alfresco.util.message("button.claim")+'</button></span> ';
					}
					
					var reassignable = oRecord._oData.reassignable;
					if (reassignable=="true") {
						var reassignButton = new YAHOO.widget.Button({
							type: "button",
							container: htmlid,
							label: Alfresco.util.message("button.reassign"),
							onclick: {
								fn: onReassignButtonClick,
							},
						});
						Dom.removeClass(Selector.query(".actions .reassign", htmlid), "hidden");
						//buttonsHTML+='<span class="reassign" id="'+htmlid+'-reassign-span"><button id="'+htmlid+'-reassign">'+Alfresco.util.message("button.reassign")+'</button></span> ';
					}
					
					var releasable = oRecord._oData.releasable;
					if (releasable=="true") {
						var releaseButton = new YAHOO.widget.Button({
							type: "button",
							container: htmlid,
							label: Alfresco.util.message("button.release"),
							onclick: {
								fn: onReleaseButtonClick,
							},
						});
						Dom.removeClass(Selector.query(".actions .release", htmlid), "hidden");
						//buttonsHTML+='<span class="release" id="'+htmlid+'-release-span"><button id="'+htmlid+'-release">'+Alfresco.util.message("button.release")+'</button></span>';
					}
					elCell.innerHTML+='<div style="display: none;"> <div id="'+htmlid+'-reassignPanel" class="task-edit-header reassign-panel"> <div class="hd">'+Alfresco.util.message("panel.reassign.header")+'</div> <div class="bd"> <div style="margin: auto 10px;"> <div id="'+htmlid+'-peopleFinder"></div> </div> </div> </div> </div>';
						
						
					Alfresco.util.Ajax.request(
					{
						url: Alfresco.constants.URL_SERVICECONTEXT + "components/people-finder/people-finder",
						dataObj:
						{
							htmlid: htmlid + "-peopleFinder"
						},
						successCallback:
						{
							fn: function (response) {
								var finderDiv = Dom.get(htmlid+'-peopleFinder');
								finderDiv.innerHTML = response.serverResponse.responseText;

								// Create the Assignee dialog
								reassignPanel = Alfresco.util.createYUIPanel(htmlid + "-reassignPanel");

								// Find the People Finder by container ID
								var peopleFinder = Alfresco.util.ComponentManager.get(htmlid + "-peopleFinder");

								// Set the correct options for our use
								peopleFinder.setOptions(
								{
									singleSelectMode: true,
									addButtonLabel: Alfresco.util.message("button.select")
								});

								// Make sure we listen for events when the user selects a person
								YAHOO.Bubbling.on("personSelected", onPersonSelected, this);
							} 
						},
						failureMessage: "Could not load People Finder component",
						execScripts: true
					});
					//var onPeopleFinderLoaded = 
					var onPersonSelected = function(e, args)
					{
						// This is a "global" event so we ensure the event is for the current panel by checking panel visibility.
						var peopleFinder = Alfresco.util.ComponentManager.get(htmlid + "-peopleFinder");
						//var reassignPanel = Alfresco.util.ComponentManager.get(htmlid + "-reassignPanel");
						if (Alfresco.util.hasEventInterest(peopleFinder, args))
						{
							reassignPanel.hide();
							updateTaskProperties(
							{
								"cm_owner": args[1].userName
							}, "reassign");
						}
					};
				};
				this.subscribe('renderEvent', move, this, true);
			}
		},
        
        taskAttachments: function() {
            return function(elCell, oRecord, oColumn, oData) {
                if(!oData || oData.length == 0) {
                    elCell.innerHTML = "";
                    return;
                }
                elCell.innerHTML = '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'citeck/images/attachment-16.png" />'
            }
        },

        taskLink: function() {
            return function(elCell, oRecord, oColumn, sData) {
                if(!sData) {
                    elCell.innerHTML = "";
                    return;
                }
                elCell.innerHTML = '<a title="' + Alfresco.util.message("button.view.detailed") + '" href="' + Alfresco.constants.URL_PAGECONTEXT + 'task-details?taskId=' + sData + '" target="_blank">' + 
                    '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'citeck/images/task-16.png"></a>';
            }
        },

        lastTask: function(isDisplayOnlyOneActor) {
            var cache = typeNameCache;
            return function(elCell, oRecord, oColumn, sData) {
                function getOwner() {
                    var owner = '';
                    var assignee = sData['wfm:assignee'];
                    var actors = sData['wfm:actors'];
                    if (assignee) {
						owner = (assignee['type'] === "cm:person") ? assignee['cm:lastName'] + ' ' + assignee['cm:firstName'] : assignee['cm:authorityDisplayName'] || assignee['cm:authorityName'] || assignee.displayName;
					} else if (actors && actors.length != 0) {
						if (isDisplayOnlyOneActor){
							if(actors[0]) {
								var actor = actors[0];
								owner = (actor.type == "cm:person") ? actor['cm:lastName'] + ' ' + actor['cm:firstName'] : actor['cm:authorityDisplayName'] || actor['cm:authorityName'] || actor.displayName;
							}
						}else {
							owner = actors.reduce(function (actorsList, actor) {
								return actorsList += (actorsList ? ", " : "") +
									(actor.type == "cm:person" ? actor['cm:lastName'] + ' ' + actor['cm:firstName'] : actor['cm:authorityDisplayName'] || actor['cm:authorityName'] || actor.displayName);
							}, owner);
						}
                    }
                    return owner;
                }

                function setType(id, type) {
                    var element = $('#' + id).get(0);
                    if (element) {
                        element.innerHTML = " (" + type + ")";
                    }
                }

                if (!sData) return;

                elCell.innerHTML = '';
                if (sData['bpm:status'] == "Not Yet Started") {
                    var taskId = _.uniqueId('last-task-type-');
                    elCell.innerHTML = getOwner() + '<span id="' + taskId + '"></`span>';

                    if(cache[sData.type]) {
                        elCell.innerHTML = getOwner() + " (" + cache[sData.type] + ")";
                    } else {
                        Alfresco.util.Ajax.jsonGet({
                            url: Alfresco.constants.PROXY_URI + "api/classes/" + sData.type.replace(':', '_'),
                            successCallback: {
                                fn: function (response) {
                                    setType(taskId, cache[sData.type] = response.json.title);
                                }
                            },
                            failureCallback: {
                                fn: function (response) {
                                    setType(taskId, cache[sData.type] = sData.type);
                                }
                            }
                        });
                    }
                }

            }
        },

		overdueNodeRef: function(propertyName) {
			return function(elCell, oRecord, oColumn, sData) {
				if(!sData) {
					elCell.innerHTML = "";
					return;
				}
                else
                {
                    Alfresco.util.Ajax.request({
                        url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=" + oRecord._oData.nodeRef,
                        successCallback: {
                            scope: this,
                            fn: function(response) {
                                if (response.json && response.json.props) {
                                    propValue = response.json.props[propertyName];
                                    var now = new Date();
                                    if(propValue && propValue==true)
                                    {
                                            Dom.addClass(elCell.parentElement.parentElement, "yui-overdue-node");
                                            var currentElement = elCell.parentElement.nextElementSibling;
                                            while(currentElement!=null)
                                            {
                                                if(currentElement.children[0] && currentElement.children[0].children[0] && currentElement.children[0].children[0].localName=="a")
                                                {
                                                    for(var i=0; i<currentElement.children[0].children.length; i++)
                                                    {
                                                        Dom.addClass(currentElement.children[0].children[i], "yui-overdue-node");
                                                    }
                                                }
                                                currentElement=currentElement.nextElementSibling;
                                            }
                                    }
                                }
                            }
                        }, failureCallback: { scope: this, fn: function(response) {} }, execScripts: true
                    });
                }
                elCell.innerHTML = sData;
			};
		},

		onFailure : function(response) {
			var failure = Alfresco.util.message("message.failure");
			var errorMsg = failure;
			if (response.json && response.json.message)
				errorMsg = response.json.message;
			Alfresco.util.PopupManager.displayPrompt({
				title : failure,
				text : errorMsg
			});
		},

	// TODO move to external module in maxxium project
        maxxiumAddressFormatter : function(prefix) {
            return function(elCell, oRecord, oColumn, sData) {
                var index = oRecord.getData("attributes['" + prefix + "Index']");
                var region = oRecord.getData("attributes['" + prefix + "Region']");
                var city = oRecord.getData("attributes['" + prefix + "City']");
                var street = oRecord.getData("attributes['" + prefix + "Street']");
                var house = oRecord.getData("attributes['" + prefix + "House']");
                elCell.innerHTML  = (region ? region : '') + (city ? ', ' + city : '') + (street ? ', ' + street : '') + (house ? ', ' + house : '') + (index ? ', ' + index : '');
            };
        },

        propertyDisplayName: function(key) {
            return function(elCell, oRecord, oColumn, sData) {
                if (!sData) return;
				var doc = oRecord.getData(key);
				
                elCell.innerHTML = '';
                        Alfresco.util.Ajax.jsonGet({
                            url: Alfresco.constants.PROXY_URI + "api/classes/" + doc.type.replace(':', '_'),
                            successCallback: {
                                fn: function (response) {
									if(response.json.properties[sData.shortQName])
									{
										elCell.innerHTML = response.json.properties[sData.shortQName].title;
									}
									else
									{
										if(response.json.associations[sData.shortQName])
										{
											elCell.innerHTML = response.json.associations[sData.shortQName].title;
										}
										else
										{
											if(response.json.childassociations[sData.shortQName])
											{
												if(sData.shortQName=='cm:contains')
												{
													elCell.innerHTML = Alfresco.util.message('dochist.assoc.contains');
												}
												else
												{
													elCell.innerHTML = response.json.childassociations[sData.shortQName].title;
												}
											}
											else
											{
												elCell.innerHTML = sData.shortQName;
											}
										}
									}
                                }
                            },
                            failureCallback: {
                                fn: function (response) {
                                    elCell.innerHTML = sData.shortQName;
                                }
                            }
                        });
                }

        },
		
        fieldsListFormatter : function(fields, separator) {
            if (!fields) {
                fields = [];
            }
            return function(elCell, oRecord, oColumn, sData) {
                var cellHtml = '';
                for(var i = 0; i < fields.length; i++) {
                    var field = fields[i];
                    var fieldValue = oRecord.getData("attributes['" + field + "']") ? oRecord.getData("attributes['" + field + "']") : '';
                    cellHtml += cellHtml? separator + fieldValue : fieldValue;
                }
                elCell.innerHTML = cellHtml;
            };
        },

        historyChanges : function() {
            return function(elCell, oRecord, oColumn, sData) {
				var taskType = oRecord.getData('attributes["event:taskType"]');
				var wfType = oRecord.getData('attributes["event:workflowType"]');
				var propertyName = oRecord.getData('attributes["event:propertyName"]');
				if(taskType)
				{
					Alfresco.util.Ajax.jsonGet({
						url: Alfresco.constants.PROXY_URI + "api/classes/" + taskType.shortQName.replace(':','_'),
						successCallback: {
							fn: function(response) {
								elCell.innerHTML = response.json.title;
							}
						},
                        failureCallback: {
                            fn: function(response) {
                                elCell.innerHTML = taskType.shortQName;
                            }
                        }
					});
				}
				else 
				{
					if(wfType)
					{
						Alfresco.util.Ajax.jsonGet({
							url: Alfresco.constants.PROXY_URI + "api/workflow-definitions",
							successCallback: {
								fn: function(response) {
									var data = response.json.data;
									for(var i=0; i<data.length; i++)
									{
										if(data[i].name==wfType)
										{
											elCell.innerHTML = data[i].title;
											break;
										}
									}
								}
							},
							failureCallback: {
								fn: function(response) {
									elCell.innerHTML = '';
								}
							}
						});
					}
					else
					{
						if(propertyName)
						{
							var doc = oRecord.getData('attributes["event:document"][0]');
							Alfresco.util.Ajax.jsonGet({
								url: Alfresco.constants.PROXY_URI + "api/get-all-properties",
								successCallback: {
									fn: function (response) {
										var data = response.json;
										for(var i=0; i<data.length; i++)
										{
											if(data[i].prefixedName==propertyName.shortQName)
											{
												elCell.innerHTML = data[i].title;
												break;
											}
										}
										if(elCell.innerHTML=='')
										{
											if(propertyName.shortQName=='cm:contains')
											{
												var targetNodeKind = oRecord.getData('attributes["event:targetNodeKind"]');
												var targetNodeType = oRecord.getData('attributes["event:targetNodeType"]');
												if(targetNodeKind)
												{
													elCell.innerHTML = targetNodeKind.displayName;
												}
												else
												if(targetNodeType)
												{
													elCell.innerHTML = targetNodeType.displayName;
												}
												else
													elCell.innerHTML = Alfresco.util.message('dochist.assoc.contains');
											}
											else
											{
												Alfresco.util.Ajax.jsonGet({
													url: Alfresco.constants.PROXY_URI + "api/get-all-association",
													successCallback: {
														fn: function (response) {
															var data = response.json;
															for(var i=0; i<data.length; i++)
															{
																if(data[i].prefixedName==propertyName.shortQName)
																{
																	elCell.innerHTML = data[i].title;
																	break;
																}
															}
														}
													},
													failureCallback: {
														fn: function (response) {
															elCell.innerHTML = propertyName.shortQName;
														}
													}
												});
											}
										}
									}
								},
								failureCallback: {
									fn: function (response) {
										elCell.innerHTML = propertyName.shortQName;
									}
								}
							});
						}
					}
				}
            };
        },

		userAssocActions: function(sourceRef, assocTypes) {
			return function(elCell, oRecord, oColumn, sData) {
				var targetRef = oRecord.getData("nodeRef");
				var recordUserId = oRecord.getData('attributes["cm:userName"]');
				var panelId = "yui-actions-non-content-buttonsPanel-" + oRecord.getCount();
				var userId = Alfresco.constants.USERNAME;
				elCell.innerHTML = '';
				var div = document.createElement("div");
				div.id = panelId;
				elCell.appendChild(div);
				/**
				 * @type_evn type_evn is type of event. It will be sent through fire-event and it will be a part of
				 *  the css-class name.
				 * */
				var createButton = function(pnl, type_evn, className, action) {
					var btnTag = document.createElement('div');
					btnTag.className = "btn-" + type_evn + " " + className;
					btnTag.onclick = action;
					btnTag.style = "width:auto;";
					pnl.appendChild(btnTag);
					return btnTag;
				};

				var buttonRemoveAction = function() {
					Alfresco.util.PopupManager.displayPrompt({
						title: Alfresco.util.message("message.confirm.delete.1.title", 1),
						text: Alfresco.util.message("message.confirm.delete"),
						noEscape: true,
						buttons: [
							{
								text: Alfresco.util.message("actions.button.ok"),
								handler: function dlA_onActionOk()
								{
									Alfresco.util.Ajax.request({
										url: Alfresco.constants.PROXY_URI + "citeck/remove-assocs?sourceRef="+sourceRef+
																			"&targetRef="+targetRef+"&assocTypes="+assocTypes,
										method: Alfresco.util.Ajax.DELETE,
										successCallback: {
											fn: function (response) {
												YAHOO.Bubbling.fire("metadataRefresh");
											},
											scope: this
										},
										failureMessage: Alfresco.util.message("message.delete.failure", "", targetRef),
										scope: this
									});
									this.destroy();
								}
							},
							{
								text: Alfresco.util.message("actions.button.cancel"),
								handler: function dlA_onActionCancel()
								{
									this.destroy();
								},
								isDefault: true
							}]
					});
				};

				if(sourceRef && targetRef && assocTypes) {

					var hasPermissionUrl = Alfresco.constants.PROXY_URI +
											'citeck/has-permission?nodeRef=' +
											sourceRef + '&permission=Write';

					YAHOO.util.Connect.asyncRequest(
						'GET',
						hasPermissionUrl, {
							success: function (response) {
								if (response.responseText.trim() == "true" || userId == recordUserId) {
									var remove_btn = createButton(div, "remove", "remove-link", buttonRemoveAction);
									remove_btn.setAttribute("title", Alfresco.util.message('title.table-children.removeItem'));
								}
							},
							scope: this
						}
					);
				}
			}
		},


		assocOrProps: function(props) {
			if (!props) props = "cm:name";

			return function(elCell, oRecord, oColumn, sData) {
				var request = function(nodeRef) {
					Alfresco.util.Ajax.request({
						url: Alfresco.constants.PROXY_URI + "citeck/node?nodeRef=" + nodeRef + "&props=" + props + "&replaceColon=_",
						successCallback: {
							fn: function(response) {
								if (response.json && response.json.props) {
									if (elCell.innerHTML) elCell.innerHTML += "<br>";
									elCell.innerHTML += _.values(response.json.props).join(", ");
								}
							}
						},
						failureCallback: {
							fn: function(response) {
								elCell.innerHTML = sData;
							}
						},
						execScripts: true
					});
				}

				if (!sData || !(sData instanceof Object || sData instanceof String)) {
					elCell.innerHTML = "";
					return;
				}

				if (sData instanceof String) {
					if (sData.indexOf("workspace") != -1) { request(sData) } 
					else if (/^-?\d*(\.\d+)?$/.test(sData)) { elCell.innerHTML = parseFloat(sData) }
					else { elCell.innerHTML = sData }
				}

				if (sData instanceof Object) {
					var renderRequest = function(object) {
						if (_.has(object, "nodeRef")) { 
							request(object.nodeRef) 
						} else {
							if (elCell.innerHTML) elCell.innerHTML += "<br>";
							elCell.innerHTML = _.values(object).join(", ") 
						}
					};

					if (sData instanceof Array) {
						for (var d = 0; d < sData.length; d++) {
							if (sData[d]) renderRequest(sData[d]);
						}
					} else { renderRequest(sData); }
				} 

					};

					if (sData instanceof Array) {
						for (var d = 0; d < sData.length; d++) {
							if (sData[d]) renderRequest(sData[d]);
						}
					} else { renderRequest(sData); }
				}

			};
	},

		// change property to another property if original is not exist
		replaceable: function(attributeName, formatter, direction) {
			return function (elCell, oRecord, oColumn, sData) {
				var anotherAttribute = oRecord.getData(attributeName);

				if ((direction || sData == undefined) && anotherAttribute) {
					if (formatter.another)  {
						formatter.another(elCell, oRecord, oColumn, anotherAttribute);
						return;
					}

					elCell.innerHTML = anotherAttribute;
					return;
				}

				if (formatter.original) {
					formatter.original(elCell, oRecord, oColumn, sData);
					return					
				}

				elCell.innerHTML = sData;
				return
			}
		}

	});

})();

