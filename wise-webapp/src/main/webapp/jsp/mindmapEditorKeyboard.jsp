<%@page pageEncoding="UTF-8" %>
<%@ include file="/jsp/init.jsp" %>

<!DOCTYPE HTML>

<p><spring:message code="KEYBOARD_SHORTCUTS_MSG"/></p>
<style type="text/css">
    #keyboardTable {
        font-family: Arial, verdana, serif;
        font-size: 13px;
    }

    #keyboardTable td {
        padding: 3px;
        white-space: nowrap;
    }

    #keyboardTable th {
        padding: 5px;
        white-space: nowrap;
    }

    #keyboardTable th {
        background-color: #000000;
        color: #ffffff;
    }
</style>
<script>
    function submitDialogForm() {}
</script>

<div id="keyboardTable">
    <table>
        <colgroup>
            <col width="40%"/>
            <col width="30%"/>
            <col width="30%"/>
        </colgroup>
        <thead>
        <tr>
            <th><spring:message code="ACTION"/></th>
            <th>Windows - Linux</th>
            <th>Mac OS X</th>
        </tr>
        </thead>
        <tbody>
        <tr>
            <td><spring:message code="SAVE_CHANGES"/></td>
            <td>Ctrl + s</td>
            <td>⌘ + s</td>
        </tr>
        <tr>
            <td><spring:message code="CREATE_SIBLING_TOPIC"/></td>
            <td>Enter</td>
            <td>Enter</td>
        </tr>
        <tr>
            <td><spring:message code="CREATE_CHILD_TOPIC"/></td>
            <td>Insert / Tab</td>
            <td>⌘ + Enter / Tab</td>
        </tr>
        <tr>
            <td><spring:message code="DELETE_TOPIC"/></td>
            <td>Delete</td>
            <td>Delete</td>
        </tr>
        <tr>
            <td><spring:message code="EDIT_TOPIC_TEXT"/></td>
            <td><spring:message code="JUST_START_TYPING"/> | F2</td>
            <td><spring:message code="JUST_START_TYPING"/> | F2</td>
        </tr>
        <tr>
            <td><spring:message code="MULTIPLE_LINES"/></td>
            <td>Ctrl + Enter</td>
            <td>⌘ + Enter</td>
        </tr>
        <tr>
            <td><spring:message code="COPY_AND_PASTE_TOPICS"/></td>
            <td>Ctrl + c/Ctrl + v</td>
            <td>⌘ + c/⌘ + v</td>
        </tr>

        <tr>
            <td><spring:message code="COLLAPSE_CHILDREN"/></td>
            <td>Space bar</td>
            <td>Space bar</td>
        </tr>
        <tr>
            <td><spring:message code="TOPIC_NAVIGATION"/></td>
            <td><spring:message code="ARROW_KEYS"/></td>
            <td><spring:message code="ARROW_KEYS"/></td>
        </tr>
        <tr>
            <td><spring:message code="SELECT_MULTIPLE_NODES"/></td>
            <td>Ctrl + Mouse Click</td>
            <td>Ctrl + Mouse Click</td>
        </tr>
        <tr>
            <td><spring:message code="UNDO_EDITION"/></td>
            <td>Ctrl + z</td>
            <td>⌘ + z</td>
        </tr>
        <tr>
            <td><spring:message code="REDO_EDITION"/></td>
            <td>Ctrl + Shift + z</td>
            <td>⌘ + Shift + z</td>
        </tr>
        <tr>
            <td><spring:message code="SELECT_ALL_TOPIC"/></td>
            <td>Ctrl + a</td>
            <td>⌘ + a</td>
        </tr>
        <tr>
            <td><spring:message code="CANCEL_TEXT_CHANGES"/></td>
            <td>Esc</td>
            <td>Esc</td>
        </tr>
        <tr>
            <td><spring:message code="DESELECT_ALL_TOPIC"/></td>
            <td>Ctrl + Shift + a</td>
            <td>⌘ + Shift + a</td>
        </tr>
        <tr>
            <td><spring:message code="CHANGE_TEXT_ITALIC"/></td>
            <td>Ctrl + i</td>
            <td>⌘ + i</td>
        </tr>
        <tr>
            <td><spring:message code="CHANGE_TEXT_BOLD"/></td>
            <td>Ctrl + b</td>
            <td>⌘ + b</td>
        </tr>
        </tbody>
    </table>
</div>