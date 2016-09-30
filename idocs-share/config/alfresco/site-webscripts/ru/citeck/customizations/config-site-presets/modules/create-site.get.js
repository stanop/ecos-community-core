var sitePresets = [];
var presetsCfg = config.scoped["SurfPresets"]["site-presets"].children;
for(var i = 0; i < presetsCfg.size(); i++) {
	var presetCfg = presetsCfg.get(i);
	sitePresets.push({
		id: presetCfg.attributes.id,
		name: msg.get(presetCfg.attributes.name)
	});
}
model.sitePresets = sitePresets;