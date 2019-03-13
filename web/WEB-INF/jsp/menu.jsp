<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<!DOCTYPE html>
<html>
    <head>
        <%@ include file="infouser.jsp" %>
        <script>
            $(document).ready(function () {
                $('#selectIS').modal({
                    backdrop: 'static',
                    keyboard: false
                });
                $('#selectIS').modal("show");
                $("#selectIS .close").hide();
            });
            function getYears() {
                var id = $("#divSelectDepartament option:selected").val();
                var nameSchool = $("#divSelectDepartament option:selected").text();

                $("#nameSchoolTitle").text(nameSchool);
                $("#schoolcode").val(id);
                $("#schoolName").val(nameSchool);

                if (id !== "") {
                    $.ajax({
                        type: "POST",
                        url: "menu/years.htm?id=" + id,
                        data: id,
                        dataType: 'text',
                        success: function (data) {
                            var years = JSON.parse(data);
                            for (var t in years) {
                                $('#selectyear').append("<option value='" + years[t].x + "'>" + years[t].y + "</option>");
                            }
                            $('#selectIS').modal("hide");
                            $(".modal-backdrop").hide();
                        },
                        error: function (xhr, ajaxOptions, thrownError) {
                            console.log(xhr.status);
                            console.log(xhr.responseText);
                            console.log(thrownError);
                        }
                    });
                }
            }
            function templates() {
                var id = $("#selectyear option:selected").val();
                if (id !== "") {
                    $.ajax({
                        type: "POST",
                        url: "menu/temp.htm?id=" + id,
                        data: id,
                        dataType: 'text',
                        success: function (data) {
                            var tmps = JSON.parse(data);
                            $('#selecttemplate').empty();
                            var ind = 1;
                            for (var t in tmps) {
                                $('#selecttemplate').append("<option value='" + tmps[t].id + "-" + tmps[t].rows + "-" + tmps[t].cols + "#" + ind + "'>" + tmps[t].name + "</option>");
                                ind++;
                            }
                            $("#buttonCreate").attr("disabled", false);
                        },
                        error: function (xhr, ajaxOptions, thrownError) {
                            console.log(xhr.status);
                            console.log(xhr.responseText);
                            console.log(thrownError);
                        }
                    });
                } else {
                    $("#buttonCreate").attr("disabled", true);
                    $("#selecttemplate").empty();
                }
            }
            function hideroomsgroup() {
                var selectval = $('#roomsmode').val();
                if (selectval === 0 || selectval === 1) {
                    $('#grouprooms').hide();
                } else {
                    $('#grouprooms').show();
                }
            }
            function enviando()
            {
                $('#crearhorario').submit();
                $('#pleaseWaitDialog').modal({
                    backdrop: 'static',
                    keyboard: false
                });
                $('#pleaseWaitDialog').modal('show');
                var start = new Date();
                var maxTime = 80000;
                var timeoutVal = Math.floor(maxTime / 100);
                animateUpdate();
                function updateProgress(percentage) {
                    $('#pbar_innerdiv').css("width", percentage + "%");
                    $('#pbar_innertext').text(percentage + "%");
                }
                function animateUpdate() {
                    var now = new Date();
                    var timeDiff = now.getTime() - start.getTime();
                    var perc = Math.round((timeDiff / maxTime) * 100);
                    console.log(perc);
                    if (perc <= 100) {
                        updateProgress(perc);
                        setTimeout(animateUpdate, timeoutVal);
                    }
                }
            }

        </script>
    </head>
    <body>
        <div class="modal fade" id="selectIS" role="dialog">
            <div class="modal-dialog">

                <!-- Modal content-->
                <div class="modal-content">
                    <div class="modal-header">
                        <button type="button" class="close" data-dismiss="modal">&times;</button>
                        <h4 class="modal-title">Select School</h4>
                    </div>
                    <div class="modal-body">
                        <div id="divSelectDepartament" style="text-align: center;">
                            <select style="width: 50%;" onchange="getYears()">
                                <option></option>
                                <%--Se accede aquí desde menu de Homepage: Este forEach coge los datos que se han establecido en dicho menu,
                               y se introducen los datos de x e y(codigo y nombre de las escuelas), a través de ${schools} --%>                                
                                <c:forEach var="year" items="${schools}">
                                    <%-- year.x y year.y son los cogidos y nombres de escuelas que se han añadido a través del método getSchools de Consultas(que conecta con menu de Homepage) --%>                     
                                    <option value="${year.x}">${year.y}</option>                                 
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                </div>
                <%--Después de aplicar el evento de selección de la escuela (High School por ejemplo), se pasa a menu/create.htm,
                para poder crear un horario(indicado más abajo)--%>   
            </div>
        </div>
        <!-- Modal -->
        <div class="modal fade" id="pleaseWaitDialog" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header" align="center">
                        <h1>Loading, wait please ...</h1>
                    </div>
                    <div class="modal-body">
                        <div class="progress">
                            <div class="progress-bar progress-bar-success progress-bar-striped progress-bar-animated" style="background-color: #2d2f42 !important;" role="progressbar" aria-valuenow="40" aria-valuemin="0" aria-valuemax="100" id="pbar_innerdiv">
                                <div id="pbar_innertext" >0%</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xs-12">
            <%-- A través de menu/create.htm puede enviar esta información al Controlador de Homepage, RequestMapping@(menu/create.htm)
            Aquí no pasa por el dispatcher porque se indica la dirección entera(menu/create.htm), aunque lo correcto sería usar el dispatcher.
            Gracias a esto se puede crear el horario, con los siguientes 4 campos(year, template,rosters, rooms) y el boton buttonCreate--%>            
            <form:form action="menu/create.htm" method="POST" id="crearhorario">                
                <input id="schoolcode" name="schoolcode" type="hidden" value="">
                <input id="schoolName" name="schoolName" type="hidden" value="">
                <div class="col-xs-3">        
                    <fieldset>
                        <legend>Select Year</legend>
    <%--El siguiente select coge los datos de Year desde la funcion templates. Dicha funcion coge la url menu/temp.htm?id=
    que lleva a HomePage.getTemplates. Esto es así porque al modificar una opción en SelectYear, se modifica automáticamente
    la opcion de Template, pero no de las opciones de Check y Room(y por eso pasa por la función de JavaScript y modificarlo por pantalla)--%> 
    
                        <select class="form-control" id="selectyear" name="yearid" onchange="templates()">
                            <option></option>
                            <%-- Aquí se implantan las distintas opciones que se pueden elegir(años académicos) a través de ${years}: --%>                         
                            <c:forEach var="year" items="${years}">
                                <option value="${year.x}">${year.y}</option>
                            </c:forEach>
                        </select>
                    </fieldset>
                </div>
                <div class="col-xs-3">
                    <fieldset>
                        <legend>Select Template</legend>
                        <select class="form-control" name="templateInfo" id="selecttemplate">
                        </select>
                    </fieldset>
                </div>
                <div class="col-xs-2 ">
                    <fieldset>
<%-- Modificado Shuffle (estaba mal escrito, ponía Suffle)           --%>             
                        <legend>Shuffle Rosters</legend>
                        <select class="form-control" id="suffleCheck" name="suffleCheck">
                            <option value="0">disabled</option>
                            <option value="1">enabled</option>
                        </select>
                    </fieldset>
                </div>
                <div class="col-xs-2 ">
                    <fieldset>
                        <legend>Active Rooms</legend>
                        <select class="form-control" id="roomsCheck" name="roomsCheck">
                            <option value="0">disabled</option>
                            <option value="1">enabled</option>
                        </select>
                    </fieldset>
                </div>

                <div class="col-xs-2">
                    <fieldset>
                        <legend>Create Schedule</legend>
                        <input id="buttonCreate" disabled="" class="btn col-xs-12" type="button" name="Submit" value="Create" style="background-color: #2d2f42 !important;color: azure;" onclick="enviando()">
                    </fieldset>
                </div>
            </form:form>
        </div>
    </body>
</html>