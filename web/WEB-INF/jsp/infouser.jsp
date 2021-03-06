<%-- 
    Document   : bannerinfo
    Created on : 12-jul-2016, 16:23:16
    Author     : Jes�s Arag�n
--%>
<%@page import="java.util.ArrayList"%>
<%@ page session="true" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" type="image/png" href="<c:url value="/recursos/img/iconos/favicon.ico" />" >
    <link rel="apple-touch-icon" href="<c:url value="/recursos/img/iconos/favicon.ico"/>">
    <link rel="shortcut icon" href="<c:url value="/recursos/img/iconos/favicon.ico"/>">
    <link rel="stylesheet" type="text/css" href="https://fonts.googleapis.com/css?family=Montserrat">
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/fonts/icons/iconsAragon.ttf"/>">
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/fonts/icons/iconsAragon.svg"/>">
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/fonts/icons/iconsAragon.eot"/>">
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/fonts/icons/iconsAragon.wott"/>">

    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/style.css" />"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/menu-lateral.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/bootstrap.min.css"/>"/>
    <%--    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/calendar.css"/>"/>--%>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/bootstrap-theme.min.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/bootstrap-datetimepicker.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/bootstrap-toggle.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/treeJs/default/style.min.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/treeGrid/default/tree.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/treeGrid/default/easyui.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/treeGrid/default/icon.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/treeGrid/default/demo.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/treeGrid/bootstrap/datagrid.css"/>"/>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/treeGrid/bootstrap/tree.css"/>"/>
    <link rel="stylesheet" href="<c:url value="/recursos/fullcalendar/fullcalendar.css"/>"/>
    <script type="text/javascript" src="<c:url value="/recursos/js/jquery-2.2.0.js" />"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/treeGrid/jquery.easyui.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/treeGrid/jquery.datagrid.js" />"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/treeGrid/jquery.tree.js" />"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/treeGrid/jquery.treegrid.js" />"></script>
    <script type="text/javascript" src="<c:url value="/recursos/starrating/bootstrap-rating-input.min.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/bootstrap.js" />"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/jquery-ui-1.11.4.custom/jquery-ui.js" />"></script>

    <script type="text/javascript" src="<c:url value="/recursos/js/tree/jstree.js" />"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/tree/jstree.checkbox.js" />"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/tree/jstree.search.js" />"></script>

    <script type="text/javascript" src="<c:url value="/recursos/js/moment.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/transition.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/collapse.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/bootstrap-toggle.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/bootstrap-datetimepicker.js"/>"></script>
    <%--<script type="text/javascript" src="<c:url value="/scripts/json.min.js" /> "></script>--%>
    <script type="text/javascript" src="<c:url value="/recursos/js/es.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/ar.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/moment.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/recursos/fullcalendar/fullcalendar.js"/>"></script>
    <!--        CKEDITOR-->
    <script type="text/javascript" src="<c:url value="/recursos/js/ckeditor.js"/>"></script>

    <!--        DATATABLES-->
    <%--        <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/dataTables/dataTables.bootstrap.css"/>" />--%>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/dataTables/dataTables.foundation.css"/>" />
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/dataTables/dataTables.jqueryui.css"/>" />
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/dataTables/dataTables.semanticui.css"/>" />
    <%--        <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/dataTables/dataTables.material.css"/>" />--%>
    <%--        <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/dataTables/dataTables.uikit.css"/>" />--%>
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/dataTables/jquery.dataTables.css"/>" />
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/dataTables/jquery.dataTables_themeroller.css"/>" />
    <link rel="stylesheet" type="text/css" href="<c:url value="/recursos/css/estilocolegio.css" />"/>

    <script type="text/javascript" src="<c:url value="/recursos/js/dataTables/jquery.dataTables.js"/>" ></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/dataTables/dataTables.bootstrap.js"/>" ></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/dataTables/dataTables.bootstrap4.js"/>" ></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/dataTables/dataTables.buttons.min.js"/>" ></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/dataTables/buttons.print.min.js"/>" ></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/dataTables/dataTables.foundation.js"/>" ></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/dataTables/dataTables.jqueryui.js"/>" ></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/dataTables/dataTables.material.js"/>" ></script>
    <script type="text/javascript" src="<c:url value="/recursos/js/dataTables/dataTables.uikit.js"/>" ></script>
</head>

<script>
    $(document).ready(function () {
        setInterval(function () {
            alert("Your session is going to be end by 5 min, Please click OK and continue")
        }, 3000000);
    });

    var ajax;
    function terms() {

        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        } else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }

        ajax.onreadystatechange = CallBackYear;
        var seleccion = $('#yearSelect option:selected').val();
        var url = "<c:url value="/getyear.htm"/>?id=" + seleccion;
        ajax.open("POST", url, true);
        ajax.send("");
    }

    function CallBackYear()
    {
        if (ajax.readyState === 4 && ajax.status === 200) {
            $('#termSelect').empty();
            var jsonObj = JSON.parse(ajax.responseText);
            for (var i in jsonObj) {
                $('#termSelect').append("<option value='" + jsonObj[i].x + "'>"
                        + jsonObj[i].y + "</option>");
            }
        }
    }

    function changeTermYear() {
        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        } else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }

        ajax.onreadystatechange = function () {
            if (ajax.readyState === 4 && ajax.status === 200) {
                window.location.reload();
            }
        };
        var seleccion = $('#yearSelect option:selected').val();
        var term = $('#termSelect option:selected').val();
        var url = "<c:url value="/changeTermYear.htm"/>?yearid=" + seleccion + "&termid=" + term;
        ajax.open("POST", url, true);
        ajax.send("");
    }

    function logout() {
        document.location.href = "<c:url value="/cerrarLogin.htm"/>";
    }
</script>

<div class="infousuario noPrint bg-primary" id="infousuario" style="background-color: #2d2f42;">

    <div class="col-xs-9 text-center">
        <div class="col-xs-12">
            <h1 id="nameSchoolTitle" class="text-center"></h1>
        </div>
        <div class="col-xs-12">
            <h3 class="text-center">Hi, <c:out value="${sessionScope.user.name}"/></h3>
        </div>
    </div>
    <div class="col-xs-3 text-right">
        <!--<a href="<c:url value="/cerrarLogin.htm"/>" role="button" aria-haspopup="true" aria-expanded="false"><img class="imgUser" src="<c:url value="/recursos/img/iconos/user-01.svg"/>"></a>-->
        <div class="col-xs-12">
        </div>
        <div class="col-xs-10">
            <a onclick="$('#logoutmodal').modal('show');" role="button" class="btn btn-default" aria-haspopup="true" aria-expanded="false">Log out<!--<img class="imgUser" src="<c:url value="/recursos/img/iconos/user-01.svg"/>"  style="width: 100% !important;max-height: 75px !important;">--></a>
        </div>   
    </div>
</div>    

<div id="yearTermModal" class="modal fade" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="col-xs-12 col-md-12">
                <div class="col-xs-12 col-md-5">
                    <div class="form-group">
                        <label for="yearSelect">Year</label>
                        <select class="form-control" id="yearSelect" onchange="terms()">
                        </select> 
                    </div>  
                </div>
                <script>
                    var yearId = ${yearId};
                    var termId = ${termId};
                    var listYear = ${yearsids};

                    for (var i = 0; i < listYear.length; ++i) {
                        $("#yearSelect").append("<option value='" + listYear[i].x + "'>" + listYear[i].y + "</option>");
                    }

                    var my_options = $("#yearSelect option");
                    my_options.sort(function (a, b) {
                        if (a.text > b.text)
                            return 1;
                        if (a.text < b.text)
                            return -1;
                        return 0
                    })

                    $("#yearSelect").empty().append(my_options);
                    $("#yearSelect").val(yearId);
                    terms();
                    $("#termSelect").val(termId);

                </script>
                <div class="col-xs-12 col-md-5">
                    <div class="form-group">
                        <label for="termSelect">Term</label>
                        <select class="form-control" id ="termSelect">
                        </select>
                    </div> 
                </div>
                <div class="col-xs-12 col-md-2" style="padding: 0px;">
                    <div class="form-group">
                        <label for="buttonYear">&nbsp</label>
                        <button id="buttonYear" onclick="changeTermYear()" type="submit" class="form-control btn btn-primary" data-dismiss="modal">Change</button>
                    </div>
                </div>
            </div>
        </div>  
    </div>
</div>


<div id="logoutmodal" class="modal fade" role="dialog">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header modal-header-delete">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">are you sure you want to logout?</h4>
            </div>
            <div class="modal-footer text-center">
                <button id="buttonDelete" type="button" class="btn btn-danger" data-dismiss="modal" onclick="logout()">Yes</button>
                <button type="button" class="btn btn-default" data-dismiss="modal">No</button>
            </div>
        </div>

    </div>
</div>

