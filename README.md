# performance-test
Tools to run a performance test of a web service


## Tools

The applications (jar) that are produced are:

### Recorder

name: `performance-test-recorder.jar`

This produces an output with lines that looks like this:

    delta query-string

the delta is number of ms between the 1st line in this file was requested, and
current line was requested. The query-string is the content posted to the service

### Replayer

name: `performance-test-replayer.jar`

This takes a file generated from performance-test-recorder.jar and replays it
against an instance of the service. The queries sendt with the actual execution time is recorded 
as json in a file.

Explanation for the sections in the output:<br>
**loglines**      = Statistics about the individual requests<br>
**configuration** = The parameters sendt to the replayer<br>
**callstat**      = Summarized statistics about the calltimes (min/max/avg)<br>
**counter**       = Cound of http returncodes from the requests<br>
**status**        = Program status-codes<br>
                 0 = OK<br>
                 1 = RUNTIME_EXCEEDED<br>
                 2 = CALLTIME_EXCEEDED<br>
                 3 = MAXLINES_EXCEEDED<br>
                 4 = IOERROR<br>

Example output:
```{
   "loglines": [
     {
       "originalTimeDelta": 0,
       "callDelay": 0,
       "callDuration": 1,
       "query": "/suggest?query=bob",
       "status": "200",
       "timestamp": 1560933529081
     },
     {
       "originalTimeDelta": 115470,
       "callDelay": 0,
       "callDuration": 11,
       "query": "/suggest?query=m",
       "status": "200",
       "timestamp": 1560933529084
     },
     {
       "originalTimeDelta": 115571,
       "callDelay": 0,
       "callDuration": 17,
       "query": "/suggest?query=m%C3%A6",
       "status": "200",
       "timestamp": 1560933529084
     },
     {
       "originalTimeDelta": 115693,
       "callDelay": 0,
       "callDuration": 16,
       "query": "/suggest?query=m%C3%A6l",
       "status": "200",
       "timestamp": 1560933529084
     }
   ],
   "configuration": {
     "output": "example.res",
     "input": "./example.out",
     "service": "http://example-service.dbc.dk/api",
     "limit": "9223372036854775807",
     "durationConstraint": "3600000",
     "replayTime": "3600000",
     "callConstraint": "5000/10/100",
     "replay": "0"
   },
   "callStat": {
     "count": 4,
     "sum": 45,
     "min": 0,
     "max": 17,
     "average": 11.00
   },
   "counter": {
     "200": 4
   },
   "status": {
     "code": 0,
     "message": ""
   }
 }
```