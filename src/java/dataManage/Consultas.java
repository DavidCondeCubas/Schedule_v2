/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataManage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Algoritmo;
import model.Course;
import model.DBConnect;
import model.Room;
import model.Student;
import model.Teacher;
import model.Template;

/**
 *
 * @author Norhan
 */
public class Consultas {

    private ArrayList<Integer> teachers;
    private Teacher tdefault;
    private Student stDefault;
    private HashMap<Integer, String> courseName;
    //*****////
    private HashMap<Integer, String> namePersons;
    private HashMap<Integer, String> nameCourses;

    /**
     *
     *////
    private ArrayList<ArrayList<Boolean>> totalBlocksStart;
    private int totalBlocks;

    public Consultas() {
        teachers = new ArrayList<>();
        tdefault = teacherDefault();
        stDefault = new Student(0);
        stDefault.setGenero("Male");
        stDefault.setName("default");
        courseName = new HashMap<>();
        totalBlocksStart = this.totalBlocksStart();
        totalBlocks = this.totalBlocks();
        cargarNames();
    }

    private void cargarNames() {
        this.namePersons = new HashMap<>();
        this.nameCourses = new HashMap<>();

        String consulta = "select * from person";
        String ret = "";
        ResultSet rs;
        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret = rs.getString("lastname") + ", ";
                ret += rs.getString("firstname");
                if (!this.namePersons.containsKey(rs.getInt("personid"))) {
                    this.namePersons.put(rs.getInt("personid"), ret);
                }
            }

            consulta = "select * from courses";
            ResultSet rs2 = DBConnect.renweb.executeQuery(consulta);

            while (rs2.next()) {
                int idCourse = rs2.getInt("CourseID");
                String title = rs2.getString("Title");
                if (!this.nameCourses.containsKey(idCourse)) {
                    this.nameCourses.put(idCourse, title);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    --------------------------------------
    ---FUNCIONES PARA EXTRACCION DE DATOS-
    ---DE RENWEB.-------------------------
    --------------------------------------
     */
    public static ArrayList<Tupla<Integer, String>> getYears() { // OBTIENE EL GETYEARS FALTA FILTRAR POR COLEGIO 
        ArrayList<Tupla<Integer, String>> ret = new ArrayList<>();
        String consulta = "select * from SchoolYear";
        try {
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                int yearid = rs.getInt("yearid");
                String yearName = rs.getString("SchoolYear");
                ret.add(new Tupla<>(yearid, yearName));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static HashMap<Integer, ArrayList<Integer>> getCoursesGroups(ArrayList<Student> st, ArrayList<Integer> listaCourses,
            String yearid, String tempid) {
        String consulta = "select * from ClassGroups where yearid =" + yearid + " and templateid=" + tempid;
        ArrayList<Integer> groups = new ArrayList();

        HashMap<Integer, ArrayList<Integer>> classes = new HashMap();
        HashMap<Integer, ArrayList<Integer>> courses = new HashMap();
        try {
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                groups.add(rs.getInt("GroupID"));
                st.add(new Student(rs.getInt("GroupID"), "group" + rs.getInt("GroupID"), "group"));
            }

            HashMap<Integer, Integer> classesData = new HashMap();
            HashMap<Integer, Integer> coursesData = new HashMap();

            consulta = "select * from ClassGroupClasses";
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                classesData.put(rs.getInt("GroupID"), rs.getInt("classid"));
            }

            consulta = "select * from classes";
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                coursesData.put(rs.getInt("classid"), rs.getInt("courseid"));
            }

            for (Integer g : groups) {
                classes.put(g, new ArrayList());
                /*consulta = "select * from ClassGroupClasses where GroupID=" + g;
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    classes.get(g).add(rs.getInt("classid"));
                }*/
                if (classesData.containsKey(rs.getInt("classid"))) {
                    classes.get(g).add(classesData.get(g));
                }

                for (Integer c : classes.get(g)) {
                    /*  consulta = "select * from classes where classid=" + c;
                    rs = DBConnect.renweb.executeQuery(consulta);
                    while (rs.next()) {
                        if (!courses.containsKey(rs.getInt("courseid"))) {
                            courses.put(rs.getInt("courseid"), new ArrayList());
                            listaCourses.add(rs.getInt("courseid"));
                        }
                        courses.get(rs.getInt("courseid")).add(g);
                    }*/
                    if (!courses.containsKey(coursesData.get(c))) {
                        courses.put(coursesData.get(c), new ArrayList());
                        listaCourses.add(coursesData.get(c));
                    }
                    courses.get(coursesData.get(c)).add(g);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return courses;
    }

    public static ArrayList<Template> getTemplates(String yearid) {
        ArrayList<Template> ret = new ArrayList();
        String consulta = "select * from ScheduleTemplate where yearid=" + yearid;
        try {
            // SELECT count(*) FROM IS_PAN.dbo.ScheduleTemplateTimeTable where templateid = 51 and col =0; para las rows
            // SELECT count(*) FROM IS_PAN.dbo.ScheduleTemplateTimeTable where templateid = 51 and row =0; para las cols
            //  int numRowsVacias = 2;
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                String name = rs.getString("TemplateName");
                int cols = rs.getInt("cols");
                int rows = rs.getInt("rows");
                int id = rs.getInt("templateid");
                ret.add(new Template(id, cols, rows, name));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public static ArrayList<Tupla<String, String>> getRowHeader(int id, int rows) {
        String consulta = "";

        ResultSet rs;
        ArrayList<Tupla<String, String>> ret = new ArrayList();
        for (int i = 1; i <= rows; i++) {
            consulta = "select * from ScheduleTemplateTimeTable "
                    + "where templateid=" + id + " and Row=" + i + " and Col=0";
            // SELECT count(*) FROM IS_PAN.dbo.ScheduleTemplateTimeTable where templateid = 51 and col =0; para las rows
            // SELECT count(*) FROM IS_PAN.dbo.ScheduleTemplateTimeTable where templateid = 51 and row =0; para las cols

            try {
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ret.add(new Tupla(rs.getString("TemplateTime"),
                            rs.getString("TemplateText")));
                }
            } catch (SQLException ex) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }

    public static ArrayList<String> getColHeader(int id, int cols) { //modificar 
        String consulta = "";
        ResultSet rs;
        ArrayList<String> ret = new ArrayList();
        for (int i = 1; i <= cols; i++) {
            // SELECT count(*) FROM IS_PAN.dbo.ScheduleTemplateTimeTable where templateid = 51 and col =0; para las rows
            // SELECT count(*) FROM IS_PAN.dbo.ScheduleTemplateTimeTable where templateid = 51 and row =0; para las cols

            consulta = "select * from ScheduleTemplateTimeTable "
                    + "where templateid=" + id + " and Col=" + i + " and Row=0";
            try {
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ret.add(rs.getString("TemplateText"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }

    private void cargarHashMap(HashMap<Integer, Object> hashObject, String groupType, String fieldName, String type) {
        String consulta = "select udd.data,udd.id\n"
                + "                from uddata udd\n"
                + "                inner join udfield udf\n"
                + "                    on udd.fieldid = udf.fieldid\n"
                + "                inner join udgroup udg\n"
                + "                    on udg.groupid = udf.groupid\n"
                + "                    and udg.grouptype = '" + groupType + "'\n"
                + "                    and udg.groupname = 'Schedule'\n"
                + "                    and udf.fieldName = '" + fieldName + "'\n";

        ResultSet rs;
        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                //   ret.get(i).setBlocksWeek(rs.getInt(1));
                switch (type) {
                    case "Integer":
                        hashObject.put(rs.getInt("id"), rs.getInt("data"));
                        break;
                    case "Boolean":
                        hashObject.put(rs.getInt("id"), rs.getBoolean("data"));
                        break;
                    case "String":
                        hashObject.put(rs.getInt("id"), rs.getString("data"));
                        break;
                    default:
                        break;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //AQUI ES DONDE TARDA *****
    public ArrayList<Course> getRestriccionesCourses(int[] ids, int[] tempinfo) {
        ArrayList<Course> ret = new ArrayList<>();
        String consulta = "";
        HashMap<Integer, String> hashUno = new HashMap<>();
        HashMap<Integer, String> hashDos = new HashMap<>();

        try {
            ResultSet rs;

            consulta = "select * from courses"
                    + " where Elementary=" + tempinfo[0]
                    + " and HS=" + tempinfo[1]
                    + " and MidleSchool=" + tempinfo[2]
                    + " and PreSchool=" + tempinfo[3];
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                hashUno.put(rs.getInt("courseid"), "");
            }

            consulta = "select udd.data,udd.id\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'course'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'Schedule'\n";
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                hashDos.put(rs.getInt("id"), rs.getString("data"));
            }

            for (int i = 0; i < ids.length; i++) {
                /*tempcorrect = false;
                consulta = "select * from courses where courseid=" + ids[i]
                        + " and Elementary=" + tempinfo[0]
                        + " and HS=" + tempinfo[1]
                        + " and MidleSchool=" + tempinfo[2]
                        + " and PreSchool=" + tempinfo[3];
                rs = DBConnect.renweb.executeQuery(consulta);
                if (rs.next()) {
                    tempcorrect = true;
                }
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'Schedule'\n"
                        + "                where udd.id =" + ids[i];
                rs = DBConnect.renweb.executeQuery(consulta);
                if (rs.next() && tempcorrect) {
                    if (rs.getInt(1) == 1) {
                        Course r = new Course(ids[i]);
                        ret.add(r);
                        courseName.put(ids[i], this.nameCourses.get(ids[i]));
                    }
                }*/
                if (hashUno.containsKey(ids[i]) && hashDos.containsKey(ids[i])) {
                    Course r = new Course(ids[i]);
                    ret.add(r);
                    courseName.put(ids[i], this.nameCourses.get(ids[i]));
                }
            }
            HashMap<Integer, Object> hashBlocksPerWeek = new HashMap<>();
            HashMap<Integer, Object> hashGR = new HashMap<>();
            HashMap<Integer, Object> hashMaxSections = new HashMap<>();
            HashMap<Integer, Object> hashMinGapBlocks = new HashMap<>();
            HashMap<Integer, Object> hashMinGapDays = new HashMap<>();
            HashMap<Integer, Object> hashRank = new HashMap<>();
            HashMap<Integer, Object> hashTeachers = new HashMap<>();
            HashMap<Integer, Object> hashRooms = new HashMap<>();
            HashMap<Integer, Object> hashExcludeBlocksCourse = new HashMap<>();
            HashMap<Integer, Object> hashExcludeBlocksSchool = new HashMap<>();
            HashMap<Integer, Object> hashPreferredBlock = new HashMap<>();
            HashMap<Integer, Object> hashbalanceTeachers = new HashMap<>();
            HashMap<Integer, Object> hashMandatoryBlocksRange = new HashMap<>();
            HashMap<Integer, Object> hashmaxBxD = new HashMap<>();

            //cargarHashMap(hashBlocksPerWeek,'course','BlocksPerWeek','int');
            cargarHashMap(hashBlocksPerWeek, "course", "BlocksPerWeek", "Integer");
            cargarHashMap(hashGR, "course", "GR", "Boolean");
            cargarHashMap(hashMaxSections, "course", "MaxSections", "String");
            cargarHashMap(hashMinGapBlocks, "course", "MinGapBlocks", "String");
            cargarHashMap(hashMinGapDays, "course", "MinGapDays", "Integer");
            cargarHashMap(hashRank, "course", "Rank", "Integer");
            cargarHashMap(hashTeachers, "course", "Teachers", "String");
            cargarHashMap(hashRooms, "course", "Rooms", "String");
            cargarHashMap(hashExcludeBlocksCourse, "course", "ExcludeBlocks", "String");
            cargarHashMap(hashExcludeBlocksSchool, "school", "ExcludeBlocks", "String");
            cargarHashMap(hashPreferredBlock, "course", "PreferredBlock", "String");
            cargarHashMap(hashbalanceTeachers, "course", "balanceTeachers", "Boolean");
            cargarHashMap(hashMandatoryBlocksRange, "course", "MandatoryBlocksRange", "String");
            cargarHashMap(hashmaxBxD, "course", "maxBxD", "Integer");

            for (int i = 0; i < ret.size(); i++) {

                /*int prueba1 = (int) hashBlocksPerWeek.get(ret.get(i).getIdCourse());
                boolean prueba2 = (boolean) hashGR.get(ret.get(i).getIdCourse());
                // for (int i = 0; i < 1; i++) {
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'BlocksPerWeek'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ret.get(i).setBlocksWeek(rs.getInt(1));
                }*/
                if (hashBlocksPerWeek.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setBlocksWeek((int) hashBlocksPerWeek.get(ret.get(i).getIdCourse()));
                }
                /*
                
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'GR'\n"
                        + "                where udd.id=" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ret.get(i).setGR(rs.getBoolean(1));
                }
                 */
                if (hashGR.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setGR((boolean) hashGR.get(ret.get(i).getIdCourse()));
                }
                /*
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'MaxSections'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ret.get(i).setMaxSections(rs.getString(1));
                }
                 */
                if (hashMaxSections.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setMaxSections((String) hashMaxSections.get(ret.get(i).getIdCourse()));
                }
                /*
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'MinGapBlocks'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ret.get(i).setMinGapBlocks(rs.getString(1));
                }
/*/
                if (hashMinGapBlocks.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setMinGapBlocks((String) hashMinGapBlocks.get(ret.get(i).getIdCourse()));
                }
                /*
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'MinGapDays'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    try {
                        ret.get(i).setMinGapDays(rs.getInt(1));
                    } catch (Exception e) {
                    }
                }*/

                if (hashMinGapDays.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setMinGapDays((int) hashMinGapDays.get(ret.get(i).getIdCourse()));
                }
                /*
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'Rank'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    try {
                        ret.get(i).setRank(rs.getInt(1));
                    } catch (Exception e) {
                    }
                }*/
                if (hashRank.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setRank((int) hashRank.get(ret.get(i).getIdCourse()));
                }
                /*
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'Teachers'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                String[] s = new String[2];
                while (rs.next()) {
                    s = rs.getString(1).split(",");
                }*/

                String[] s = new String[2];
                if (hashTeachers.containsKey(ret.get(i).getIdCourse())) {
                    s = ((String) hashTeachers.get(ret.get(i).getIdCourse())).split(",");
                }
                ArrayList<Integer> ar = new ArrayList<>();
                for (String s2 : s) {
                    if (s2 != null) {
                        int idt = convertString(s2);
                        ar.add(idt);
                        if (!teachers.contains(idt)) {
                            teachers.add(idt);
                        }
                    }
                }
                ret.get(i).setTrestricctions(ar);
                /*
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'Rooms'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                String rooms = "";
                while (rs.next()) {
                    rooms = rs.getString(1);
                }
                if (!rooms.equals("")) {
                    for (String room : rooms.split(",")) {
                        try {
                            ret.get(i).addRoom(Integer.parseInt(room));
                        } catch (Exception e) {
                            System.err.println("no se puede leer bien el campo rooms en el curso"
                                    + ret.get(i).getIdCourse());
                        }
                    }
                }
                 */
                String rooms = "";
                if (hashRooms.containsKey(ret.get(i).getIdCourse())) {
                    rooms = (String) hashRooms.get(ret.get(i).getIdCourse());
                }
                if (!rooms.equals("")) {
                    for (String room : rooms.split(",")) {
                        try {
                            ret.get(i).addRoom(Integer.parseInt(room));
                        } catch (Exception e) {
                            System.err.println("no se puede leer bien el campo rooms en el curso"
                                    + ret.get(i).getIdCourse());
                        }
                    }
                }
                /*
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'ExcludeBlocks'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                String excludes = "";
                while (rs.next()) {
                    excludes += rs.getString(1);
                }
                 */
                String excludes = "";
                if (hashExcludeBlocksCourse.containsKey(ret.get(i).getIdCourse())) {
                    excludes += (String) hashExcludeBlocksCourse.get(ret.get(i).getIdCourse());
                }

                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'school'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'ExcludeBlocks'";

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    if (!excludes.contains(rs.getString(1))) {
                        excludes += rs.getString(1);
                    }
                }

                ret.get(i).setExcludeBlocks(excludes);

                //**David solo prueba**//         
                String prefered = "";
                /*consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'PreferredBlock'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    prefered += rs.getString(1);
                }*/
                if (hashPreferredBlock.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setPreferedBlocks((String) hashPreferredBlock.get(ret.get(i).getIdCourse()));
                }

                //**David solo prueba**//         
                boolean balanceTeachers = false;
                /* consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'balanceTeachers'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    balanceTeachers = rs.getBoolean(1);
                }
                 */
                if (hashbalanceTeachers.containsKey(ret.get(i).getIdCourse())) {
                    balanceTeachers = (boolean) hashbalanceTeachers.get(ret.get(i).getIdCourse());
                }
                ret.get(i).setBalanceTeachers(balanceTeachers);

                String mandatoryBlock = "";
                /*consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'MandatoryBlocksRange'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    mandatoryBlock = rs.getString(1);
                }
                */
                if (hashMandatoryBlocksRange.containsKey(ret.get(i).getIdCourse())) {
                    mandatoryBlock = (String) hashMandatoryBlocksRange.get(ret.get(i).getIdCourse());
                }
                
                ret.get(i).setMandatoryBlockRange(mandatoryBlock);

                int numBxD = 1;
                /*consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'course'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'maxBxD'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    numBxD = rs.getInt(1);
                }
*/ 
                if (hashmaxBxD.containsKey(ret.get(i).getIdCourse())) {
                    numBxD = (int) hashmaxBxD.get(ret.get(i).getIdCourse());
                }
                ret.get(i).setMaxBlocksPerDay(numBxD);

                ///***///
                consulta = "select MaxSize from courses where courseid="
                        + ret.get(i).getIdCourse();
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ret.get(i).setMaxChildPerSection(rs.getInt(1));
                }
                //prueba
                ret.get(i).insertarOActualizarCurso();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public HashMap<Integer, Room> getRooms() {
        HashMap<Integer, Room> rooms = new HashMap();
        String consulta = "select * from rooms";
        ResultSet rs;
        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                int id = rs.getInt("roomid");
                Room r = new Room(rs.getInt("roomid"), rs.getString("room"), rs.getInt("size"));
                rooms.put(id, r);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rooms;
    }

    private int totalBlocks() {

        String excludes = "";
        int ret = Algoritmo.TAMX * Algoritmo.TAMY;
        Course caux = new Course(1);
        String consulta = "select udd.data\n"
                + "                from uddata udd\n"
                + "                inner join udfield udf\n"
                + "                    on udd.fieldid = udf.fieldid\n"
                + "                inner join udgroup udg\n"
                + "                    on udg.groupid = udf.groupid\n"
                + "                    and udg.grouptype = 'school'\n"
                + "                    and udg.groupname = 'Schedule'\n"
                + "                    and udf.fieldName = 'ExcludeBlocks03'";

        ResultSet rs;
        try {
            rs = DBConnect.renweb.executeQuery(consulta);

            while (rs.next()) {
                if (!excludes.contains(rs.getString(1))) {
                    excludes += rs.getString(1);
                }
            }
            caux.setExcludeBlocks(excludes); //quita los bloques excluidos
            //ret = caux.opciones().size(); 

            ret = 33; //solo prueba

        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    // * @author David
    private ArrayList<ArrayList<Boolean>> totalBlocksStart() {

        String excludes = "";
        int ret = Algoritmo.TAMX * Algoritmo.TAMY;
        Course caux = new Course(1);
        String consulta = "select udd.data\n"
                + "                from uddata udd\n"
                + "                inner join udfield udf\n"
                + "                    on udd.fieldid = udf.fieldid\n"
                + "                inner join udgroup udg\n"
                + "                    on udg.groupid = udf.groupid\n"
                + "                    and udg.grouptype = 'school'\n"
                + "                    and udg.groupname = 'Schedule'\n"
                + "                    and udf.fieldName = 'ExcludeBlocks03'";

        ResultSet rs;
        try {
            rs = DBConnect.renweb.executeQuery(consulta);

            while (rs.next()) {
                if (!excludes.contains(rs.getString(1))) {
                    excludes += rs.getString(1);
                }
            }
            caux.setExcludeBlocks(excludes); //quita los bloques excluidos 

        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return caux.opcionesStart();
    }

    private Teacher teacherDefault() {
        Teacher ret = new Teacher();
        String consulta;
        ResultSet rs;
        try {
            consulta = "select udd.data\n"
                    + "        from uddata udd\n"
                    + "        inner join udfield udf\n"
                    + "            on udd.fieldid = udf.fieldid\n"
                    + "        inner join udgroup udg\n"
                    + "            on udg.groupid = udf.groupid\n"
                    + "            and udg.grouptype = 'school'\n"
                    + "            and udg.groupname = 'Schedule'\n"
                    + "            and udf.fieldName = 'MaxSections'";
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret.setMaxSections(rs.getInt(1));
            }

            consulta = "select udd.data\n"
                    + "        from uddata udd\n"
                    + "        inner join udfield udf\n"
                    + "            on udd.fieldid = udf.fieldid\n"
                    + "        inner join udgroup udg\n"
                    + "            on udg.groupid = udf.groupid\n"
                    + "            and udg.grouptype = 'school'\n"
                    + "            and udg.groupname = 'Schedule'\n"
                    + "            and udf.fieldName = 'MaxPreps'";
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret.setPreps(rs.getInt(1));
            }

            consulta = "select udd.data\n"
                    + "        from uddata udd\n"
                    + "        inner join udfield udf\n"
                    + "            on udd.fieldid = udf.fieldid\n"
                    + "        inner join udgroup udg\n"
                    + "            on udg.groupid = udf.groupid\n"
                    + "            and udg.grouptype = 'school'\n"
                    + "            and udg.groupname = 'Schedule'\n"
                    + "            and udf.fieldName = 'MaxBxD'\n";
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret.setMaxBxD(rs.getInt(1));
            }
        } catch (Exception ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public ArrayList<Teacher> teachersList(String tempid) {
        ArrayList<Teacher> ret = new ArrayList<>();
        for (Integer s : teachers) {
            if (!s.equals("")) {
                ret.add(restriccionesTeacher(tempid, s));
            }
        }
        return ret;
    }

    public Teacher restriccionesTeacher(String tempid, int id) {
        Teacher ret = new Teacher();
        String consulta = "";

        ResultSet rs;
        if (id != 0) {
            try {
                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'Staff'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'MaxSections'\n"
                        + "                where udd.id =" + id;
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ret.setMaxSections(rs.getInt(1));
                }
                if (ret.getMaxSections() == 0) {
                    ret.setMaxSections(tdefault.getMaxSections());
                }

                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'Staff'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'Preps'\n"
                        + "                where udd.id =" + id;
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ret.setPreps(rs.getInt(1));
                }
                if (ret.getPreps() == 0) {
                    ret.setPreps(tdefault.getPreps());
                }

                consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'Staff'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'MaxBxD'\n"
                        + "                where udd.id =" + id;
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    String s = rs.getString(1);
                    try {
                        ret.setMaxBxD(Integer.parseInt(s));
                    } catch (Exception e) {
                        ret.setMaxBxD(1);
                    }
                }
                if (ret.getMaxBxD() == 0) {
                    ret.setMaxBxD(tdefault.getMaxBxD());
                }

                consulta = "select * from ScheduleTemplateStaff where staffid=" + id
                        + " and " + "templateid=" + tempid;
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    if (rs.getBoolean("scheduleblock")) {
                        Tupla t = new Tupla(rs.getInt("day") - 1, rs.getInt("period") - 1);
                        ret.addExcludeBlock(t);
                    }
                }
                ret.setIdTeacher(id);
            } catch (Exception ex) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ret.setName(this.namePersons.get(id));
        return ret;
    }

    //to do: FUNCION NO PROBADA CONVIERTE EXCLUDE BLOCKS ENTRE TEMPLATES
    //SE LO EXPLIQUE A DAVID EL ULTIMO DIA.
    private void setExcludeBlocksTeacher(Teacher t, String tempid) {
        String consulta = "select * from ScheduleTemplateStaff where staffid=" + t.getIdTeacher();
        try {
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                if (rs.getBoolean("scheduleblock")) {
                    ArrayList<Tupla> ar = conversionTemplatesBlocks(tempid, rs.getString("templateid"),
                            rs.getInt("day"), rs.getInt("period"));
                    for (Tupla t2 : ar) {
                        t.addExcludeBlock(t2);
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    private boolean esMultiplo(int x, int y) {
        if (x % y == 0 || y % x == 0) {
            return true;
        } else {
            return false;
        }
    }

    //FUNCION NO PROBADA
    private ArrayList<Tupla> conversionTemplatesBlocks(String iddestino, String idorigen, int day, int period) {
        ArrayList<Tupla> ret = new ArrayList();
        ArrayList<Integer> colsBlock = new ArrayList();
        ArrayList<Integer> rowsBlock = new ArrayList();
        int maxtemp1 = 0, maxtemp2 = 0;
        String consulta = "select cols as maximo "
                + "from ScheduleTemplate where TemplateID=" + iddestino;
        try {
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                maxtemp1 = rs.getInt("maximo");
            }
            consulta = "select cols as maximo "
                    + "from ScheduleTemplate where TemplateID=" + idorigen;
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                maxtemp2 = rs.getInt("maximo");
            }
            if (esMultiplo(maxtemp1, maxtemp2)) {
                int colblock = day;
                if (maxtemp1 <= maxtemp2) {
                    while (colblock > maxtemp1) {
                        colblock -= maxtemp1;
                    }
                    colsBlock.add(colblock);
                } else {
                    while (colblock < maxtemp1) {
                        colsBlock.add(colblock);
                        colblock += maxtemp1;
                    }
                }
            }

            //saco los intervalos de tiempo de cada bloque en su respectivo template
            int minutosOrigenIni = 0;
            int minutosOrigenFin = 0;
            int minutosDestinoIni = 0;
            int minutosDestinoFin = 0;
            consulta = "select TemplateTime "
                    + "from ScheduleTemplateTimeTable where TemplateID=" + idorigen
                    + " and col=" + day + " and row=" + period;
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                String[] time = rs.getString(1).split("-");
                String[] tmaux = time[0].split(":");
                minutosOrigenIni = Integer.parseInt(tmaux[0]) * 60
                        + Integer.parseInt(tmaux[1]);
                tmaux = time[1].split(":");
                minutosOrigenFin = Integer.parseInt(tmaux[0]) * 60
                        + Integer.parseInt(tmaux[1]);
            }
            for (int i = 0; i < maxtemp2; i++) {
                consulta = "select TemplateTime "
                        + "from ScheduleTemplateTimeTable where TemplateID=" + iddestino
                        + " and col=1 and row=" + i;
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    String[] time = rs.getString(1).split("-");
                    String[] tmaux = time[0].split(":");
                    minutosDestinoIni = Integer.parseInt(tmaux[0]) * 60
                            + Integer.parseInt(tmaux[1]);
                    tmaux = time[1].split(":");
                    minutosDestinoFin = Integer.parseInt(tmaux[0]) * 60
                            + Integer.parseInt(tmaux[1]);
                    if (!rowsBlock.contains(i) && minutosDestinoIni < minutosOrigenIni || minutosOrigenFin < minutosDestinoFin) {
                        rowsBlock.add(i);
                    }
                }
            }

            if (colsBlock.isEmpty()) {
                for (int i = 0; i < maxtemp1; i++) {
                    for (Integer row : rowsBlock) {
                        ret.add(new Tupla(i, row));
                    }
                }
            } else {
                for (Integer i : colsBlock) {
                    for (Integer row : rowsBlock) {
                        ret.add(new Tupla(i, row));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public int[] templateInfo(String tempid) {
        int[] ret = new int[4];
        String consulta = "select * from ScheduleTemplate where templateid=" + tempid;
        try {
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret[0] = rs.getInt("Elementary");
                ret[1] = rs.getInt("HighSchool");
                ret[2] = rs.getInt("MiddleSchool");
                ret[3] = rs.getInt("Preschool");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public ArrayList<Integer> roomsGroup(String groupOfRooms) {
        ArrayList<Integer> rooms = new ArrayList();
        if (!groupOfRooms.equals("0")) {
            try {
                String consulta = "select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                    on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                    on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'school'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = '" + groupOfRooms + "'";

                ResultSet rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    groupOfRooms = rs.getString(1);
                    String[] s = groupOfRooms.split(",");
                    for (String s2 : s) {
                        try {
                            rooms.add(Integer.parseInt(s2));
                        } catch (Exception ex) {
                            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return rooms;
    }

    /**
     *
     * @param c
     * @param stCourse
     * @param yearid
     * @return
     */
    public ArrayList<Student> restriccionesStudent(ArrayList<Integer> c, HashMap<Integer, ArrayList<Integer>> stCourse, String yearid) {
        ArrayList<Student> ret = new ArrayList<>();
        String consulta = "    select sr.courseid, sr.studentid, p.gender\n"
                + "    from studentrequests sr, person p, person_student ps \n"
                + "     where sr.studentid = p.personid\n"
                + "    and ps.studentid = p.personid\n"
                + "    and sr.yearid = " + yearid
                + "    and ps.status = 'enrolled'\n"
                + "    and ps.nextstatus = 'enrolled'\n"
                + "    order by gender";
        ResultSet rs;
        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                int courseid = rs.getInt("courseid");
                int studentid = rs.getInt("studentid");
                Student st = new Student(studentid);
                st.setGenero(rs.getString("gender"));
                if (!c.contains(courseid)) {
                    c.add(courseid);
                }
                if (!ret.contains(st)) {
                    ret.add(st);
                }
                if (!stCourse.containsKey(courseid)) {
                    stCourse.put(courseid, new ArrayList());
                }
                stCourse.get(courseid).add(studentid);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (ret.isEmpty()) {
            ret.add(stDefault);
        } else {
            for (Student st : ret) {
                st.setName(this.namePersons.get(st.getId()));
            }
        }
        return ret;
    }

    public static int[] convertIntegers(ArrayList<Integer> integers) {
        int[] ret = new int[integers.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    private String fetchNameCourse(int id) {
        String ret = "";
        try {
            String consulta = "select * from courses where courseid = " + id;
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret = rs.getString("title");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public String nameCourse(int id) {
        return courseName.get(id);
    }

    public String nameCourseAndSection(int id) {
        if (id == 0) {
            return "0";
        }
        int idc = id / 100;
        id = id - (idc * 100);

        return nameCourse(idc) + " Section: " + id;
    }

    public String fetchName(int id) {
        String consulta = "select * from person where personid=" + id;
        String ret = "";
        ResultSet rs;
        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret = rs.getString("lastname") + ", ";
                ret += rs.getString("firstname");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    private int convertString(String s) { // "crea identificador"
        int ret = 0;
        for (int i = 1; i <= s.length(); i++) {
            switch (s.substring(i - 1, i)) {
                case "0":
                case "1":
                case "2":
                case "3":
                case "4":
                case "5":
                case "6":
                case "7":
                case "8":
                case "9":
                    ret *= 10;
                    ret += Integer.parseInt(s.substring(i - 1, i));
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    /*
    ------------------------
    --CONSULTAS OWN SERVER--
    ------------------------
     */
    public ArrayList<Course> getCoursesOwnDB() {
        ArrayList<Course> ret = new ArrayList();
        String consulta = "select * from courses order by id ASC";
        String teachers = "";
        try {
            ResultSet rs = DBConnect.own.executeQuery(consulta);
            while (rs.next()) {
                Course c = new Course(rs.getInt("id"));
                c.setBlocksWeek(rs.getInt("blocksperweek"));
                c.setMaxSections("" + rs.getInt("maxsections"));
                c.setMinGapBlocks("" + rs.getInt("mingapblocks"));
                c.setMinGapDays(rs.getInt("mingapdays"));
                c.setRank(rs.getInt("rank"));
                c.setGR(rs.getBoolean("gender"));
                c.setExcludeBlocksOwnDB(rs.getString("excludeblocks"));
                // c.setMaxBlocksPerDay(rs.getInt("maxblocksperday"));
                c.setRooms(rs.getString("rooms"));
                c.setExcludeCols("excludecols");
                c.setExcludeRows("ecluderows");
                /**/
                c.setBalanceTeachers(rs.getBoolean("balanceteacher"));

                // String sAux = rs.getString("preferedblocks");
                //sAux = sAux.substring(1, sAux.length()-1);
                c.setPreferedBlocks(rs.getString("preferedblocks"));
                /**/

                teachers = rs.getString("teachers");
                teachers = teachers.replace("[", "");
                teachers = teachers.replace("]", "");
                String[] tlist = teachers.split(",");
                ArrayList<Integer> tids = new ArrayList();
                for (String s : tlist) {
                    try {
                        tids.add(Integer.parseInt(s));
                    } catch (Exception e) {
                    }
                }
                c.setTrestricctions(tids);
                ret.add(c);
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public ArrayList<Teacher> getTeachersOwnDB() {
        ArrayList<Teacher> ret = new ArrayList();
        String consulta = "select * from teachers";
        try {
            ResultSet rs = DBConnect.own.executeQuery(consulta);
            while (rs.next()) {
                Teacher t = new Teacher();
                t.setIdTeacher(rs.getInt("id"));
                t.setMaxSections(rs.getInt("maxsections"));
                t.setPreps(rs.getInt("maxpreps"));
                t.setMaxBxD(rs.getInt("maxblocksperday"));
                t.setExcludeBlocks(rs.getString("excludeblocks"));
                t.setName(rs.getString("name"));
                ret.add(t);
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public HashMap<Integer, Room> getRoomsOwnDB() {
        HashMap<Integer, Room> ret = new HashMap();
        String consulta = "select * from rooms";
        try {
            ResultSet rs = DBConnect.own.executeQuery(consulta);
            while (rs.next()) {
                int id = rs.getInt("id");
                Room r = new Room(id, rs.getString("name"), rs.getInt("size"));
                ret.put(id, r);
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public HashMap<Integer, Student> getStudnetsOwnDB() {
        HashMap<Integer, Student> ret = new HashMap();
        String consulta = "select * from students";
        try {
            ResultSet rs = DBConnect.own.executeQuery(consulta);
            while (rs.next()) {
                int id = rs.getInt("id");
                Student st = new Student(id, rs.getString("name"), rs.getString("genero"));
                ret.put(id, st);
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public HashMap<Integer, ArrayList<Integer>> getStudentsCourseOwnDB() {
        HashMap<Integer, ArrayList<Integer>> ret = new HashMap();
        String consulta = "select distinct * from students_course";
        try {
            ResultSet rs = DBConnect.own.executeQuery(consulta);
            while (rs.next()) {
                int idc = rs.getInt("id_course");
                int ids = rs.getInt("id_student");
                if (ret.containsKey(idc)) {
                    ret.get(idc).add(ids);
                } else {
                    ret.put(idc, new ArrayList());
                    ret.get(idc).add(ids);
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    /*
    -----------------------
    --GETTERS AND SETTERS--
    -----------------------
     */
    public ArrayList<ArrayList<Boolean>> getTotalBlocksStart() {
        return totalBlocksStart;
    }

    public void setTotalBlocksStart(ArrayList<ArrayList<Boolean>> totalBlocks) {
        this.totalBlocksStart = totalBlocks;
    }

    void fillHashCourses(ArrayList<Course> courses) {
        for (int i = 0; i < courses.size(); i++) {
            courseName.put(courses.get(i).getIdCourse(), this.nameCourses.get(courses.get(i).getIdCourse()));
        }
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(int totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

}
