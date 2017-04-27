(function() {

var presetType = url.templateArgs.type;

var presetsCfg = config.scoped["SurfPresets"][presetType + "-presets"];
if(!presetsCfg) {
	status.setCode(status.STATUS_NOT_FOUND, "Presets of type '" + presetType + "' not found");
	return;
}

var presets = [];
for(var i = 0; i < presetsCfg.children.size(); i++) {
	var preset = presetsCfg.children.get(i);
	presets.push({
		id: preset.attributes.id,
		name: preset.attributes.name
	});
}

model.presetType = presetType;
model.presets = presets;

})();