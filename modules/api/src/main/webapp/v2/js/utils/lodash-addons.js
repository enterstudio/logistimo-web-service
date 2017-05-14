_.filterWithProperty = function(collection, name, value) {
	var result = _.filter(collection, function(item) {
		return (item[name] === value);
	});
	return (result);
};

_.findWithProperty = function(collection, name, value) {
	var result = _.find(collection, function(item) {
		return (item[name] === value);
	});
	return (result);
};

_.sortOnProperty = function(collection, name, direction) {
	var indicator = ((direction.toLowerCase() === "asc") ? -1 : 1);
	collection.sort(function(a, b) {
		if (a[name] < b[name]) {
			return (indicator);
		} else if (a[name] > b[name]) {
			return (-indicator);
		}
		return (0);
	});
	return (collection);
};
