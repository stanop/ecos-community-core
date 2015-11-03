<#assign params = viewScope.region.params!{} />

<#-- TODO show validation messages in control -->

<button data-bind="click: function() {
    var generator = ko.computed(function() { 
        return enumeration.getNumber('${params.template}', node()); 
    }, this, { deferEvaluation: true });

    var number = generator();
    if (number) {
        value(number);
        generator.dispose();
    } else {
        koutils.subscribeOnce(generator, function(number) { 
            value(number);
            generator.dispose();
        });
    }
}, disable: protected">${msg('button.generate')}</button>