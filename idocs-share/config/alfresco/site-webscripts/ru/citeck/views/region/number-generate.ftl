<#assign params = viewScope.region.params!{} />
<#assign mode = params.mode!"button" />

<!-- ko component: { name: "number-generate", params: {
    label: "${msg('button.generate')}",       
    generator: function() { 
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
    },
    disable: protected,
    mode: "${mode}"
}} --><!-- /ko -->