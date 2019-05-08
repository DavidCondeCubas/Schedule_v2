<%@page import="java.text.DecimalFormat"%>
<%@page import="model.Algoritmo"%>
<%@page import="dataManage.Restrictions"%>
<%@page import="java.util.TreeMap"%>
<%@page import="model.Room"%>
<%@page import="dataManage.Tupla"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="model.Course"%>
<%@page import="dataManage.Exceptions"%>
<%@page import="dataManage.Consultas"%>
<%@page import="model.Student"%>
<%@page import="model.Teacher"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <%try {%>   
    <head>
        <!-- ESTO ES EL NUEVO BRANCH-->

        <%@ include file="infouser.jsp" %>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Menu</title>
        <style>
            tr.tcolores{
                background-color: lightblue !important;
            }
            #table_id{
                table-layout: fixed;
            }
            #table_id td{
                height: 33%;
            }
            .espacioLibre{
                background-color: green;
            }
            .course
            {
                page-break-inside: avoid !important;
               
            }
            .course{
                border-bottom: solid 3px black;padding-top: 10px;
            }
            .students
            {
                border-bottom: 1px black solid;
            }
            .horario
            {
                height: 35px;
            }
            .contentTable{
                text-align: center;

            }

            @media print
            {
                .noPrint
                {
                    display: none;
                }
                tr.tcolores{
                    background-color: lightblue !important;
                }
                html, body, table
                {
                    font-size: 9pt;
                }
            }
        </style>
        <script>
            $(document).ready(function () {

                $("#showSTC").click(function () {
                    $("#STC").toggleClass('in');
                });

                $("#showTC").click(function () {
                    $("#TC").toggleClass('in');
                });

                $("#showLogComplete").click(function () {
                    $("#LogComplete").toggleClass('in');
                });


                $("#showCourses").click(function () {
                    $("#Coursestable").toggleClass('in');
                });

                $("#showRooms").click(function () {
                    $("#Roomstable").toggleClass('in');
                });
                $("#showRooms2").click(function () {
                    $("#Roomstable2").toggleClass('in');
                });
                $("#showRoomsdisp").click(function () {
                    $("#Roomsdisp").toggleClass('in');
                });
                $("#showRoomsdisp2").click(function () {
                    $("#Roomsdisp2").toggleClass('in');
                });
                $("#showCoursesenrol").click(function () {
                    $("#Coursesenrol").toggleClass('in');
                });

                $("#showFullSchedule").click(function () {
                    $("#FullSchedule").toggleClass('in');
                });

                $("#showTeachers").click(function () {
                    $("#Teacherstable").toggleClass('in');
                });

                $("#showTeachers2").click(function () {
                    $("#Teacherstable2").toggleClass('in');
                });

                $("#showTeachersdisp").click(function () {
                    $("#Teachersdisp").toggleClass('in');
                });
                $("#showTeachersdisp2").click(function () {
                    $("#Teachersdisp2").toggleClass('in');
                });

                $("#showStudents").click(function () {
                    $("#Studentstable").toggleClass('in');
                });

                $("#showStudentsEnrolled").click(function () {
                    $("#StudentsEnrolled").toggleClass('in');
                });
                
                $("#showObservations").click(function () {
                    $("#Observationstable").toggleClass('in');
                });

            });

            function enviando()
            {

                location.reload();

                $('#pleaseWaitDialog').modal({
                    backdrop: 'static',
                    keyboard: false
                });
                $('#pleaseWaitDialog').modal('show');


                var start = new Date();
                var maxTime = 150000;
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

        <%  //Definición de variables y captura de datos de algoritmo (en dicha clase se mandan ModelAndView por cada request.getAttribute de esta página):

            DecimalFormat df = new DecimalFormat("#.00");
            Exceptions avisos = (Exceptions) request.getAttribute("avisos");
            Consultas cs = (Consultas) request.getAttribute("cs");
            Integer TAMX = (Integer) request.getAttribute("TAMX");
            Integer TAMY = (Integer) request.getAttribute("TAMY");
            ArrayList<Tupla<String, String>> headRow = (ArrayList<Tupla<String, String>>) request.getAttribute("hFilas");
            ArrayList<String> headCol = (ArrayList<String>) request.getAttribute("hcols");
            String[][] templateText = (String[][]) request.getAttribute("templateText");
            List<Course> courses = (List) request.getAttribute("Courses");
            List<Teacher> lista = (List) request.getAttribute("profesores");
            HashMap<Integer, Room> roomsHash = (HashMap<Integer, Room>) request.getAttribute("rooms");
            ArrayList<Room> rooms = new ArrayList<>(roomsHash.values());
            boolean activeRoom = (boolean) request.getAttribute("activeRoom");
            HashMap<Integer, Student> lista2 = (HashMap) request.getAttribute("students");
            List<Student> studentsOrdered = (List) request.getAttribute("orderedStudents");
            HashMap<Integer, String> hashPersons = (HashMap) request.getAttribute("persons");
            ArrayList<String> log = (ArrayList<String>) request.getAttribute("log");
            boolean swapcolor = true;
            String headCols = "<tr><th>Period</th>";
            for (String s : headCol) {
                headCols += "<th class='text-center'>" + s;
                headCols += "</th>";
            }
            headCols += "</tr>";
        %>
        <div class="col-xs-12">
            <c:out value="${estudiante.identificacion}"></c:out>
            </div>
            <div class="col-xs-12 text-center noPrint" id="myTab">
                <div class="col-xs-9">
                    <ul class="nav nav-tabs">
                        <li class="active"><a id="Courses" data-toggle="tab" href="#courses" role="tab" >Courses</a></li>
                        <li><a id="Teachers" data-toggle="tab" href="#teachers" role="tab">Teachers</a></li>
                        <li><a id="Students" data-toggle="tab" href="#students" role="tab">Students</a></li>

                    <%-- En el siguiente if de activeRoom se decide si mostrar la pestaña de Rooms o no. Proceso:
                    1) En la clase ScheduleController se captura si se ha dado a disabled o enabled en el apartado de Active Room de la página web y se aplica el método updateActiveRooms
                   para establecer la variable activeRooms (de la clase Restrictions) true o false.
                    2) Este valor se pasa a la vista (esta página) desde Algoritmo en forma de ModelAndView (mv.addObject("activeRoom", r.activeRooms))
                    3) El valor obtenido se le transmite a la siguiente variable activeRoom--%>

                    <%if (activeRoom == true) {%>
                    <li><a id="Rooms" data-toggle="tab" href="#rooms" role="tab">Rooms</a></li>       
                        <%  }%>     
                    <li><a id="Observations" data-toggle="tab" href="#observations" role="tab">Observations</a></li>
                </ul>
            </div>
            <div class="col-xs-3">
                <button id="btnRefresh" type="button" class="btn btn-info" onclick="enviando()">Reload</button>
            </div>
        </div>


        <div class="tab-content">

            <%-- --------------------------------------------------------------COURSES ----------------------------------------------- --%>

            <div role="tabpanel" class="col-xs-12 tab-pane in active" id="courses">
                <div class="col-xs-12">
                    <legend id="showCourses" class="noPrint">
                        Schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>

                    <div class="form-group collapse" id="Coursestable">

                        <%


                            //------------------------------ DATOS DE LA PESTAÑA SCHEDULE DE COURSES -----------------------------------------------

 //acumTeacher sirve para mostrar en la pestaña Teachers del Schedule solo los profesores que están dando clase (los que aparecen en la pestaña de Courses)                      
                            String acumTeacher = "";
                            String[][] tupla = new String[100][100];
                            for (int j = 0; j < 99; j++) {
                                for (int k = 0; k < 99; k++) {
                                    tupla[k][j] = "";

                                }
                            }
                            for (Course t : courses) {
                                //1º Impresión de teachers, sections y students para cada curso:
                                if (t.hayEstudiantes()) {
                                    out.println("<div class='col-xs-12 course'>");
                                    out.println("<h3>" + cs.getAbbrevCourses().get(t.getIdCourse()) + "</h3>");
                                    out.println("<table id='table_id' width='100%' border='0' class='table' style='page-break-inside: avoid !important;'>");
                                    out.println("<tr class='students'>");
                                    for (int j = 0; j < t.getArraySecciones().size(); j++) {
                                        String studentNames = "";

                                        // nameTeacher se usa en cada curso (pestaña Course) y acumTeacher acumula los datos para usar en la pestaña Teachers:
                                        String nameTeacher = "" + hashPersons.get(t.getArraySecciones().get(j).getIdTeacher());
                                        acumTeacher += "" + hashPersons.get(t.getArraySecciones().get(j).getIdTeacher()) + ";";

                                        if (!nameTeacher.equals("null")) {
                                            out.println("<td valign=\"top\"><strong>Section " + t.getArraySecciones().get(j).getNameSeccion() + ":<br>"
                                                    + "Teacher: " + nameTeacher + " </strong>");
                                        } else {
                                            out.println("<td valign=\"top\"><strong>Section " + t.getArraySecciones().get(j).getNameSeccion() + ":<br>"
                                                    + "There's no teacher available in Section/Course </strong>");

                                        }
                                        if (t.getArraySecciones().get(j).getIdStudents().size() == 0) {
                                            out.println("<br> There's no students enrolled in this Section");

                                        }
                                        else{
                                            for (int k = 0; k < t.getArraySecciones().get(j).getIdStudents().size(); k++) {
                                                if(lista2.containsKey(t.getArraySecciones().get(j).getIdStudents().get(k)))
                                                studentNames += "<br>" + (k + 1) + "- " + lista2.get(t.getArraySecciones().get(j).getIdStudents().get(k)).getName();
                                            }                                           
                                        }

                                        out.println(studentNames);
                                        out.println("</td>");

                                    }
                                    out.println("</tr>");

                                    //2º: Impresión de horarios por curso:
                                    //Gracias a swapcolor se alterna el azul y el blanco por cada fila del schedule:
                                    out.println(headCols);
                                    swapcolor = true;
                                    for (int i = 0; i < TAMY; i++) {
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores horario'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr class='horario'>");
                                            swapcolor = true;
                                        }
                                        if (i < headRow.size()) {
                                            out.println("<td>" + headRow.get(i).text() + "</td>");
                                        } else {
                                            out.println("<td></td>");
                                        }
                                        for (int j = 0; j < TAMX; j++) {
                                            //Para cada hueco: primero se pregunta si hay más de una sección o no, y el resultado se guarda en el String acumulación para imprimirlo por pantalla:
                                            if (t.getHuecos()[j][i].contains("and")) {
                                                String aux = t.getHuecos()[j][i];
                                                ArrayList<Integer> cadena = new ArrayList<>();
                                                String acumulacion = "";
                                                String[] splitString = null;
                                                splitString = aux.split(" and ");

                                                cadena = new ArrayList<>();
                                                for (String st : splitString) {
                                                    cadena.add(((t.getIdCourse() * 100) + Integer.parseInt(st)));
                                                }
                                                for (Integer st : cadena) {
                                                    acumulacion += cs.nameSection(st) + " and ";
                                                }
                                                acumulacion = acumulacion.substring(0, acumulacion.length() - 5);
                                                out.println("<td class='text-center'>" + cs.nameCourse(t.getIdCourse()) + " Sections: " + acumulacion + "</td>");
                                                //En el array bidimensional tupla se va guardando la información de cursos y secciones para después poder mostrarlo en Full Schedule:
                                                if (tupla[j][i].equals("")) {
                                                    if (templateText[j + 1][i + 1] != "" && !tupla[j][i].contains(templateText[j + 1][i + 1])) {
                                                        tupla[j][i] = tupla[j][i].concat("<td class='text-center'>" + templateText[j + 1][i + 1] + "<br/>" + cs.nameCourse(t.getIdCourse()) + " Sections: " + acumulacion + "<br/>");

                                                    } else {
                                                        tupla[j][i] = tupla[j][i].concat("<td class='text-center'>" + cs.nameCourse(t.getIdCourse()) + " Sections: " + acumulacion + "<br/>");
                                                    }

                                                } else {
                                                    tupla[j][i] += cs.nameCourse(t.getIdCourse()) + " Sections: " + acumulacion + "<br/>";
                                                }
                                            } else if (!t.getHuecos()[j][i].equals("0")) {
                                                int aux = Integer.parseInt(t.getHuecos()[j][i]);
                                                aux = t.getIdCourse() * 100 + aux;
                                                out.println("<td class='text-center'>" + cs.nameCourse(t.getIdCourse()) + " Section: " + cs.nameSection(aux) + "</td>");
                                                System.out.println("");
                                                if (tupla[j][i].equals("")) {
                                                    if (templateText[j + 1][i + 1] != "" && !tupla[j][i].contains(templateText[j + 1][i + 1])) {
                                                        tupla[j][i] += "<td class='text-center'>" + templateText[j + 1][i + 1] + "<br/>" + cs.nameCourse(t.getIdCourse()) + " Section: " + cs.nameSection(aux) + "<br/>";
                                                    } else {
                                                        tupla[j][i] += "<td class='text-center'>" + cs.nameCourse(t.getIdCourse()) + " Section: " + cs.nameSection(aux) + "<br/>";
                                                    }

                                                } else {
                                                    tupla[j][i] += cs.nameCourse(t.getIdCourse()) + " Section: " + cs.nameSection(aux) + "<br/>";
                                                }

                                            } else {
                                                if (templateText[j + 1][i + 1] != "" && !tupla[j][i].contains(templateText[j + 1][i + 1])) {
                                                    tupla[j][i] += "<td class='text-center'>" + templateText[j + 1][i + 1] + "</td>";
                                                }
                                                out.println("<td> </td>");
                                            }

                                        }

                                        out.println("</tr>");

                                    }
                                    out.println("</table>");
                                    out.println("</div>");
                                }
                            }
                        %>
                    </div>
                </div>
                <div class="col-xs-12">
                    <%-- Full Schedule sirve a modo resumen de todos los huecos ocupados por todas las secciones de todos los cursos del apartado anterior:--%>
                    <legend id="showFullSchedule" class="noPrint">
                        Full Schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="col-xs-12 form-group collapse" id="FullSchedule">
                        <%
                            out.println("<div class='col-xs-12'>");
                            out.println("<table id='table_id' width='100%' border='0' class=''>");

                            out.println(headCols);
                            swapcolor = true;

                            for (int i = 0; i < TAMY; i++) {

                                if (swapcolor) {
                                    out.println("<tr class='tcolores horario'>");
                                    swapcolor = false;
                                } else {
                                    out.println("<tr class='horario'>");
                                    swapcolor = true;
                                }
                                if (i < headRow.size()) {
                                    out.println("<td>" + headRow.get(i).text() + "</td>");
                                } else {
                                    out.println("<td></td>");
                                }
                                //Toda la información se muestra para cada hueco que debe rellenarse gracias al array bidimensional tupla:
                                for (int j = 0; j < TAMX; j++) {
                                    if (!tupla[j][i].equals("")) {
                                        tupla[j][i] += "</td>";

                                        out.println(tupla[j][i]);

                                    } else {
                                        out.println("<td> </td>");
                                    }
                                }

                                out.println("</tr>");
                            }

                            out.println(
                                    "</table>");

                            out.println(
                                    "</div>");

                        %>


                    </div>
                </div>
                <div class="col-xs-12">   
                    <legend id="showCoursesenrol" class="noPrint">
                        <%-- Información de cursos/secciones que no han podido rellenar todos los estudiantes que estaban en los rosters de secciones/request de cursos por falta de espacio--%>
                        Missing Enrolled
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="col-xs-12 form-group collapse" id="Coursesenrol">
                        <%  for (Course c : courses) {
                                //Información: porcentaje de students enrolled, nº secciones no enrolled, students no enrolled de cada curso:
                                if (c.getPercentEnrolled() != 100) {
                                    out.println("<h3>" + cs.nameCourse(c.getIdCourse()) + "</h3>");
                                    out.println("<table id='table_id' class='table'>");
                                    out.println("<tr><th>Field</th><th>Content</th></tr>");

                                    out.println("<tr>");
                                    out.println("<td>Enrolled student percent</td>");
                                    if (c.getPercentEnrolled() < 1) {
                                        out.println("<td>" + "0" + df.format(c.getPercentEnrolled()) + "%" + "</td>");
                                    } else {
                                        out.println("<td>" + df.format(c.getPercentEnrolled()) + "%" + "</td>");
                                    }
                                    out.println("</tr>");

                                    out.println("<tr>");
                                    out.println("<td>Number of sections no enrolled</td>");
                                    out.println("<td>" + c.getSectionsNoEnrolled() + "</td>");
                                    out.println("</tr>");

                                    String studentNames = "";
                                    out.println("<tr>");
                                    out.println("<td>Students no enrolled</td>");
                                    out.println("<td>");
                                    if (!c.getStudentsNoAsignados().isEmpty()) {
                                        studentNames += lista2.get(c.getStudentsNoAsignados().get(0)).getName();
                                    }
                                    for (int i = 1; i < c.getStudentsNoAsignados().size(); i++) {
                                        studentNames += "; " + lista2.get(c.getStudentsNoAsignados().get(i)).getName();
                                    }
                                    out.println(studentNames + ".");
                                    out.println("</td>");
                                    out.println("</tr>");
                                    out.println("</table>");

                                    //Mismo schedule que en el primer apartado de cursos (Schedule), pero solo con los cursos que no han conseguido meter el 100% de los students:
                                    out.println("<table id='table_id' class='table'>");
                                    out.println(headCols);
                                    swapcolor = true;
                                    for (int i = 0; i < TAMY; i++) {
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                        if (i < headRow.size()) {
                                            out.println("<td>" + headRow.get(i).text() + "</td>");
                                        } else {
                                            out.println("<td></td>");
                                        }
                                        for (int j = 0; j < TAMX; j++) {
                                            if (c.getHuecos()[j][i].contains("and")) {
                                                String aux = c.getHuecos()[j][i];
                                                ArrayList<Integer> cadena = new ArrayList<>();
                                                String acumulacion = "";
                                                String[] splitString = null;
                                                splitString = aux.split(" and ");

                                                cadena = new ArrayList<>();
                                                for (String st : splitString) {
                                                    cadena.add(((c.getIdCourse() * 100) + Integer.parseInt(st)));
                                                }
                                                for (Integer st : cadena) {
                                                    acumulacion += cs.nameSection(st) + " and ";
                                                }
                                                acumulacion = acumulacion.substring(0, acumulacion.length() - 5);
                                                out.println("<td class='text-center'>" + cs.nameCourse(c.getIdCourse()) + " Sections: " + acumulacion + "</td>");
                                            } else if (!c.getHuecos()[j][i].equals("0")) {
                                                int aux = Integer.parseInt(c.getHuecos()[j][i]);
                                                aux = c.getIdCourse() * 100 + aux;
                                                out.println("<td class='text-center'>" + cs.nameCourse(c.getIdCourse()) + " Section: " + cs.nameSection(aux) + "</td>");

                                            } else {
                                                out.println("<td> </td>");
                                            }

                                        }
                                        out.println("</tr>");
                                    }
                                    out.println("</table>");
                                } else {
                                    out.println("<h4> All students enrolled from " + cs.getAbbrevCourses().get(c.getIdCourse()) + "</h4>");

                                }
                            }

                        %>
                    </div>
                </div>
            </div>

            <%-- --------------------------------------------------------------TEACHERS ----------------------------------------------- --%>


            <div role="tabpanel" class="col-xs-12 tab-pane" id="teachers">
                <div class="col-xs-12">
                    <legend id="showTeachers">
                        Schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="form-group collapse" id="Teacherstable">
                        <%//------------------------- OBSERVACIONES: SE MUESTRAN AVISOS SI HA HABIDO ALGÚN PROBLEMA AL CARGAR LOS TEACHERS: ------------------------                        
                            //Aquí se muestra si se han asignado teachers a un curso que ya no tiene disponibilidad (y no deja que tenga más secciones ni cursos asociados).
                            //SÓLO SE HACE CON TEACHERS ASIGNADOS A LOS CURSOS. SI HA SIDO SELECCIONADO EN UNA SECCIÓN, AQUÍ EL PROGRAMA SÍ DEJA ASIGNARLOS Y NO AVISA:
                            out.println("<h2>Teachers</h2>");
                            for (Teacher t : lista) {

                                if (avisos.getTeachersFull().contains(t.getName())) {

                                    if (t.seccionesDisponibles(cs.getTotalBlocks()) < 1) {
                                        out.println(t.getName() + " has not section availability.");
                                    }
                                    if (t.prepsDisponibles(cs.getTotalBlocks()) < 1) {
                                        out.println(t.getName() + " has not course availability.");
                                    }
                                }
                            }
                            //------------------------- DATOS DE LA PESTAÑA SCHEDULE DE TEACHERS: ------------------------       
                            //Se cargan solo los teachers que han aparecido en Courses gracias a la variable acumTeacher:
                            for (Teacher t : lista) {
                                if (acumTeacher.contains(t.getName())) {
                                    out.println("<h3>" + t.getName() + "</h3>");
                                    out.println("<table id='table_id' class='table'>");
                                    out.println(headCols);
                                    swapcolor = true;
                                    for (int i = 0; i < TAMY; i++) {
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                        if (i < headRow.size()) {
                                            out.println("<td>" + headRow.get(i).text() + "</td>");
                                        } else {
                                            out.println("<td></td>");
                                        }
                                        for (int j = 0; j < TAMX; j++) {
                                            if (t.getHuecos()[j][i] != 0) {
                                                out.println("<td align='center'>" + cs.nameCourseAndSection(t.getHuecos()[j][i]) + "</td>");
                                            } else {
                                                out.println("<td></td>");
                                            }
                                        }
                                        out.println("</tr>");
                                    }
                                    out.println("</table>");
                                }
                            }
                        %>
                    </div>
                </div>
                <div class="col-xs-12">
                    <legend id="showTeachers2">
                        <%-- Datos de los profesores por columnas del schedule (por día de la semana) --%>                    
                        Teacher Master Schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="form-group collapse" id="Teacherstable2">
                        <%
                            //Se cargan solo los teachers que han aparecido en Courses gracias a la variable acumTeacher:                        
                            int countDays = 0;
                            for (String s : headCol) {
                                out.println("<h3>" + s + "</h3>");
                                out.println("<table class='table'>");
                                swapcolor = true;
                                out.println("<tr>");
                                out.println("<th>Teachers | Hours</th>");
                                for (int i = 0; i < headRow.size(); i++) {
                                    out.println("<th>" + headRow.get(i).text() + "</th>");
                                }
                                out.println("</tr>");

                                for (Teacher t : lista) {
                                    if (acumTeacher.contains(t.getName())) {
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                        out.println("<td>" + t.getName() + "</td>");
                                        for (int i = 0; i < TAMY; i++) {
                                            if (t.getHuecos()[countDays][i] != 0) {
                                                out.println("<td>" + cs.nameCourseAndSection(t.getHuecos()[countDays][i]) + "</td>");
                                            } else {
                                                out.println("<td></td>");
                                            }
                                        }
                                        out.println("</tr>");

                                    }
                                }
                                countDays++;
                                out.println("</table>");

                            }

                        %>
                    </div>
                </div>
                <div class="col-xs-12">
                    <legend id="showTeachersdisp">
                        Availability
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="form-group collapse" id="Teachersdisp">
                        <%//Carga de datos de los todos los profesores que se capturan de RenWeb, y se ve si tienen disponibilidad o no:                        
                            for (Teacher t : lista) {
                                out.println("<h3>" + t.getName() + "</h3>");
                                out.println("<table id='table_id' class='table'>");
                                out.println("<tr><th>Field</th><th>Content</th></tr>");

                                out.println("<tr>");
                                out.println("<td>Courses teaching</td>");
                                out.println("<td>");
                                String sumaCursos = "";
                                for (Integer i : t.getPrepsComplete()) {

                                    sumaCursos = sumaCursos.concat(", " + cs.getAbbrevCourses().get(i));
                                }
                                if (!sumaCursos.equals("")) {
                                    sumaCursos = sumaCursos.substring(1, sumaCursos.length());
                                    out.println(sumaCursos);
                                } else {
                                    out.println("0");
                                }
                                out.println("</td>");
                                out.println("</tr>");

                                out.println("<tr>");
                                out.println("<td>Section availability</td>");
                                out.println("<td>" + t.seccionesDisponibles(cs.getTotalBlocks()) + "</td>");
                                out.println("</tr>");

                                out.println("<tr>");
                                out.println("<td>Prep availability</td>");
                                out.println("<td>" + t.prepsDisponibles(cs.getTotalBlocks()) + "</td>");
                                out.println("</tr>");

                                out.println("</table>");
                            }

                        %>
                    </div>
                </div>
                <div class="col-xs-12">
                    <legend id="showTeachersdisp2">
                        Full Availability Teachers
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="form-group collapse" id="Teachersdisp2">
                        <%  //Aquí se cargan solo los profesores que no se han asignado finalmente a ningún curso (después de realizar el Schedule, no se refiere a los datos de RenWeb, sino al resultado final):
                            //Por lo tanto se cargarán todos los profesores que no estén en la variable acumTeacher:
                            out.println("<h2>Teachers</h2>");
                            out.println("<table id='table_id' class='table'>");
                            for (Teacher t : lista) {
                                if (!acumTeacher.contains(t.getName())) {

                                    out.println("<tr>");
                                    out.println("<td>" + t.getName() + "</td>");
                                    out.println("</tr>");

                                }
                            }
                            out.println("</table>");
                        %>
                    </div>
                </div>
            </div>

            <!-- Mensaje de carga de datos: -->
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
            <%-- -------------------------------------------------------------- STUDENTS ----------------------------------------------------------------- --%>            

            <div role="tabpanel" class="col-xs-12 tab-pane" id="students">
                <div class="col-xs-12">
                    <legend id="showStudents">
                        Students schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="form-group collapse" id="Studentstable">            
                        <%  //Se cargan los horarios de cada alumno y las secciones que se le han asignado finalmente:                      
                            String gradeLevel = "";
                            for (Student st : studentsOrdered) {

                                if (st.getGradeLevel() != null && !gradeLevel.equals(st.getGradeLevel())) {
                                    gradeLevel = st.getGradeLevel();
                                    out.println("<h2><u> GRADE LEVEL CURRENT: " + gradeLevel + "</u></h2>");
                                }
                                out.println("<h3 style='border-top: solid 2px black;padding-top: 10px;'>" + st.getName() + "</h3>");

                                out.println("<h5>" + st.getCoursesUnenrolled() + "</h5>");
                                out.println("<table id='table_id' class='table'>");
                                out.println(headCols);
                                swapcolor = true;
                                for (int i = 0; i < TAMY; i++) {
                                    if (swapcolor) {
                                        out.println("<tr class='tcolores'>");
                                        swapcolor = false;
                                    } else {
                                        out.println("<tr>");
                                        swapcolor = true;
                                    }
                                    if (i < headRow.size()) {
                                        out.println("<td>" + headRow.get(i).text() + "</td>");
                                    } else {
                                        out.println("<td></td>");
                                    }
                                    for (int j = 0; j < TAMX; j++) {
                                        if (st.getHuecos()[j][i] != 0) {
                                            String tdStyle = "<td align='center'>";
                                            String solapado = st.checkSolapamiento(lista2.get(st.getId()).getHuecos()[j][i]);
                                            if (!solapado.equals("")) {
                                                tdStyle = "<td align='center' style='color: red;border: solid;'>";
                                            }
                                            out.println(tdStyle + cs.nameCourseAndSection(st.getHuecos()[j][i])
                                                    + "<br>" + solapado + "</td>");

                                        } else {
                                            out.println("<td></td>");
                                        }
                                    }
                                    out.println("</tr>");
                                }

                                out.println("</table>");

                                String s = st.getSolapamientoSeccionesFromRenWeb();
                                if (s.length() > 0) {
                                    out.println("<h5 style='color:red'>" + s + "</h5>");
                                }
                            }
                        %>
                    </div>
                </div>
            </div>
            <%-- ------------------------------------------------------------------------ROOOOOMS----------------------------------------------------------------------------- --%>

            <div role="tabpanel" class="col-xs-12 tab-pane" id="rooms">
                <div class="col-xs-12">
                    <legend id="showRooms">
                        Schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="form-group collapse" id="Roomstable">
                        <%//------------------------- OBSERVACIONES: SE MUESTRAN AVISOS SI HA HABIDO ALGÚN PROBLEMA AL CARGAR LAS ROOMS: ------------------------                        
                            //Aquí se muestra si se han asignado rooms a un curso que ya no tiene disponibilidad (y no deja que tenga más secciones ni cursos asociados).
                            //SÓLO SE HACE CON ROOMS ASIGNADAS A LOS CURSOS O A SCHOOL. SI HA SIDO SELECCIONADO EN UNA SECCIÓN, AQUÍ EL PROGRAMA SÍ DEJA ASIGNARLOS Y NO AVISA:
                            for (Room r : rooms) {
                                if (avisos.getRoomsFull().contains(r.getName())) {

                                    if (r.getDisponibilidad() == 0) {
                                        out.println(r.getName() + " has not availability.");
                                    }
                                }
                            }
                            // La variable alert te avisa si el tamaño de la room es menor que el asignado al curso, o si el campo de texto de room está vacío en sección, o en sección y curso:
                            // ES IMPORTANTE ASEGURARSE QUE LAS ROOMS ASIGNADAS A SCHOOL TIENEN UN SIZE ADECUADO A LOS CURSOS VINCULADOS.
                            out.println("<h2>Rooms</h2>");
                            if (!Consultas.alert.equals("")) {
                                out.println("<br/><h5>" + Consultas.alert + "</h5>");
                            }
                            //---------------------------DATOS DEL SCHEDULE DE ROOMS: --------------------------------------------------
                            //Impresión de los schedules de las rooms. En la variable aviso se guardan todas las rooms que han sido asignadas finalmente en el Schedule (no en RenWeb).
                            //Esto permitirá que solo se muestren las rooms utilizadas, y posteriormente pueda mostrarse una lista de las rooms no usadas (al igual que en profesores):
                            String aviso = "";
                            for (Room r : rooms) {
                                for (int i = 0; i < TAMY; i++) {
                                    for (int j = 0; j < TAMX; j++) {
                                        if (r.getHuecos()[j][i] != 0) {
                                            aviso += r.getName() + ";";
                                        }
                                    }
                                }
                                if (aviso.contains(r.getName() + ";")) {
                                    out.println("<h3>" + r.getName() + "</h3>");
                                    out.println("<table id='table_id' class='table'>");
                                    out.println(headCols);
                                    swapcolor = true;
                                    for (int i = 0; i < TAMY; i++) {
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                        if (i < headRow.size()) {
                                            out.println("<td>" + headRow.get(i).text() + "</td>");
                                        } else {
                                            out.println("<td></td>");
                                        }
                                        for (int j = 0; j < TAMX; j++) {
                                            if (r.getHuecos()[j][i] != 0) {
                                                out.println("<td class='contentTable'>" + cs.nameCourseAndSection((r.getHuecos()[j][i] * 100) + r.getHuecosSeccion()[j][i]) + "</td>");
                                            } else {
                                                out.println("<td></td>");
                                            }
                                        }
                                        out.println("</tr>");
                                    }
                                    out.println("</table>");
                                }
                            }
                        %>
                    </div>
                </div>
                <div class="col-xs-12">
                    <legend id="showRooms2">
                        Room Master Schedule
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="form-group collapse" id="Roomstable2">
                        <%//Al igual que en Teachers, se muestran las asignaciones a las Rooms por columnas (días de la semana):
                            countDays = 0;
                            for (String s : headCol) {
                                out.println("<h3>" + s + "</h3>");
                                out.println("<table class='table'>");
                                swapcolor = true;
                                out.println("<tr>");
                                out.println("<th>Rooms | Hours</th>");
                                for (int i = 0; i < headRow.size(); i++) {
                                    out.println("<th>" + headRow.get(i).text() + "</th>");
                                }
                                out.println("</tr>");

                                for (Room r : rooms) {
                                    if (aviso.contains(r.getName() + ";")) {
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                        out.println("<td>" + r.getName() + "</td>");
                                        for (int i = 0; i < TAMY; i++) {
                                            if (r.getHuecos()[countDays][i] != 0) {
                                                out.println("<td>" + cs.nameCourseAndSection((r.getHuecos()[countDays][i] * 100) + r.getHuecosSeccion()[countDays][i]) + "</td>");
                                            } else {
                                                out.println("<td></td>");
                                            }
                                        }
                                        out.println("</tr>");
                                    }
                                }

                                countDays++;
                                out.println("</table>");
                            }

                        %>
                    </div>
                </div>
                <div class="col-xs-12">
                    <legend id="showRoomsdisp">
                        Availability
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="form-group collapse" id="Roomsdisp">

                        <%  //Disponibilidad de las todas rooms que se capturan de RenWeb:
                            //Se muestra el porcentaje de ocupación, y la disponibilidad y la ocupación en términos de huecos en el Schedule:
                            //(disponibilidad y ocupación de cada hora de cada día de la semana):
                            for (Room r : rooms) {
                                out.println("<h3>" + r.getName() + "</h3>");
                                out.println("<table id='table_id' class='table'>");
                                out.println("<tr><th>Field</th><th>Content</th></tr>");

                                out.println("<tr>");
                                out.println("<td>Room Occupation Percent</td>");
                                out.println("<td>");

                                if (r.getPercentOcupation() >= 0 && r.getPercentOcupation() < 1) {
                                    out.println("0" + df.format(r.getPercentOcupation()) + "%");
                                } else {
                                    out.println(df.format(r.getPercentOcupation()) + "%");
                                }

                                out.println("</td>");
                                out.println("</tr>");

                                out.println("<tr>");
                                out.println("<td>Room availability</td>");
                                out.println("<td>" + r.getDisponibilidad() + "</td>");
                                out.println("</tr>");

                                out.println("<tr>");
                                out.println("<td>Room occupation</td>");
                                out.println("<td>" + r.getOcupacion() + "</td>");
                                out.println("</tr>");

                                out.println("</table>");
                            }

                        %>
                    </div>
                </div>
                <div class="col-xs-12">
                    <legend id="showRoomsdisp2">
                        Full Availability Rooms
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>
                    <div class="form-group collapse" id="Roomsdisp2">              
                        <% //Lista las rooms que no se hayan ocupado para ningún curso del Schedule actual:
                            out.println("<table id='table_id' class='table'>");
                            out.println("<h2>Rooms:</h2>");
                            for (Room r : rooms) {
                                if (!aviso.contains(r.getName() + ";")) {
                                    out.println("<tr>");
                                    out.println("<td>" + r.getName() + "</td>");
                                    out.println("</tr>");
                                }
                            }
                            out.println("</table>");
                        %>

                    </div>
                </div>    
            </div>
            <%-- --------------------------------------------------------------OBSERVATIONS ----------------------------------------------- --%>

            <div role="tabpanel" class="col-xs-12 tab-pane" id="observations">
                <div class="col-xs-12">
                    <legend id="showObservations">
                        Schedule Observations
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                    </legend>

                    <div class="form-group collapse" id="Observationstable">
                        <%//------------------------- OBSERVACIONES: SE MUESTRAN AVISOS SI HA HABIDO ALGÚN PROBLEMA AL CARGAR EL SCHEDULE: ------------------------
                                                                                     
                            
                                                      
                            
                            if (!avisos.getAvisosSectionRoomsAvailable().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisosSectionRoomsAvailable(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisosSectionRoomsSize().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisosSectionRoomsSize(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisosSectionRoomsFull().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisosSectionRoomsFull(), swapcolor);
                            out.print(tabla);
                            }   
                            
                            if (!avisos.getAvisosCourseRoomsAvailable().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisosCourseRoomsAvailable(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisosCourseRoomsSize().isEmpty()) {                               
                            String tabla =avisos.cargaTablaHash(avisos.getAvisosCourseRoomsSize(), swapcolor);
                            out.print(tabla);
                            }                            
                            if (!avisos.getAvisosCourseRoomsFull().isEmpty()) {                               
                            String tabla =avisos.cargaTablaHash(avisos.getAvisosCourseRoomsFull(), swapcolor);
                            out.print(tabla);
                            }                                                       
                            if (!avisos.getAvisosSchoolRoomsAvailable().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisosSchoolRoomsAvailable(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisosSchoolRoomsSize().isEmpty()) {
                            String tabla =avisos.cargaTablaHash(avisos.getAvisosSchoolRoomsSize(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisosSchoolRoomsFull().isEmpty()) {
                            String tabla =avisos.cargaTablaHash(avisos.getAvisosSchoolRoomsFull(), swapcolor);
                            out.print(tabla);
                            }                                                                                                                
//----------------------------------------------------------------------------------------------
                            if (!avisos.getAvisosSectionTeachersAvailable().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisosSectionTeachersAvailable(), swapcolor);
                            out.print(tabla);
                            }                              
                            if (!avisos.getAvisosCourseTeachersAvailable().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisosCourseTeachersAvailable(), swapcolor);
                            out.print(tabla);
                            }                                
                            if (!avisos.getAvisosSectionTeachersFull().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisosSectionTeachersFull(), swapcolor);
                            out.print(tabla);
                            }                              
                            if (!avisos.getAvisosCourseTeachersFull().isEmpty()) {
                            String tabla =avisos.cargaTablaHash(avisos.getAvisosCourseTeachersFull(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisosCourseTeachersEB().isEmpty()) {
                            String tabla =avisos.cargaTablaHash(avisos.getAvisosCourseTeachersEB(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getCourseWithoutSections().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getCourseWithoutSections(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisoMaxSizePerSectionCourse().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisoMaxSizePerSectionCourse(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisoMinSizePerSectionCourse().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisoMinSizePerSectionCourse(), swapcolor);
                            out.print(tabla);
                            }                            
                            if (!avisos.getAvisoMaxSizePerSectionSchool().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisoMaxSizePerSectionSchool(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisoMinSizePerSectionSchool().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisoMinSizePerSectionSchool(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisoCourseWithoutTemplate().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisoCourseWithoutTemplate(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisoSchoolWithoutScheduleActive().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisoSchoolWithoutScheduleActive(), swapcolor);
                            out.print(tabla);
                            }
                            if (!avisos.getAvisoWitouthMatches().isEmpty()) {
                            String tabla =avisos.cargaTabla(avisos.getAvisoWitouthMatches(), swapcolor);
                            out.print(tabla);
                            }                            
                            if (!avisos.getAvisoCadena().isEmpty()) {
                            String tabla =avisos.cargaTablaHash(avisos.getAvisoCadena(), swapcolor);
                            out.print(tabla);
                            }
                            
                            
                            //Con este for se imprime en la parte de Courses/Schedule los cursos que no tienen estudiantes asignados desde la BBDD(RenWeb/Academic/Courses/Requests)
   
                            if (!avisos.getCoursesWithoutStudents().isEmpty()) {
                                out.println("<div class='col-xs-12 students'>"); 
                                out.println("<table id='table_id' class='table'>");
                                out.println("<h4> Course/s empty of students, with ActiveSchedule enabled: " + "</h4>");
                                swapcolor=true;
                                for (Map.Entry<Integer, String> entry : avisos.getCoursesWithoutStudents().entrySet()) {
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                    out.println("<td>"+ "-Id: " + entry.getKey() + ", Course name: " + entry.getValue() + "." + "</td></tr>");

                                }
                                 out.println("</table>");   
                                 out.println("</div>");
                            }

                            //Con esto se imprime el aviso de si hay asignada (en Renweb) una seccion con un template diferente a la del curso de origen (en el apartado 
                            //Menu Lateral/Academics/Classes/Schedule/al seleccionar Template para asignar un Pattern).
                            //Los datos se han almacenado en el treeMap tempIdSect de Consultas, y se han volcado posteriormente en el String tempidsect para que no se repitan
                            //los nombres de los cursos y las secciones:
                            if (!avisos.getTemplateIdSection().isEmpty()) {
                                out.println("<div class='col-xs-12 students'>");
                                out.println("<h4>"+" Section/s with different template respect its origin course: "+ "</h4>");
                                out.println("<table id='table_id' class='table'>"); 
                                for (Map.Entry<String, ArrayList<String>> entry : avisos.getTemplateIdSection().entrySet()) {
                                    swapcolor=true;
                                    out.println("<tr><td><b>"+ "Course: " + entry.getKey()+": </b></td>");
                                    out.println("</tr>");
                                    for(String value: entry.getValue()){
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                        out.println("<td>"+"Section: " + value +"</td></tr>");                                       
                                    }
                                }
                                out.println("</table>");   
                                out.println("</div>");                 
                            }
                            
                           if (!avisos.getAvisoMinStudents().isEmpty()) {
                                out.println("<div class='col-xs-12 students'>");
                                out.println("<h4>"+" Impossible fill section/s: "+ "</h4>");
                                out.println("<table id='table_id' class='table'>");                               
                                out.println("<tr><td><b>Possible reasons: </b></td>");
                                out.println("<tr><td><b>-Less students than required by Course MinStudentsPerSection field </b></td>");
                                out.println("<tr><td><b>-Requests/Roster field/s empty or insufficient </b></td>");
                                out.println("<tr><td><b>-Student overlap with other section </b></td>");
                                for (Map.Entry<String, ArrayList<String>> entry : avisos.getAvisoMinStudents().entrySet()) {
                                    swapcolor=true;
                                    out.println("<tr><td><b>"+ "Course: " + entry.getKey()+": </b></td>");
                                    out.println("</tr>");
                                    for(String value: entry.getValue()){
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                        out.println("<td>"+"Section: " + value +"</td></tr>");                                       
                                    }
                                }
                                out.println("</table>");   
                                out.println("</div>");                 
                            }
                           
                            if (!avisos.getAvisoWithoutPatterns().isEmpty()) {   
                                out.println("<div class='col-xs-12 students'>");
                                out.println("<h4>"+" There are section/s with Lock Schedule enabled without section patterns(ScheduleWeb action: Lock Schedule disabled) "+ "</h4>");
                                out.println("<table id='table_id' class='table'>"); 
                                for (Map.Entry<String, ArrayList<String>> entry : avisos.getAvisoWithoutPatterns().entrySet()) {
                                    swapcolor=true;
                                    out.println("<tr><td><b>"+ "Course: " + entry.getKey()+": </b></td>");
                                    out.println("</tr>");
                                    for(String value: entry.getValue()){
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                        out.println("<td>"+"Section: " + value +"</td></tr>");                                       
                                    }
                                }
                                out.println("</table>");   
                                out.println("</div>");                 
                            }

                            //Aviso de si hay alumnos asignados a secciones que no están en los requests del curso de origen de la que parte la seccion:
                            //Se ha calculado en el treeMap arrayStuderroneos de Consultas. Desde este treeMap
                            //se han volcado todos los datos a una variable String para que no se repitan datos, que es la que se visualiza aquí.
                            if (!avisos.getDifStudents().isEmpty()) {
                                out.println("<div class='col-xs-12 students'>");
                                out.println("<h4>"+" There were different students between Course Requests and Section Roster."
                                        + " Students out of Course Requests have been removed in Section Roster:"+ "</h4>");
                                out.println("<table id='table_id' class='table'>");                       
                                for(String course:avisos.getDifStudents().keySet()){
                                    swapcolor=true; 
                                    out.println("<tr><td><b>"+ "Course: " + course+": </b></td></tr>");
                                    for(String section: avisos.getDifStudents().get(course).keySet()){
                                        if (swapcolor) {
                                            out.println("<tr class='tcolores'>");
                                            swapcolor = false;
                                        } else {
                                            out.println("<tr>");
                                            swapcolor = true;
                                        }
                                        out.println("<td>Section "+section+": ");                               
                                        ArrayList<String> students = new ArrayList();
                                        students.addAll(avisos.getDifStudents().get(course).get(section).values());
                                        for(String student:students){
                                            if(!students.get(students.size()-1).equals(student)){
                                                out.println(student+"; ");
                                            }
                                            else{
                                              out.println(student);
                                            }                                                                                                        
                                        }
                                        out.println("</td></tr>");
                                    }
                             
                                }
                                out.println("</table>"); 
                                out.println("</div>");
                            }
                              
                       
                        %>
                    </div>
                </div>
            </div>
        </div>
    </body>
    <%} catch (Exception e) {
            e.getMessage();
        }%>
</html>
