package dataManage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Algoritmo;
import static model.Algoritmo.NumNomSection;
import model.Course;
import model.DBConnect;
import model.Room;
import model.Seccion;
import model.Student;
import model.Teacher;
import model.Template;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

public class Consultas {

    private ArrayList<Integer> teachers;
    public static HashMap<Integer, ArrayList<Integer>> teachersCOURSE = new HashMap<>();
    private Teacher tdefault;
    private Student stDefault;
    public static HashMap<Integer, String> courseName = new HashMap<>();
    public static int DEFAULT_RANK = 10;
    private HashMap<Integer, String> namePersons;
    private HashMap<Integer, String> nameCourses;
    private HashMap<Integer, String> abbrevCourses;
    public int templateIdSection;
    public static TreeMap<String, ArrayList<String>> tempIdSect = new TreeMap<>();
    public static String tempidsect;
    public static TreeMap<String, TreeMap< String, ArrayList<String>>> studError = new TreeMap<>();
    public static String studE;
    public static String error = "";
    public static HashMap<Integer, Integer> countCourse = new HashMap<>();
    public static HashMap<Integer, ArrayList<Integer>> countCourse2 = new HashMap<>();
    public static HashMap<Integer, ArrayList<Integer>> countSchool = new HashMap<>();
    public static String alert = "";
    private ArrayList<ArrayList<Boolean>> totalBlocksStart;
    private int totalBlocks;

    public Consultas(String tempid, int x, int y) {
//Datos por defecto que se establecen (profesores, instructor por defecto, condiciones de alumnos,
//carga de nombres de personas y cursos, y bloques con los que se inicia el template):        
        teachers = new ArrayList<>();
//Se establecen las restricciones que tendrá un profesor por defecto en el caso de que,
//al buscar en BBDD las restricciones, no las encuentre:
        tdefault = teacherDefault(x, y);
        stDefault = new Student(0);
        stDefault.setGenero("Male");
        stDefault.setName("default");
//Se definen los totalBlocksStart, y con esos se definen los totalBlocks en SetTotalBlocks:        
        totalBlocksStart = this.totalBlocksStart(tempid);
        setTotalBlocks(totalBlocksStart);
        cargarNames();
    }
//Se cargan nombres de personas y cursos con el siguiente método:

    private void cargarNames() {
        this.namePersons = new HashMap<>();
        this.nameCourses = new HashMap<>();
        this.abbrevCourses = new HashMap<>();

        String consulta = "select firstname, lastname, personid from person";
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

            consulta = "select CourseID, Title, Abbreviation from courses";
            ResultSet rs2 = DBConnect.renweb.executeQuery(consulta);

            while (rs2.next()) {
                int idCourse = rs2.getInt("CourseID");
                String title = rs2.getString("Title");
                if (!this.nameCourses.containsKey(idCourse)) {
                    this.nameCourses.put(idCourse, title);
                    this.abbrevCourses.put(idCourse, rs2.getString("Abbreviation"));
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap<Integer, String> getAbbrevCourses() {
        return abbrevCourses;
    }

    public void setAbbrevCourses(HashMap<Integer, String> abbrevCourses) {
        this.abbrevCourses = abbrevCourses;
    }

    public HashMap<Integer, String> getNamePersons() {
        return namePersons;
    }

    public void setNamePersons(HashMap<Integer, String> namePersons) {
        this.namePersons = namePersons;
    }

    /*
    --------------------------------------
    ---FUNCIONES PARA EXTRACCION DE DATOS-
    ---DE RENWEB.-------------------------
    --------------------------------------
     */
//Este método retorna ret(tupla):
//Esto retorna a Homepage.create para devolver datos(yearId y SchoolYear) de la tabla SchoolYear, dado un determinado schoolCode(en este caso "IS-PAN"):
    public static ArrayList<Tupla<Integer, String>> getYears(String schoolCode) { // OBTIENE EL GETYEARS FALTA FILTRAR POR COLEGIO 
        ArrayList<Tupla<Integer, String>> ret = new ArrayList<>();
        String consulta = "select * from SchoolYear where SchoolCode= '" + schoolCode + "'";
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
//Aquí se accede a través del método menu de Homepage para poder obtener los datos de las escuelas según el districtCode
//El districtCode realmente aquí ya no hace falta (se incluye para ver que número tiene al lanzar el debugger),
//porque se van a obtener los datos en función de la BBDD a la que se haya accedido previamente (id de school y schoolname son diferentes).    
//Coge el id de School y el nombre para vincularlos posteriormente a la vista:

    public static ArrayList<Tupla<String, String>> getSchools(String districtCode) {

        ArrayList<Tupla<String, String>> ret = new ArrayList<>();
        String consulta = "SELECT SchoolName,SchoolCode FROM ConfigSchool ";
        try {
//Con el rs se pueden rastrear el SchoolName y el SchoolCode de la tabla CongigSchool:    
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
//En el siguiente método guarda los datos del colegio en cuestión, y se guardan en la tupla ret (por ejemplo:x= GCS1(schoolid),y= Elementary School(schoolName))            
            while (rs.next()) {
                String schoolid = rs.getString("SchoolCode");
                String schoolName = rs.getString("SchoolName");
                ret.add(new Tupla<>(schoolid, schoolName));
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
                if (classesData.containsKey(rs.getInt("classid"))) {
                    classes.get(g).add(classesData.get(g));
                }

                for (Integer c : classes.get(g)) {
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
//De aquí se extraen los datos del Template para utilizarlo en menu.jsp a través del HomePage.getTemplates():

    public static ArrayList<Template> getTemplates(String yearid) {
        ArrayList<Template> ret = new ArrayList();
        String consulta = "select * from ScheduleTemplate where yearid=" + yearid;
        try {
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
//Este método lo va a capturar el método ScheduleEduweb de la clase ScheduleControler para implementar cabeceras de filas 
//(por eso se sacan datos de columna=0, que es la columna con los datos de la cabecera de las filas):
//Se tiene en cuenta el template (id):

    public static ArrayList<Tupla<String, String>> getRowHeader(int id) {
        String consulta = "";

        ResultSet rs;
        ArrayList<Tupla<String, String>> ret = new ArrayList();
        consulta = "select TemplateTime, TemplateText from ScheduleTemplateTimeTable "
                + "where templateid=" + id + " and Col=0";

        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret.add(new Tupla(rs.getString("TemplateTime"),
                        rs.getString("TemplateText")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ret;
    }
//Este método lo va a capturar el ScheduleEduweb de la clase ScheduleControler para implementar cabeceras de columnas(por eso se sacan datos de fila=0,
//es decir, fila con los datos de la cabecera de las columnas):

    public static ArrayList<String> getColHeader(int id) { //modificar 
        String consulta = "";
        ResultSet rs;
        ArrayList<String> ret = new ArrayList();

        consulta = "select * from ScheduleTemplateTimeTable "
                + "where templateid=" + id + " and Row=0";
        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret.add(rs.getString("TemplateText"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ret;
    }

    public static String[][] getTemplateText(int id, int x, int y) {
        String consulta = "";

        ResultSet rs;
        String[][] ret = new String[x + 1][y + 1];
        consulta = "select TemplateTime, TemplateText, Col, Row  from ScheduleTemplateTimeTable "
                + "where templateid=" + id;

        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                int col = Integer.parseInt(rs.getString("Col"));
                int row = Integer.parseInt(rs.getString("Row"));
                if (rs.getString("TemplateTime").isEmpty()) {
                    ret[col][row] = rs.getString("TemplateText");
                } else if (rs.getString("TemplateText").isEmpty()) {
                    ret[col][row] = rs.getString("TemplateTime");
                } else if (rs.getString("TemplateText").isEmpty() && "TemplateTime".isEmpty()) {
                    ret[col][row] = "";
                } else {
                    ret[col][row] = rs.getString("TemplateTime") + " " + rs.getString("TemplateText");
                }

            }
            System.out.println("");
            System.out.println("");
            for (int i = 0; i < x + 1; i++) {
                for (int j = 0; j < y + 1; j++) {
                    if (ret[i][j] == null) {
                        ret[i][j] = "";
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ret;
    }

//Este método se usa para los HashMap de los Objetos de cada uno de los elementos de las restricciones de los cursos:
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
                String aux = rs.getString("data");
                if (rs.getString("data") != null && !rs.getString("data").equals("")) {
                    switch (type) {
                        case "Integer":
                            if (!aux.contains(",")) {
                                hashObject.put(rs.getInt("id"), Integer.parseInt(aux));
                                break;
                            }
                        case "Boolean":
                            if (aux.equals("1")) {
                                hashObject.put(rs.getInt("id"), true);
                            } else {
                                hashObject.put(rs.getInt("id"), false);
                            }
                            break;
                        case "String":
                            hashObject.put(rs.getInt("id"), aux);
                            break;

                        default:
                            break;
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //Aquí se obtienen las restricciones para los cursos. Si por ejemplo se ha cogido el template standard, el templateID=10:
    public ArrayList<Course> getRestriccionesCourses(int[] ids, String templateID, HashMap<String, ArrayList<Integer>> groupCourses, String schoolCode, Exceptions aviso) {
//ESTE ES EL ARRAY DONDE SE VAN A CARGAR TODAS LAS RESTRICCIONES DE LOS CURSOS(ret):        
        ArrayList<Course> ret = new ArrayList<>();
        String consulta = "";

//Estos dos Hash son para hacer match entre el ID de los cursos con template activado y el ID de los cursos con el Schedule activado.
//Los cursos con template activado se pueden ver en: RenWeb/ReportManager/Schedules/SchedulesCourseTemplate y print(Hash1)
//Los cursos con Schedule activado se pueden ver en RenWeb/Academic/Courses y se selecciona el curso, se aplica en User Defined y se comprueba
//si el apartado Schedule Schedule esta marcado en Yes (estos son los que sacará Hash2)
        HashMap<Integer, String> hashUno = new HashMap<>();
        HashMap<Integer, String> hashDos = new HashMap<>();

        try {
            ResultSet rs;

            consulta = "select udd.id\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'course'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'Templateid'\n"
                    + "                    and udd.data =" + templateID;
            rs = DBConnect.renweb.executeQuery(consulta);
//El hash1 coge los cursos que tiene asignado el template : RenWeb/ReportManager/Schedules/SchedulesCourseTemplate y print        
            while (rs.next()) {
                hashUno.put(rs.getInt(1), "");
            }

//Después se cambia el Query, para seleccionar los IDs que se asignaran al hash2:
            consulta = "select udd.data,udd.id\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'course'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'Schedule'\n"
                    + "                    and udd.data =1"
                    + "                    and udg.SchoolCode= '" + schoolCode + "'";
//Y estos datos se introducen en el hashDos. Se cogen los ids que tienen data=1 en la BBDD, es decir, los que están activados.
//Estos se guardan en hash2 (se puede comprobar en:RenWeb/Academic/Courses y se selecciona el curso, se aplica en User Defined y se comprueba
//si el apartado Schedule/Schedule esta marcado en Yes):
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                hashDos.put(rs.getInt("id"), rs.getString("data"));
            }
            
            if (hashUno.isEmpty()) {
                aviso.addAvisoCourseWithoutTemplate(templateID);
            }
            if (hashDos.isEmpty()) {
                aviso.addAvisoSchoolWithoutScheduleActive(schoolCode);
            }
            for (int i = 0; i < ids.length; i++) {

//Se necesita que ambos Hash tengan las ids para poder establecer las restricciones de los cursos:                
                if (hashUno.containsKey(ids[i]) && hashDos.containsKey(ids[i])) {
//Se crea un objeto de curso en funcion de los ids recogidos, para luego guardar los datos en el array hecho previamente de curso:                    
                    Course r = new Course(ids[i]);
                    ret.add(r);
//Aquí se asignan el nombre de los cursos en función de su id, pero no se carga en ret:                    
                    courseName.put(ids[i], this.nameCourses.get(ids[i]));
                }

            }
            if (ret.isEmpty()) {
                aviso.addAvisoWithoutMatches(templateID, schoolCode);
            }

//Se hace un HashMap para cada una de las restricciones de cursos, para que posteriormente pueda ser cargado cada Hash:          
            HashMap<Integer, Object> hashBlocksPerWeek = new HashMap<>();
            HashMap<Integer, Object> hashGR = new HashMap<>();
            HashMap<Integer, Object> hashMaxSections = new HashMap<>();
            HashMap<Integer, Object> hashMaxSectionsSchool = new HashMap<>();
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
            HashMap<Integer, Object> hashminSizePerSection = new HashMap<>();

//Se carga cada hash que se ha definido previamente, independientemente de los ids obtenidos en el match de hash1 y hash2.
//La carga se hace en funcion de 4 parametros: el hash especifico a cargar, si es cargado con datos del user defined de course, school(NOTA) u otros...,
//,la restriccion especifica que es al fin y al cabo el nombre de la columna en la BBDD de donde se saca, y el tipo de dato.
//NOTA:los datos que se sacan de los User Defined de school es porque se obtienen por defecto al no introducir datos en los User Defined de course.
            cargarHashMap(hashBlocksPerWeek, "course", "BlocksPerWeek", "Integer");
            cargarHashMap(hashGR, "course", "GR", "Boolean");
            cargarHashMap(hashMaxSections, "course", "MaxSections", "String");
            cargarHashMap(hashMaxSectionsSchool, "school", "MaxSections", "String");
            cargarHashMap(hashMinGapBlocks, "course", "MinGapBlocks", "Integer");
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
            cargarHashMap(hashminSizePerSection, "course", "minSizePerSection", "Integer");

//Gracias al siguiente for y a los siguientes if(cada uno por cada Hash especifico), se cargan 
//en el array ret(tipo Course) las variables de su constructor con los datos
//del hash (por la condicion que se establece dentro del if: a través de correspondencia de Ids). 
//Cuando el for ha acabado, en el ret se han cargado todos
//los valores de las distintas restricciones en funcion del curso(id).
            int minSizePerSectionSchool = 0;
            consulta = "select udd.data\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'school'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'minSizePerSection'\n"
                    + "                    and udg.schoolCode = '" + schoolCode + "'";

            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                try {
                    minSizePerSectionSchool = rs.getInt(1);
                } catch (Exception e) {
                    minSizePerSectionSchool = 0;
                }

            }
            int maxSectionSchool = 0;
            consulta = "select udd.data\n"
                    + "from uddata udd\n"
                    + " inner join udfield udf\n"
                    + "    on udd.fieldid = udf.fieldid\n"
                    + "inner join udgroup udg\n"
                    + "  on udg.groupid = udf.groupid\n"
                    + " and udg.grouptype = 'school'\n"
                    + " and udg.groupname = 'Schedule'\n"
                    + "and udf.fieldName = 'MaxSections'\n"
                    + "                    and udg.schoolCode = '" + schoolCode + "'";

            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                maxSectionSchool = rs.getInt(1);
            }

            for (int i = 0; i < ret.size(); i++) {

//Los set se consiguen gracias a setters,getters y variables definidos en la clase Course:
//Los hashBlocks aquí ya están cargados con la información de las restricciones. La carga se hace en función del id del curso: 
//Los hash tienen toda la información y cuando se hace un match con algún id de un curso, se carga la información que se contenga en ese curso en función de los hash.
//Por ejemplo: hashBlocksPerWeek--> tiene 7 identificadores de cursos(cursos que no tienen porque tener el schedule ni el template activados) y 
//cada identificador está asociado a un valor concreto. En este punto, hay dos cursos que coinciden en que tienen el template y el schedule:
//que son english1 y admath1 con ids 1245 y 1216, respectivamente. De los 7 identificadores que tiene hashBlocksPerWeek, 
//uno de ellos es 1245, con un valor asociado de 5, es decir, que para el curso de english1 se cargan en user defined 
//5 bloques por semana, que es la restricción que tiene el hash para el curso 1245:
                if (hashBlocksPerWeek.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setBlocksWeek((int) hashBlocksPerWeek.get(ret.get(i).getIdCourse()));
                }

                if (hashGR.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setGR((boolean) hashGR.get(ret.get(i).getIdCourse()));
                }

                if (hashMaxSections.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setMaxSections((String) hashMaxSections.get(ret.get(i).getIdCourse()));
                } else {
                    ret.get(i).setMaxSections(String.valueOf(maxSectionSchool));
                }

                if (hashMinGapDays.containsKey(ret.get(i).getIdCourse())) {
                    ret.get(i).setMinGapDays((int) hashMinGapDays.get(ret.get(i).getIdCourse()));
                }
                if (hashRank.containsKey(ret.get(i).getIdCourse())) {
                    if (!hashRank.containsKey(ret.get(i).getIdCourse())) {
                        ret.get(i).setRank(DEFAULT_RANK);
                    } else {
                        ret.get(i).setRank((int) hashRank.get(ret.get(i).getIdCourse()));
                    }
                }
//El hash tiene los profesores de los UD de los cursos que tienen activado el Schedule.                
//Aquí los ids de ret y hasTeachers que coincidan--> se saca el valor de hashTeachers: por ejemplo de 1245--> se saca el
//valor: 11038, 10630 . Se hace un split y se guarda cada uno de los 2 valores en el array [] s.
//Además, aparte de esos dos profesores se asigna un profesor por defecto (en academic/course está abajo en el apartado default instructor,
//en el que aparece su apellido, n.--> el id de este profesor se añade a través del auxTeacherDefault:)
//Se deben añadir también los profesores de las secciones del curso.
                String[] s = new String[100];
                if (hashTeachers.containsKey(ret.get(i).getIdCourse())) {
                    s = ((String) hashTeachers.get(ret.get(i).getIdCourse())).split(",");
                }

                int auxTeacherDefault = -1;
                int auxRoomDefault = -1;
                ArrayList<Integer> ar = new ArrayList<>();
                consulta = "select DefaultStaffID, RequiredRoomId from courses where courseid="
                        + ret.get(i).getIdCourse();
                rs = DBConnect.renweb.executeQuery(consulta);
//Aquí se ha añadido el id a auxTeacherDefault (10584) a través de la consulta de arriba:                
                while (rs.next()) {
                    if (rs.getInt(1) != 0) {
                        auxTeacherDefault = rs.getInt(1);
                    }
                    if (rs.getInt(2) != 0) {
                        auxRoomDefault = rs.getInt(2);
                    }
                }

                ArrayList<Integer> sectionTeachers = new ArrayList<>();
                consulta = "SELECT distinct StaffID,RequiredRoom FROM Classes left join roster on \n"
                        + "                        (Classes.ClassID = roster.ClassID)\n"
                        + "                        where CourseId = " + ret.get(i).getIdCourse() + " and templateId = " + templateID;
                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    if (rs.getInt(1) != 0) {
                        sectionTeachers.add(rs.getInt(1));
                        ar.add((rs.getInt(1)));
                        if (!teachers.contains(rs.getInt(1))) {
                            teachers.add(rs.getInt(1));
                        }
                    }
                }

//Se añade a ar y a teachers el id del profesor auxiliar:                
//Se crea un array al que se le añade el id del profesor por defecto si es diferente de -1:
                teachersCOURSE.put(ret.get(i).getIdCourse(), new ArrayList<>());
                if (auxTeacherDefault != -1) {
                    ar.add(auxTeacherDefault);
//Aquí se indica que si la variable teachers no contiene el valor de auxTeacherDefault, que se le añada: 
                    if (!teachers.contains(auxTeacherDefault)) {
                        teachers.add(auxTeacherDefault);
                        teachersCOURSE.get(ret.get(i).getIdCourse()).add(auxTeacherDefault);
                    }
                }
//Y aquí se termina de añadir a teachers y a ar los dos valores de los profesores que se han seleccionado:
                for (String s2 : s) {
                    if (s2 != null) {
                        try {
                            int idt = Integer.parseInt(s2);
                            ar.add(idt);
                            if (!teachers.contains(idt)) {
                                teachers.add(idt);
                                teachersCOURSE.get(ret.get(i).getIdCourse()).add(idt);
                            }
                        } catch (Exception e) {
                            aviso.addAvisoCadena(this.nameCourse(ret.get(i).getIdCourse()), "Teachers", s2);
                        }

                    }
                }
//Con lo que al llegar a este punto: teachers y ar tienen los dos ids de profesores añadidos y el id por defecto:
//Aquí ya se devolvería a ret las restricciones de profesores añadidas a ar, siempre y cuando el hash de profesores indique
//el id de curso que coincida con alguno en ret:
                ret.get(i).setTrestricctions(ar);

//Aquí hay 3 opciones:
//1ª: Se meten a ret las rooms que están configuradas en el course (si hay mas de 1, están separadas por comas, entonces se quitan
//con split y se añaden a ret las rooms una por una (si solo hay una también funciona)):
//2ª: Si el campo de rooms está vacío en el curso en cuestión (en RenWeb): se ponen por defecto las rooms que están
//en School, si encuentra el template que tienen asignado.
//3ª En el caso de que el template del que se parta no tenga ids de rooms por defecto, se añade el identificador -1
// que no corresponde a ninguna room pero que posibilita que no caiga el programa.
                rs = DBConnect.renweb.executeQuery(consulta);
                String rooms = "";
                int contaRoomsSec = 0;
                int contaRoomsCourse = 0;
                countCourse2.put(ret.get(i).getIdCourse(), new ArrayList<>());
                countSchool.put(ret.get(i).getIdCourse(), new ArrayList<>());
                while (rs.next()) {
                    if (rs.getInt(2) != 0) {
                        contaRoomsSec++;
                        //       ret.get(i).addRoom(rs.getInt(2));
                    }
                }
                if (auxRoomDefault != -1) {
                    ret.get(i).addRoom(auxRoomDefault);
                    contaRoomsCourse++;
                    countCourse2.get(ret.get(i).getIdCourse()).add(auxRoomDefault);

                }
                if (hashRooms.containsKey(ret.get(i).getIdCourse())) {

                    rooms = (String) hashRooms.get(ret.get(i).getIdCourse());
                    for (String room : rooms.split(",")) {
                        try {
                            contaRoomsCourse++;
                            ret.get(i).addRoom(Integer.parseInt(room));
                            countCourse2.get(ret.get(i).getIdCourse()).add(Integer.parseInt(room));

                        } catch (Exception e) {
                            System.err.println("No se puede leer bien el campo rooms en el curso"
                                    + ret.get(i).getIdCourse());
                        }
                    }

                } else if (groupCourses.containsKey(templateID)) {
                    ret.get(i).addArrayRooms(groupCourses.get(templateID));

                }
                countCourse.put(ret.get(i).getIdCourse(), contaRoomsCourse);
                if (groupCourses.containsKey(templateID)) {
                    for (Integer room : groupCourses.get(templateID)) {
                        countSchool.get(ret.get(i).getIdCourse()).add(room);
                    }
                }

//Se añaden los bloques excluidos del hash a la variable excludes
//para luego más abajo añadirle a la misma variable excludes los bloques excluidos por defecto de school,
//con lo que se devolverá a ret las restricciones de bloques del curso concreto y de los user defined de school:
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
                        + "                    and udf.fieldName = 'ExcludeBlocks'"
                        + "                    and udg.schoolCode = '" + schoolCode + "'";

                rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    if (!excludes.contains(rs.getString(1))) {
                        excludes += rs.getString(1);
                    }
                }
                try {
                    ret.get(i).setExcludeBlocks(excludes);
                } catch (Exception e) {
                    aviso.addAvisoCadena(this.nameCourse(ret.get(i).getIdCourse()), "Exclude Blocks", excludes);
                }

                try {
                    if (hashPreferredBlock.containsKey(ret.get(i).getIdCourse())) {
                        ret.get(i).setPreferedBlocks((String) hashPreferredBlock.get(ret.get(i).getIdCourse()));
                    }
                } catch (Exception e) {
                    aviso.addAvisoCadena(this.nameCourse(ret.get(i).getIdCourse()), "Prefered Blocks", (String) hashPreferredBlock.get(ret.get(i).getIdCourse()));
                }

                boolean balanceTeachers = false;

                if (hashbalanceTeachers.containsKey(ret.get(i).getIdCourse())) {
                    balanceTeachers = (boolean) hashbalanceTeachers.get(ret.get(i).getIdCourse());
                }
                ret.get(i).setBalanceTeachers(balanceTeachers);

                String mandatoryBlock = "";

                if (hashMandatoryBlocksRange.containsKey(ret.get(i).getIdCourse())) {
                    mandatoryBlock = (String) hashMandatoryBlocksRange.get(ret.get(i).getIdCourse());
                }

                ret.get(i).setMandatoryBlockRange(mandatoryBlock);

                int numBxD = Algoritmo.TAMY;

                if (hashmaxBxD.containsKey(ret.get(i).getIdCourse())) {
                    numBxD = (int) hashmaxBxD.get(ret.get(i).getIdCourse());
                }
                ret.get(i).setMaxBlocksPerDay(numBxD);

                consulta = "select MaxSize from courses where courseid="
                        + ret.get(i).getIdCourse();
                rs = DBConnect.renweb.executeQuery(consulta);
                int numeroMaxChildPerSection = 0;
                while (rs.next()) {
//----CAMBIO----- Se guarda ahora la restricción de maximo tamaño de secciones en el array ret y en una variable.
//En la variable se guarda también, porque es necesario para que más adelante calcular el número minimo de estudiantes por seccion
//ya que del hashminSizePerSection se obtiene el porcentaje y se necesita el numero minimo para el algoritmo.
                    try {
                        ret.get(i).setMaxChildPerSection(rs.getInt(1));
                        numeroMaxChildPerSection = rs.getInt(1);
                    } catch (Exception e) {
                        ret.get(i).setMaxChildPerSection(0);
                        numeroMaxChildPerSection = 0;
                    }

                }
                if (numeroMaxChildPerSection == 0) {
                    aviso.addAvisoMaxSizePerSectionCourse(this.nameCourses.get(ret.get(i).getIdCourse()));
                    consulta = "select udd.data\n"
                            + "                from uddata udd\n"
                            + "                inner join udfield udf\n"
                            + "                    on udd.fieldid = udf.fieldid\n"
                            + "                inner join udgroup udg\n"
                            + "                    on udg.groupid = udf.groupid\n"
                            + "                    and udg.grouptype = 'school'\n"
                            + "                    and udg.groupname = 'Schedule'\n"
                            + "                    and udf.fieldName = 'MaxSizePerSection'"
                            + "                    and udg.schoolCode = '" + schoolCode + "'";
                    rs = DBConnect.renweb.executeQuery(consulta);
                    while (rs.next()) {
                        try {
                            ret.get(i).setMaxChildPerSection(rs.getInt(1));
                            numeroMaxChildPerSection = rs.getInt(1);
                        } catch (Exception e) {
                            ret.get(i).setMaxChildPerSection(25);
                            numeroMaxChildPerSection = 25;
                            aviso.addAvisoMaxSizePerSectionSchool(schoolCode);
                        }
                    }
                }
                if (numeroMaxChildPerSection == 0) {
                    ret.get(i).setMaxChildPerSection(25);
                    numeroMaxChildPerSection = 25;
                    aviso.addAvisoMaxSizePerSectionSchool(schoolCode);
                }
//----CAMBIO----- Ahora se calcula el número mínimo de estudiantes aquí (antes simplemente se aplicaba 70% en el algoritmo y ya no):                   
// No hace falta cargar un hash de minimo de niños por seccion en escuela porque es único(y esto está dentro de un for):
//Si se ejecuta el if significa que coge la restricción por curso, y si se ejecuta el else significa que coge la restricción por defecto de school:
//El mínimo que se obtiene del hash es un porcentaje en función del máximo, por lo que hay que pasar ese porcentaje a número de alumnos:
//En el ejemplo de 1216 (addmath1) el hashminSizePerSectin pasa el valor 70, que es realmente 70% del valor máximo que se obtiene de
//ret(maxChildPerSection): en este caso es 25, por lo que obtiene un mínimo de 17,5, y al truncarse el dato, se queda en 17.
//En el caso de 1245 (eng1) no se obtiene ningún porcentaje de esta restricción en el Schedule de Cursos, por lo que se obtiene
//el porcentaje por defecto de School, que es 30% siempre. Este 30% se aplica al máximo de estudiantes por seccion de eng1, que es 20,
//y el método obtiene por lo tanto un mínimo de 6 alumnos:
                ret.get(i).setMinChildPerSection(0);
                if (hashminSizePerSection.containsKey(ret.get(i).getIdCourse())) {
                    int numeroMinSizePerSection = ((int) (hashminSizePerSection.get(ret.get(i).getIdCourse())) * (numeroMaxChildPerSection)) / 100;
                    ret.get(i).setMinChildPerSection(numeroMinSizePerSection);
                }
                if (minSizePerSectionSchool > 0 && ret.get(i).getMinChildPerSection() == 0) {
                    aviso.addAvisoMinSizePerSectionCourse(this.nameCourses.get(ret.get(i).getIdCourse()));
                    int numeroMinSizePerSection = (minSizePerSectionSchool * numeroMaxChildPerSection) / 100;
                    ret.get(i).setMinChildPerSection(numeroMinSizePerSection);
                } else if (!hashminSizePerSection.containsKey(ret.get(i).getIdCourse())) {
                    aviso.addAvisoMinSizePerSectionCourse(this.nameCourses.get(ret.get(i).getIdCourse()));
                    aviso.addAvisoMinSizePerSectionSchool(schoolCode);
                    int numeroMinSizePerSection = (50 * numeroMaxChildPerSection) / 100;
                    ret.get(i).setMinChildPerSection(numeroMinSizePerSection);
                }
//Se actualizan los PatternGroups en funcion del curso y el template asignado:
                updatePatternGroups(ret.get(i), templateID);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
//Se devuelve el array ret después de aplicar todas las restricciones en función del curso a la clase Restrictions:        
        return ret;
    }

    private void updatePatternGroups(Course c, String templateId) {
        try {
            ResultSet rs;
            String consulta = "select patternGroup from courses where CourseID = " + c.getIdCourse();
            rs = DBConnect.renweb.executeQuery(consulta);
            if (rs.next() && !rs.getString(1).equals("")) {
                consulta = "SELECT PatternNumber FROM SchedulePatterns where "
                        + "PatternGroup = '" + rs.getString(1) + "' and templateID =" + templateId;
                rs = DBConnect.renweb.executeQuery(consulta);
                Stack<Integer> auxPatternNumbers = new Stack<>();
                while (rs.next()) {
                    auxPatternNumbers.push(rs.getInt(1));
                }
                while (!auxPatternNumbers.isEmpty()) {
                    consulta = "SELECT * from SchedulePatternsTimeTable "
                            + " where patternnumber =" + auxPatternNumbers.peek() + " and templateID =" + templateId;
                    rs = DBConnect.renweb.executeQuery(consulta);
                    ArrayList<Tupla> auxTuplas = new ArrayList<>();
                    while (rs.next()) {
                        auxTuplas.add(new Tupla(rs.getInt("col") - 1, rs.getInt("row") - 1));
                    }
                    auxPatternNumbers.pop();
                    c.addOption(auxTuplas);
                }
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Failed: dataManage.Consultas.updatePatternGroups()");
        }
        System.out.println("dataManage.Consultas.updatePatternGroups()");
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

    protected HashMap<String, Course> getLinkedCourses() {
//Como clave será el curso padre y como valor curso hijo:
//El getLinkedCourses sirve para aplicar las restricciones de un curso en otro
//(se copian los roster de cada seccion, o de las indicadas). 
//NOTA: el nº de secciones debe ser igual en ambos cursos:

        HashMap<String, Course> resultHash = new HashMap<>();
        String linkedIds = "";
//El grouptype 'school' corresponde al campo de RenWeb/System/Configuration/SchoolConfiguration/LinkedCourses:        
        String consulta = "select udd.data\n"
                + "                from uddata udd\n"
                + "                inner join udfield udf\n"
                + "                    on udd.fieldid = udf.fieldid\n"
                + "                inner join udgroup udg\n"
                + "                    on udg.groupid = udf.groupid\n"
                + "                    and udg.grouptype = 'school'\n"
                + "                    and udg.groupname = 'Schedule'\n"
                + "                    and udf.fieldName = 'LinkedCourses'";

        ResultSet rs;
        try {
            rs = DBConnect.renweb.executeQuery(consulta);

            while (rs.next()) {
                linkedIds = rs.getString(1);
            }
//Con las siguientes instrucciones se quitan todos los caracteres (;,][) para poder almacenar limpios los datos en resultHash:
//Las condiciones obtenidas de la base de datos se guarda en resultHash(en este caso como el grouptype 'school' está vacío, no se pone nada)

            if (!linkedIds.equals("")) {
                String[] all_Links = linkedIds.split(";");
                for (int i = 0; i < all_Links.length; i++) {
                    String aux = all_Links[i];
                    aux = aux.replace("[", "");
                    aux = aux.replace("]", "");
                    String[] idsCourses = aux.split(",");
//Con los siguientes if/else se calcula el curso hijo.

//En el caso de que la longitud sea 2, significa que no se indican las secciones 
// (se cogerían todas en este caso, por lo que es importante que el número de secciones sean las mismas).
//Además se indicaría sólo el id del curso puesto que no es necesario añadir información de las secciones.
//En el caso de que se indiquen las secciones, significa que la longitud es mayor que 2 (curso padre, secciones, curso hijo).
//Además, se cogería un objeto de curso entero para añadir el id del curso y posteriormente las secciones linkeadas 
//y el maxSections a través de la longitud de las secciones que hay.
//Sea como sea, se guarda en resultHash el curso padre primero (solo id, como key) y luego el curso hijo(objeto/id de curso como value):
                    if (idsCourses.length == 2) {
                        resultHash.put(idsCourses[0], new Course(Integer.parseInt(idsCourses[1])));

                    } else { // hay sectiones linkeadas
                        Course auxC = new Course(Integer.parseInt(idsCourses[2]));
                        String s = idsCourses[1];
                        s = s.replace("(", "");
                        s = s.replace(")", "");
                        String[] secLinks = s.split("-");
                        auxC.setSectionsLinkeadas(secLinks);
                        auxC.setMaxSections("" + secLinks.length);
                        resultHash.put(idsCourses[0], new Course(auxC));
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultHash;
    }

//Con este método se establece el total de bloques de inicio en un curso concreto en función del template:    
    private ArrayList<ArrayList<Boolean>> totalBlocksStart(String tempid) {

        String excludes = "";
//Se establece un objeto course auxiliar para poder establecer bloques disponibles para el template elegido:
        Course caux = new Course(1);
//El array de bloques es boolean: true es la activación de bloques que se van a usar:
//Ejemplo: hay 5 registros true por cada posición y un total de 10 posiciones: 5 columnas de días y 10 filas para las horas, total 50 bloques:
        ArrayList<ArrayList<Boolean>> auxTotalStart = new ArrayList<>();
//Se quitan los bloques excluidos (los indicados en Configuration de School):        
        String consulta = "select udd.data\n"
                + "                from uddata udd\n"
                + "                inner join udfield udf\n"
                + "                    on udd.fieldid = udf.fieldid\n"
                + "                inner join udgroup udg\n"
                + "                    on udg.groupid = udf.groupid\n"
                + "                    and udg.grouptype = 'school'\n"
                + "                    and udg.groupname = 'Schedule'\n"
                + "                    and udf.fieldName = 'ExcludeBlocks'";

        ResultSet rs;
        try {

            rs = DBConnect.renweb.executeQuery(consulta);

            while (rs.next()) {
                if (!excludes.contains(rs.getString(1))) {
                    excludes += rs.getString(1);
                }
            }
//Se establece el curso auxiliar con ExcludeCols, ExcludeRows y ExcludeBlocks en función la consulta de ExcludeBlocks previa:            
            caux.setExcludeBlocks(excludes);
//Palabras excluidas (Exclude Words de Configuration/School),
//Por ejemplo: Lunch(esta palabra se recoge para luego aplicarla al template, y que en los bloques bloqueados ponga: Lunch):                
            consulta = "select udd.data\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'school'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'ExcludeWords'";

            rs = DBConnect.renweb.executeQuery(consulta);
            List<String> arrExcludeWords = new ArrayList<>();
            while (rs.next()) {

                String[] auxS = rs.getString("data").split(",");
                arrExcludeWords = Arrays.asList(auxS);
            }
//Aquí es donde se limitan los huecos disponibles al inicio (para un curso, en función de excludeCols, excludeRows y excludeBlocks):
//Se obtiene un array de size=filas, en cada fila: size=columnas, con los bloques disponibles a true:  
            auxTotalStart = caux.opcionesStart();
            consulta = "SELECT * FROM ScheduleTemplateTimeTable where templateid =" + tempid;

            rs = DBConnect.renweb.executeQuery(consulta);
//Se añade el esquema del template(monday,tuesday... a tmpText).
//Se resta 1 porque aquí las filas y columnas empiezan en 0 y no en 1:
            while (rs.next()) {
                int row = rs.getInt("row") - 1;
                int col = rs.getInt("col") - 1;
                String tmpText = rs.getString("TemplateText");
//Aquí se comprueba si el template en concreto que se está usando contiene alguna de las palabras reservadas (arrExcludeWords.contains(tmpText)).
//Se irán comprobando todas las palabras incluidas en el template, y se verificará la que no sea de cabecera
//(las condiciones de row y col se incluyen para que no se refiera a las cabecera, ya que valdrían -1 con las condiciones establecidas previamente),
//y que el hueco tampoco esté vacío.
//Si se encuentra una palabra reservada que no sea cabecera, se establece ese hueco a false:
                if (!tmpText.equals("") && arrExcludeWords.contains(tmpText)
                        && row >= 0 && col >= 0) {
                    auxTotalStart.get(row).set(col, false);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
//Se devuelve un boolean array de las filas x columnas que tenga el template, y quitando los ExcludeBlocks y los bloques que contengan ExcludeWords (todos estos a false, el resto a true):        
        return auxTotalStart;
    }
//Se establecen los datos que se adjudicarán a los profesores en caso de que sus restricciones no estén configuradas
//Es decir, los datos por defecto(con el máximo de secciones, máximo de cursos, y máximo de BxD que están configurados en el apartado de SetupSchoolSchedule):

    public Teacher teacherDefault(int x, int y) {
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

            consulta = "select udd.data\n"
                    + "        from uddata udd\n"
                    + "        inner join udfield udf\n"
                    + "            on udd.fieldid = udf.fieldid\n"
                    + "        inner join udgroup udg\n"
                    + "            on udg.groupid = udf.groupid\n"
                    + "            and udg.grouptype = 'school'\n"
                    + "            and udg.groupname = 'Schedule'\n"
                    + "            and udf.fieldName = 'ExcludeBlocks'\n";
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret.setExcludeBlocks(rs.getString(1), x, y);
            }
        } catch (Exception ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public HashMap<Integer, Teacher> teachersList(String tempid, int[] tempinfo, int x, int y) {
        HashMap<Integer, Teacher> ret = new HashMap<>();
        try {
//Se obtiene una lista de profesores en función de los colegios asignados para dichos profesores(HS,MS...) 
// que coincidan con los colegios del template y se añade a un array:
//El array teachers ya tenía los ids de los profesores de UD y default de todos los cursos (se carga en getRestriccionesCourse). Aquí se añaden todos los demás
//de las escuelas asignadas al template, para poder hacer en la vista una lista de los profesores que tienen disponibilidad y los que no:
            String consulta = "SELECT staffID FROM Person_Staff ps inner join Person p on (ps.StaffID = p.PersonID)\n"
                    + "where ps.active=1 and ps.faculty=1 and " + getWhereTemplate(tempinfo);
            ResultSet rs;
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                int staffId = rs.getInt(1);

                if (!this.teachers.contains(staffId)) {
                    teachers.add(staffId);
                }
            }
            for (Integer teacher : teachers) {
                ret.put(teacher, new Teacher());
                ret.get(teacher).setIdTeacher(teacher);
            }
            consulta = "select udd.id,udd.data\n"
                    + "from uddata udd\n"
                    + "inner join udfield udf\n"
                    + "on udd.fieldid = udf.fieldid\n"
                    + "inner join udgroup udg\n"
                    + "on udg.groupid = udf.groupid\n"
                    + "and udg.grouptype = 'Staff'\n"
                    + "and udg.groupname = 'Schedule'\n"
                    + "and udf.fieldName = 'MaxSections'\n";

            rs = DBConnect.renweb.executeQuery(consulta);

            while (rs.next()) {
                int staffId = rs.getInt(1);
                if (this.teachers.contains(staffId)) {
                    ret.get(staffId).setMaxSections(rs.getInt(2));

                }
            }
            for (Integer teacher : teachers) {
                if (ret.get(teacher).getMaxSections() == 0) {
                    ret.get(teacher).setMaxSections(tdefault.getMaxSections());
                }
            }

            consulta = "               select udd.id,udd.data\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'Staff'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'Preps'\n";
            rs = DBConnect.renweb.executeQuery(consulta);

            while (rs.next()) {
                int staffId = rs.getInt(1);
                if (this.teachers.contains(staffId)) {
                    ret.get(staffId).setPreps(rs.getInt(2));
                }
            }
            for (Integer teacher : teachers) {
                if (ret.get(teacher).getPreps() == 0) {
                    ret.get(teacher).setPreps(tdefault.getPreps());
                }
            }
            consulta = "               select udd.id,udd.data\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'Staff'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'MaxBxD'\n";
            rs = DBConnect.renweb.executeQuery(consulta);

            while (rs.next()) {
                int staffId = rs.getInt(1);
                if (this.teachers.contains(staffId)) {
                    ret.get(staffId).setMaxBxD(rs.getInt(2));
                }
            }
            for (Integer teacher : teachers) {
                if (ret.get(teacher).getMaxBxD() == 0) {
                    ret.get(teacher).setMaxBxD(tdefault.getMaxBxD());
                }
            }
            consulta = "select * from ScheduleTemplateStaff where templateid=" + tempid;
            rs = DBConnect.renweb.executeQuery(consulta);

//Con esta consulta obtiene de la BBDD si, primero, se aplica el esquema de bloques(con el boolean).
//Si es así se aplica en ret las restricciones de exclusion de bloques, por dia y por periodo para el profesor en concreto.
//Se establece así los bloques que no se pueden adjuntar a un profesor por defecto.
//(Exclusion de bloques:excluir un conjunto de bloques para todos los cursos. 
//Seleccionando como primer valor la hora y como segundo el día definido en el Scheduling):                 
            while (rs.next()) {
                int staffId = rs.getInt("staffid");
                if (rs.getBoolean("scheduleblock") && this.teachers.contains(staffId)) {
                    Tupla t = new Tupla(rs.getInt("day") - 1, rs.getInt("period") - 1);
                    ret.get(staffId).addExcludeBlock(t);
                }
            }
            consulta = "select udd.id, udd.data\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'Staff'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'ExcludeBlocks'";
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                int staffId = rs.getInt(1);

                if (this.teachers.contains(staffId)) {
                    String ExcludeBlocks = rs.getString(2);
                    ret.get(staffId).setExcludeBlocks(ExcludeBlocks, x, y);
                }
            }
            for (Integer teacher : teachers) {
                if (ret.get(teacher).getExcludeBlocks().isEmpty()) {
                    ret.get(teacher).setExcludeBlocks(tdefault.getExcludeBlocksString(), x, y);
                }
            }
            for (Integer teacher : teachers) {
                ret.get(teacher).setName(this.namePersons.get(teacher));
            }

        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
//Aquí se cargan las restricciones de cada profesor cuando se entra en teachersList:

    //ESTE SIGUIENTE MÉTODO YA NO SE USA:
    public Teacher restriccionesTeacher(String tempid, int id) {
        Teacher ret = new Teacher();
        String consulta = "";
//El parametro id de restriccionesTeacher es el id de cada profesor que se recorre en el for de teachers de teachersList(arriba):
//
        ResultSet rs;
//Se coge el ide de cada profesor y se consulta en la BBDD, la información de cada una de las restricciones:   
        if (id != 0) {
            try {
                consulta = "                select udd.data\n"
                        + "                from uddata udd\n"
                        + "                inner join udfield udf\n"
                        + "                on udd.fieldid = udf.fieldid\n"
                        + "                inner join udgroup udg\n"
                        + "                on udg.groupid = udf.groupid\n"
                        + "                    and udg.grouptype = 'Staff'\n"
                        + "                    and udg.groupname = 'Schedule'\n"
                        + "                    and udf.fieldName = 'MaxSections'\n"
                        + "                where udd.id =" + id;
                rs = DBConnect.renweb.executeQuery(consulta);
//Se obtiene el máximo de secciones correspondiente a la consulta que se ha hecho de cada id:
//Max Sections: número máximo de secciones que puede tener el profeso a cargo.
                while (rs.next()) {
                    ret.setMaxSections(rs.getInt(1));
                }
//Si no encuentra el dato en la BBDD, se asigna el valor por defecto del MaxSections
//(este valor ha sido asignado Restrictions, al cargar el constructor de consultas
//(se han volcado los datos del método teacherDefault() en tdefault)):                
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
//Preps: número máximo de asignaturas por profesor:
//Igual que con MaxSections:
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
//MaxBxD. número máximo de bloques por día del profesor.              
//Igual que MaxSections:                
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

//Con esta consulta obtiene de la BBDD si, primero, se aplica el esquema de bloques(con el boolean).
//Si es así se aplica en ret las restricciones de exclusion de bloques, por dia y por periodo para el profesor en concreto.
//Se establece así los bloques que no se pueden adjuntar a un profesor por defecto.
//(Exclusion de bloques:excluir un conjunto de bloques para todos los cursos. 
//Seleccionando como primer valor la hora y como segundo el día definido en el Scheduling):                 
                while (rs.next()) {
                    if (rs.getBoolean("scheduleblock")) {
                        Tupla t = new Tupla(rs.getInt("day") - 1, rs.getInt("period") - 1);
                        ret.addExcludeBlock(t);
                    }
                }
                ret.setIdTeacher(id);

            } catch (Exception ex) {
                Logger.getLogger(Consultas.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
//Se guarda el nombre de los profesores en ret, en función de su id :      
        ret.setName(this.namePersons.get(id));
        return ret;
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
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
//Con este método se recoge un array de 4 posiciones para las diferentes escuelas, 1 es que true y 0 false.
//Para el ejemplo de RWI-SPAIN, serían todos a 0, menos HS que sería 1:    

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
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public HashMap<String, ArrayList<Integer>> roomsGroup(String tmpId) {
        HashMap<String, ArrayList<Integer>> roomsTemplate = new HashMap();
        ArrayList<Integer> rooms = new ArrayList();
        String groupOfRooms = "";

//De esta consulta se sacan los rooms asignados en School, que tienen asignado para determinados template
        try {
            String consulta = "select udd.data\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'school'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'Rooms'";
//Con lo siguiente se quitan los ; parentesis y otros caracteres, para almacenar todo en el hashmap roomsTemplate(
//con key: id de los template, y value: los ids que tienen asignado a determinado id de template )
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                String result = rs.getString(1);

                String[] resultSplit = result.split(";");
                int i = 0;
                while (i < resultSplit.length) {
                    groupOfRooms = resultSplit[i].substring(1, resultSplit[i].length() - 1);
                    rooms = new ArrayList<>();
                    String[] s;
                    s = groupOfRooms.split(",");
                    if (s.length == 2) {
                        String auxTemplate = s[0];

                        String auxRooms = s[1];
                        auxRooms = auxRooms.substring(1, auxRooms.length() - 1);
                        s = auxRooms.split("-");

                        for (String s2 : s) {
                            try {
                                rooms.add(Integer.parseInt(s2));

                            } catch (Exception ex) {
                                Logger.getLogger(Consultas.class
                                        .getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        roomsTemplate.put(auxTemplate, (ArrayList<Integer>) rooms.clone());
                    }
                    i++;

                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return roomsTemplate;
    }

    /**
     *
     * @param c
     * @param stCourse
     * @param yearid
     * @param tempid
     * @param schoolCode
     * @param aviso
     * @return
     * @returng
     */
    //@ExceptionHandler
    public ArrayList<Student> restriccionesStudent(ArrayList<Integer> c, HashMap<Integer, ArrayList<Integer>> stCourse, String yearid, int tempid, String schoolCode, Exceptions aviso) throws Exception {
        String consulta = "";
        HashMap<Integer, String> hashUno = new HashMap<>();
        ResultSet rs;
        ArrayList<Integer> CoursesScheduleActive = new ArrayList<>();
        try {
//Consulta: id de los cursos que tienen asignado el template:
            consulta = "select udd.id\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'course'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'Templateid'\n"
                    + "                    and udd.data =" + tempid;
            rs = DBConnect.renweb.executeQuery(consulta);
//Al hash1 se le asignan los id de los cursos que tienen asignado el template:             
            while (rs.next()) {
                hashUno.put(rs.getInt("id"), "");
            }

            consulta = "select udd.id\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'course'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'Schedule'\n"
                    + "                    and udd.data = '1'"
                    + "                    and udg.SchoolCode= '" + schoolCode + "'";
            rs = DBConnect.renweb.executeQuery(consulta);

//CAMBIO:Ahora el hash1 tiene los ids con el template asignado, y estos se comparan con los ids que tienen el schedule active. Los que coincidan
//se añaden a CoursesScheduleActive:
            while (rs.next()) {
                int id = rs.getInt(1);
                if (hashUno.containsKey(id)) {
                    CoursesScheduleActive.add(id);

                }
            }
        } catch (Exception e) {
            error = error.concat("The template " + tempid + " has wrong configuration.");
            System.out.println("dataManage.Consultas.restriccionesStudent()");
        }

        ArrayList<Student> ret = new ArrayList<>();
        consulta = " select sr.courseid, sr.studentid, p.gender, ps.gradelevel\n"
                + "    from studentrequests sr\n"
                + "    inner join person p\n"
                + "        on p.PersonID = sr.StudentID\n"
                + "    inner join courses c\n"
                + "        on c.CourseID = sr.courseid\n"
                + "    inner join students s\n"
                + "        on s.studentid = sr.StudentID\n"
                + "        and p.personid = s.studentid\n"
                + "    inner join person_student ps\n"
                + "        on ps.StudentID = s.StudentID\n"
                + "        and ps.StudentID = p.PersonID\n"
                + "        and ps.StudentID = sr.StudentID"
                + "        where sr.yearid =" + yearid
                + "        and ps.SchoolCode = s.SchoolCode\n"
                + "    and s.SchoolCode = '" + schoolCode + "' ";

        try {
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {

//Se va cogiendo fila a fila de la BBDD(porque hay una PK compuesta de idstudent e idcourse):
//esto quiere decir que para coger todos los estudiantes de un curso se comprueba varias veces el mismo curso(1 estudiante por fila).
//En CoursesScheduleActive están almacenados los ids de los cursos que tienen asignados template y activeSchedule.
//Se va comprobando de la última consulta si el courseid coincide con el id del CourseScheduleActive.
//Si coincide, se saca el studentid que hay en esa fila en concreto.
                int courseid = rs.getInt("courseid");
                if (CoursesScheduleActive.contains(courseid)) {
                    int studentid = rs.getInt("studentid");
                    Student st = new Student(studentid);
                    try {
                        if (rs.getString("gender") != null) {
                            st.setGenero(rs.getString("gender"));
                        } else {
                            st.setGenero("Male");
                        }
                    } catch (NullPointerException e) {
                        st.setGenero("Male");
                    }
                    try {
                        if (rs.getString("gradelevel") != null) {
                            st.setGradeLevel(rs.getString("gradelevel"));
                        } else {
                            st.setGradeLevel("There's no gradelevel");
                        }
                    } catch (NullPointerException e) {
                        st.setGradeLevel("There's no gradelevel");
                    }
//estos id de cursos se van a añadir a c(c se va a devolver en la clase Restrictions y se va a aplicar como parámetro también en 
//las restricciones de cursos:)                    
                    if (!c.contains(courseid)) {
                        c.add(courseid);
                    }
//En ret se van a añadir a todos los ids, gender y gradelevel de los estudiantes que se han encontrado en los cursos
//(los que coincidan con el id de la bbdd). ret será retornado directamente a la clase Restrictions y se usará para añadir
//todos los ids de los estudiantes a :
                    if (!ret.contains(st)) {
                        ret.add(st);
                    }
//Se carga en el hash de stCourse todos los ids de los estudiantes que corresponden por cada id de curso.
//Esto servirá para luego usar este hash en el método chargeHashStudents de la clase Restrictions:
                    if (!stCourse.containsKey(courseid)) {
                        stCourse.put(courseid, new ArrayList());
                    }
                    stCourse.get(courseid).add(studentid);
                }

            }
//Si difiere el size de stCourse del size de CoursesScheduleActive significa que hay curso/s que no tienen estudiantes asignados
//porque si pasa esto significa que stCourse.size es menor. La razón por la que es menor es que la consulta previa solo captura estudiantes de cursos que tienen estudiantes.
//Si hay algún curso que no tiene estudiantes, la consulta ni siquiera saca datos. En el caso de que difieran, se lanza una excepcion (en Exceptions e), para que no continúe
//el programa y avise al usuario de que en un curso no hay asignados estudiantes:
//Con el if se caputura el id del curso que no tiene estudiantes:
//El CoursesWithoutStudents es un HashMap que guarda  id de cursos y nombres de cursos que no tienen estudiantes asignados (cada uno en una posición).
//Esto es necesario enviarlo a la vista y mostrar estos datos cuando se vaya a cargar el horario. Si encuentra datos en este array, debe mostrar un mensaje
//diciendo los cursos (nombres e ids) que no tiene estudiantes cargados y parar el programa. Para ello es necesario capturar estos datos con las excepciones:

            //----------------------------------->CAPTURAR EXCEPCION PARA VISTA: NECESARIO:          
//            System.out.println("Nº cursos a los que se han añadido estudiantes: " + stCourse.size());
//            System.out.println("Nº cursos con Schedule Active: " + CoursesScheduleActive.size());
            aviso.CourseWithoutStudents(stCourse, CoursesScheduleActive, this.nameCourses);

        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

//Aquí te dice que si el array está vacío, se asignen datos por defecto(male y nombre:default), y en caso contrario,
//que se asigne el nombre de la persona por cada id de los estudiantes en st. Los datos se asignan desde el método
//cargarNames(en este método se hace una consulta para sacar todos los nombres de la BBDD) y se guarda en en el ret:            
        if (ret.isEmpty()) {
            ret.add(stDefault);
        } else {
            for (Student st : ret) {
                st.setName(this.namePersons.get(st.getId()));
            }
        }
        return ret;
    }
//Convierte un ArrayList de enteros a un array [] de enteros:

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
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
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
        String nombre = "";
        for (Map.Entry<Integer, String> entry : NumNomSection.entrySet()) {

            if (entry.getKey() == (idc * 100 + id)) {
                nombre = entry.getValue();
            }

        }

        return nameCourse(idc) + " Section: " + nombre;
    }

    public String nameSection(int id) {
        if (id == 0) {
            return "0";
        }
        String nombre = "";
        for (Map.Entry<Integer, String> entry : NumNomSection.entrySet()) {

            if (entry.getKey() == id) {
                nombre = entry.getValue();
            }
        }

        return nombre;
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
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
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
    public HashMap<Integer, ArrayList<Seccion>> getDataSections(HashMap<Integer, Student> students, HashMap<Integer, Teacher> teachers, HashMap<Integer, Room> rooms, ArrayList<Course> courses, String yearID, String templateID, HashMap<String, Course> linkedCourses, String schoolCode, HashMap<Integer, ArrayList<Integer>> stCourse, Exceptions aviso) {
        //HashMap<Integer,ArrayList<Seccion>> rsSection = new ArrayList();
        HashMap<Integer, ArrayList<Seccion>> auxSections = new HashMap<>();
        ArrayList<ArrayList<Integer>> sectionsModificadas = new ArrayList<>();
        ArrayList<String> sectionsModificadasName = new ArrayList<>();
        ArrayList<String> sectionsModificadasFullName = new ArrayList<>();
        ArrayList<String> CourseName = new ArrayList<>();
        ArrayList<Seccion> auxSectionsIn;

//Los cursos que hay hasta ahora (en este caso dos) se van añadiendo a course. Se van a obtener los ids:)        
        for (Course course : courses) {
            try {
                int count = 1;
                int idCourse = course.getIdCourse();

                auxSectionsIn = new ArrayList<>();
                sectionsModificadas = new ArrayList<>();
                sectionsModificadasName = new ArrayList<>();
                sectionsModificadasFullName = new ArrayList<>();
                CourseName = new ArrayList<>();
//----------------->CAMBIO: SE HA AÑADIDO A LA CONSULTA EL TEMPLATEID (ES DE LA SECCION EN CONCRETO, SE HA HECHO ESTO PARA COMPROBAR QUE ES IGUAL AL DEL CURSO
//esto habría que hacerlo si se activa la opción de lock schedule, aunque hay que capturarlo aquí de una forma u otra porque si no no captura la sección, ni el curso
//ni el resto de cursos,ni pinta nada en pantalla(captura todo o nada)) :
//-----------------> RenWeb/Academic/Classes/Schedule. 
//RequiredRoom son los ids de rooms de las secciones de un curso concreto:

                String consulta = "SELECT distinct Section,StaffID,Pattern,LockEnrollment,LockSchedule,Classes.ClassID,RequiredRoom,templateId,name FROM Classes left join roster on \n"
                        + "                        (Classes.ClassID = roster.ClassID)\n"
                        + "                        where CourseId = " + course.getIdCourse() + " and Classes.YearId = " + yearID;
                ResultSet rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ArrayList<Integer> aux = new ArrayList<>();
//En el array aux se van añadiendo los datos y restricciones, uno en cada posición. La sección, staffID, Pattern, LockEnroolment, LockSchedule, classId, Room:     

//El id no se coge de la BBDD, se genera un autoincremento para evitar que si se pone un string en vez de un numero, se pueda guardar igualmente la sección:
//Es por eso que el primer dato se guarda como string en  sectionsModificadasName más abajo. Este es el dato que mostrará al usuario, pero el programa realmente
//cogerá el count y así poder manejarlo.
                    aux.add(count); // section

                    aux.add(rs.getInt(2)); // staffId
                    if (rs.getBoolean(5)) {
                        aux.add(rs.getInt(3)); // pattern
                    } else {
                        aux.add(-1);//pattern=-1 si no está activado el lockSchedule, para que luego no cargue los huecos de ambos
                    }

//Estos boolean son para activar o desactivar los lock:
                    if (rs.getBoolean(4)) {
                        aux.add(1); // lockEnrollment
                    } else {
                        aux.add(0); // lockEnrollment
                    }
                    if (rs.getBoolean(5)) {
                        aux.add(1); // lockSchedulle
                    } else {
                        aux.add(0); //  lockSchedulle
                    }
                    aux.add(rs.getInt(6)); // classID

                    aux.add(rs.getInt(7)); // room

                    //AÑADIDO: templateId de la SECCION EN CONCRETO.
                    templateIdSection = rs.getInt(8);

//Se añaden todos los datos de todas las posiciones de aux en una sola posición de sectionsModificadas, para poder guardar en este array todos los
//datos de todas las secciones:
//Cambio: se realiza lo anteriormente dicho SI coincide el id del template del curso con el id del template de cada seccion
//(Para que luego no de fallo al cargar el programa)
//En caso de que no se pueda se meten los datos en un arrayList dentro de un treeMap, que luego se concatenará todo en un String (para que al mostrar por pantalla no se repitan datos).
                    if (templateIdSection == Integer.parseInt(templateID)) {
                        sectionsModificadas.add((ArrayList<Integer>) aux.clone());
                        sectionsModificadasName.add(rs.getString(1));
                        sectionsModificadasFullName.add(this.nameCourses.get(course.getIdCourse()) + ": " + rs.getString(1));
                        CourseName.add(this.nameCourses.get(course.getIdCourse()));

                        NumNomSection.put(idCourse * 100 + count, rs.getString(1));

                        count++;
                        //En tempIdSect se guardan las secciones con un template diferente a la del curso de origen.
                    } else {
                        if (aux.get(4) == 1) {
                            aux.set(4, 0);
                        }
                        sectionsModificadas.add((ArrayList<Integer>) aux.clone());
                        sectionsModificadasName.add(rs.getString(1));
                        sectionsModificadasFullName.add(this.nameCourses.get(course.getIdCourse()) + ": " + rs.getString(1));

                        CourseName.add(this.nameCourses.get(course.getIdCourse()));

                        NumNomSection.put(idCourse * 100 + count, rs.getString(1));

                        count++;
                        aviso.templateIdSection(nameCourses, course, rs);
                    }
                }
//Ahora se van añadiendo a sectionsModificadas los ids de estudiantes que están almacenados en renweb/academic/classes/roster al arrayStud y posteriormente se añaden a auxSec:
//Si no hay ningún estudiante añadido en el roster, no se añade nada a arrayStud.
//También se añaden a auxSec los datos de las sectionsModificadas:
                if (!sectionsModificadas.isEmpty()) {
                    for (int i = 0; i < sectionsModificadas.size(); i++) {
                        Seccion auxSec = new Seccion();
                        ArrayList<Integer> arrayStud = new ArrayList<>();
                        consulta = "SELECT StudentID FROM Classes inner join roster on"
                                + "(Classes.ClassID = roster.ClassID)"
                                + " where roster.enrolled = 1 and Classes.CourseId = " + course.getIdCourse() + " and Classes.YearId = " + yearID
                                + " and Classes.Section ='" + sectionsModificadasName.get(i) + "'";
                        rs = DBConnect.renweb.executeQuery(consulta);
//Con este while se van añadiendo los distintos id de los estudiantes que hay en el Roster de una sección en concreto (sección que se ha establecido en el for previo).
//(Renweb/academic/Classes/Roster):
                        while (rs.next()) {
                            arrayStud.add(rs.getInt(1));
                        }

//Aquí se añaden a auxSec las condiciones que están establecidas en la parte de secciones de Renweb (para el curso concreto). Va sección a sección (gracias al for previo):
                        auxSec.setNumSeccion(sectionsModificadas.get(i).get(0));
                        auxSec.setIdTeacher(sectionsModificadas.get(i).get(1));
                        auxSec.setLockEnrollment(sectionsModificadas.get(i).get(3));
                        auxSec.setLockSchedule(sectionsModificadas.get(i).get(4));
                        auxSec.setTeacher(teachers.get(sectionsModificadas.get(i).get(1)));
//                        if (auxSec.getIdTeacher() == 0 && !Consultas.teachersCOURSE.get(course.getIdCourse()).isEmpty() && auxSec.isLockSchedule()) {
//                                auxSec.setTeacher(teachers.get(Consultas.teachersCOURSE.get(course.getIdCourse()).get(0)));
//                                auxSec.setIdTeacher(Consultas.teachersCOURSE.get(course.getIdCourse()).get(0));
//                             
//
//                        }
                        auxSec.setClassId(sectionsModificadas.get(i).get(5));
//Aquí se le añade el id del Room para que posteriormente se le pueda añadir el nombre:                        
                        auxSec.setIdRoom(sectionsModificadas.get(i).get(6));
                        auxSec.setRoom(rooms.get(sectionsModificadas.get(i).get(6)));

//Para añadir a arrayStud2 los alumnos que hay en la sección y en el curso (se coge los que coinciden en ambos, arrayStud son los que están en el roster de las secciones): 
//Esto se hace para que no de error, porque si se cogieran alumnos de la seccion que no se encuentran en el Request del curso daría fallo en todos los cursos (la obtención de datos
//se realiza de forma global):
                        if (arrayStud.size() > 0 && stCourse.containsKey(idCourse)) {
                            for (int j = 0; j < arrayStud.size(); j++) {
                                if (!stCourse.get(idCourse).contains(arrayStud.get(j))) {
                                    aviso.addDifStudents(course.getNameCourse(), sectionsModificadasName.get(i), arrayStud.get(j), this.namePersons.get(arrayStud.get(j)));
                                }
                            }

                            for (int j = 0; j < arrayStud.size(); j++) {
                                if (!stCourse.get(idCourse).contains(arrayStud.get(j))) {
                                    arrayStud.remove(j);
                                }
                            }
                        }

//Con este for se cogen los alumnos que están en el roster de la sección pero no se encuentran en el request del curso (el arrayStuderroneos se utiliza para lanzar un mensaje al usuario si
//existen alumnos que no pertenecen al curso en sección, y así el usuario será consciente de que debe cambiarlo):
                        auxSec.setIdStudents((ArrayList<Integer>) arrayStud.clone());
//                        auxSec.setStudents((ArrayList<Student>)this.namePersons.get(arrayStud.clone()));

                        if (auxSec.isLockSchedule()) {
                            consulta = "SELECT * from SchedulePatternsTimeTable "
                                    + " where patternnumber = " + sectionsModificadas.get(i).get(2) + " and templateID =" + templateID;
//De esta consulta se obtienen las columnas y filas necesarias para aplicar el patron especifico seleccionado en: 
//Renweb/Academic/Classes/Schedule(aquí se puede elegir el pattern en función del template asignado):

                            rs = DBConnect.renweb.executeQuery(consulta);
//Se añade a auxSec el patronUsado en función de las columnas y filas obtenidas de la consulta previa:
//Nota: al añadir a la tupla se empieza la x y la y por 0: es por eso por lo que respecto a row y col se le resta 1
//Aquí también se asignan los patrones a las rooms que se hayan cargado previamente (se guarda en el hashmap de rooms los huecos de secciones ocupados).
//Por lo tanto, por una parte se aplica a auxSec, y por otra parte, si encuentra el idRoom concreto en rooms, se guarda también el
//hueco en la room de una sección concreta.

                            while (rs.next()) {
                                auxSec.addTuplaPatron(new Tupla(rs.getInt("col") - 1, rs.getInt("row") - 1));
                                if (rooms.containsKey(auxSec.getIdRoom())) {
                                    ArrayList<Tupla> auxTupla = new ArrayList<>();
                                    auxTupla.add(new Tupla(rs.getInt("col") - 1, rs.getInt("row") - 1));
//                                rooms.get(auxSec.getIdRoom()).ocuparHueco(course.getIdCourse(), auxSec.getNumSeccion(), auxTupla);
                                }
                            }
                        }
//Se asigna a auxSec el id de curso, y el auxSec (con los datos anteriores y el id) se añade como una posición de auxSectionIn                        
                        auxSec.setCourseID(idCourse);
                        auxSec.setNameSeccion(sectionsModificadasName.get(i));
                        auxSectionsIn.add(new Seccion(auxSec));
//Todos los datos del método hasta ahora se almacenan en el for de updateStudent:
//NOTA: aquí updateStudent ya tiene en cuenta todos los estudiantes del Requests de todos los cursos.
                        updateStudent_fromRenWeb_Sections(students, arrayStud, auxSec, linkedCourses);
                    }
                    //-->En cada posición de auxSection se almacenan todas las restricciones de las secciones, y se acceden a ellas a través del identificador,
//que es el id de cada curso:
//Por ejemplo, en una posición del HashMap del curso 1245, se almacenan a su vez las 5 posiciones de 5 secciones
//(son las secciones que están añadidas desde RenWeb/Academics/Classes y en el curso de ENG1) con sus respectivas restricciones:
//Este es el Hash que se va a retornar para devolver todaa la información que contiene a la clase Restricciones:
                    auxSections.put(course.getIdCourse(), (ArrayList<Seccion>) auxSectionsIn.clone());
                }
            } catch (Exception e) {
                e.getMessage();
            }
        }
//Gracias a este treemap se puede visualizar por pantalla de forma correcta si hay alumnos que están asignados en secciones de un curso(roster de class section en renweb),
//pero que no están asignados en el propio curso(requests en courses de renweb):
//NOTA: lo que hace es visualizarlo de forma correcta, pero los datos ya han sido obtenidos previamente en arrayStuderroneos.
//La visualización correcta consiste en que no se repiten los nombres de los cursos y las secciones cuando se van a 
//visualizar los estudiantes en cuestión que están mal asignados a seccion/es.

        return auxSections;
    }

    private void updateStudent_fromRenWeb_Sections(HashMap<Integer, Student> students, ArrayList<Integer> arrayStud, Seccion auxSec, HashMap<String, Course> linkedCourses) {
        //linkedCourses.containsValue(auxSec)
        for (Integer arrayStud1 : arrayStud) {
            try {
                students.get(arrayStud1).addSeccionFromrenweb(new Seccion(auxSec));
            } catch (Exception e) {

            }
        }
    }

    private boolean containsCourse(HashMap<String, Course> linkedCourses, int courseID) {
        for (HashMap.Entry<String, Course> entry : linkedCourses.entrySet()) {
            if (entry.getValue().getIdCourse() == courseID) {
                return true;
            }
        }
        return false;
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
            courses.get(i).setNameCourse(this.nameCourses.get(courses.get(i).getIdCourse()));
        }
    }

    public int getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(ArrayList<ArrayList<Boolean>> totalBlocks) {
        this.totalBlocks = 0;
        for (int i = 0; i < totalBlocks.size(); i++) {
            for (int j = 0; j < totalBlocks.get(i).size(); j++) {
                if (totalBlocks.get(i).get(j) == true) {
                    this.totalBlocks++;
                }
            }
        }
    }

    public static HashMap<Integer, String> getPersons() {
        HashMap<Integer, String> ret = new HashMap();
        String consulta = "SELECT PersonID,LastName,FirstName from person";
        try {
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                ret.put(rs.getInt(1), rs.getString(2) + ", " + rs.getString(3));
            }
        } catch (Exception e) {
        }
        return ret;
    }
//Esto se devuelve a getRestriccionesCourses los siguientes datos en función de los valores de los array:
    //Esto permite seleccionar los tipos de escuela que son susceptibles de aplicar a los horarios desde los template:

    private String getWhereTemplate(int[] tempInfo) {
        if (tempInfo[0] == 0 && tempInfo[1] == 0 && tempInfo[2] == 0 && tempInfo[3] == 0) {
            return "Elementary=0 and HS=0 and MiddleSchool=0 and PreSchool=0";
        } else if (tempInfo[0] == 0 && tempInfo[1] == 0 && tempInfo[2] == 0 && tempInfo[3] == 1) {
            return "PreSchool=1 ";
        } else if (tempInfo[0] == 0 && tempInfo[1] == 0 && tempInfo[2] == 1 && tempInfo[3] == 0) {
            return "MiddleSchool=1";
        } else if (tempInfo[0] == 0 && tempInfo[1] == 0 && tempInfo[2] == 1 && tempInfo[3] == 1) {
            return "MiddleSchool=1 or PreSchool=1";
        } else if (tempInfo[0] == 0 && tempInfo[1] == 1 && tempInfo[2] == 0 && tempInfo[3] == 0) {
            return "HS=1";
        } else if (tempInfo[0] == 0 && tempInfo[1] == 1 && tempInfo[2] == 0 && tempInfo[3] == 1) {
            return "PreSchool=1 or HS=1";
        } else if (tempInfo[0] == 0 && tempInfo[1] == 1 && tempInfo[2] == 1 && tempInfo[3] == 0) {
            return "HS=1 or MiddleSchool=1";
        } else if (tempInfo[0] == 0 && tempInfo[1] == 1 && tempInfo[2] == 1 && tempInfo[3] == 1) {
            return "HS=1 or MiddleSchool=1 or PreSchool=1 ";
        } else if (tempInfo[0] == 1 && tempInfo[1] == 0 && tempInfo[2] == 0 && tempInfo[3] == 0) {
            return "Elementary=1";
        } else if (tempInfo[0] == 1 && tempInfo[1] == 0 && tempInfo[2] == 0 && tempInfo[3] == 1) {
            return "Elementary=1 or PreSchool=1";
        } else if (tempInfo[0] == 1 && tempInfo[1] == 0 && tempInfo[2] == 1 && tempInfo[3] == 0) {
            return " Elementary=1 and MiddleSchool=1";
        } else if (tempInfo[0] == 1 && tempInfo[1] == 0 && tempInfo[2] == 1 && tempInfo[3] == 1) {
            return "Elementary=1 or MiddleSchool=1 or PreSchool=1 ";
        } else if (tempInfo[0] == 1 && tempInfo[1] == 1 && tempInfo[2] == 0 && tempInfo[3] == 0) {
            return "Elementary=1 or HS=1";
        } else if (tempInfo[0] == 1 && tempInfo[1] == 1 && tempInfo[2] == 0 && tempInfo[3] == 1) {
            return "Elementary=1 or HS=1 or PreSchool=1";
        } else if (tempInfo[0] == 1 && tempInfo[1] == 1 && tempInfo[2] == 1 && tempInfo[3] == 0) {
            return "Elementary=1 or HS=1 or MiddleSchool=1";
        } else {
            return "Elementary=1 or HS=1 or MiddleSchool=1 or PreSchool=1";
        }
    }
}
