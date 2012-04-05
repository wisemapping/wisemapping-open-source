<%@ include file="/jsp/init.jsp" %>
<h1>
    <spring:message code="EXPORT"/>'${mindmap.title}'</h1>

<div>
    <form method="POST" id="exportForm" name="exportForm" action="<c:url value="/service/transform"/>"
          style="height:100%;" enctype="application/x-www-form-urlencoded">
        <input name="svgXml" value="" type="hidden"/>
        <input name="mapXml" value="" type="hidden"/>
        <input name="filename" value="${mindmap.title}" type="hidden"/>
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
                    <input type="button" id="cancel" value="<spring:message code="CANCEL"/>" class="btn-secondary"
                           onclick="">
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

        var form = $('exportForm');

        // Look for the selected format and append export suffix...
        var value = $$('input[name=exportFormat]:checked')[0].get('value');
        var suffix;
        if (value == 'IMG_EXPORT_FORMAT') {
            var selected = $('imgFormat');
            suffix = selected.options[selected.selectedIndex].value;
        } else {
            suffix = value;
        }
        suffix = suffix.toLowerCase();
        form.action = form.action + "." + suffix;

        // Store SVG o native map...
        if (suffix == "freemind") {
            var mindmap = designer.getMindmap();
            var serializer = mindplot.persistence.XMLSerializerFactory.getSerializerFromMindmap(mindmap);
            var domMap = serializer.toXML(mindmap);
            form.mapXml.value = core.Utils.innerXML(domMap);
        } else {
            form.svgXml.value = $("workspaceContainer").innerHTML;
        }

        // Finally, submit map ...
        form.submit();
        MooDialog.Request.active.close();
    });

</script>