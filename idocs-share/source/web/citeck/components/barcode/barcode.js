(function () {
    Citeck = typeof Citeck != "undefined" ? Citeck : {};
    Citeck.widget = Citeck.widget || {};

    Citeck.widget.Barcode = function (htmlid) {
        Citeck.widget.Barcode.superclass.constructor.call(this, "Citeck.widget.Barcode", htmlid, null);

        YAHOO.Bubbling.on("metadataRefresh", this.doRefresh, this);
    };

    YAHOO.extend(Citeck.widget.Barcode, Alfresco.component.Base);

    YAHOO.lang.augmentObject(Citeck.widget.Barcode.prototype, {
        doRefresh: function Barcode_doRefresh() {
            YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);
            this.refresh('/citeck/components/barcode?nodeRef={nodeRef}&property={property}&barcodeType={barcodeType}' +
                '&header={header}&width={width?}&height={height?}&printScale={printScale}&printMargins={printMargins}' +
                '&rnd={rnd}');
        }
    }, true);

})();
