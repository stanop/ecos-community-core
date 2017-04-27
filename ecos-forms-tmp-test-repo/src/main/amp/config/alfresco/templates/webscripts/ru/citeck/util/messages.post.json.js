var array = json.get('keys'), keys = [];
for(var i = 0, ii = array.length(); i < ii; i++) {
    keys.push(array.get(i));
}

model.keys = keys;
