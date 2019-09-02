use( "Log" );

var PERFTEST_FLAG = "dbcPerfTest=true";
var SERVICE_NAME  = "suggester-laesekompas-webservice";

var lineFilter =  function (line) {
    var data = JSON.parse(line);
    var timestamp = data["@timestamp"];
    var message = data.message;
    var mdc = data.mdc;
    var name = data["sys_kubernetes"]["labels"]["app"]["kubernetes"]["io/name"]
    
    Log.debug( "Entering lineFilter. Timestamp:", timestamp, ", message:'", message, "'");

    if( typeof name === "undefinded" || name === "" ) {
        Log.debug("lineFilter. name is undefinded or empty");
        return;
    }

    if( name !== SERVICE_NAME ) {
        Log.debug("lineFilter. name is not " + SERVICE_NAME);
        return;
    }

    if( typeof mdc === "undefined" || mdc === "" ) {
        Log.debug("lineFilter. mdc is undefinded or empty");
        return;
    }

    if( typeof mdc !== "object") {
        Log.debug( "lineFilter. mdc is not of type Object");
        return
    }

    if (! ('requestType' in mdc)) {
        Log.debug( "Leaving lineFilter. Not a request. Skipped");
        return;
    }
    Log.debug( "lineFilter. mdc:", JSON.stringify(mdc));

    var requestType = mdc['requestType'];
    Log.debug( "lineFilter. requestType:", requestType);

    if (! ('query' in mdc)) {
        Log.error( "Leaving lineFilter. Request is missing query. Skipped");
        return;
    }
    var query = mdc['query'];
    Log.debug( "lineFilter. query:", query);

    // Base request for suggest & search
    var constructedQuery = "/" + requestType + "?query=" + encodeURI(query);
    switch ( requestType ) {
        case "suggest":
            // mdc:{"requestType":"suggest","query":"kjærs","collection":"suggest-all"}
            // /suggest{/e_book|audio_book}?query={q}
            // TODO: {/e_book|audio_book} ???
            Log.debug( "lineFilter. requestType suggest, mdc: " + JSON.stringify(mdc));
            break;
        case "search":
            // "mdc":{"merge_workid":"true","requestType":"search","field":"author","query":"\"Peter Handke\"","exact":"true","rows":"200"}}
            // /search?query={q}&field={author|title|workid|pid optional}&exact={optional default: false}&rows={optional, default: 10}
            Log.debug( "lineFilter. requestType search, mdc: " + JSON.stringify(mdc));

            if ('field' in mdc) {
                var field = mdc['field'];
                Log.debug( "lineFilter. field: " + field);
                constructedQuery += "&field=" + field;
            }
            if ('rows' in mdc) {
                var rows = mdc['rows'];
                Log.debug( "lineFilter. rows: " + rows);
                constructedQuery += "&rows=" + rows
            }

            break;
        default:
            Log.error( "Leaving lineFilter. unknown requestType:", requestType, ", mdc: " + JSON.stringify(mdc));
            return;
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
