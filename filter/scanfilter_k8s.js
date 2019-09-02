use( "Log" );

var PERFTEST_FLAG = "dbcPerfTest=true";
var SERVICE_NAME  = "datawell-scan-service";

var lineFilter =  function (line) {
    var data = JSON.parse(line);
    var timestamp = data["@timestamp"];
    var message = data.message;
    var name = data["sys_kubernetes"]["labels"]["app"]["kubernetes"]["io/name"]

    Log.trace( "Entering lineFilter. Timestamp:", timestamp, ", message:", message );

    if( typeof name === "undefinded" || name === "" ) {
        Log.debug("lineFilter. name is undefinded or empty");
        return;
    }

    if( name !== SERVICE_NAME ) {
        Log.debug("lineFilter. name is not " + SERVICE_NAME);
        return;
    }

    var regex = /RequestParam{.*}/g;
    var msg = message.match(regex);
    if( typeof msg === "undefined" || msg === null || msg === "" ) {
        Log.debug("lineFilter. Could not find RequestParam part of message");
        return;
    }
    msg = msg[0].replace("RequestParam{", "").replace(/}$/, "");

    var queryString = msg.replace(/,\s+/g, "&");
    Log.debug( "lineFilter. queryString:", queryString);

    // distrib=false is solr's distributed queries
    // skip them.
    var queryStringMatcher = "&" + queryString + "&";
    if (queryStringMatcher.indexOf("&distrib=false&") > -1 ||
        queryStringMatcher.indexOf("&" + PERFTEST_FLAG + "&") > -1) {
        return undefined;
    }

    var constructedQuery = "";
    if( queryString.indexOf("child+of") !== -1 ) {
        constructedQuery = ( queryString + "&" + PERFTEST_FLAG ).replaceFirst("&trackingId=[^&]*&", "&");
    }
    else {
        constructedQuery = encodeURI(( queryString + "&" + PERFTEST_FLAG ).replaceFirst("&trackingId=[^&]*&", "&"));
    }
    var result =
        {
            "timestamp": timestamp,
            "app": SERVICE_NAME,
            "query": constructedQuery
        };
    Log.debug( "lineFilter. result:", JSON.stringify(result));
    return result;
};
