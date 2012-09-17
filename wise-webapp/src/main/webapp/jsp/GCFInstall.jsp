<%@page pageEncoding="UTF-8" %>
<%@include file="/jsp/init.jsp" %>


<div style="position:relative;">
    <div id="prompt">
        <!-- if IE without GCF, prompt goes here -->
    </div>
</div>
<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js"></script>

<script type="text/javascript">
    CFInstall.check({
        mode: "inline",
        node:"prompt",
        destination:"../c/maps/"
    });
</script>