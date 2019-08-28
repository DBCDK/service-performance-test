use( "Log" );

var PERFTEST_FLAG = "dbcPerfTest=true";

var lineFilter =  function (text) {
    
    var data = JSON.parse(text)
    var timestamp = data.timestamp;
    var app = data.app;
    var message = data.message;
    
    Log.trace( "Entering lineFilter. Timestamp:", timestamp, ", app:", app, ", message:", message );

    var msg = message.split(/\s+/);
    var parts = {};

    for (var i = 0; i < msg.length; i++) {
        var part = msg[i];
        //Log.trace( "lineFilter. part:", part);

        var separator = part.indexOf("=");
        if (separator > -1) {
            var key = part.substring(0, separator);
            var value = part.substring(separator+1);
            //Log.trace( "lineFilter. ", key, "=", value);
            parts[key] = value;
        }
    }
    Log.debug( "lineFilter. message map:", JSON.stringify(parts));

    var path = parts["path"];
    var logpath = Log.safeValue(path);
    Log.debug( "path: ", logpath);
    if (path === undefined || path !== "/select") {
        Log.debug( "lineFilter. path not select: ", logpath);
        return undefined;
    }

    var params = parts["params"];
    if (params === undefined || params === "") {
        return undefined;
    }

    var paramsString = params.substring(1, params.length - 1);
    //Log.debug( "lineFilter. paramsString:", paramsString);

    var paramsStringMatcher = "&" + paramsString + "&";
    if (paramsStringMatcher.indexOf("&distrib=false&") > -1 ||
        paramsStringMatcher.indexOf("&" + PERFTEST_FLAG + "&") > -1) {
        return undefined;
    }

    var query;
    if( paramsString.indexOf("child+of") !== -1 ) {
        query = ( paramsString + "&" + PERFTEST_FLAG ).replaceFirst("&trackingId=[^&]*&", "&");
    }
    else {
        query = encodeURI(( paramsString + "&" + PERFTEST_FLAG ).replaceFirst("&trackingId=[^&]*&", "&"));
    }
    Log.debug( "lineFilter. result:", result);
    var result = 
    {
        "timestamp": timestamp,
        "app": app,
        "query": query
    };
    return result;
};
