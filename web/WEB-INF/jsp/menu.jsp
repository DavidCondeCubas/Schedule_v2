<%-- 
    Document   : menu
    Created on : 13-nov-2017, 10:13:52
    Author     : Norhan
--%>

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
                // var schoolsList = JSON.parse(${schools});

            });

            function getYears() {
                var id = $("#divSelectDepartament option:selected").val();
                $("#schoolcode").val(id);

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
                var maxTime = 64000;
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
            /*private int id;
             private int cols;
             private int rows;
             private String name;*/
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
                                <c:forEach var="year" items="${schools}">
                                    <option value="${year.x}">${year.y}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                </div>

            </div>
        </div>
        <!-- Modal -->
        <div class="modal fade" id="pleaseWaitDialog" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h1>Procesando por favor espere ...</h1>
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
            <form:form action="menu/create.htm" method="POST" id="crearhorario">                
                <input id="schoolcode" name="schoolcode" type="hidden" value="">
                <div class="col-xs-4">         
                    <fieldset>
                        <legend>Select Year</legend>
                        <select class="form-control" id="selectyear" name="yearid" onchange="templates()">
                            <option></option>
                            <c:forEach var="year" items="${years}">
                                <option value="${year.x}">${year.y}</option>
                            </c:forEach>
                        </select>
                    </fieldset>
                </div>
                <div class="col-xs-4">
                    <fieldset>
                        <legend>Select Template</legend>
                        <select class="form-control" name="templateInfo" id="selecttemplate">
                        </select>
                    </fieldset>
                </div>
                <div class="col-xs-3 hide">
                    <fieldset>
                        <legend>schedule mode</legend>
                        <select class="form-control" id="roomsmode" name="rooms" onchange="hideroomsgroup()">
                            <option value="0">disabled</option>
                            <option value="1">only courses with room restrictions</option>
                            <option value="2">only default school user defined</option>
                            <option value="3">both (courses and default)</option>
                        </select>
                        <legend>Select rooms</legend>
                        <select class="form-control" id="grouprooms" name="groupofrooms">
                            <option value="rooms01">rooms 01</option>
                            <option value="rooms02">rooms 02</option>
                            <option value="rooms03">rooms 03</option>
                            <option value="rooms04">rooms 04</option>
                        </select>
                    </fieldset>
                </div>

                <div class="col-xs-4">
                    <fieldset>
                        <legend>Create Schedule</legend>
                        <input id="buttonCreate" disabled="" class="btn col-xs-12" type="button" name="Submit" value="Create" style="background-color: #2d2f42 !important;color: azure;" onclick="enviando()">
                    </fieldset>
                </div>
            </form:form>
        </div>
    </body>
</html>
