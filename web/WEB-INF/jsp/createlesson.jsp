<%-- 
    Document   : createlesson
    Created on : 30-ene-2017, 14:59:17
    Author     : nmohamed
--%>


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Create Lessons</title>
        
        <link href="recursos/css/bootstrap.css" rel="stylesheet" type="text/css"/>
      
        <link href="recursos/css/bootstrap-theme.min.css" rel="stylesheet" type="text/css" />
        <link href="recursos/css/bootstrap-datetimepicker.css" rel="stylesheet" type="text/css"/>
        <link href="recursos/css/bootstrap-toggle.css" rel="stylesheet" type="text/css"/>

        <script src="recursos/js/jquery-2.2.0.js" type="text/javascript"></script>
        
        <script src="recursos/js/moment.js" type="text/javascript"></script>
        <script src="recursos/js/bootstrap-datetimepicker.js" type="text/javascript"></script>
        <script src="recursos/js/es.js" type="text/javascript"></script>
        <script src="recursos/js/ar.js" type="text/javascript"></script>
        <script src="recursos/js/bootstrap-toggle.js" type="text/javascript"></script>

        <script>



 $(document).ready(function(){
       var userLang = navigator.language || navigator.userLanguage;
       var myDate = new Date();
         //Muestra calendario
  
        $('#fecha').datetimepicker({
            
            format: 'YYYY-MM-DD',
            locale: userLang.valueOf(),
            daysOfWeekDisabled: [0, 6],
            useCurrent: false//Important! See issue #1075
            //defaultDate: '08:32:33',

  
        });
         $('#horainicio').datetimepicker({
            format: 'HH:mm',
            locale: userLang.valueOf(),
            useCurrent: false, //Important! See issue #1075
            stepping: 5
        });
        $('#horafin').datetimepicker({
            
            format: 'HH:mm',
            locale: userLang.valueOf(),
            useCurrent: false, //Important! See issue #1075
            stepping: 5
        });
        
        $("#horainicio").on("dp.change", function (e) {
            $('#horafin').data("DateTimePicker").minDate(e.date);
        });
        
        $("#horafin").on("dp.change", function (e) {
            $('#horainicio').data("DateTimePicker").maxDate(e.date);
        });
        

//       //Menu lateral
//        $('#nav-expander').on('click',function(e){
//      		e.preventDefault();
//      		$('body').toggleClass('nav-expanded');
//      	});
//      	$('#nav-close').on('click',function(e){
//      		e.preventDefault();
//      		$('body').removeClass('nav-expanded');
//      	});
//        $('#barralateral').mouseleave(function(o){
//      		o.preventDefault();
//      		$('body').removeClass('nav-expanded');
//      	});
    });            
            
        $().ready(function() 
	{
		$('.pasar').click(function() { return !$('#origen option:selected').remove().appendTo('#destino'); });  
		$('.quitar').click(function() { return !$('#destino option:selected').remove().appendTo('#origen'); });
		$('.pasartodos').click(function() { $('#origen option').each(function() { $(this).remove().appendTo('#destino'); }); });
		$('.quitartodos').click(function() { $('#destino option').each(function() { $(this).remove().appendTo('#origen'); }); });
		$('.submit').click(function() { $('#destino option').prop('selected', 'selected'); });
	});
        
        var ajax;

 function funcionCallBackLevelStudent()
    {
           if (ajax.readyState===4){
                if (ajax.status===200){
                    document.getElementById("origen").innerHTML= ajax.responseText;
                    }
                }
            }
            

    function comboSelectionLevelStudent()
    {
        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        }
        else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }
        
        ajax.onreadystatechange=funcionCallBackLevelStudent;
        var seleccion = document.getElementById("levelStudent").value;
        var alumnos = document.getElementById("destino").innerHTML;
        ajax.open("POST","createlesson.htm?select=studentlistLevel&seleccion="+seleccion,true);
        ajax.send("");
    }
    
     function funcionCallBackSubject()
    {
           if (ajax.readyState===4){
                if (ajax.status===200){
                    document.getElementById("subject").innerHTML= ajax.responseText;
                    }
                }
            }
    function funcionCallBackSubsection()
    {
           if (ajax.readyState===4){
                if (ajax.status===200){
                    document.getElementById("subsection").innerHTML= ajax.responseText;
                    }
                }
            }
    function funcionCallBackLoadTemplateLessons()
    {
           if (ajax.readyState===4){
                if (ajax.status===200){
                    document.getElementById("lessons").innerHTML= ajax.responseText;
                    }
                }
            }        
    function funcionCallBackTemplateLessons()
    {
           if (ajax.readyState===4){
                if (ajax.status===200){
                    document.getElementById("template").innerHTML= ajax.responseText;
                    }
                }
            }
            
    function funcionCallBackEquipment()
    {
           if (ajax.readyState===4){
                if (ajax.status===200){
                    document.getElementById("equipment").innerHTML= ajax.responseText;
                    }
                }
            }

    function comboSelectionLevel()
    {
        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        }
        else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }

        $('#createOnClick').attr('disabled', true);
        ajax.onreadystatechange = funcionCallBackSubject;
        var seleccion1 = document.getElementById("level").value;
        ajax.open("POST","createlesson.htm?select=subjectlistLevel&seleccion1="+seleccion1,true);
        
        ajax.send("");
       
    }
    function comboSelectionSubject()
    {
        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        }
        else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }

        
        ajax.onreadystatechange=funcionCallBackSubsection;
        var seleccion2 = document.getElementById("subject").value;
        ajax.open("POST","createlesson.htm?select=subsectionlistSubject&seleccion2="+seleccion2,true);

        ajax.send("");
        
    }
   
    function comboSelectionLoadTemplateLessons()
    {
        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        }
        else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }
        
        
        ajax.onreadystatechange=funcionCallBackLoadTemplateLessons;
        var seleccionSubject = document.getElementById("subject").value;
        ajax.open("POST","createlesson.htm?select=namelistSubject&seleccionTemplate="+seleccionSubject,true);
        ajax.send("");
    }
     function comboSelectionTemplateLessons()
    {
        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        }
        else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }
        
        
        ajax.onreadystatechange=funcionCallBackTemplateLessons;
        var seleccionTemplate = document.getElementById("lessons").value;
        //ajax.open("POST","createlesson.htm?select=subsectionlistSubject&seleccion2="+seleccionTemplate,true);
        ajax.open("POST","createlesson.htm?select=loadLessonplan&seleccionTemplate="+seleccionTemplate,true);
        ajax.send("");
    }
     function comboSelectionSubsection()
    {
        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        }
        else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }

        if(document.getElementById("subsection").value === 0){
            $('#createOnClick').attr('disabled', true);
        }else{
            $('#createOnClick').attr('disabled', false);
        }
        ajax.onreadystatechange=funcionCallBackEquipment;
        var seleccion3 = document.getElementById("subsection").value;
        ajax.open("POST","createlesson.htm?select=equipmentlistSubsection&seleccion3="+seleccion3,true);
        ajax.send("");
    }
    
$(function() {
    $('#subject').change(function() {
        $('#LoadTemplates').parent().attr("disabled",false);
        $('#LoadTemplates').attr("disabled",false);
    });
    
    $('#LoadTemplates').change(function() {
         if (this.checked) {
    $("#lessons").attr("disabled", true);
    $('#divCrearLessons').removeClass('hidden');
    $('#divLoadLessons').addClass('hidden');
//    $("#NameLessons").attr("disabled", true);
    } else {
    $("#lessons").attr("disabled", false);
    $('#divLoadLessons').removeClass('hidden');
    $('#divCrearLessons').addClass('hidden');
//    $("#NameLessons").attr("disabled", false);
    }
    });
})
        </script>
<script>
 
</script>
    </head>
    <body>
        
        
        <div class="container">
        <h1 class="text-center">Create Lessons</h1>

        
        <form:form id="formStudents" method ="post" action="createlesson.htm?select=createlesson" >
            <fieldset>
                <legend>Options Date</legend>
                <div class="form-group">
                    <div class="row">

                        <div class='col-xs-4'>
                            <div class="form-group">
                                <label class="control-label" for="fecha"><spring:message code="etiq.txtstartdate"/></label>
                                <div class='input-group date' id='fecha'>
                                    <input type='text' name="TXTfecha" class="form-control" id="fecha" required="required"/>
                                    <span class="input-group-addon">
                                        <span class="glyphicon glyphicon-calendar"></span>
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div class='col-xs-4'>
                            <div class="form-group">
                                <label class="control-label" for="horainicio"><spring:message code="etiq.txtenddate"/></label>
                                <div class='input-group date' id='horainicio'>
                                    <input type='text' name="TXThorainicio" class="form-control" required="required"/>
                                    <span class="input-group-addon">
                                        <span class="glyphicon glyphicon-calendar"></span>
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div class='col-xs-4'>
                            <div class="form-group">
                                <label class="control-label" for="horafin"><spring:message code="etiq.txtenddate"/></label>
                                <div class='input-group date' id='horafin'>
                                    <input type='text' name="TXThorafin" class="form-control" required="required"/>
                                    <span class="input-group-addon">
                                        <span class="glyphicon glyphicon-calendar"></span>
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>
 
                </div>
            </fieldset>
            <fieldset>
                <legend>Options Lessons</legend>
                

                <div class="col-xs-3 form-group">
                    <label class="control-label"><spring:message code="etiq.txtlevels"/></label>
                    <select class="form-control" name="TXTlevel" id="level" onchange="comboSelectionLevel()">
                        <c:forEach var="levels" items="${gradelevels}">
                            <option value="${levels.id[0]}" >${levels.name}</option>
                        </c:forEach>
                    </select>
                          
                </div>
                <div class="col-xs-3 center-block">
                    <label class="control-label"><spring:message code="etiq.txtsubject"/></label>
                    <select class="form-control" name="TXTsubject" id="subject"  onchange="comboSelectionSubject()">
                    
                       <c:forEach var="subject" items="${subjects}">
                                <option value="${subject.id[0]}" >${subject.name}</option>
                            </c:forEach>
                    </select>
                </div>
                <div class="col-xs-3 center-block text-center">
                    <label class="control-label">Select your option</label>
                    <input disabled="true" type="checkbox" data-width="200px" data-onstyle="primary" data-offstyle="success" data-toggle="toggle" data-on="Load Lessons" data-off="Create Lessons" name="TXTloadtemplates" id="LoadTemplates" value="Loadtemplates" onchange="comboSelectionLoadTemplateLessons()">
                </div>
                <div class="col-xs-3 center-block">
                    <label class="control-label"><spring:message code="etiq.namelessons"/></label>
                    <input type="text" class="form-control" name="TXTnombreLessons" id="NameLessons" required="" placeholder="<spring:message code="etiq.namelessons"/>">
                </div>
                
                <div class="hidden col-xs-12" id="divCrearLessons" style="padding-left: 0px;">
                    <div class="col-xs-3 center-block">
                        <label class="control-label"><spring:message code="etiq.txtsubsection"/></label>
                        <select class="form-control" name="TXTsubsection" id="subsection" onchange="comboSelectionSubsection()">
                           <c:forEach var="subsection" items="${subsections}">
                                    <option value="${subsection.id[0]}" >${subsection.name}</option>
                                </c:forEach>
                        </select>
                    </div>
                    <div class="col-xs-3 center-block">
                        <label class="control-label"><spring:message code="etiq.txtequipment"/></label>
                        <select class="form-control" name="TXTequipment" id="equipment" multiple>
                           <c:forEach var="equipment" items="${equipments}">
                                    <option value="${equipment.id[0]}" >${equipment.name}</option>
                                </c:forEach>
                        </select>
                    </div>
                </div>
                        
                <div class="hidden col-xs-12" id="divLoadLessons" style="padding-left: 0px;">   
                    <div class="col-xs-3 center-block">
                        <label class="control-label">Select template lessons</label>
                        <select class="form-control" name="lessons" id="lessons" onchange="comboSelectionTemplateLessons()">
                           <c:forEach var="template" items="${lessons}">
                                    <option value="${template.id}" >${template.name}</option>
                                </c:forEach>
                        </select>
                    </div>
<%--                    <div class="col-xs-3 center-block">
                        <label class="control-label"><spring:message code="etiq.txtsubsection"/></label>
                        <select class="form-control" name="TXTsubsection" id="template">
                             <option value="${subsection.name}" >${subsection.name}</option>

                        </select>
                    </div>--%>
                    <div class="col-xs-3 center-block">
                        <label class="control-label"><spring:message code="etiq.txtequipment"/> Template</label>
                        <select class="form-control" name="TXTequipment" id="template" multiple>
                            <c:forEach var="allequipments" items="${allequipments}">
                                <option selected="true" value="${allequipments.id[0]}" >${allequipments.name}</option>
                            </c:forEach><%--
                            <c:forEach var="equipments" items="${equipments}">
                                <option value="${allequipments.id[0]}" >${allequipments.name}</option>
                            </c:forEach>--%>
                        </select>
                    </div>
                </div>    
   
            </fieldset>
            <fieldset>
                    <legend>Select students</legend>
                    <div class="col-xs-12">
                        <div class="col-xs-2"></div>
                        <div class="col-xs-3">
                            <label>Filter</label>

                            
                        </div>
                    </div>
                    <div class="col-xs-12">
                        <div class="col-xs-2">
                            <select class="form-control" name="levelStudent" id="levelStudent" style="width: 100% !important;" onchange="comboSelectionLevelStudent()">

                                <c:forEach var="levels" items="${gradelevels}">
                                    <option value="${levels.id[0]}" >${levels.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="col-xs-3">
                            <select class="form-control" size="20" multiple name="origen[]" id="origen" style="width: 100% !important;">
                                <c:forEach var="alumnos" items="${listaAlumnos}">
                                    <option value="${alumnos.id_students}" >${alumnos.nombre_students}</option>
                                </c:forEach>
                            </select>
                        </div>
                        
                        <div class="col-xs-2">
                            <div class="col-xs-12 text-center" style="padding-bottom: 10px;">
                                <input type="button" class="btn pasar" value="<spring:message code="etiq.txtadd"/> »">
                            </div>
                            <div class="col-xs-12 text-center" style="padding-bottom: 10px;">
                                <input type="button" class="btn quitar" value="« <spring:message code="etiq.txtremove"/>">
                            </div>
                            <div class="col-xs-12 text-center" style="padding-bottom: 10px;">
                                <input type="button" class="btn pasartodos" value="<spring:message code="etiq.txtaddAll"/> »">
                            </div>
                            <div class="col-xs-12 text-center" style="padding-bottom: 10px;">
                                <input type="button" class="btn quitartodos" value="« <spring:message code="etiq.txtremoveAll"/>">
                            </div>
                        </div>
                
                        <div class="col-xs-3">
                            <select class="form-control submit" size="20" multiple name="destino[]" id="destino" style="width: 100% !important;"> 

                            </select>
                        </div>
                    </div>
                <div class="col-xs-2"></div>
            </fieldset>
        <div class="col-xs-12 text-center">
            <input type="submit" class="btn btn-success" id="createOnClick" disabled="True" value="<spring:message code="etiq.txtcreate"/>">
        </div>
        </form:form>
        
        </div>
        <c:out value="${message}"/>
    </body>
</html>
