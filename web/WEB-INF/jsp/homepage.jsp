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
                    $("#roomstable").toggleClass('in');
                });

                $("#showCoursesenrol").click(function () {
                    $("#Coursesenrol").toggleClass('in');
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

                $("#showStudents").click(function () {
                    $("#Studentstable").toggleClass('in');
                });

                $("#showStudentsEnrolled").click(function () {
                    $("#StudentsEnrolled").toggleClass('in');
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

        <%
            Consultas cs = (Consultas) request.getAttribute("cs");
            Integer TAMX = (Integer) request.getAttribute("TAMX");
            Integer TAMY = (Integer) request.getAttribute("TAMY");
            ArrayList<Tupla<String, String>> headRow = (ArrayList<Tupla<String, String>>) request.getAttribute("hFilas");
            ArrayList<String> headCol = (ArrayList<String>) request.getAttribute("hcols");
            List<Course> courses = (List) request.getAttribute("Courses");
            List<Teacher> lista = (List) request.getAttribute("profesores");
            HashMap<Integer, Student> lista2 = (HashMap) request.getAttribute("students");
            List<Student> studentsOrdered = (List) request.getAttribute("orderedStudents");
            HashMap<Integer, String> hashPersons = (HashMap) request.getAttribute("persons");
            ArrayList<String> log = (ArrayList<String>) request.getAttribute("log");
            ArrayList<Integer> groupRooms = (ArrayList<Integer>) request.getAttribute("grouprooms");
            HashMap<Integer, Room> rooms = (HashMap<Integer, Room>) request.getAttribute("rooms");
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
        <div class="col-xs-12 text-center noPrint" id="myTab">
            <div class="col-xs-9">
                <ul class="nav nav-tabs">
                    <li class="active"><a id="Courses" data-toggle="tab" href="#courses" role="tab" >Courses</a></li>
                    <li><a id="Teachers" data-toggle="tab" href="#teachers" role="tab">Teachers</a></li>
                    <li><a id="Students" data-toggle="tab" href="#students" role="tab">Students</a></li>
                    <li><a id="Rooms" data-toggle="tab" href="#rooms" role="tab">Rooms</a></li>               
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

                    <%
                        for (Course t : courses) {
                            if(t.hayEstudiantes()){
                               out.println("<div class='col-xs-12 course'>");
                                out.println("<h3>" + cs.getAbbrevCourses().get(t.getIdCourse()) + " - " + cs.nameCourse(t.getIdCourse()) + "</h3>");
                               //   out.println("<h3>" + t.getIdCourse() + "</h3>");
                               out.println("<table id='table_id' width='100%' border='0' class=''>");
                               out.println("<tr class='students'>");
                               for (int j = 0; j < t.getArraySecciones().size(); j++) {
                                   String studentNames = "";
                                   String nameTeacher = "" + hashPersons.get(t.getArraySecciones().get(j).getIdTeacher());
                                   /*  String nameTeacher = "No Teacher";

                                  if (hashPersons.containsKey(t.getArraySecciones().get(j).getIdTeacher())) {
                                       nameTeacher = hashPersons.get(t.getArraySecciones().get(j).getIdTeacher());
                                   }*/
                                   out.println("<td><strong>Section " + t.getArraySecciones().get(j).getNumSeccion() + ":<br>"
                                           + "Teacher: " + nameTeacher + " </strong>");
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
                                       if (!t.getHuecos()[j][i].equals("0")) {
                                           String aux = t.getHuecos()[j][i];
                                           out.println("<td class='text-center'> section " + aux + "</td>");
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
                <legend id="showCoursesenrol" class="noPrint">
                    Missing Enrolled
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Coursesenrol">
                    <%
                        for (Course c : courses) {
                            if (c.getPercentEnrolled() != 100) {
                                out.println("<h3>" + cs.nameCourse(c.getIdCourse()) + "</h3>");
                                out.println("<table id='table_id' class='table'>");
                                out.println("<tr><th>Field</th><th>Content</th></tr>");

                                out.println("<tr>");
                                out.println("<td>Enrolled students percent</td>");
                                out.println("<td>" + c.getPercentEnrolled() + "</td>");
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
                                    studentNames += " ," + lista2.get(c.getStudentsNoAsignados().get(i)).getName();
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
                                        if (huecosStudents[j][i] != 0) {
                                            out.println("<td class='espacioLibre'> free space </td>");
                                        } else {
                                            out.println("<td> </td>");
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
            <div role="tabpanel" class="col-xs-12 tab-pane" id="teachers">
                <legend id="showTeachers">
                    Schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Teacherstable">
                    <%
                        out.println("<h2>Teachers</h2>");
                        for (Teacher t : lista) {
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
                    %>
                </div>

                <legend id="showTeachers2">
                    Teachers Master Schedule
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
                            for (Integer i : t.getPrepsComplete()) {
                                out.println(", " + cs.nameCourse(i));
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

            </div>

        </div>
    </body>
</html>
