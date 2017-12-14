<@filterLib.renderRegion "actions" />
<@filterLib.renderRegion "label" />
<@filterLib.renderRegion "predicate" />
<span class="criterion-value" data-bind="visible: resolve('predicate.needsValue', false)">
<@filterLib.renderRegion "select" />
<@filterLib.renderRegion "input" />
</span>
