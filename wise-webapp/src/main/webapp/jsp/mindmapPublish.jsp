<%@ include file="/jsp/init.jsp" %>
<h1>
    <spring:message code="PUBLISH"/>
    '${mindmap.title}'
</h1>

<h2>
    <spring:message code="PUBLISH_DETAILS"/>
</h2>

<div>
    <form method="post" id="publishForm" name="publishForm" action="<c:url value="publish.htm"/>" style="height:100%;">
        <input type="hidden" name="actionId" value="save"/>
        <input type="hidden" name="mapId" value="${mindmap.id}"/>
        <table>
            <colgroup>
                <col width="20%"/>
                <col width="80%"/>
            </colgroup>
            <tbody>
                <tr>
                    <td>
                        &nbsp;
                    </td>
                    <td>

                        <input type="checkbox" id="publicViewId" name="publicView" value="true"
                                <c:if test="${mindmap.public}">

                                    checked="checked"
                                </c:if>

                                />
                        <spring:message code="PUBLISH_MAP_TO_INTERNET"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        &nbsp;
                    </td>
                    <td>
                        <div id="disabledPanel"
                             style="position:absolute;background-color:white;opacity:0.8;width:600px;height:160px;left:10px;visibility:hidden;">

                        </div>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        <spring:message code="URL"/>:
                    </td>
                    <td style="padding-bottom: 5px;">
                        <input name="url" value="http://www.wisemapping.com/c/publicView.htm?mapId=${mindmap.id}"
                               style="width:400px" readonly="readonly"/>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        <spring:message code="DIRECT_LINK"/>:
                    </td>
                    <td>
                        <textarea style="width:400px;height:30px;overflow:hidden;" cols="55" rows="3" readonly="readonly">
&lt;a href="http://www.wisemapping.com/c/publicView.htm?mapId=${mindmap.id}">${mindmap.title}&lt;/a></textarea>
                    </td>
                </tr>
                <tr>
                    <td class="formLabel">
                        &nbsp;
                    </td>
                    <td>
                        &nbsp;
                    </td>
                </tr>
                <tr>
                    <td class="formLabel" style="white-space:normal;">
                        <spring:message code="BLOG_INCLUSION"/>:
                    </td>
                    <td>
                        <textarea style="width:400px;height:70px;overflow:hidden;" cols="55" rows="5" readonly="readonly">
&lt;iframe
style="width:600px;height:400px;border: 1px solid black"
src="http://www.wisemapping.com/c/embeddedView.htm?mapId=${mindmap.id}&zoom=1"&gt;
&lt;/iframe&gt;
                         </textarea>

                        <p><spring:message code="EMBEDDED_MAP_SIZE"/></p>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:center;margin-top:30px;" colspan="2">
                        <input type="submit" id="ok" value="<spring:message code="OK"/>" class="btn-primary">
                        <input type="button" value="<spring:message code="CANCEL"/>" class="btn-secondary" id="cancelBtn">
                    </td>
                </tr>
            </tbody>
        </table>
    </form>
</div>

<script type="text/javascript">

    var isPublicPanelEnabled = false;
    var panelEnabler = function()
    {
        if (isPublicPanelEnabled)
        {
            $('disabledPanel').setStyle("visibility", "hidden");
        } else
        {
            $('disabledPanel').setStyle("visibility", "visible");
        }
        isPublicPanelEnabled = !isPublicPanelEnabled;
    };

    if (${mindmap.public==false})
    {
        panelEnabler();
    }
    $('publicViewId').addEvent('click', panelEnabler);

     $('cancelBtn').addEvent('click', function(event) {
        MooDialog.Request.active.close();
    });
</script>