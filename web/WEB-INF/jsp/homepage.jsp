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
            <ul class="nav nav-tabs">
                <li class="active"><a id="Courses" data-toggle="tab" href="#courses" role="tab" >Courses</a></li>
                <li><a id="Teachers" data-toggle="tab" href="#teachers" role="tab">Teachers</a></li>
                <li><a id="Students" data-toggle="tab" href="#students" role="tab">Students</a></li>
                <li><a id="Rooms" data-toggle="tab" href="#rooms" role="tab">Rooms</a></li>               
            </ul>
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
                            out.println("<div class='col-xs-12 course'>"); 
                            out.println("<h3>" + cs.nameCourse(t.getIdCourse()) + "</h3>");
                            out.println("<table id='table_id' width='100%' border='0' class=''>");
                            out.println("<tr class='students'>");
                            for (int j = 1; j < t.getSections(); j++) {
                                String studentNames = "";
                                
                               /* out.println("<td>Section " + j + ":<br>"
                                        + "Teacher: "+t.getTeacher(lista, t.getIdCourse(), j).getName()+"</td>");
                                */
//                                out.println("<td></td>");
                                out.println("<td><strong>Section " + j + ":<br>"
                                        + "Teacher: "+t.getTeacher(lista, t.getIdCourse(), j).getName()+"</strong>");
                                
                                for (int k = 0; k < t.getStudentsAsignados().size(); k++) {
                                   /* if(k == 0)
                                            {}*/
                                    if(lista2.get(t.getStudentsAsignados().get(k)).getNumSectionByCourse(t.getIdCourse()) == j)
                                    {
                                       // studentNames +=  lista2.get(t.getStudentsAsignados().get(k)).getNumSectionByCourse(t.getIdCourse());
                                        if(!studentNames.contains(lista2.get(t.getStudentsAsignados().get(k)).getName()))
                                            studentNames += "<br>" + lista2.get(t.getStudentsAsignados().get(k)).getName();
                                    }
                                    
                                }
                                
                               out.println(studentNames);
                                out.println("</td>");
                                
                            }
                            out.println("</tr>");
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
                                        out.println("<td class='text-center'> section " + t.getHuecos()[j][i] + "</td>");
                                    } else {
                                        out.println("<td> </td>");
                                    }
                                }
                                out.println("</tr>");
                            }
                            out.println("</table>");
                            out.println("</div>");
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
                    <%
                        for (Teacher t : lista) {
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

            <div role="tabpanel" class="col-xs-12 tab-pane" id="students">
                <legend id="showStudents">
                    Students schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="Studentstable">
                    <%                    out.println("<h2>Students</h2>");
                        for (Map.Entry<Integer, Student> entry : lista2.entrySet()) {
                            out.println("<h3>" + entry.getValue().getName() + "</h3>");
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
                                    if (entry.getValue().getHuecos()[j][i] != 0) {
                                        out.println("<td>" + cs.nameCourseAndSection(entry.getValue().getHuecos()[j][i]) + "</td>");
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

                <legend id="showStudentsEnrolled">
                    Students enrolled
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="StudentsEnrolled">
                    <%
                        for (Map.Entry<Integer, Student> entry : lista2.entrySet()) {
                            out.println("<table id='table_id' class='table'>");
                            out.println("<h3>" + entry.getValue().getName() + "</h3>");
                            out.println("<tr><th>Course Name</th><th>Enrolled</th></tr>");
                            for (Integer i : entry.getValue().getCursosAsignados()) {
                                out.println("<tr>");
                                out.println("<td>" + cs.nameCourse(i) + "</td><td>yes</td>");
                                out.println("</tr>");
                            }
                            for (Integer i : entry.getValue().getCursosNoAsignados()) {
                                out.println("<tr>");
                                out.println("<td>" + cs.nameCourse(i) + "</td><td>no</td>");
                                out.println("</tr>");
                            }
                            out.println("</table>");
                            out.println("<table id='table_id' class='table'>");
                            out.println("<tr><th>Total enrolled</th><th>Total no enrolled</th></tr>");
                            out.println("<tr>");
                            out.println("<td>" + entry.getValue().getCursosAsignados().size() + "</td><td>" + entry.getValue().getCursosNoAsignados().size() + "</td>");
                            out.println("</tr>");
                            out.println("</table>");
                            totalenrolled += entry.getValue().getCursosAsignados().size();
                            totalnoenrolled += entry.getValue().getCursosNoAsignados().size();
                        }
                    %>
                </div>
            </div>

            <!--<div role="tabpanel" class="col-xs-12 tab-pane in active" id="rooms">
                <legend id="showRooms">
                    Schedule
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="roomstable">
                    <%
                        for (Integer roomid : groupRooms) {
                            out.println("<h3>" + rooms.get(roomid).getName() + "</h3>");
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
                                    if (rooms.get(roomid).getHuecos()[j][i] != 0) {
                                        out.println("<td>" + cs.nameCourseAndSection(rooms.get(roomid).getHuecos()[j][i]) + "</td>");
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

                <legend id="ocupacionrooms">
                    ocupation
                    <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                    </span>
                </legend>
                <div class="form-group collapse" id="ocupacion">
                    <%
                        out.println("<table id='table_id' class='table'>");
                        swapcolor = true;
                        out.println("<tr>"
                                + "<th>name</th>"
                                + "<th>Aviavility</th>"
                                + "<th>Ocupation</th>"
                                + "<th>Ocupation percent"
                                + "</tr>");
                        for (Integer roomid : groupRooms) {
                            if (swapcolor) {
                                out.println("<tr class='tcolores'>");
                                swapcolor = false;
                            } else {
                                out.println("<tr>");
                                swapcolor = true;
                            }
                            out.println("<th>" + rooms.get(roomid).getName() + "</th>");
                            out.println("<td>" + rooms.get(roomid).getDisponibilidad() + "</td>");
                            out.println("<td>" + rooms.get(roomid).getOcupacion() + "</td>");
                            out.println("<td>" + rooms.get(roomid).getPercentOcupation() + "</td>");
                            out.println("</tr>");
                        }
                    %>
                </div>
            </div>-->


            <!--<div role="tabpanel" class="col-xs-12 tab-pane" id="log">
                <legend id="showSTC">
                        Student Course
                        <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                        </span>
                </legend>
                <div class="form-group collapse" id="STC">
            <%
                out.println("<table id='table_id' class='table'>");
                out.println("<tr>");
                out.println("<th> Students|courses </th>");
                for (Course c : courses) {
                    out.println("<th>" + cs.nameCourse(c.getIdCourse()) + "</th>");
                }
                out.println("</tr>");
                for (Map.Entry<Integer, Student> entry : lista2.entrySet()) {
                    if (swapcolor) {
                        out.println("<tr class='tcolores'>");
                        swapcolor = false;
                    } else {
                        out.println("<tr>");
                        swapcolor = true;
                    }
                    out.println("<td>" + entry.getValue().getName() + "</td>");
                    ArrayList<Integer> sta;
                    for (Course c : courses) {
                        sta = c.getStudentsAsignados();
                        if (sta != null && sta.contains(entry.getValue().getId())) {
                            out.println("<td>" + 1 + "</td>");
                        } else if (sta != null && sta.contains(entry.getValue().getId())) {
                            out.println("<td>" + 2 + "</td>");
                        } else {
                            out.println("<td>" + 0 + "</td>");
                        }
                    }
                    out.println("</tr>");
                }
                out.println("</table>");
            %>
           </div> 
            
           <legend id="showTC">
                   Teacher Course
                   <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                   </span>
           </legend>
           <div class="form-group collapse" id="TC">
            <%
                out.println("<h2>Teacher Course</h2>");
                out.println("<table id='table_id' class='table'>");
                out.println("<tr>");
                out.println("<th> Teachers|courses </th>");
                for (Course c : courses) {
                    out.println("<th>" + cs.nameCourse(c.getIdCourse()) + "</th>");
                }
                out.println("<th>setions/maxsections</th>");
                out.println("</tr>");
                swapcolor = true;
                for (Teacher t : lista) {
                    if (swapcolor) {
                        out.println("<tr class='tcolores'>");
                        swapcolor = false;
                    } else {
                        out.println("<tr>");
                        swapcolor = true;
                    }
                    out.println("<td>" + t.getName() + "</td>");
                    for (Course c : courses) {
                        if (t.getSecciones().containsKey(c.getIdCourse())) {
                            out.println("<td>" + t.getSecciones().get(c.getIdCourse()) + "</td>");
                        } else {
                            out.println("<td>" + 0 + "</td>");
                        }
                    }
                    out.println("<td>" + t.getSecsComplete() + "/" + t.getMaxSections() + "</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            %>
            </div> -->
            <legend id="showLogComplete">
                Log complete
                <span class="col-xs-12 text-right glyphicon glyphicon-triangle-bottom">
                </span>
            </legend>
            <div class="form-group collapse" id="LogComplete">
                <%
                    out.println("<table id='table_id' class='table'>");
                    out.println("<tr><th>Total students enrolled</th>"
                            + "<th>Total students no enrolled</th>"
                            + "<th>Percent students enrolled</th></tr>");
                    out.println("<tr>");
                    out.println("<td>" + totalenrolled + "</td><td>" + totalnoenrolled + "</td>");
                    double totalRequest = totalenrolled + totalnoenrolled;
                    out.println("<td>" + (totalenrolled / totalRequest) * 100 + "</td>");
                    out.println("</tr>");
                    out.println("</table>");
                    for (String s : log) {
                        out.print("<p>");
                        out.print(s);
                        out.print("</p>");
                    }
                %>
            </div>
            <!--</div>-->
        </div>
    </body>
</html>
