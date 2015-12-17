<#if form.data.wfcf_canConfirmWithComments!false>
	<#assign outcomes = [ "Confirmed", "ConfirmedWithComment", "Reject" ] />
<#else/>
	<#assign outcomes = [ "Confirmed", "Reject" ] />
</#if>
<#assign outcomePrefix = form.data.wfcf_confirmOutcomeI18nPrefix!"" />
<#if outcomePrefix != "">
	<#assign outcomeLabels = {} />
	<#list outcomes as outcome>
		<#assign outcomeLabels = outcomeLabels + {
			outcome : msg(outcomePrefix + outcome)
		} />
	</#list>
</#if>