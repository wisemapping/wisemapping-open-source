<%@ page import="org.apache.log4j.Logger" %>
<%@ page autoFlush="true" buffer="none" %>
<%@ include file="/jsp/init.jsp" %>

<%!
    final Logger logger = Logger.getLogger("com.wisemapping");
%>
<div style="position:relative;">
    <div id="prompt">
        <!-- if IE without GCF, prompt goes here -->
    </div>
</div>
<script type="text/javascript"
        src="http://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js"></script>

<script type="text/javascript">
    function getURLParameter(name) {
        return unescape(
                (RegExp(name + '=' + '(.+?)(&|$)').exec(location.search)||[,null])[1]
                );
    }
    CFInstall.check({
        mode: "inline",
        node:"prompt",
        destination:"${pageContext.request.contextPath}/c/editor?mapId="+getURLParameter("mapId")
    });
</script>