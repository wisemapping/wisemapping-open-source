<html>
<head>
    <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.5.0/jquery.min.js"></script>
    <script type="text/javascript"
            src="http://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js"></script>
</head>
<body>
<div style="width:100%; font-size:130%;">
    you need to install Google Chrome Frame.
    <br/>
    <div id="div"> click here</div>
</div>

<div id="prompt">
    <!-- if IE without GCF, prompt goes here -->
</div>
<script>
    // The conditional ensures that this code will only execute in IE,
    // Therefore we can use the IE-specific attachEvent without worry
    $(document).ready(function(){
        $("#div").click(function(event){
            $(".chromeFrameOverlayContent").css("display","block");
            $(".chromeFrameOverlayUnderlay").css("display","block");
        });
    });
    window.attachEvent("onload", function() {
        CFInstall.check({
            mode: "overlay" // the default
        });
    });
</script>
</body>
</html>