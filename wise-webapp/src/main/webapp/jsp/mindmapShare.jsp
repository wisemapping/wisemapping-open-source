<%@ include file="/jsp/init.jsp" %>
<style type="text/css">
    #sharingContainer {
        height: 300px;
        width: 300px;
        overflow: scroll;
    }
</style>

<p>Who has access</p>

<div id="sharingContainer">
    <table class="table">
        <tbody>
        <tr>
            <td>Name</td>
            <td>Email</td>
            <td><a href="">Action</a></td>
        </tr>
        </tbody>
    </table>

</div>

<form method="post" id="dialogMainForm" action="#" class="well form-inline">
    <label for="collabEmails" class="control-label">Add People:
        <input type="text" id="collabEmails" name="collabEmail" placeholder="Enter collaborators emails separared by comas."/>
    </label>
</form>


<script type="text/javascript">

    // Hook for interaction with the main parent window ...
    var submitDialogForm = function() {
        $('#dialogMainForm').submit();
    }
</script>

