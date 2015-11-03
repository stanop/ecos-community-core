<#assign el = args.htmlid />

<div id="${el}" style="visibility: hidden; position: absolute" >
	<div id="${el}-dialog">
		<div class="hd">${msg("title.help")}</div>
		<div class="bd">
			<div id="${el}-tabs">
				<ul class="yui-nav">
					<li><a href="#${el}-tab-text"><em>${msg("title.text")}</em></a></li>
					<li><a href="#${el}-tab-video"><em>${msg("title.video")}</em></a></li>
				</ul>
				<div class="yui-content"> 
					<div id="${el}-tab-text"></div> 
					<div id="${el}-tab-video">
						<div id="${el}-player"><p>${msg("message.no-flash")}</p></div>
					</div>
				</div>
			</div>
		</div
	</div>
</div>

<script type="text/javascript">
new Citeck.module.HelpModule("${el}").setOptions({

}).setMessages(${messages});

YAHOO.util.Event.onDOMReady(function() {
	var elems = $("li > a[templateuri]");
	var elemsLength = elems.length;
	for(var i = 0; i < elemsLength; i++) {
		elems[i].parentNode.setAttribute("templateuri", elems[i].getAttribute("templateuri"));
	}
});
</script>


<#--
    <div id="tabs">
	  <ul>
	    <li><a href="#tabs-1">Текстовая информация</a></li>
	    <li><a href="#tabs-2">Видео</a></li>
	  </ul>
	  <div id="tabs-1">
	  </div>
	  <div id="tabs-2">
	    <div id="player">Возможно не включен flash в браузере или неверно указан путь к видео файлу.</div>
	  </div>
	 </div>
-->
