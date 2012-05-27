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

Math.sign = function(value) {
    return (value >= 0) ? 1 : -1;
};

/*
 * DOMParser HTML extension
 * 2012-02-02
 *
 * By Eli Grey, http://eligrey.com
 * Public domain.
 * NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.
 */

/*! @source https://gist.github.com/1129031 */
/*global document, DOMParser*/

(function(DOMParser) {
    "use strict";

    var DOMParser_proto = DOMParser.prototype , real_parseFromString = DOMParser_proto.parseFromString;

    // Firefox/Opera/IE throw errors on unsupported types
    try {
        // WebKit returns null on unsupported types
        if ((new DOMParser).parseFromString("", "text/html")) {
            // text/html parsing is natively supported
            return;
        }
    } catch (ex) {
    }

    DOMParser_proto.parseFromString = function(markup, type) {
        if (/^\s*text\/html\s*(?:;|$)/i.test(type)) {
            var
                doc = document.implementation.createHTMLDocument("")
                , doc_elt = doc.documentElement
                , first_elt
                ;

            doc_elt.innerHTML = markup;
            first_elt = doc_elt.firstElementChild;

            if (// are we dealing with an entire document or a fragment?
                doc_elt.childElementCount === 1
                    && first_elt.localName.toLowerCase() === "html"
                ) {
                doc.replaceChild(first_elt, doc_elt);
            }

            return doc;
        } else {
            return real_parseFromString.apply(this, arguments);
        }
    };
}(DOMParser));