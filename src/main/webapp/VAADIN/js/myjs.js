function myFunction(button) {
    var text = $(button).text();
    var girdlayout = $(button).closest("div.v-widget").find(".v-gridlayout-custom-feildset");
    if ($(girdlayout).css("display") === "none") {
        $(button).html('<img src="VAADIN/themes/tests-valo/img/navigate_close.png">' + text);
    } else {
        $(button).html('<img src="VAADIN/themes/tests-valo/img/navigate_open.png">' + text);
    }
    $(girdlayout).toggle("slow");
}

function myFunctionTable(button) {
    var text = $(button).text();
    var girdlayout = $(button).closest("div.v-widget").find(".v-verticallayout-custom-feildset");
    if ($(girdlayout).css("display") === "none") {
        $(button).html('<img src="VAADIN/themes/tests-valo/img/navigate_close.png">' + text);
    } else {
        $(button).html('<img src="VAADIN/themes/tests-valo/img/navigate_open.png">' + text);
    }
    $(girdlayout).toggle("slow");
}
function setValueDate() {
    var dateString = $("#date input").val();
    var pattern = /^(((((0[1-9])|(1\d)|(2[0-8]))\/((0[1-9])|(1[0-2])))|((31\/((0[13578])|(1[02])))|((29|30)\/((0[1,3-9])|(1[0-2])))))\/((20[0-9][0-9])|(19[0-9][0-9])))|((29\/02\/(19|20)(([02468][048])|([13579][26]))))$/;
    if (dateString !== null && dateString !== "")
    {
        if (!pattern.test(dateString))
        {
            if (dateString.length === 8)
            {
                var output = dateString.substr(0, 2) + "/" + dateString.substr(2, 2) + "/" + dateString.substr(4);
                if (pattern.test(output))
                {
                    $("#date input").val(output);
                }
                else
                {
                    $("#date input").css("border-color", "red");
                    $("#error-date").show();
//                    $(".v-tooltip").css("background-color", "#fff");
                }
            }
            else
            {
                $("#date input").css("border-color", "red");
                $("#error-date").show();
//                $(".v-tooltip").css("background-color", "#fff")
            }
        }

    }
}
function focusDate() {
    $("#date input").css("border-color", "#c5c5c5");
    $("#error-date").hide();
//    $(".v-tooltip").css("background-color", "#000")
} 
function showError(){
    
     $(".v-tooltip").css("background-color", "#fff");
}
function hideError(){
      $(".v-tooltip").css("background-color", "#000");
   
}
function showMenu(){
    $("#testMenu").toggle("slow");
//    if($("#testMenu").css("display")==="none")
//    {
//        $("#testMenu").show();
//    }
}
function hideMenu(){
     $("#testMenu").toggle("slow");
//     if($("#testMenu").css("display")==="block")
//    {
//        $("#testMenu").hide();
//    }
}
function printDiv() {
//    alert("thieu");
     var printContents = document.getElementById("print-popup").innerHTML;
     var originalContents = document.body.innerHTML;
     window.print();
     document.body.innerHTML = printContents;
     document.body.innerHTML = originalContents;
}