(function(){

	try {
		var pageParams = json.get("pageParams");
		var regExp = /nodeRef":"(.*)"/;
		var nodeRef = pageParams.match(regExp)[1];
		var content = json.get("content");
		var nodeDetailsLink = (json.has("site") && json.has("itemTitle") && json.has("page"))?
			'page/site/' + json.get('site') + '/card-details?nodeRef=' + nodeRef :
			'page/card-details?nodeRef=' + nodeRef;
		var subscribersString = json.has("subscribers") ? json.get("subscribers") : "";

		scriptCommentService.onAddComment(
			nodeRef,
			content,
			nodeDetailsLink,
			person.properties.userName,
			subscribersString
		);
		model.code = 200;
	} catch (e) {
		model.code = 500;
		model.message = e.message;
	}
})();