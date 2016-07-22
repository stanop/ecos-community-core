define(["dojo/dom-class"], function(domClass) {
  return {

    visibilityByWindowSizeEventSubscription: function(elementId, size, executeNow) {
      if (elementId && size) {
        var minWidth = size.minWidth || 0,
            maxWidth = size.maxWidth || screen.width;

        window.addEventListener("resize", function(event) {
          if (window.innerWidth >= minWidth && window.innerWidth <= maxWidth) {
              if (domClass.contains(elementId, "hidden")) {
                domClass.remove(elementId, "hidden");
              }
          } else if (window.innerWidth < minWidth || window.innerWidth > maxWidth) {
              if (!domClass.contains(elementId, "hidden")) {
                domClass.add(elementId, "hidden");
              }
          }
        });

        if (executeNow) {
          // TODO: trigger event
        }
      }
    }

  };
});
