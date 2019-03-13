
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
    public static HashMap<Integer, ArrayList<Integer>> teachersCOURSE;
    private Teacher tdefault;
    private Student stDefault;
    public static HashMap<Integer, String> courseName;
    public static int DEFAULT_RANK = 10;
    private HashMap<Integer, String> namePersons;
    private HashMap<Integer, String> nameCourses;
    private HashMap<Integer, String> abbrevCourses;
    public static String cursosSin1;
    public static HashMap<Integer, String> CoursesWithoutStudents;
    public int templateIdSection;
    public static TreeMap<String, ArrayList<String>> tempIdSect = new TreeMap<>();
    public static String tempidsect;
    public static ArrayList<String> arrayStuderroneos = new ArrayList<>();
    public static TreeMap<String, TreeMap< String, ArrayList<String>>> studError = new TreeMap<>();
    public static String studE;
    public static String error;
    public static HashMap<Integer, Integer> countCourse = new HashMap<>();
    public static HashMap<Integer, ArrayList<Integer>> countCourse2 = new HashMap<>();
    public static HashMap<Integer, ArrayList<Integer>> countSchool = new HashMap<>();
    public static String alert = "";

    private ArrayList<ArrayList<Boolean>> totalBlocksStart;
    private int totalBlocks;

    public Consultas(String tempid) {
        teachers = new ArrayList<>();
        teachersCOURSE = new HashMap<>();
        tdefault = teacherDefault();
        stDefault = new Student(0);
        stDefault.setGenero("Male");
        stDefault.setName("default");
        courseName = new HashMap<>();
        totalBlocksStart = this.totalBlocksStart(tempid);
        //El siguiente totalBlocks es de prueba, el bueno es totalBlocksStart.
        totalBlocks = this.totalBlocks();
        CoursesWithoutStudents = new HashMap<>();
        cargarNames();
        error = "";
    }

    private void cargarNames() {
        this.namePersons = new HashMap<>();
        this.nameCourses = new HashMap<>();
        this.abbrevCourses = new HashMap<>();

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
//Aquí se accede a través del método menu de Homepage para poder obtener los datos de las escuelas según el districtCode:

    public static ArrayList<Tupla<String, String>> getSchools(String districtCode) { // OBTIENE EL GETYEATS FALTA FILTRAR POR COLEGIO 

        ArrayList<Tupla<String, String>> ret = new ArrayList<>();
        String consulta = "SELECT SchoolName,SchoolCode FROM ConfigSchool ";
        try {
//Con el rs se pueden rastrear el SchoolName y el SchoolCode de la tabla CongigSchool        :    
            ResultSet rs = DBConnect.renweb.executeQuery(consulta);
//En el siguiente método guarda los datos del colegio en cuestión, y se guardan en la tupla ret (por ejemplo:x= GCS1,y= Elementary School)            
            while (rs.next()) {
                String yearid = rs.getString("SchoolCode");
                String yearName = rs.getString("SchoolName");
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
//De aquí se extraen los datos del Template para utilizarlo en menu.jsp a través del HomePage.getTemplates():

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
//Este método lo va a capturar el ScheduleEduweb de la clase ScheduleControler para implementar cabeceras de filas 
//Por eso se sacan datos de columna=0:
//Se obtienen los datos de la tabla ScheduleTemplateTimeTable

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
//Este método lo va a capturar el ScheduleEduweb de la clase ScheduleControler para implementar cabeceras de columnas(por eso se sacan datos de fila=0):
//Se obtienen los datos de la tabla ScheduleTemplateTimeTable    

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
                    //   ret.get(i).setBlocksWeek(rs.getInt(1));
                    switch (type) {
                        case "Integer":
                            hashObject.put(rs.getInt("id"), Integer.parseInt(aux));
                            break;
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

    //AQUI ES DONDE TARDA *****
    //Aquí se obtienen las restricciones para los cursos. Si por ejemplo se ha cogido el template standard, el templateID=10:
    public ArrayList<Course> getRestriccionesCourses(int[] ids, int[] tempinfo, String templateID, HashMap<String, ArrayList<Integer>> groupCourses) {
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

            /*  consulta = "select * from courses"
                    + " where Elementary=" + tempinfo[0]
                    + " and HS=" + tempinfo[1]
                    + " and MidleSchool=" + tempinfo[2]
                    + " and PreSchool=" + tempinfo[3]
                    + " and active=1";*/
 /*  consulta = "select * from courses"
                    + " where "                  
                    + getWhereTemplate(tempinfo, "MidleSchool")
                    + " and active=1";*/
//          
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
//El has1 coge los cursos que tiene asignado el template : RenWeb/ReportManager/Schedules/SchedulesCourseTemplate y print        
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
                    + "                    and udd.data =1";
//Y estos datos se introducen en el hashDos. Se cogen los ids que tienen data=1 en la BBDD, es decir, los que están activados.
//Estos se guardan en hash2 (se puede comprobar en:RenWeb/Academic/Courses y se selecciona el curso, se aplica en User Defined y se comprueba
//si el apartado Schedule Schedule esta marcado en Yes ):
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
//Se necesita que ambos Hash tengan las ids para poder establecer las restricciones de los cursos:                
                if (hashUno.containsKey(ids[i]) && hashDos.containsKey(ids[i])) {
//Se crea un objeto de curso en funcion de los ids recogidos, para luego guardar los datos en el array hecho previamente de curso:                    
                    Course r = new Course(ids[i]);
                    ret.add(r);
//Aquí se asignan el nombre de los cursos en función de su id, pero no se carga en ret:                    
                    courseName.put(ids[i], this.nameCourses.get(ids[i]));
                }
            }
            //HashMap<String, String> map = new HashMap<>();
            /*  for (HashMap.Entry<Integer, String> entry : hashUno.entrySet()) {
                Course r = new Course(entry.getKey());
                ret.add(r);
                courseName.put(entry.getKey(), this.nameCourses.get(entry.getKey()));
            }
             */
//Se hace un HashMap para cada una de las restricciones de cursos, para que posteriormente pueda ser cargado cada Hash:          
            HashMap<Integer, Object> hashBlocksPerWeek = new HashMap<>();
            HashMap<Integer, Object> hashGR = new HashMap<>();
            HashMap<Integer, Object> hashMaxSections = new HashMap<>();
            HashMap<Integer, Object> hashMaxSectionsSchool = new HashMap<>();
            HashMap<Integer, Object> hashMinGapBlocks = new HashMap<>();
            HashMap<Integer, Object> hashMinGapDays = new HashMap<>();
            HashMap<Integer, Object> hashRank = new HashMap<>();
            HashMap<Integer, Object> hashTeachers = new HashMap<>();
//hashRooms no aparece cargado en ret            
            HashMap<Integer, Object> hashRooms = new HashMap<>();
            HashMap<Integer, Object> hashExcludeBlocksCourse = new HashMap<>();
            HashMap<Integer, Object> hashExcludeBlocksSchool = new HashMap<>();
            HashMap<Integer, Object> hashPreferredBlock = new HashMap<>();
            HashMap<Integer, Object> hashbalanceTeachers = new HashMap<>();
            HashMap<Integer, Object> hashMandatoryBlocksRange = new HashMap<>();
            HashMap<Integer, Object> hashmaxBxD = new HashMap<>();
            HashMap<Integer, Object> hashminSizePerSection = new HashMap<>();

            //cargarHashMap(hashBlocksPerWeek,'course','BlocksPerWeek','int');
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
            //cargarHashMap(hashminSizePerSectionSchool, "school", "minSizePerSection", "Integer");
//Gracias al siguiente for y a los siguientes if(cada uno por cada Hash especifico), se cargan 
//en el array ret(tipo Course) las variables de su constructor con los datos
//del hash (por la condicion que se establece dentro del if:a través de correspondencia de Ids). 
//Cuando el for ha acabado, en el ret se han cargado todos
//los valores de las distintas restricciones en funcion del curso(id).
            int minSizePerSectionSchool = 0;
            consulta = "select udd.data,udd.id\n"
                    + "                from uddata udd\n"
                    + "                inner join udfield udf\n"
                    + "                    on udd.fieldid = udf.fieldid\n"
                    + "                inner join udgroup udg\n"
                    + "                    on udg.groupid = udf.groupid\n"
                    + "                    and udg.grouptype = 'school'\n"
                    + "                    and udg.groupname = 'Schedule'\n"
                    + "                    and udf.fieldName = 'minSizePerSection'\n";

            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                minSizePerSectionSchool = rs.getInt(1);
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
                    + "and udf.fieldName = 'MaxSections'\n";

            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                maxSectionSchool = rs.getInt(1);
            }

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
//Los set se consiguen gracias a setters,getters y variables definidos en la clase Course:
//Los hashBlocks aquí ya están cargados con la información de las restricciones. Por ejemplo:
//hashBlocksPerWeek--> tiene 7 identificadores de cursos(cursos que no tienen porque tener el schedule ni el template activados) y 
//cada identificador está asociado a un valor concreto. En este punto, hay dos cursos que coinciden en que tienen el template y el schedule:
//que son english1 y admath1 con ids 1245 y 1216, respectivamente. De los 7 identificadores que tiene hashBlocksPerWeek, 
//uno de ellos es 1245, con un valor asociado de 5, es decir, que para el curso de english1 se cargan en user defined 
//5 bloques por semana de restricciones
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
                } else {
                    ret.get(i).setMaxSections(String.valueOf(maxSectionSchool));
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
                    ret.get(i).setMinGapBlocks((Integer) hashMinGapBlocks.get(ret.get(i).getIdCourse()));
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
                    if (!hashRank.containsKey(ret.get(i).getIdCourse())) {
                        ret.get(i).setRank(DEFAULT_RANK);
                    } else {
                        ret.get(i).setRank((int) hashRank.get(ret.get(i).getIdCourse()));
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
                        + "                    and udf.fieldName = 'Teachers'\n"
                        + "                where udd.id =" + ret.get(i).getIdCourse();

                rs = DBConnect.renweb.executeQuery(consulta);
                String[] s = new String[2];
                while (rs.next()) {
                    s = rs.getString(1).split(",");
                }*/
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

                /* SOLO MD-PAN al principio, ahora se aplica a todos:*/
                ArrayList<String> aux = new ArrayList<String>(Arrays.asList(s));
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
                    }//;
                    if (rs.getInt(2) != 0) {
                        auxRoomDefault = rs.getInt(2);
                    }
                }
               
                ArrayList<Integer> sectionTeachers = new ArrayList<>();
                /*  SELECT staffid, requiredroom FROM Classes left join roster on 
                        (Classes.ClassID = roster.ClassID)
                        where CourseId = 649 and templateid=57 */
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
                    }//;
                }
                // s[s.length] = auxTeacherDefault;    
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
                        int idt = convertString(s2);
                        ar.add(idt);
                        if (!teachers.contains(idt)) {
                            teachers.add(idt);
                            teachersCOURSE.get(ret.get(i).getIdCourse()).add(idt);
                        }
                    }
                }
//Con lo que al llegar a este punto: teachers y ar tienen los dos ids de profesores añadidos y el id por defecto:
//Aquí ya se devolvería a ret las restricciones de profesores añadidas a ar, siempre y cuando el hash de profesores indique
//el id de curso que coincida con alguno en ret:
                ret.get(i).setTrestricctions(ar);
//POR AHORA ESTÁN DESACTIVADAS LAS RESTRICCIONES EN ROOMS PORQUE FALTA TERMINAR CÓDIGO Y TESTEAR:                

                /*consulta = "select udd.data\n"
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
                }*/
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
                        ret.get(i).addRoom(rs.getInt(2));
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
                            System.err.println("no se puede leer bien el campo rooms en el curso"
                                    + ret.get(i).getIdCourse());
                        }
                    }

                } else if (groupCourses.containsKey(templateID)) {
                    ret.get(i).addArrayRooms(groupCourses.get(templateID));
                    

                } //else if (!groupCourses.containsKey(templateID)) {
//                    rooms = "-1";
//                    ret.get(i).addRoom(Integer.parseInt(rooms));
//                }
                countCourse.put(ret.get(i).getIdCourse(), contaRoomsCourse);
                if(groupCourses.containsKey(templateID)) {
                   for (Integer room : groupCourses.get(templateID)) {
                        countSchool.get(ret.get(i).getIdCourse()).add(room);
                    } 
                }
                // groupOfRooms = rs.getString(1);
                /* String result = rooms;
                    String groupOfRooms;
                    boolean exito = false;
                    String[] resultSplit = result.split(";");
                    int k = 0;

                    while (k < resultSplit.length && !exito) {
                        groupOfRooms = resultSplit[k].substring(1, resultSplit[k].length() - 1);

                        String[] sResult;
                        sResult = groupOfRooms.split(",");
                        if (sResult.length == 2) {
                            String auxTemplate = sResult[0];
                            //  auxTemplate = auxTemplate.substring(1, auxTemplate.length());
                            if (auxTemplate.equals(templateID)) {
                                String auxRooms = sResult[1];
                                auxRooms = auxRooms.substring(1, auxRooms.length() - 1);
                                sResult = auxRooms.split("-");
                                ret.get(i).resetRooms();
                                for (String s2 : sResult) {
                                    try {

                                        ret.get(i).addRoom(Integer.parseInt(s2));

                                    } catch (Exception ex) {
                                        Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                //roomsTemplate.put(auxTemplate, rooms);
                                exito = true;
                            }
                        }
                        k++;
                    }

                   for (String room : rooms.split(",")) {
                        try {
                            ret.get(i).addRoom(Integer.parseInt(room));
                        } catch (Exception e) {
                            System.err.println("no se puede leer bien el campo rooms en el curso"
                                    + ret.get(i).getIdCourse());
                        }
                    }*/
                //}
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
                int numeroMaxChildPerSection = 0;
                while (rs.next()) {
//----CAMBIO----- Se guarda ahora la restricción de maximo tamaño de secciones en el array ret y en una variable.
//En la variable se guarda también, porque es necesario para que más adelante calcular el número minimo de estudiantes por seccion
//ya que del hashminSizePerSection se obtiene el porcentaje y se necesita el numero minimo para el algoritmo.
                    ret.get(i).setMaxChildPerSection(rs.getInt(1));
                    numeroMaxChildPerSection = rs.getInt(1);
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
                if (hashminSizePerSection.containsKey(ret.get(i).getIdCourse())) {
                    //  ret.get(i).setMinSections((int) hashminSizePerSection.get(ret.get(i)));
                    int numeroMinSizePerSection = ((int) (hashminSizePerSection.get(ret.get(i).getIdCourse())) * (numeroMaxChildPerSection)) / 100;
                    ret.get(i).setMinChildPerSection(numeroMinSizePerSection);
                } else {
                    int numeroMinSizePerSection = (minSizePerSectionSchool * numeroMaxChildPerSection) / 100;
                    ret.get(i).setMinChildPerSection(numeroMinSizePerSection);
                }
//Se actualizan los PatternGroups en funcion del curso y el template asignado:
                updatePatternGroups(ret.get(i), templateID);

                //prueba
                //    ret.get(i).insertarOActualizarCurso();
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
            System.out.println("dataManage.Consultas.updatePatternGroups()");
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
    protected HashMap<String, Course> getLinkedCourses() { //return hash de linkeados
        //Como clave sera la asignatura(curso) que se debera insertar primero y como valor
        // la que tiene que estar como consecutiva:
//El getLinkedCourses sirve para aplicar las restricciones de un curso en otro(se copian los roster de cada seccion, o de las indicadas. NOTA: el nº de secciones debe ser igual en ambos cursos):

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

    // * @author David
//Con este método se establece el total de bloques de inicio en un curso concreto en función del template:    
    private ArrayList<ArrayList<Boolean>> totalBlocksStart(String tempid) {

        String excludes = "";

        Course caux = new Course(1);
//El array de bloques es boolean: true es la activación de bloques que se van a usar:
//Ejemplo:Hay 5 registros true por cada posición y un total de 10 posiciones: 5 columnas de días y 10 filas para las horas, total 50 bloques:
        ArrayList<ArrayList<Boolean>> auxTotalStart = new ArrayList<>();
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
//Se quitan los bloques excluidos (los indicados en Configuration de School):
            while (rs.next()) {
                if (!excludes.contains(rs.getString(1))) {
                    excludes += rs.getString(1);
                }
            }
            caux.setExcludeBlocks(excludes);

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
//Palabras excluidas (Exclude Words de Configuration/School), en este caso: Lunch(esta palabra se recoge para luego aplicarla al template, y que en los bloques bloqueados ponga:Lunch):                
                String[] auxS = rs.getString("data").split(",");
                arrExcludeWords = Arrays.asList(auxS);
            }

            auxTotalStart = caux.opcionesStart();
            consulta = "SELECT * FROM ScheduleTemplateTimeTable where templateid =" + tempid;

            rs = DBConnect.renweb.executeQuery(consulta);
//Se añade el esquema del template(monday,tuesday... a tmpText):            
            while (rs.next()) {
                int row = rs.getInt("row") - 1;
                int col = rs.getInt("col") - 1;
                String tmpText = rs.getString("TemplateText");
                if (!tmpText.equals("") && arrExcludeWords.contains(tmpText)
                        && row >= 0 && col >= 0) {
                    auxTotalStart.get(row).set(col, false);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return auxTotalStart;
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

    public ArrayList<Teacher> teachersList(String tempid, int[] tempinfo) {
        ArrayList<Teacher> ret = new ArrayList<>();
        try {
            /*
             consulta = "select * from courses"
                    + " where Elementary=" + tempinfo[0]
                    + " and HS=" + tempinfo[1]
            x        + " and MidleSchool=" + tempinfo[2]
                    + " and PreSchool=" + tempinfo[3];
            rs = DBConnect.renweb.executeQuery(consulta);
             */
            //   String consulta = "SELECT * FROM Person_Staff where active=1 and faculty=1";

            String consulta = "SELECT staffID FROM Person_Staff ps inner join Person p on (ps.StaffID = p.PersonID)\n"
                    + "                        where ps.active=1 and ps.faculty=1 and "+getWhereTemplate(tempinfo);
//Se guardan en el array teachers las ids de los profesores sacadas de la BBDD que hay en el PersonStaff de la escuela en concreto:
            ResultSet rs;
            rs = DBConnect.renweb.executeQuery(consulta);
            while (rs.next()) {
                int staffId = rs.getInt(1);
                if (!this.teachers.contains(staffId)) {
                    teachers.add(staffId);
                }
            }
//El siguiente for significa que para cada posición que haya un id(es decir, para cada profesor), se añaden restricciones en el array ret(que es de tipo teacher)
//teniendo en cuenta tab el id del template del curso(qué template tiene asignado):            
            for (Integer s : teachers) {
                if (!s.equals("")) {
                    ret.add(restriccionesTeacher(tempid, s));
                }
//Hasta aquí se han devuelto las restricciones de cada profesor contemplado en el template:                
            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public Teacher restriccionesTeacher(String tempid, int id) {
        Teacher ret = new Teacher();
        String consulta = "";
//El parametro id de restriccionesTeacher es el id de cada profesor que se recorre en el for de teachers de teacherList(arriba):
//
        ResultSet rs;
//Se coge el ide de cada profesor y se consulta en la BBDD, la información de cada una de las restricciones:   
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
//Se obtiene el máximo de secciones correspondiente a la consulta que se ha hecho de cada id:
//Max Sections: número máximo de secciones que puede tener el profeso a cargo.
                while (rs.next()) {
                    ret.setMaxSections(rs.getInt(1));
                }
//Si no encuentra el dato en la BBDD, se asigna el valor por defecto del MaxSections:                
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
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
//Se guarda el nombre de los profesores en ret, en función de su id :      
        ret.setName(this.namePersons.get(id));
        return ret;
    }

    //to do: FUNCION NO PROBADA CONVIERTE EXCLUDE BLOCKS ENTRE TEMPLATES
    //SE LO EXPLIQUE A DAVID EL ULTIMO DIA.
//Esta función no hace falta porque ya se tiene en cuenta el template al usar las restricciones de profesores:
    /* private void setExcludeBlocksTeacher(Teacher t, String tempid) {
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
    }*/
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

    public HashMap<String, ArrayList<Integer>> roomsGroup(String groupOfRooms, String tmpId) {
        HashMap<String, ArrayList<Integer>> roomsTemplate = new HashMap();
        ArrayList<Integer> rooms = new ArrayList();
        boolean exito = false;

        //De esta consulta se sacan los rooms asignados en School, que tienen asignado para determinados template
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
                        + "                    and udf.fieldName = 'Rooms'";
//Con lo siguiente se quitan los ; parentesis y otros caracteres, para almacenar todo en el hashmap roomsTemplate(
//con key: id de los template, y value: los ids que tienen asignado a determinado id de template )
                ResultSet rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    // groupOfRooms = rs.getString(1);
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
                            //  auxTemplate = auxTemplate.substring(1, auxTemplate.length());

                            String auxRooms = s[1];
                            auxRooms = auxRooms.substring(1, auxRooms.length() - 1);
                            s = auxRooms.split("-");

                            for (String s2 : s) {
                                try {
                                    rooms.add(Integer.parseInt(s2));
                                } catch (Exception ex) {
                                    Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            roomsTemplate.put(auxTemplate, (ArrayList<Integer>) rooms.clone());
                            //exito = true;
                        }
                        i++;
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return roomsTemplate;
    }

    /**
     *
     * @param c
     * @param stCourse
     * @param yearid
     * @returng
     */
    //@ExceptionHandler
    public ArrayList<Student> restriccionesStudent(ArrayList<Integer> c, HashMap<Integer, ArrayList<Integer>> stCourse, String yearid, int tempid, String schoolCode) throws Exception {
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
                    + "                    and udd.data = '1'";
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
        /*     consulta = "    select sr.courseid, sr.studentid, p.gender, ps.gradelevel\n"
                + "    from studentrequests sr, person p, person_student ps,courses c \n"
                + "    where sr.studentid = p.personid\n"
                + "    and ps.studentid = p.personid\n"
                + "    and sr.yearid = " + yearid
                + "    and ps.status = 'enrolled'\n"
                + "    and ps.nextstatus = 'enrolled'\n"
                + "    and c.CourseID = sr.courseid"
                + "    and c.Elementary=" + tempinfo[0]
                + "    and c.HS=" + tempinfo[1]
                + "    and c.MidleSchool=" + tempinfo[2]
                + "    and c.PreSchool=" + tempinfo[3];
        //+ "    order by gender";
         */
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
                    st.setGenero(rs.getString("gender"));
                    st.setGradeLevel(rs.getString("gradelevel"));
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
            System.out.println("Nº cursos a los que se han añadido estudiantes: " + stCourse.size());
            System.out.println("Nº cursos con Schedule Active: " + CoursesScheduleActive.size());
            if (stCourse.size() != CoursesScheduleActive.size()) {
                for (int i = 0; i < CoursesScheduleActive.size(); i++) {
                    if (!stCourse.containsKey(CoursesScheduleActive.get(i))) {
                        System.out.println(CoursesScheduleActive.get(i));
                        CoursesWithoutStudents.put(CoursesScheduleActive.get(i), this.nameCourses.get(CoursesScheduleActive.get(i)));
                        // cursosSin1 = CoursesWithoutStudents.toString();
                        /*consulta = "select Title from courses where CourseID = "+CoursesScheduleActive.get(i);
                            ResultSet rs2 = DBConnect.renweb.executeQuery(consulta);

            while (rs2.next()) {
  
                String title = rs2.getString("Title");
                CoursesWithoutStudents.put(CoursesScheduleActive.get(i),title);
                System.out.println(title);
            }*/
                    }
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
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
    //OWN:Se obvia esta conexion porque ya no se usa la cuenta de EEUU:
/*    
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
                c.setMinGapBlocks(rs.getInt("mingapblocks"));
                c.setMinGapDays(rs.getInt("mingapdays"));
                c.setRank(rs.getInt("rank"));
                c.setGR(rs.getBoolean("gender"));
                c.setExcludeBlocksOwnDB(rs.getString("excludeblocks"));
                // c.setMaxBlocksPerDay(rs.getInt("maxblocksperday"));
                c.setRooms(rs.getString("rooms"));
                c.setExcludeCols("excludecols");
                c.setExcludeRows("ecluderows");
                /**//*
                c.setBalanceTeachers(rs.getBoolean("balanceteacher"));

                // String sAux = rs.getString("preferedblocks");
                //sAux = sAux.substring(1, sAux.length()-1);
                c.setPreferedBlocks(rs.getString("preferedblocks"));
                /**/
 /*
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
    //HashMap<Integer,HashMap<Integer,Seccion>> mapSecciones;//HashMap<idCourse,HashMap<numSeccion,Seccion>>
     */
    public HashMap<Integer, ArrayList<Seccion>> getDataSections(HashMap<Integer, Student> students, HashMap<Integer, Teacher> teachers, HashMap<Integer, Room> rooms, ArrayList<Course> courses, String yearID, String templateID, HashMap<String, Course> linkedCourses, String schoolCode, HashMap<Integer, ArrayList<Integer>> stCourse) {
        //HashMap<Integer,ArrayList<Seccion>> rsSection = new ArrayList();
        HashMap<Integer, ArrayList<Seccion>> auxSections = new HashMap<>();
        ArrayList<ArrayList<Integer>> sectionsModificadas = new ArrayList<>();
        ArrayList<String> sectionsModificadasName = new ArrayList<>();
        ArrayList<String> sectionsModificadasFullName = new ArrayList<>();
        ArrayList<String> CourseName = new ArrayList<>();
        ArrayList<Seccion> auxSectionsIn;
        tempidsect = "";

        /*        String consulta = "SELECT distinct Section,Classes.YearId,StaffID,Pattern,CourseId FROM Classes inner join roster on\n" +
"                                           (Classes.ClassID = roster.ClassID)";
                ResultSet rs = DBConnect.renweb.executeQuery(consulta);
                while (rs.next()) {
                    ArrayList<Integer> aux = new ArrayList<>();
                    aux.add(rs.getInt(1)); // section
                    aux.add(rs.getInt(2)); // staffId
                    aux.add(rs.getInt(3)); // pattern
                    sectionsModificadas.add(aux);
                }
         */
//Los cursos que hay hasta ahora (en este caso dos) se van añadiendo a course. Se van a obtener los ids:)        
        for (Course course : courses) {
            try {
                int count = 1;
                int idCourse = course.getIdCourse();

                if (idCourse == 1303) {
                    System.err.println("");
                }
                //int numSeccion = 1;
                //String sAux =
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
                    //try {
//En el array aux se van añadiendo los datos y restricciones, uno en cada posición. La sección, staffID, Pattern, LockEnroolment, LockSchedule, classId, Room:     

//El id no se coge de la BBDD, se genera un autoincremento para evitar que si se pone un string en vez de un numero, se pueda guardar igualmente la sección:
//Es por eso que el primer dato se guarda como string en  sectionsModificadasName más abajo. Este es el dato que mostrará al usuario, pero el programa realmente
//cogerá el count y así poder manejarlo.
                    aux.add(count); // section
                    /* } catch (Exception e) {
                        aux.add(1);
                    }*/

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
//                    if(rs.getInt(7)==0 && Consultas.countCourse.get(course.getIdCourse())){ Se ha usado más abajo
//                        )
//                    }
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
                        String p1 = "Course Name: " + this.nameCourses.get(course.getIdCourse());
                        String p2 = "-Class Section: " + rs.getString(9) + ":" + rs.getString(1);

                        if (!tempIdSect.containsKey(p1)) {
                            tempIdSect.put(p1, new ArrayList<>());
                            tempIdSect.get(p1).add(p2);
                            tempidsect = tempidsect.concat("<br/>" + p1 + ": " + "<br/>" + p2);

                        } else {
                            tempIdSect.get(p1).add(p2);
                            tempidsect = tempidsect.concat("<br/>" + p2);
                        }
                    }
                }
//Ahora se van añadiendo a sectionsModificadas los ids de estudiantes que están almacenados en renweb/academic/classes/roster al arrayStud y posteriormente se añaden a auxSec:
//Si no hay ningún estudiante añadido en el roster, no se añade nada a arrayStud.
//También se añaden a auxSec los datos de las sectionsModificadas:
                if (!sectionsModificadas.isEmpty()) {
                    for (int i = 0; i < sectionsModificadas.size(); i++) {
                        Seccion auxSec = new Seccion();
                        ArrayList<Integer> arrayStud = new ArrayList<>();
                        ArrayList<Integer> arrayStud2 = new ArrayList<>();
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
                        if (auxSec.getIdTeacher() == 0 && !Consultas.teachersCOURSE.get(course.getIdCourse()).isEmpty()) {
                            auxSec.setTeacher(teachers.get(Consultas.teachersCOURSE.get(course.getIdCourse()).get(0)));
                            auxSec.setIdTeacher(Consultas.teachersCOURSE.get(course.getIdCourse()).get(0));
                        }
                        auxSec.setClassId(sectionsModificadas.get(i).get(5));
//Aquí se le añade el id del Room para que posteriormente se le pueda añadir el nombre:                        
                        auxSec.setIdRoom(sectionsModificadas.get(i).get(6));
                        auxSec.setRoom(rooms.get(sectionsModificadas.get(i).get(6)));

                        if ((auxSec.getIdRoom() == 0 || course.getMaxChildPerSection() > auxSec.getRoom().getSize()) && !Consultas.countCourse2.get(course.getIdCourse()).isEmpty()) {
                            if (auxSec.getIdRoom() == 0) {
                                alert += "Room text field in " + sectionsModificadasFullName.get(i) + " is empty. <br/>";
                            }
                            if (auxSec.getIdRoom() != 0 && course.getMaxChildPerSection() > auxSec.getRoom().getSize()) {
                                alert += "Room "+auxSec.getRoom().getName()+" size in Section " + sectionsModificadasFullName.get(i) + " is smaller than Course " + CourseName.get(i) + " max size. <br/>";
                            }
                            auxSec.setRoom(rooms.get(Consultas.countCourse2.get(course.getIdCourse()).get(0)));
                            auxSec.setIdRoom(Consultas.countCourse2.get(course.getIdCourse()).get(0));

                            if (course.getMaxChildPerSection() > auxSec.getRoom().getSize()) {
                                alert += "Room "+auxSec.getRoom().getName()+" size in Course " + CourseName.get(i) + " is smaller than course max size too. <br/>";
                                if (!Consultas.countSchool.get(course.getIdCourse()).isEmpty()) {
                                    auxSec.setRoom(rooms.get(Consultas.countSchool.get(course.getIdCourse()).get(0)));
                                     auxSec.setIdRoom(Consultas.countSchool.get(course.getIdCourse()).get(0));
                                }
                               
                            }

                        } else if ((auxSec.getIdRoom() == 0 || course.getMaxChildPerSection() > auxSec.getRoom().getSize()) && !Consultas.countSchool.get(course.getIdCourse()).isEmpty()) {
                            auxSec.setRoom(rooms.get(Consultas.countSchool.get(course.getIdCourse()).get(0)));
                            auxSec.setIdRoom(Consultas.countSchool.get(course.getIdCourse()).get(0));
                            if (auxSec.getIdRoom() == 0) {
                                alert += "Room text fields in " + sectionsModificadasFullName.get(i) + " and " + CourseName.get(i) + " are empty. <br/>";
                            } else if (course.getMaxChildPerSection() > auxSec.getRoom().getSize()) {
                                alert += "Room "+auxSec.getRoom().getName()+" size in Course " + CourseName.get(i) + " is smaller than course max size. <br/>";
                            }
                        }

//                        consulta = " select sr.studentid\n"
//                + "    from studentrequests sr\n"
//                + "    inner join person p\n"
//                + "        on p.PersonID = sr.StudentID\n"
//                + "    inner join courses c\n"
//                + "        on c.CourseID = sr.courseid\n"
//                + "    inner join students s\n"
//                + "        on s.studentid = sr.StudentID\n"
//                + "        and p.personid = s.studentid\n"
//                + "    inner join person_student ps\n"
//                + "        on ps.StudentID = s.StudentID\n"
//                + "        and ps.StudentID = p.PersonID\n"
//                + "        and ps.StudentID = sr.StudentID"
//                + "        where sr.yearid =" + yearID
//                + "        and ps.SchoolCode = '" + schoolCode + "'"
//                + "        and c.CourseId= "+course.getIdCourse();
//                        rs = DBConnect.renweb.executeQuery(consulta);
//Para añadir a arrayStud2 los alumnos que hay en la sección y en el curso (se coge los que coinciden en ambos, arrayStud son los que están en el roster de las secciones): 
//Esto se hace para que no de error, porque si se cogieran alumnos de la seccion que no se encuentran en el Request del curso daría fallo en todos los cursos (la obtención de datos
//se realiza de forma global):
                        for (int j = 0; j < stCourse.get(idCourse).size(); j++) {

                            if (arrayStud.contains(stCourse.get(idCourse).get(j))) {
                                arrayStud2.add(stCourse.get(idCourse).get(j));
                            }
                        }
                        //     if(arrayStud.contains(rs.getInt(1)))
                        //    arrayStud2.add(rs.getInt(1));

//Con este for se cogen los alumnos que están en el roster de la sección pero no se encuentran en el request del curso (el arrayStuderroneos se utiliza para lanzar un mensaje al usuario si
//existen alumnos que no pertenecen al curso en sección, y así el usuario será consciente de que debe cambiarlo):
                        for (Integer arrayStud1 : arrayStud) {

                            if (!arrayStud2.contains(arrayStud1)) {
                                arrayStuderroneos.add(this.nameCourses.get(course.getIdCourse()) + ";" + sectionsModificadasFullName.get(i) + ";" + this.namePersons.get(arrayStud1) + ";");
                            }

                        }

                        auxSec.setIdStudents((ArrayList<Integer>) arrayStud2.clone());

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
                                rooms.get(auxSec.getIdRoom()).ocuparHueco(course.getIdCourse(), auxSec.getNumSeccion(), auxTupla);
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
                System.err.println("");
            }
        }
//Gracias a este treemap se puede visualizar por pantalla de forma correcta si hay alumnos que están asignados en secciones de un curso(roster de class section en renweb),
//pero que no están asignados en el propio curso(requests en courses de renweb):
//NOTA: lo que hace es visualizarlo de forma correcta, pero los datos ya han sido obtenidos previamente en arrayStuderroneos.
//La visualización correcta consiste en que no se repiten los nombres de los cursos y las secciones cuando se van a 
//visualizar los estudiantes en cuestión que están mal asignados a seccion/es.
//-----------PENDIENTE REALIZAR ESTO EN LA VISTA-(AUNQUE HOY POR HOY FUNCIONA PERFECTAMENTE)-----
        if (!Consultas.arrayStuderroneos.isEmpty()) {
            studE = "";
            for (String array : Consultas.arrayStuderroneos) {
                String[] hueco = array.split(";");
                String p1 = hueco[0];
                String p2 = hueco[1];
                String p3 = hueco[2];
                if (!studError.containsKey(p1)) {
                    studError.put(p1, new TreeMap<>());
                    studError.get(p1).put(p2, new ArrayList<>());
                    studError.get(p1).get(p2).add(p3);
                    studE = studE.concat("<br/>" + p1 + ": " + "<br/>" + p2 + ": " + "<br/>" + p3);

                } else if (!studError.get(p1).containsKey(p2)) {
                    studError.get(p1).put(p2, new ArrayList<>());
                    studError.get(p1).get(p2).add(p3);
                    studE = studE.concat("<br/>" + p2 + ": " + "<br/>" + p3);
                } else {
                    studError.get(p1).get(p2).add(p3);
                    studE = studE.concat("; " + p3);
                }

            }
 
        }
  

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

//OWN:Se obvia esta conexion porque ya no se usa la cuenta de EEUU:    
/*
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
     */
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

//    private String getWhereTemplate(int[] tempInfo, String MS_Column) {
//        if (tempInfo[0] == 0 && tempInfo[1] == 0 && tempInfo[2] == 0 && tempInfo[3] == 0) {
//            return " Elementary=0 and HS=0 and " + MS_Column + "=0 and PreSchool=0 ";
//        } else if (tempInfo[0] == 0 && tempInfo[1] == 0 && tempInfo[2] == 0 && tempInfo[3] == 1) {
//            return " Elementary=0 and HS=0 and " + MS_Column + "=0 and PreSchool=1 ";
//        } else if (tempInfo[0] == 0 && tempInfo[1] == 0 && tempInfo[2] == 1 && tempInfo[3] == 0) {
//            return " Elementary=0 and HS=0 and " + MS_Column + "=1 and PreSchool=0 ";
//        } else if (tempInfo[0] == 0 && tempInfo[1] == 0 && tempInfo[2] == 1 && tempInfo[3] == 1) {
//            return " Elementary=0 and HS=0 and ( " + MS_Column + "=1 or PreSchool=1 ) ";
//        } else if (tempInfo[0] == 0 && tempInfo[1] == 1 && tempInfo[2] == 0 && tempInfo[3] == 0) {
//            return " Elementary=0 and HS=1 and " + MS_Column + "=0 and PreSchool=0 ";
//        } else if (tempInfo[0] == 0 && tempInfo[1] == 1 && tempInfo[2] == 0 && tempInfo[3] == 1) {
//            return " Elementary=0  and " + MS_Column + "=0 and (PreSchool=1 or HS=1) ";
//        } else if (tempInfo[0] == 0 && tempInfo[1] == 1 && tempInfo[2] == 1 && tempInfo[3] == 0) {
//            return " Elementary=0 and (HS=1 or " + MS_Column + "=1) and PreSchool=0 ";
//        } else if (tempInfo[0] == 0 && tempInfo[1] == 1 && tempInfo[2] == 1 && tempInfo[3] == 1) {
//            return " Elementary=0 and (HS=1 or " + MS_Column + "=1 or PreSchool=1) ";
//        } else if (tempInfo[0] == 1 && tempInfo[1] == 0 && tempInfo[2] == 0 && tempInfo[3] == 0) {
//            return " Elementary=1 and HS=0 and " + MS_Column + "=0 and PreSchool=0 ";
//        } else if (tempInfo[0] == 1 && tempInfo[1] == 0 && tempInfo[2] == 0 && tempInfo[3] == 1) {
//            return "  HS=0 and " + MS_Column + "=0 and (Elementary=1 or PreSchool=1) ";
//        } else if (tempInfo[0] == 1 && tempInfo[1] == 0 && tempInfo[2] == 1 && tempInfo[3] == 0) {
//            return " (Elementary=1 and " + MS_Column + "=1) and HS=0  and PreSchool=0 ";
//        } else if (tempInfo[0] == 1 && tempInfo[1] == 0 && tempInfo[2] == 1 && tempInfo[3] == 1) {
//            return "  HS=0 and (Elementary=1 or " + MS_Column + "=1 or PreSchool=1) ";
//        } else if (tempInfo[0] == 1 && tempInfo[1] == 1 && tempInfo[2] == 0 && tempInfo[3] == 0) {
//            return " ( Elementary=1 or HS=1 ) and " + MS_Column + "=0 and PreSchool=0 ";
//        } else if (tempInfo[0] == 1 && tempInfo[1] == 1 && tempInfo[2] == 0 && tempInfo[3] == 1) {
//            return " ( Elementary=1 or HS=1 or PreSchool=1 )and " + MS_Column + "=0 ";
//        } else if (tempInfo[0] == 1 && tempInfo[1] == 1 && tempInfo[2] == 1 && tempInfo[3] == 0) {
//            return " (Elementary=1 or HS=1 or " + MS_Column + "=1) and PreSchool=0 ";
//        } else {
//            return " (Elementary=1 or HS=1 or " + MS_Column + "=1 or PreSchool=1) ";
//
//        }
//    }
        
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
