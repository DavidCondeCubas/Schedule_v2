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
    <%@ include file="infouser.jsp" %>
    <%@ include file="menu.jsp" %>
    <head>
        <title>Create Lessons</title>
        <script>

 $(document).ready(function(){
       var userLang = navigator.language || navigator.userLanguage;
       var myDate = new Date();
         //Muestra calendario
         //VARIABLE CUANDO HEMOS CREADO UNA LESSONS CORRECTAMENTE
         var lessoncreate = '<%= request.getParameter("message") %>';
    
     if (lessoncreate === 'Lesson created' ){
     $('#myModal').modal({
        show: 'false'
    });
    }
            $( "#showPropiertys" ).click(function() {
                    $("#contenedorPropiertys").toggleClass('in');
                    //$(this).html('Lesson name and description<span class="glyphicon glyphicon-triangle-bottom"></span>');
            });
            $( "#showDate" ).click(function() {
                    $("#contenedorDate").toggleClass('in');
                    //$(this).html('Lesson name and description<span class="glyphicon glyphicon-triangle-bottom"></span>');
            });
            $( "#showDetails" ).click(function() {
                    $("#contenedorDetails").toggleClass('in');
                    //$(this).html('Lesson name and description<span class="glyphicon glyphicon-triangle-bottom"></span>');
            });
            $( "#showStudents" ).click(function() {
                    $("#contenedorStudents").toggleClass('in');
                    //$(this).html('Lesson name and description<span class="glyphicon glyphicon-triangle-bottom"></span>');
            });

            $( "#ideaCheck" ).change(function() {
                if($(this).is(":checked")) {
                    $('#showDate').addClass("desactivada");
                    $('#showDate').off('click');
                    $("#contenedorDate").removeClass('in');
                    $('#showStudents').addClass("desactivada");
                    $('#showStudents').off('click');
                    $("#contenedorStudents").removeClass('in');
                    
                }else if($("#ideaCheck :not(:checked)")) 
                {
                    $('#showDate').removeClass("desactivada");
                    $('#showDate').on('click', function(){ $("#contenedorDate").toggleClass('in');} );
//                    $("#contenedorDate").removeClass('in');
                    $('#showStudents').removeClass("desactivada");
                    $('#showStudents').on('click', function(){ $("#contenedorDate").toggleClass('in');} );
                    $("#contenedorStudents").addClass('in');   
                    }
            });
            

            
$("#method").on('mouseover', 'option' , function(e) {
    
        var $e = $(e.target);
    
    if ($e.is('option')) {
        $('#method').popover('destroy');
        $("#method").popover({
            animation: 'true',
            trigger: 'hover',
            placement: 'right',
            title: $e.attr("data-title"),
            content: $e.attr("data-content")
        }).popover('show');
    }
});
         
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
        
    });            
        
        $().ready(function() 
	{ 
                  
		$('.pasar').click(function() {
                    !$('#origen option:selected').remove().appendTo('#destino');
                    var alumnosSelected = $('#destino').length;
                    var objectiveSelected = $('#objective').val();
                    if(alumnosSelected !== 0 && objectiveSelected !== 0 && objectiveSelected !== null && objectiveSelected !== ''){
                        $('#createOnClick').attr('disabled', false);
                    }
                    return;
                });  
		$('.quitar').click(function() {
                    !$('#destino option:selected').remove().appendTo('#origen');
                    var alumnosSelected = $('#destino').length;
                    var objectiveSelected = $('#objective').val();
                    if(alumnosSelected === 0 || ( objectiveSelected === 0 || objectiveSelected === null || objectiveSelected === '')){
                        $('#createOnClick').attr('disabled', true);
                    }
                    return;  
                });
		$('.pasartodos').click(function() {
                    $('#origen option').each(function() { $(this).remove().appendTo('#destino'); });
                    var objectiveSelected = $('#objective').val();
                    if( objectiveSelected === 0 || objectiveSelected === null || objectiveSelected === ''){
                        $('#createOnClick').attr('disabled', true);
                    }
                });
		$('.quitartodos').click(function() {
                    $('#destino option').each(function() { $(this).remove().appendTo('#origen'); });
                    $('#createOnClick').attr('disabled', true);
                });
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
        ajax.open("POST","studentlistLevel.htm?seleccion="+seleccion,true);
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
    function funcionCallBackObjective()
    {
           if (ajax.readyState===4){
                if (ajax.status===200){
                    document.getElementById("objective").innerHTML= ajax.responseText;
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

    function funcionCallBackIdeaLessons()
    {
           if (ajax.readyState===4){
                if (ajax.status===200){
                   var json = JSON.parse(ajax.responseText);
                   var level = json.level;
                   var subject =  JSON.parse(json.subject).id;
                   
                   var objective =  JSON.parse(json.objective).id;
                   var method =  JSON.parse(json.method).id;
                   var content =  JSON.parse(json.content);
                   var subjects = JSON.parse(json.subjectslist);
                   var objectives = JSON.parse(json.objectiveslist);
                   var contents = JSON.parse(json.contentslist);
                   $('#subject').empty();
                     $.each(subjects, function(i, item) {
                        var test = subjects[i].id;
                        if( test === subject){
                             $('#subject').append('<option selected value= "'+subjects[i].id+'">' + subjects[i].name + '</option>');
                        }
                        else{
                         $('#subject').append('<option value= "'+subjects[i].id+'">' + subjects[i].name + '</option>');
                    }
                   });
                    }
                }
            }
            
    function funcionCallBackContent()
    {
           if (ajax.readyState===4){
                if (ajax.status===200){
                    document.getElementById("content").innerHTML= ajax.responseText;
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
        ajax.open("POST","subjectlistLevel.htm?seleccion1="+seleccion1,true);
        
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

        
        ajax.onreadystatechange=funcionCallBackObjective;
        var seleccion2 = document.getElementById("subject").value;
        ajax.open("POST","objectivelistSubject.htm?seleccion2="+seleccion2,true);

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
        ajax.open("POST","namelistSubject.htm?seleccionTemplate="+seleccionSubject,true);
        ajax.send("");
    }
     function comboSelectionIdeaLessons()
    {
        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        }
        else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }
        
        
        ajax.onreadystatechange=funcionCallBackIdeaLessons;
        var seleccionidea = document.getElementById("ideas").value;
        //ajax.open("POST","createlesson.htm?select=objectivelistSubject&seleccion2="+seleccionTemplate,true);
        ajax.open("POST","copyfromIdea.htm?seleccionidea="+seleccionidea,true);
        ajax.send("");
    }
     function comboSelectionObjective()
    {
        if (window.XMLHttpRequest) //mozilla
        {
            ajax = new XMLHttpRequest(); //No Internet explorer
        }
        else
        {
            ajax = new ActiveXObject("Microsoft.XMLHTTP");
        }

        if(document.getElementById("objective").value === 0 || document.getElementById("objective").value === '' || document.getElementById("destino").length === 0 ){
            $('#createOnClick').attr('disabled', true);
        }else{
            $('#createOnClick').attr('disabled', false);
        }
        ajax.onreadystatechange=funcionCallBackContent;
        var seleccion3 = document.getElementById("objective").value;
        ajax.open("POST","contentlistObjective.htm?seleccion3="+seleccion3,true);
        ajax.send("");
    }
    
        </script>
        <style>
            textarea 
            {
            resize: none;
            }
            .popover{
                width: 500px;
            }
            .unStyle
            {
                text-align: right;
                background-color: transparent !important;
                outline: none !important;
                box-shadow: none;
                border: none;
            }
            .desactivada
            {
                color: grey;
                text-decoration: line-through;
            }
            .btn span.glyphicon {    			
	opacity: 0;				
}
.btn.active span.glyphicon {				
	opacity: 1;				
}
/*STILOS CHECKBOX*/

.checkbox {
  padding-left: 20px; }
.checkbox label {
  display: inline-block;
  vertical-align: middle;
  position: relative;
    padding-left: 5px; }
.checkbox label::before {
  content: "";
  display: inline-block;
  position: absolute;
  width: 17px;
  height: 17px;
  left: 0;
  margin-left: -20px;
  border: 1px solid #cccccc;
  border-radius: 3px;
  background-color: #fff;
  -webkit-transition: border 0.15s ease-in-out, color 0.15s ease-in-out;
  -o-transition: border 0.15s ease-in-out, color 0.15s ease-in-out;
      transition: border 0.15s ease-in-out, color 0.15s ease-in-out; }
.checkbox label::after {
  display: inline-block;
  position: absolute;
  width: 16px;
  height: 16px;
  left: 0;
  top: 0;
  margin-left: -20px;
  padding-left: 3px;
  padding-top: 1px;
  font-size: 11px;
      color: #555555; }
.checkbox input[type="checkbox"],
.checkbox input[type="radio"] {
  opacity: 0;
  z-index: 1;
  cursor: pointer;
}
.checkbox input[type="checkbox"]:focus + label::before,
.checkbox input[type="radio"]:focus + label::before {
  outline: thin dotted;
  outline: 5px auto -webkit-focus-ring-color;
      outline-offset: -2px; }
.checkbox input[type="checkbox"]:checked + label::after,
.checkbox input[type="radio"]:checked + label::after {
  font-family: "fontprincipal";
  content: '✔';}
.checkbox input[type="checkbox"]:indeterminate + label::after,
.checkbox input[type="radio"]:indeterminate + label::after {
  display: block;
  content: "";
  width: 10px;
  height: 3px;
  background-color: #555555;
  border-radius: 2px;
  margin-left: -16.5px;
  margin-top: 7px;
}
.checkbox input[type="checkbox"]:disabled,
.checkbox input[type="radio"]:disabled {
    cursor: not-allowed;
}
.checkbox input[type="checkbox"]:disabled + label,
.checkbox input[type="radio"]:disabled + label {
      opacity: 0.65; }
.checkbox input[type="checkbox"]:disabled + label::before,
.checkbox input[type="radio"]:disabled + label::before {
  background-color: #eeeeee;
        cursor: not-allowed; }
.checkbox.checkbox-circle label::before {
    border-radius: 50%; }
.checkbox.checkbox-inline {
    margin-top: 0; }


.checkbox-success input[type="checkbox"]:checked + label::before,
.checkbox-success input[type="radio"]:checked + label::before {
  background-color: #99CC66;
  border-color: #99CC66; }
.checkbox-success input[type="checkbox"]:checked + label::after,
.checkbox-success input[type="radio"]:checked + label::after {
  color: #fff;}



.checkbox-success input[type="checkbox"]:indeterminate + label::before,
.checkbox-success input[type="radio"]:indeterminate + label::before {
  background-color: #99CC66;
  border-color: #99CC66;
}

.checkbox-success input[type="checkbox"]:indeterminate + label::after,
.checkbox-success input[type="radio"]:indeterminate + label::after {
  background-color: #fff;
}


input[type="checkbox"].styled:checked + label:after,
input[type="radio"].styled:checked + label:after {
  font-family: 'fontprincipal';
  content: '✔'; }
input[type="checkbox"] .styled:checked + label::before,
input[type="radio"] .styled:checked + label::before {
  color: #fff; }
input[type="checkbox"] .styled:checked + label::after,
input[type="radio"] .styled:checked + label::after {
  color: #fff; }

        </style>
    </head>
    <body>
        <div class="container">
        <h1 class="text-center">Edit Presentation Idea</h1>

        
        <form:form id="formStudents" method ="post" action="savelessonidea.htm" >
            <fieldset>
                <legend id="showPropiertys">
                    Presentation name and description
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="contenedorPropiertys">
                    <div class="col-xs-6 center-block">
                        <label class="control-label">Presentation Name</label>
                        <input type="text" class="form-control" name="TXTnombreLessons" id="NameLessons" required="" placeholder="<spring:message code="etiq.namelessons"/>" value="${data.name}">
                    </div>               
                    <div class="col-xs-6 center-block form-group">
                        <label class="control-label">Presentation description</label>
                        <textarea class="form-control" name="TXTdescription" id="comments" placeholder="add description" maxlength="200">${data.comments}</textarea>
                    </div>
                    
                </div>
            </fieldset>
            
            <fieldset>
                <legend id="showDetails">
                    Presentation details
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
<!--                        <button type="button" class="unStyle" data-toggle="collapse" data-target="#contenedorDetails" >
                            <span class="glyphicon glyphicon-triangle-bottom"></span>
                        </button>-->
                    </span>
                </legend>
                <div class="form-group collapse" id="contenedorDetails">
                <div class="col-xs-3 form-group">
                    <label class="control-label"><spring:message code="etiq.txtlevels"/></label>
                    <select class="form-control" name="TXTlevel" id="level" onchange="comboSelectionLevel()">
                        <c:forEach var="levels" items="${gradelevels}">
                            <c:if test="${levels.id[0] == data.level.id[0]}">
                             <option selected value="${data.level.id[0]}">${data.level.name}</option>
                            </c:if>
                            <option value="${levels.id[0]}" >${levels.name}</option>
                        </c:forEach>
                    </select>
                          
                </div>
                <div class="col-xs-3 center-block">
                    <label class="control-label"><spring:message code="etiq.txtsubject"/></label>
                    <select class="form-control" name="TXTsubject" id="subject"  onchange="comboSelectionSubject()">
                    
                       <c:forEach var="subject" items="${subjects}">
                           <c:if test="${subject.id[0] == data.subject.id[0]}">
                             <option selected value="${data.subject.id[0]}">${data.subject.name}</option>
                            </c:if>
                                <option value="${subject.id[0]}" data-toggle="tooltip" data-placement="top" title="<spring:message code="etiq.txthome"/>">${subject.name}</option>
                            </c:forEach>
                    </select>
                </div>
<!--                <div class="col-xs-3 center-block text-center">
                    <label class="control-label">Select your option</label>
                    <div class="btn-group" data-toggle="buttons" name="TXTloadtemplates" id="LoadTemplates" value="Loadtemplates" onchange="comboSelectionLoadTemplateLessons()">
                        <label class="btn btn-primary active disabled">
                            <input type="radio" name="options" id="option1" autocomplete="off" value="option1">Create Lessons
                        </label>
                        <label class="btn btn-success disabled">
                            <input type="radio" name="options" id="option2" autocomplete="off" value="option2">Load Lessons
                        </label>
                    </div>
<%--                    <input disabled="true" type="checkbox" data-width="200px" data-onstyle="primary" data-offstyle="success" data-toggle="toggle" data-on="Create Lessons" data-off="Load Lessons" name="TXTloadtemplates" id="LoadTemplates" value="Loadtemplates" onchange="comboSelectionLoadTemplateLessons()">--%>
                </div>-->
                
                
                    <div class="col-xs-3 center-block form-group">
                        <label class="control-label">Objective</label>
                        <select class="form-control" name="TXTobjective" id="objective" onchange="comboSelectionObjective()">
                           <c:forEach var="objective" items="${objectives}">
                                <c:choose>
                                
                                    <c:when test="${objective.id[0] == data.objective.id[0]}">
                                        <option selected="" value="${objective.id[0]}" >${objective.name}</option>

                                    </c:when>

                                    <c:otherwise>
                                        <option value="${objective.id[0]}" >${objective.name}</option>
                                    </c:otherwise>    
                                    
                                    
                            </c:choose>
                                </c:forEach>
                        </select>
                    </div>

                    <div class="col-xs-3 center-block form-group">
                        <label class="control-label">Content</label>
                                     <select class="form-control" name="TXTcontent" id="content" multiple>
                            <c:forEach var="content" items="${contents}">
                                    <c:forEach var="selcontent" items="${data.contentid}">
                            <c:choose>
                                
                                    <c:when test="${content.id[0] == selcontent}">
                                        <option selected="" value="${content.id[0]}" >${content.name}</option>
<%--                                        <option selected >${selcontent}</option>--%>
                                    </c:when>

                                    <c:otherwise>
                                        <option value="${content.id[0]}" >${content.name}</option>
                                    </c:otherwise>    
                                    
                                    
                            </c:choose>
                                        
                                        </c:forEach>
                                </c:forEach>
                        </select>
                    </div>
  
                    <div class="col-xs-12" id="divLoadLessons" style="padding-left: 0px;">   
                      
                    </div>    
                    <div class="col-xs-3 center-block form-group">
                        <label class="control-label">Method</label>
                        <select class="form-control" name="TXTmethod" size="2" id="method">  
                            
                            <c:forEach var="method" items="${methods}">
                                 <c:choose>
                                
                                    <c:when test="${method.id[0] == data.method.id[0]}">
                                        <option selected="" value="${method.id[0]}" >${method.name}</option>
                                    </c:when>

                                    <c:otherwise>
                                        <option value="${method.id[0]}" >${method.name}</option>
                                    </c:otherwise>    
                                    
                                    
                            </c:choose>
                            </c:forEach>
                        </select>

                    </div>
                </div>
            </fieldset>
            
            <div class="col-xs-12 text-center">
            <input type="submit" class="btn btn-success" id="createOnClick" value="Save">
            </div>
        </form:form>
        
        </div>
<%--        <div class="col-xs-6">
                <div class="form-group">
                    <label class="control-label"></label>
                        <div class='input-group' style="margin-top:19px;">
                            <form:form id="formCreate" action="createsetting.htm?select=start">
                               <button type="submit" id="crearLessons" value="Crear" class="btn btn-success">Create Settings</button>
                            </form:form>
                        </div>
                </div>
        </div>--%>
<!--<div class="col-xs-12">
<button type="button" class="btn btn-primary btn-lg" data-toggle="modal" data-target="#myModal">
  Launch demo modal
</button>
</div>-->
<div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">
  <div class="modal-dialog" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
<!--        <h4 class="modal-title" id="myModalLabel">Modal title</h4>-->
      </div>
      <div class="modal-body text-center">
       <H1><%= request.getParameter("message") %></H1>
      </div>
<!--      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
        <button type="button" class="btn btn-primary">Save changes</button>
      </div>-->
    </div>
  </div>
</div>



    </body>
</html>