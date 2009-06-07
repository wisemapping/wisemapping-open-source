<%@ include file="/jsp/init.jsp" %>
<h1>
    <spring:message code="EXPORT"/>
    '${mindmap.title}'</h1>

<div>
    <form method="post" id="exportForm" name="exportForm" action="<c:url value="export.htm"/>" style="height:100%;">
        <input type="hidden" name="action" value="export"/>
        <input type="hidden" name="mapId" value="${mindmap.id}"/>
        <table>
            <tbody>
                <tr>
                    <td>
                        <input type="radio" id="svg" name="exportFormat" value="SVG"/>
                        <b>
                            <spring:message code="SVG_EXPORT_FORMAT"/>
                        </b>

                        <p>
                            <spring:message code="SVG_EXPORT_FORMAT_DETAILS"/>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td>
                        <input type="radio" name="exportFormat" value="PDF" id="pdf"/>
                        <b>
                            <spring:message code="PDF_EXPORT_FORMAT"/>
                        </b>

                        <p>
                            <spring:message code="PDF_EXPORT_FORMAT_DETAILS"/>
                        </p>
                    </td>
                </tr>
                 <tr>
                    <td>
                        <input type="radio" id="freemind" name="exportFormat" value="FREEMIND" checked="checked"/>
                        <b>
                            <spring:message code="FREEMIND_EXPORT_FORMAT"/>
                        </b>

                        <p>
                            <spring:message code="FREEMIND_EXPORT_FORMAT_DETAILS"/>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td>
                        <input type="radio" name="exportFormat" id="img" value="IMG_EXPORT_FORMAT"/>
                        <b>
                            <spring:message code="IMG_EXPORT_FORMAT"/>
                        </b><select name="imgFormat" id="imgFormat" style="visibility:hidden;margin-left:5px;">
                        <option>PNG</option>
                        <option>JPEG</option>
                    </select>

                        <p>
                            <spring:message code="IMG_EXPORT_FORMAT_DETAILS"/>
                        </p>
                    </td>
                </tr>
                <tr>
                    <td style="text-align:center;margin-top:10px;">
                        <input type="button" id="ok" value="<spring:message code="OK"/>" class="btn-primary">
                        <input type="button" value="<spring:message code="CANCEL"/>" class="btn-primary"
                               onclick="MOOdalBox.close();">
                    </td>
                </tr>
            </tbody>
        </table>
    </form>
</div>
<script type="text/javascript">
    $('img').addEvent('click', function(event) {
        $('imgFormat').setStyle('visibility', 'visible');
    });
    $('pdf').addEvent('click', function(event) {
        $('imgFormat').setStyle('visibility', 'hidden');
    });
    $('svg').addEvent('click', function(event) {
        $('imgFormat').setStyle('visibility', 'hidden');
    });

    $('ok').addEvent('click', function(event) {
        $('exportForm').submit();
        MOOdalBox.close();

    });

</script>