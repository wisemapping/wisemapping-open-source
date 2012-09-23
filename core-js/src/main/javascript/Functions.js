/*
 Function: $defined
 Returns true if the passed in value/object is defined, that means is not null or undefined.

 Arguments:
 obj - object to inspect
 */

$defined = function (obj) {
    return (obj != undefined);
};


$assert = function (assert, message) {
    if (!$defined(assert) || !assert) {
        logStackTrace();
        console.log(message);
        throw new Error(message);
    }
};

Math.sign = function (value) {
    return (value >= 0) ? 1 : -1;
};

function logStackTrace(exception) {

    if (!$defined(exception)) {
        try {
            throw Error("Unexpected Exception");
        } catch (e) {
            exception = e;
        }
    }
    var result = "";
    if (exception.stack) { //Firefox  and Chrome...
        result = exception.stack;
    }
    else if (window.opera && exception.message) { //Opera
        result = exception.message;
    } else {  //IE and Safari
        result = exception.sourceURL + ': ' + exception.line + "\n\n";

        var currentFunction = arguments.callee.caller;
        while (currentFunction) {
            var fn = currentFunction.toString();
            result = result + "\n" + fn;
            currentFunction = currentFunction.caller;
        }
    }
    window.errorStack = result;
    return result;
}

// Support for Windows ...
if (!window.console) {
    console = {
        log:function (e) {

        }
    };
}