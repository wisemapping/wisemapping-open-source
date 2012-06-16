<%@ include file="/jsp/init.jsp" %>

<div>
    <h2 style="font-weight:bold;">Thanks for signing up!</h2>
    <c:if test="${confirmByEmail==true}">
        <p>
            You will receive a confirmation message shortly from WiseMapping. This message will ask you to activate your
            WiseMapping account.
            Please select the link to activate and start creating and sharing maps.
        </p>
        <br/>

        <p>
            Thanks so much for your interest in WiseMapping.
        </p>
        <br/>

        <p>
            If you have any questions or have any feedback, please don't hesitate to use the on line form.
            We'd love to hear from you.
        </p>
    </c:if>
    <c:if test="${confirmByEmail==false}">
        <p>
            Your account has been created successfully, click <a href="c/login">here</a> to sign in and start enjoying
            WiseMapping.
        </p>
    </c:if>
</div>
