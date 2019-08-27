use( "Log" );

var PERFTEST_FLAG = "dbcPerfTest=true";

// Log example:
// {"timestamp":"2019-06-14T09:56:50.076+00:00","version":"1","message":"/search performed with query: \"Peter Handke\", field: author, exact: true, merge_workid: true, rows: 200","logger":"dk.dbc.laesekompas.suggester.webservice.SearchResource","thread":"http-thread-pool::http-listener(1)","level":"INFO","level_value":20000,"mdc":{"merge_workid":"true","requestType":"search","field":"author","query":"\"Peter Handke\"","exact":"true","rows":"200"}}
// {"timestamp":"2019-06-14T10:19:14.949+00:00","version":"1","message":"suggestion performed with query: kjærs, collectcion: ALL","logger":"dk.dbc.laesekompas.suggester.webservice.SuggestResource","thread":"http-thread-pool::http-listener(2)","level":"INFO","level_value":20000,"mdc":{"requestType":"suggest","query":"kjærs","collection":"suggest-all"}}

var lineFilter =  function (line) {
    var data = JSON.parse(line)
    var timestamp = data["@timestamp"]
    var message = data.message
    var mdc = data.mdc

    Log.debug( "Entering lineFilter. Timestamp:", timestamp, ", message:'", message, "', mdc:", mdc);

    if (mdc === "") {
        Log.debug( "lineFilter. No mdc in line");
        return ;
    }

    if (! 'requestType' in mdc) {
        Log.debug( "Leaving lineFilter. Not a request. Skipped");
        return;
    }
    Log.debug( "lineFilter. mdc:", JSON.stringify(mdc));

    var requestType = mdc['requestType'];
    Log.debug( "lineFilter. requestType:", requestType);

    if (! 'query' in mdc) {
        Log.error( "Leaving lineFilter. Request is missing query. Skipped");
        return;
    }
    var query = mdc['query'];
    Log.debug( "lineFilter. query:", query);

    // Base request for suggest & search
    var constructedQuery = "/" + requestType + "?query=" + encodeURI(query)
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
            "app": "",
            "query": constructedQuery
        };
    Log.debug( "lineFilter. result:", result);
    return result;
};
