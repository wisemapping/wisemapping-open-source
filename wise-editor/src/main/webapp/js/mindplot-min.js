//
//  This is only a mock used for development. In production, it's replaced by the real zip file...
//
function JSPomLoader(pomUrl, callback) {
    console.log("POM Load URL:" + pomUrl);
    var jsUrls;
    var request = new Request({
        url: pomUrl,
        method: 'get',
        onRequest: function() {
            console.log("loading ...");
        },
        onSuccess: function(responseText, responseXML) {

            // Collect JS Urls ...
            var concatRoot = responseXML.getElementsByTagName('includes');
            var fileSetArray = Array.filter(concatRoot[0].childNodes, function(elem) {
                return elem.nodeType == Node.ELEMENT_NODE
            });

            jsUrls = new Array();
            Array.each(fileSetArray, function(elem) {
                    var jsUrl = elem.firstChild.nodeValue;
                    if (jsUrl.indexOf("${basedir}") != -1) {
                        jsUrls.push(pomUrl.substring(0, pomUrl.lastIndexOf('/')) + jsUrl.replace("${basedir}",""));
                    } else {

                        jsUrls.push(pomUrl.substring(0, pomUrl.lastIndexOf('/')) + "/src/main/javascript/" + jsUrl);
                    }
                }
            );

            // Load all JS dynamically ....
            jsUrls = jsUrls.reverse();

            function jsRecLoad(urls) {
                if (urls.length == 0) {
                    if ($defined(callback))
                        callback();
                } else {
                    var url = urls.pop();
                    Asset.javascript(url, {
                        onLoad: function() {
                            jsRecLoad(urls)
                        }
                    });
                }
            }

            jsRecLoad(jsUrls);
        },
        onFailure: function() {
            console.log('Sorry, your request failed :(');
        }
    });
    request.send();

}


Asset.javascript("../../../../../web2d/target/classes/web2d.svg-min.js", {
    onLoad: function() {
        JSPomLoader('../../../../../mindplot/pom.xml', function() {
        });
    }
});



