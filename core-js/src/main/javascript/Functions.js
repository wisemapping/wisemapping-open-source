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
        var stack;
        try {
            null.eval();
        } catch (e) {
            stack = e;
        }
        console.log(message + "," + stack);
        window.errorStack = stackTrace();
        throw message;
    }
};

Math.sign = function (value) {
    return (value >= 0) ? 1 : -1;
};

function stackTrace(exception) {

    if (!$defined(exception)) {
        try {
            throw "Dummy Exception"
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
        var currentFunction = arguments.callee.caller;
        while (currentFunction) {
            var fn = currentFunction.toString();
            result = result + "\n" + fn;
            currentFunction = currentFunction.caller;
        }
    }

    return result;
}

// Support for Windows ...
if (!window.console) {
    console = {
        log:function (e) {

        }
    };
}