/**
 * Patch for default alfresco method 'createYUIPanel' to override
 * loading behaviour: create panel only when it is required
 * */
define([], function () {

    function LazyPanel(createPanelImpl, p_el, p_params, p_custom) {

        let panelImpl = null;
        let hideEventSubscribers = [];
        let options = null;
        let messages = null;

        let initPanel = function () {
            if (!panelImpl) {
                panelImpl = createPanelImpl.call(null, p_el, p_params, p_custom);
                let event = panelImpl.hideEvent;
                for (let i = 0; i < hideEventSubscribers.length; i++) {
                    event.subscribe.apply(event, hideEventSubscribers[i])
                }
                hideEventSubscribers = null;
                if (options != null) {
                    panelImpl.setOptions.apply(panelImpl, options);
                    options = null;
                }
                if (messages != null) {
                    panelImpl.setMessages.apply(panelImpl, messages);
                    messages = null;
                }
            }
        };

        this.unsubscribeAll = function () {
            if (panelImpl != null) {
                panelImpl.unsubscribeAll.apply(panelImpl, arguments);
            } else {
                hideEventSubscribers = [];
            }
        };

        this.setOptions = function () {
            options = arguments;
        };

        this.setMessages = function () {
            messages = arguments;
        };

        this.hideEvent = {
            subscribe: function () {
                if (panelImpl) {
                    let event = panelImpl.hideEvent;
                    event.subscribe.apply(event, arguments)
                } else {
                    hideEventSubscribers.push(arguments);
                }
            }
        };

        this.render = function () {
            initPanel();
            if (!panelImpl._rendered) {
                panelImpl.render.apply(panelImpl, arguments);
            }
        };

        this.show = function () {
            initPanel();
            panelImpl.show();
        };

        this.hide = function () {
            panelImpl.hide();
        };

        this.setFirstLastFocusable = function () {
            initPanel();
            panelImpl.setFirstLastFocusable.apply(panelImpl, arguments);
        };

        Object.defineProperty(this, '_rendered', {
            get: function () {
                return panelImpl != null && panelImpl._rendered;
            },
            enumerable: false
        });
    }

    function customCreateYUIPanel(createPanelImpl) {

        return function (p_el, p_params, p_custom) {
            if (!p_custom || !p_custom["render"]) {
                return new LazyPanel(createPanelImpl, p_el, p_params, p_custom);
            } else {
                return createPanelImpl(p_el, p_params, p_custom);
            }
        };
    }

    if (!Alfresco || !Alfresco.util || !Alfresco.util.createYUIPanel) {
        console.warn("Alfresco.util.createYUIPanel is not defined!");
    } else {
        let impl = Alfresco.util.createYUIPanel;
        Alfresco.util.createYUIPanel = customCreateYUIPanel(impl);
    }
});