define(["dojo/dom-class"], function(domClass) {
  return {

    visibilityByWindowSizeEventSubscription: function(elementId, size, execudeAfterDOMReady) {
      if (elementId && size) {
        var minWidth = size.minWidth || 0,
            maxWidth = size.maxWidth || screen.width,
            self = this;

        window.addEventListener("resize", function(event) {
          self.visibilityByWindowSize(elementId, minWidth, maxWidth);
        });

        if (execudeAfterDOMReady) {
          var readyStateCheckInterval = setInterval(function() {
            if (document.readyState === "complete") {
                clearInterval(readyStateCheckInterval);
                self.visibilityByWindowSize(elementId, minWidth, maxWidth);
            }
          }, 10);
        }
      }
    },

    visibilityByWindowSize: function(elementId, minWidth, maxWidth) {
      if (!elementId) { throw Error("'elementId' does not specified"); }
      if ((!minWidth && minWidth != 0) || (!maxWidth && maxWidth != 0)) {
        throw Error("'minWidth' and/or 'maxWidth' does not specified");
      }
      if (!window) { throw Error("'window' not loaded"); }

      if (window.innerWidth >= minWidth && window.innerWidth <= maxWidth) {
        if (domClass.contains(elementId, "hidden")) {
          domClass.remove(elementId, "hidden");
        }
      } else if (window.innerWidth < minWidth || window.innerWidth > maxWidth) {
        if (!domClass.contains(elementId, "hidden")) {
          domClass.add(elementId, "hidden");
        }
      }
    }

  };
});
