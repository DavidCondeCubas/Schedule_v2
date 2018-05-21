  /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataManage;

/**
 *
 * @author Chema
 */
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
 
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import model.Student;
import model.Teacher;
 
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
 
 
public class XMLWriterDOM {
 
    public static void xmlCreate(ArrayList<Teacher> teachers ,ArrayList<Student> st) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();
            //add elements to Document
            Element rootElement =
                doc.createElement("Schedule");
            //append root element to document
            doc.appendChild(rootElement);
            
            
            ///TEACHERS
            Element teachersxml =
                doc.createElement("Teachers");
            //append root element to document
            rootElement.appendChild(teachersxml);
            
            //append first child element to root element
            for(Teacher t:teachers)
                teachersxml.appendChild(getTeacher(doc,t));
 
            
            ///STUDENTS
            Element studentsxml =
                doc.createElement("Students");
            //append root element to document
            rootElement.appendChild(studentsxml);
            for(Student t:st)
                studentsxml.appendChild(getStudent(doc,t));
            
            
            ///TIME TABLES TEACHERS
            Element teacherTimetablesxml =
                doc.createElement("TeacherTimetable");
            rootElement.appendChild(teacherTimetablesxml);
            
            for(Teacher t:teachers)
                teacherTimetablesxml.appendChild(getTimetableLine("Teacher",doc,t.getIdTeacher()+"",t.getAllPosiciones(),t.getHuecos()));
            
            //TIME TABLES STUDENTS
            Element studenTimetablexml =
                doc.createElement("StudentTimetable");
            rootElement.appendChild(studenTimetablexml);
            
            for(Student t:st)
                studenTimetablexml.appendChild(getTimetableLine("Student",doc,t.getId()+"",t.posicionesOcupadas(),t.getHuecos()));
            
            
            
            
            
            //for output to file, console
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            //for pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
 
            //write to console or file
            StreamResult console = new StreamResult(System.out);
            StreamResult file = new StreamResult(new File("/Users/Norhan/Documents/emps.xml"));
 
            //write data
            transformer.transform(source, console);
            transformer.transform(source, file);
            System.out.println("DONE");
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
 
    private static Node getPerson(String type,Document doc, String id, String clase) {
        Element employee = doc.createElement(type);
 
        //set id attribute
        employee.setAttribute("id", id);
 
        //create name element
        employee.appendChild(getEmployeeElements(doc, employee, "course", clase));
        
        return employee;
    }
    
    private static Node getStudent(Document doc,Student st) {
        Element employee = doc.createElement("Student");
 
        //set id attribute
        employee.setAttribute("id", st.getId()+"");
 
        //create name element
        employee.appendChild(getEmployeeElements(doc, employee, "course", st.getCursosAsignados().toString()));
        employee.appendChild(getEmployeeElements(doc, employee, "courseno", st.getCursosNoAsignados().toString()));
        employee.appendChild(getEmployeeElements(doc, employee, "genero", st.getGenero()));
        
        return employee;
    }
    //pg_dump -s -C -h localhost -U eduweb LessonsDemo | psql -h 192.168.1.9 -U eduweb Lessons

    
    private static Node getTeacher(Document doc, Teacher t) {
        Element employee = doc.createElement("Teacher");
 
        //set id attribute
        employee.setAttribute("id", t.getIdTeacher()+"");
        //create name element
        employee.appendChild(getEmployeeElements(doc, employee, "course", t.getPrepsComplete().toString()));
        employee.appendChild(getEmployeeElements(doc, employee, "secciones", t.getMaxSections()+""));
        employee.appendChild(getEmployeeElements(doc, employee, "maxpreps", t.getPreps()+""));
        employee.appendChild(getEmployeeElements(doc, employee, "maxbxd", t.getMaxBxD()+""));
        employee.appendChild(getEmployeeElements(doc, employee, "excludeblocks", t.getExcludeBlocks()+""));
        
        return employee;
    }
    
    private static Node getTimetableLine(String type,Document doc, String id,ArrayList<Tupla<Integer,Integer>> table,int[][] huecos) {
        Element employee = doc.createElement("timeLine"+type);
 
        //set id attribute
        employee.setAttribute("id", id);
        HashMap<Integer,String> secciones = new HashMap();
        //create name element
        for(Tupla<Integer,Integer> t : table){
            int seccion = huecos[t.x][t.y];
            if(secciones.containsKey(seccion))
                secciones.put(seccion,secciones.get(seccion).concat("-"+t.x+","+t.y));
            else{
                secciones.put(seccion,t.x+","+t.y);
            }
        }
        String s = "";
        boolean first = true;
        for(Map.Entry<Integer, String> entry : secciones.entrySet()){
            if(first)
                s += "["+entry.getKey()+"="+entry.getValue()+"]";
            else
                s += "&["+entry.getKey()+"="+entry.getValue()+"]";
            first = false;
        }
        employee.appendChild(getEmployeeElements(doc, employee, "seccion",s));
        return employee;
    }
 
    
 
    //utility method to create text node
    private static Node getEmployeeElements(Document doc, Element element, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }
 
}
