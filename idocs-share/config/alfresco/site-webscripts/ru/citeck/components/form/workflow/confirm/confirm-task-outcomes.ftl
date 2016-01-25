<#if form.data.prop_wfcf_canConfirmWithComments!false>
	<#assign outcomes = [ "Confirmed", "ConfirmedWithComment", "Reject" ] />
<#else/>
	<#assign outcomes = [ "Confirmed", "Reject" ] />
</#if>
<#assign outcomePrefix = form.data.prop_wfcf_confirmOutcomeI18nPrefix!"" />
<#if outcomePrefix != "">
	<#assign outcomeLabels = {} />
	<#list outcomes as outcome>
		<#assign outcomeLabels = outcomeLabels + {
			outcome : msg(outcomePrefix + outcome)
		} />
	</#list>
</#if>