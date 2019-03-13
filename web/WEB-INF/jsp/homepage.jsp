<%@page import="java.text.DecimalFormat"%>
<%@page import="model.Algoritmo"%>
<%@page import="dataManage.Restrictions"%>
<%@page import="java.util.TreeMap"%>
<%@page import="model.Room"%>
<%@page import="dataManage.Tupla"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="model.Course"%>
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
    <%try {

    %>
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

                //  alert(cursosSin1);
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
        <!--
        TO DO:
            -En la parte de master schedule de teachers los titulos de las tablas no 
            estan bien.
            -Hay que mostrar tambien el master schedule de rooms, fijaros en el de teachers
            y hacerlo parecido.
            -Cuando carga la pagina la primera vez aparecen las pestañas de rooms en la parte
            de courses.
        -->
    </head>
    <body>

        <%  Boolean enrolled = false;
            DecimalFormat df = new DecimalFormat("#.00");
            Consultas cs = (Consultas) request.getAttribute("cs");
            Integer TAMX = (Integer) request.getAttribute("TAMX");
            Integer TAMY = (Integer) request.getAttribute("TAMY");
            ArrayList<Tupla<String, String>> headRow = (ArrayList<Tupla<String, String>>) request.getAttribute("hFilas");
            ArrayList<String> headCol = (ArrayList<String>) request.getAttribute("hcols");
            List<Course> courses = (List) request.getAttribute("Courses");
            List<Teacher> lista = (List) request.getAttribute("profesores");
            HashMap<Integer, Room> roomsHash = (HashMap<Integer, Room>) request.getAttribute("rooms");
            ArrayList<Room> rooms = new ArrayList<>(roomsHash.values());
            boolean activeRoom = (boolean) request.getAttribute("activeRoom");
            //ArrayList<Object> rooms2 = new ArrayList<>(groupRooms.values());
            //List<String> cursosSin1 = (List) request.getAttribute("cursosSinEstudiantes");
            //List<String> cursosSin1 = (List) request.getAttribute("cursosSinEstudiantes");
            HashMap<Integer, Student> lista2 = (HashMap) request.getAttribute("students");
            List<Student> studentsOrdered = (List) request.getAttribute("orderedStudents");
            HashMap<Integer, String> hashPersons = (HashMap) request.getAttribute("persons");
            ArrayList<String> log = (ArrayList<String>) request.getAttribute("log");
            //ArrayList<Integer> groupRooms = (ArrayList<Integer>) request.getAttribute("grouprooms");
            //HashMap<Integer, Room> rooms = (HashMap<Integer, Room>) request.getAttribute("rooms");
            boolean swapcolor = true;
            double totalenrolled = 0;
            double totalnoenrolled = 0;
            String headCols = "<tr><th>Period</th>";
            for (String s : headCol) {
                headCols += "<th class='text-center'>" + s;
                headCols += "</th>";
            }
            headCols += "</tr>";
        %>



        <c:out value="${estudiante.identificacion}"></c:out>
            <div class="col-xs-12 text-center noPrint" id="myTab">
                <div class="col-xs-9">
                    <ul class="nav nav-tabs">
                        <li class="active"><a id="Courses" data-toggle="tab" href="#courses" role="tab" >Courses</a></li>
                        <li><a id="Teachers" data-toggle="tab" href="#teachers" role="tab">Teachers</a></li>
                        <li><a id="Students" data-toggle="tab" href="#students" role="tab">Students</a></li>
                        <%if (activeRoom == true) {%>
                    <li><a id="Rooms" data-toggle="tab" href="#rooms" role="tab">Rooms</a></li>       
                        <%  }%>     
                </ul>
            </div>
            <div class="col-xs-3">
                <button id="btnRefresh" type="button" class="btn btn-info" onclick="enviando()">Reload</button>
            </div>
        </div>

        <div class="tab-content">

            <div role="tabpanel" class="col-xs-12 tab-pane in active" id="courses">
                <legend id="showCourses" class="noPrint">
                    Schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>



                <div class="form-group collapse" id="Coursestable">

                    <%  String[][] tupla = new String[100][100];
                        String acumTeacher = "";

                        for (int j = 0; j < 99; j++) {
                            for (int k = 0; k < 99; k++) {
                                tupla[k][j] = "";

                            }
                        }
                        //   HashMap<HashMap<Integer, Integer>, ArrayList<String>> fullSchedule = new HashMap<>();
                        //Con este for se imprime en la parte de Courses/Schedule los cursos que no tienen estudiantes asignados desde la BBDD(RenWeb/Academic/Courses/Requests)
                        out.println("<div class='col-xs-12 course'>");
                        out.println("<h2 id='showCourses' class='noPrint'>");
                        if (!Consultas.CoursesWithoutStudents.isEmpty()) {

                            //***   *///
                            out.println(" Course/s empty of students, with ActiveSchedule enabled: " + "<br/>");

                            for (Map.Entry<Integer, String> entry : Consultas.CoursesWithoutStudents.entrySet()) {

                                out.println("-Id: " + entry.getKey() + ", Course name: " + entry.getValue() + "." + "<br/>");

                            }

                        }
                        //Antiguo aviso de si hay asignada (en Renweb) una seccion con un template diferente a la del curso de origen: 

                        /*   if (!Consultas.tempIdSect.isEmpty()) {

                          
                            out.println(" Section/s with different template respect its origin course: " + "<br/>");

                            for (Map.Entry<Integer, String> entry : Consultas.tempIdSect.entrySet()) {

                                out.println("-Id Class: " + entry.getKey() + " " + entry.getValue() + "." + "<br/>");

                            }

                        }   */
                        //Con esto se imprime el aviso de si hay asignada (en Renweb) una seccion con un template diferente a la del curso de origen.
                        if (!Consultas.tempIdSect.isEmpty()) {
                            out.println(" Section/s with different template respect its origin course: ");

                    %><%=Consultas.tempidsect%><%
                            out.println("<br/><br/>");
                        }

                        //Gracias al siguiente if se puede visualizar por pantalla de forma correcta
                        //si hay alumnos asignados a secciones que no están en los requests del curso de origen de la que parte la seccion:
                        //Nota: la forma correcta de visualización ya se ha calculado en un treeMap de Consultas. Desde este treeMap
                        //se han volcado todos los datos a una variable String, que es la que se visualiza aquí.
                        //----PENDIENTE CAMBIO PARA REALIZAR TODO EL CALCULO AQUÍ(AUNQUE HOY POR HOY FUNCIONA PERFECTAMENTE)
                        if (!Consultas.arrayStuderroneos.isEmpty()) {
                            out.println(" Students out of Course Requests added on Class Section Roster:");
                    %><%=Consultas.studE%><%

                        }

                        out.println("</h2>");
                        out.println("</div>");

                        for (Course t : courses) {
                            if (t.hayEstudiantes()) {
                                out.println("<div class='col-xs-12 course'>");

                                out.println("<h3>" + cs.getAbbrevCourses().get(t.getIdCourse())+ "</h3>");
                                //   out.println("<h3>" + t.getIdCourse() + "</h3>");
                                out.println("<table id='table_id' width='100%' border='0' class=''>");
                                out.println("<tr class='students'>");
                                for (int j = 0; j < t.getArraySecciones().size(); j++) {
                                    String studentNames = "";
                                    String nameTeacher = "" + hashPersons.get(t.getArraySecciones().get(j).getIdTeacher());
                                    acumTeacher += "" + hashPersons.get(t.getArraySecciones().get(j).getIdTeacher()) + ";";

                                    /*  String nameTeacher = "No Teacher";

                                  if (hashPersons.containsKey(t.getArraySecciones().get(j).getIdTeacher())) {
                                       nameTeacher = hashPersons.get(t.getArraySecciones().get(j).getIdTeacher());
                                   }*/
                                    if (!nameTeacher.equals("null")) {
                                        out.println("<td valign=\"top\"><strong>Section " + t.getArraySecciones().get(j).getNameSeccion() + ":<br>"
                                                + "Teacher: " + nameTeacher + " </strong>");
                                    } else {
                                        out.println("<td valign=\"top\"><strong>Section " + t.getArraySecciones().get(j).getNameSeccion() + ":<br>"
                                                + "There's no teacher enrolled in Section/Course </strong>");

                                    }
                                    if (t.getArraySecciones().get(j).getIdStudents().size() == 0) {
                                        out.println("<br> There's no students enrolled in this Section");

                                    }
                                    for (int k = 0; k < t.getArraySecciones().get(j).getIdStudents().size(); k++) {
                                        studentNames += "<br>" + (k + 1) + "- " + lista2.get(t.getArraySecciones().get(j).getIdStudents().get(k)).getName();
                                    }
                                    out.println(studentNames);
                                    out.println("</td>");

                                }
                                out.println("</tr>");

                                /* String[][] matSections = new String[TAMX][TAMY];

                           for (int i = 0; i < TAMX; i++) {
                               for (int j = 0; j < TAMY; j++) {
                                   matSections[i][j] ="";
                               }
                           }
                                 */
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
                                            if (tupla[j][i].equals("")) {
                                                tupla[j][i] = tupla[j][i].concat("<td class='text-center'>" + cs.nameCourse(t.getIdCourse()) + " Sections: " + acumulacion + "<br/>");
                                            } else {
                                                tupla[j][i] += cs.nameCourse(t.getIdCourse()) + " Sections: " + acumulacion + "<br/>";
                                            }
                                        } else if (!t.getHuecos()[j][i].equals("0")) {
                                            int aux = Integer.parseInt(t.getHuecos()[j][i]);
                                            aux = t.getIdCourse() * 100 + aux;
                                            out.println("<td class='text-center'>" + cs.nameCourse(t.getIdCourse()) + " Section: " + cs.nameSection(aux) + "</td>");
                                            if (tupla[j][i].equals("")) {
                                                tupla[j][i] += "<td class='text-center'>" + cs.nameCourse(t.getIdCourse()) + " Section: " + cs.nameSection(aux) + "<br/>";
                                            } else {
                                                tupla[j][i] += cs.nameCourse(t.getIdCourse()) + " Section: " + cs.nameSection(aux) + "<br/>";
                                            }

                                        } else {
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

                <legend id="showFullSchedule" class="noPrint">
                    Full Schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="FullSchedule">



                    <%
                        out.println("<div class='col-xs-12 course'>");
                        out.println("<table id='table_id' width='100%' border='0' class=''>");

                        out.println(headCols);
                        swapcolor = true;
                        //  int cuentai = 0;

                        for (int i = 0; i < TAMY; i++) {

                            if (swapcolor) {
                                out.println("<tr class='tcolores horario'>");
                                swapcolor = false;
                            } else {
                                out.println("<tr class='horario'>");
                                swapcolor = true;
                            }
                            // int cuentaCurso = 0;
                            /*for (int j = 0; j < TAMX; j++) {
                                        if (!t.getHuecos()[j][cuentai].isEmpty()) {
                                            cuentaCurso++;
                                        }
                                    }*/
                            //     cuentai++;
                            // if (cuentaCurso > 0) {
                            if (i < headRow.size()) {
                                out.println("<td>" + headRow.get(i).text() + "</td>");
                            } else {
                                out.println("<td></td>");
                            }

                            for (int j = 0; j < TAMX; j++) {
                                if (!tupla[j][i].equals("")) {
                                    tupla[j][i] += "</td>";
                                    out.println(tupla[j][i]);

                                } else {
                                    out.println("<td> </td>");
                                }
                            }

                            out.println("</tr>");
                            // }
                        }

                        out.println(
                                "</table>");

                        out.println(
                                "</div>");


                    %>


                </div>

                <legend id="showCoursesenrol" class="noPrint">
                    Missing Enrolled
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Coursesenrol">
                    <%  for (Course c : courses) {
                            if (c.getPercentEnrolled() != 100) {
                                out.println("<h3>" + cs.nameCourse(c.getIdCourse()) + "</h3>");
                                out.println("<table id='table_id' class='table'>");
                                out.println("<tr><th>Field</th><th>Content</th></tr>");

                                out.println("<tr>");
                                out.println("<td>Enrolled student percent</td>");
                                out.println("<td>" + df.format(c.getPercentEnrolled()) + "%" + "</td>");
                                out.println("</tr>");

                                out.println("<tr>");
                                out.println("<td>Number of sections no enrolled</td>");
                                out.println("<td>" + c.getSectionsNoEnrolled() + "</td>");
                                out.println("</tr>");

                                /*String studentNames = "";

                                out.println("<tr>");
                                out.println("<td>Students Enrolled</td>");
                                out.println("<td>");
                                if (!c.getStudentsAsignados().isEmpty()) {
                                    studentNames += lista2.get(c.getStudentsAsignados().get(0)).getName();
                                }
                                for (int i = 1; i < c.getStudentsAsignados().size(); i++) {
                                    studentNames += " ," + lista2.get(c.getStudentsAsignados().get(i)).getName();
                                }
                                out.println(studentNames + ".");
                                out.println("</td>");
                                out.println("</tr>");
                                 */
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

                                out.println("<table id='table_id' class='table'>");
                                out.println(headCols);
                                swapcolor = true;
                                int[][] huecosStudents = c.huecosStudents();
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
                                out.println("<h4> All students enrolled in " + cs.getAbbrevCourses().get(c.getIdCourse()) + "</h4>");

                            }
                        }


                    %>
                </div>
            </div>
            <div role="tabpanel" class="col-xs-12 tab-pane" id="teachers">
                <legend id="showTeachers">
                    Schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Teacherstable">
                    <%  out.println("<h2>Teachers</h2>");
                        for (Teacher t : lista) {
                            if (Algoritmo.teachersFULL.contains(t.getName())) {

                                if (t.seccionesDisponibles(cs.getTotalBlocks()) < 1) {
                                    out.println(t.getName() + " has not section availability.");
                                }
                                if (t.prepsDisponibles(cs.getTotalBlocks()) < 1) {
                                    out.println(t.getName() + " has not course availability.");
                                }
                            }
                        }
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
                                            out.println("<td>" + cs.nameCourseAndSection(t.getHuecos()[j][i]) + "</td>");
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

                <legend id="showTeachers2">
                    Teacher Master Schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Teacherstable2">
                    <%
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

                <legend id="showTeachersdisp">
                    Availability
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Teachersdisp">
                    <%                        for (Teacher t : lista) {

                            out.println("<h3>" + t.getName() + "</h3>");
                            out.println("<table id='table_id' class='table'>");
                            out.println("<tr><th>Field</th><th>Content</th></tr>");

                            out.println("<tr>");
                            out.println("<td>Courses teaching</td>");
                            out.println("<td>");
                            String sumaCursos = "";
                            for (Integer i : t.getPrepsComplete()) {

                                sumaCursos = sumaCursos.concat(", " + cs.nameCourse(i));
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

                <legend id="showTeachersdisp2">
                    Full Availability Teachers
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Teachersdisp2">
                    <%  out.println("<h2>Teachers</h2>");
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
            <div role="tabpanel" class="col-xs-12 tab-pane" id="students">
                <legend id="showStudents">
                    Students schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Studentstable">            
                    <%                        //  for (Map.Entry<Integer, Student> entry : lista2.entrySet()) {
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
                                        String tdStyle = "<td>";
                                        String solapado = st.checkSolapamiento(lista2.get(st.getId()).getHuecos()[j][i]);
                                        if (!solapado.equals("")) {
                                            tdStyle = "<td style='color: red;border: solid;'>";
                                        }
                                        out.println(tdStyle + cs.nameCourseAndSection(st.getHuecos()[j][i])
                                                + "<br>" + solapado + "</td>");

                                        // out.println("<td>" + cs.nameCourseAndSection(entry.getValue().getHuecos()[j][i]) + "</td>");
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
                <%-- ------------------------------------------------------------------------ROOOOOMS----------------------------------------------------------------------------- --%>
            </div>
            <div role="tabpanel" class="col-xs-12 tab-pane" id="rooms">
                <legend id="showRooms">
                    Schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Roomstable">
                    <%
                        for (Room r : rooms) {                           
                            if (Algoritmo.roomsFULL.contains(r.getName())) {

                                if (r.getDisponibilidad()==0) {
                                    out.println(r.getName() + " has not availability.");
                                }
                            }
                        }
                        
                        out.println("<h2>Rooms</h2>");
                        if (!Consultas.alert.equals("")) {
                            out.println("<br/><h5>" + Consultas.alert + "</h5>");
                        }
                        String aviso = "";
                        for (Room r : rooms) {
                            for (int i = 0; i < TAMY; i++) {
                                for (int j = 0; j < TAMX; j++) {
                                    if (r.getHuecos()[j][i] != 0) {
                                        aviso += r.getName() + ";";
                                    }
                                }
                            }
                            if (aviso.contains(r.getName())) {
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
                                            /* for (Course t : courses) {
                                            for (int z = 0; z < t.getArraySecciones().size(); z++) {
                                                if (cs.nameCourse(r.getHuecos()[j][i]).contains(cs.nameCourse(t.getIdCourse()))) {*/
                                            out.println("<td class='contentTable'>" + cs.nameCourseAndSection((r.getHuecos()[j][i] * 100) + r.getHuecosSeccion()[j][i]) + "</td>");
                                        } //+ " Section " + t.getArraySecciones().get(z).getNameSeccion()
                                        /*   }
                                        }

                                    } */ else {
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

                <legend id="showRooms2">
                    Room Master Schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Roomstable2">
                    <%
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
                                if (aviso.contains(r.getName())) {
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
                                            out.println("<td class='contentTable'>" + cs.nameCourseAndSection((r.getHuecos()[countDays][i] * 100) + r.getHuecosSeccion()[countDays][i]) + "</td>");
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

                <legend id="showRoomsdisp">
                    Availability
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Roomsdisp">
                    <%  for (Room r : rooms) {
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


                <legend id="showRoomsdisp2">
                    Full Availability Rooms
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Roomsdisp2">              
                    <%  out.println("<table id='table_id' class='table'>");
                        out.println("<h2>Rooms:</h2>");
                        for (Room r : rooms) {
                            if (!aviso.contains(r.getName())) {
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
    </body>
    <%} catch (Exception e) {
            e.getMessage();
        }%>
</html>
