/**
 * DocumentJournal document-details component.
 * @class Citeck.widget.DocumentJournal
 */

if (typeof Citeck == "undefined" || !Citeck) var Citeck = {};
if (typeof Citeck.widget == "undefined" || !Citeck.widget) Citeck.widget = {};

(function() {

  /**
   * YUI Library aliases
   */

  var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Element = YAHOO.util.Element;

  /**
   * DocumentJournal constructor
   */

  Citeck.widget.DocumentJournals = function(htmlId) {
    Citeck.widget.DocumentJournal.superclass.constructor.call(this, "Citeck.widget.DocumentJournals", htmlId, ["event","button"]);
  };

  YAHOO.extend(Citeck.widget.DocumentJournals, Alfresco.component.Base, {

    options: { },

    onReady: function() {

    },
      
  });

})();
