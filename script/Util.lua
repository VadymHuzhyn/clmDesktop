local json = require "script.Json"

function getJSON(file, name)
	dofile(file)
	return json.encode(_G[name])
end