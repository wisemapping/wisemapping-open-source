/*
 Function: $defined
 Returns true if the passed in value/object is defined, that means is not null or undefined.

 Arguments:
 obj - object to inspect
 */

$defined = function(obj) {
    return (obj != undefined);
};


$assert = function(assert, message) {
    if (!$defined(assert) || !assert) {
        var stack;
        try {
            null.eval();
        } catch(e) {
            stack = e;
        }
        console.log(message + "," + stack);
        throw message;
//        wLogger.error(message + "," + stack);
//        core.Logger.logError(message + "," + stack);

    }
};