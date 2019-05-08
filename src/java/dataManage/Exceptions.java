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
import java.util.TreeMap;
import model.Course;
import model.Room;
import model.Seccion;
import model.Teacher;

/**
 *
 * @author migue
 */
public class Exceptions {

    ArrayList<String> avisosSectionRoomsAvailable;
    ArrayList<String> avisosSectionRoomsSize;
    ArrayList<String> avisosSectionRoomsFull;
    ArrayList<String> avisosCourseRoomsAvailable;
    HashMap<String, ArrayList<String>> avisosCourseRoomsSize;
    HashMap<String, ArrayList<String>> avisosCourseRoomsFull;
    ArrayList<String> avisosSchoolRoomsAvailable;
    HashMap<String, ArrayList<String>> avisosSchoolRoomsSize;
    HashMap<String, ArrayList<String>> avisosSchoolRoomsFull;
    ArrayList<String> roomsFull;

//-------------------------------------------------
    ArrayList<String> avisosSectionTeachersAvailable;
    ArrayList<String> avisosCourseTeachersAvailable;
    ArrayList<String> avisosSectionTeachersFull;
    HashMap<String, ArrayList<String>> avisosCourseTeachersFull;
    HashMap<String, ArrayList<String>> avisosCourseTeachersEB;
    ArrayList<String> teachersFull;
//-------------------------------------------------
    HashMap<Integer, String> coursesWithoutStudents;
    TreeMap<String, ArrayList<String>> templateIdSection;
    HashMap<String, HashMap<String, HashMap<Integer, String>>> difStudents;
    HashMap<String, ArrayList<String>> avisoMinStudents;
    HashMap<String, ArrayList<String>> avisoWithoutPatterns;
    ArrayList<String> courseWithoutSections;
    ArrayList<String> avisoMaxSizePerSectionCourse;
    ArrayList<String> avisoMaxSizePerSectionSchool;
    ArrayList<String> avisoMinSizePerSectionCourse;
    ArrayList<String> avisoMinSizePerSectionSchool;
    ArrayList<String> avisoCourseWithoutTemplate;
    ArrayList<String> avisoSchoolWithoutScheduleActive;
    ArrayList<String> avisoWitouthMatches;
    HashMap<String, ArrayList<String>> avisoCadena;

    public Exceptions() {

        avisosSchoolRoomsAvailable = new ArrayList();
        avisosSchoolRoomsSize = new HashMap();
        avisosSchoolRoomsFull = new HashMap();
        avisosCourseRoomsAvailable = new ArrayList();
        avisosCourseRoomsSize = new HashMap();
        avisosCourseRoomsFull = new HashMap();
        avisosSectionRoomsAvailable = new ArrayList();
        avisosSectionRoomsSize = new ArrayList();
        avisosSectionRoomsFull = new ArrayList();
        roomsFull = new ArrayList();

//---------------------------------------------
        avisosSectionTeachersAvailable = new ArrayList();
        avisosCourseTeachersAvailable = new ArrayList();
        avisosSectionTeachersFull = new ArrayList();
        avisosCourseTeachersFull = new HashMap();
        avisosCourseTeachersEB = new HashMap();
        teachersFull = new ArrayList();
//---------------------------------------------
        coursesWithoutStudents = new HashMap();
        templateIdSection = new TreeMap();
        difStudents = new HashMap();
        avisoMinStudents = new HashMap();
        avisoWithoutPatterns = new HashMap();
        courseWithoutSections = new ArrayList();
        avisoMaxSizePerSectionCourse = new ArrayList();
        avisoMaxSizePerSectionSchool = new ArrayList();
        avisoMinSizePerSectionCourse = new ArrayList();
        avisoMinSizePerSectionSchool = new ArrayList();
        avisoCourseWithoutTemplate = new ArrayList();
        avisoCadena = new HashMap();
        avisoSchoolWithoutScheduleActive = new ArrayList();
        avisoWitouthMatches = new ArrayList();

    }

    public HashMap<Integer, String> getCoursesWithoutStudents() {
        return coursesWithoutStudents;
    }

    public TreeMap<String, ArrayList<String>> getTemplateIdSection() {
        return templateIdSection;
    }

    public void setTemplateIdSection(TreeMap<String, ArrayList<String>> templateIdSection) {
        this.templateIdSection = templateIdSection;
    }

    public HashMap<String, HashMap<String, HashMap<Integer, String>>> getDifStudents() {
        return difStudents;
    }

    public void setDifStudents(HashMap<String, HashMap<String, HashMap<Integer, String>>> difStudents) {
        this.difStudents = difStudents;
    }

    public void addDifStudents(String course, String section, Integer idStudent, String student) {
        if (!this.difStudents.containsKey(course)) {
            this.difStudents.put(course, new HashMap());
        }
        if (!this.difStudents.get(course).containsKey(section)) {
            this.difStudents.get(course).put(section, new HashMap());
        }
        this.difStudents.get(course).get(section).put(idStudent, student);
    }

    public HashMap<String, ArrayList<String>> getAvisoMinStudents() {
        return avisoMinStudents;
    }

    public void setAvisoMinStudents(HashMap<String, ArrayList<String>> avisoMinStudents) {
        this.avisoMinStudents = avisoMinStudents;
    }

    public void addAvisoMinStudents(String course, String section) {
        if (!this.avisoMinStudents.containsKey(course)) {
            this.avisoMinStudents.put(course, new ArrayList());
        }
        if (!avisoMinStudents.get(course).contains(section)) {
            this.avisoMinStudents.get(course).add(section);
        }

    }

    public HashMap<String, ArrayList<String>> getAvisoWithoutPatterns() {
        return avisoWithoutPatterns;
    }

    public void setAvisoWithoutPatterns(HashMap<String, ArrayList<String>> avisoWithoutPatterns) {
        this.avisoWithoutPatterns = avisoWithoutPatterns;
    }

    public void addAvisoWithoutPatterns(String course, String section) {
        if (!this.avisoWithoutPatterns.containsKey(course)) {
            this.avisoWithoutPatterns.put(course, new ArrayList());
        }
        if (!avisoWithoutPatterns.get(course).contains(section)) {
            this.avisoWithoutPatterns.get(course).add(section);
        }

    }

    public ArrayList<String> getCourseWithoutSections() {
        return courseWithoutSections;
    }

    public void setCourseWithoutSections(ArrayList<String> courseWithoutSections) {
        this.courseWithoutSections = courseWithoutSections;
    }

    public void addCourseWithoutSections(Course c) {
        if (this.courseWithoutSections.isEmpty()) {
            this.courseWithoutSections.add("There is no sections generated for this/these courses (Schedule Web generate automatically default sections):");
        }
        if (!this.courseWithoutSections.contains(c.getNameCourse())) {
            this.courseWithoutSections.add(c.getNameCourse());
        }
    }

    public ArrayList<String> getAvisoMaxSizePerSectionCourse() {
        return avisoMaxSizePerSectionCourse;
    }

    public void setAvisoMaxSizePerSectionCourse(ArrayList<String> avisoMaxSizePerSectionCourse) {
        this.avisoMaxSizePerSectionCourse = avisoMaxSizePerSectionCourse;
    }

    public ArrayList<String> getAvisoMaxSizePerSectionSchool() {
        return avisoMaxSizePerSectionSchool;
    }

    public void setAvisoMaxSizePerSectionSchool(ArrayList<String> avisoMaxSizePerSectionSchool) {
        this.avisoMaxSizePerSectionSchool = avisoMaxSizePerSectionSchool;
    }

    public ArrayList<String> getAvisoMinSizePerSectionCourse() {
        return avisoMinSizePerSectionCourse;
    }

    public void setAvisoMinSizePerSectionCourse(ArrayList<String> avisoMinSizePerSectionCourse) {
        this.avisoMinSizePerSectionCourse = avisoMinSizePerSectionCourse;
    }

    public ArrayList<String> getAvisoMinSizePerSectionSchool() {
        return avisoMinSizePerSectionSchool;
    }

    public void setAvisoMinSizePerSectionSchool(ArrayList<String> avisoMinSizePerSectionSchool) {
        this.avisoMinSizePerSectionSchool = avisoMinSizePerSectionSchool;
    }

    public void addAvisoMaxSizePerSectionCourse(String course) {
        if (!avisoMaxSizePerSectionCourse.contains("There is no right config for MaxSizePerSection in Course/s (the value of the School Config will be taken):")) {
            avisoMaxSizePerSectionCourse.add("There is no right config for MaxSizePerSection in Course/s (the value of the School Config will be taken):");
        }
        if (!avisoMaxSizePerSectionCourse.contains(course)) {
            avisoMaxSizePerSectionCourse.add(course);
        }
    }

    public void addAvisoMaxSizePerSectionSchool(String schoolCode) {
        if (!avisoMaxSizePerSectionSchool.contains("There is no right config for MaxSizePerSection in School Config (default value=25):")) {
            avisoMaxSizePerSectionSchool.add("There is no right config for MaxSizePerSection in School Config (default value=25):");
        }
        if (!avisoMaxSizePerSectionSchool.contains(schoolCode)) {
            avisoMaxSizePerSectionSchool.add(schoolCode);
        }
    }

    public void addAvisoMinSizePerSectionCourse(String course) {
        if (!avisoMinSizePerSectionCourse.contains("There is no right config for MinSizePerSection in Course/s(the value of the School Config will be taken):")) {
            avisoMinSizePerSectionCourse.add("There is no right config for MinSizePerSection in Course/s(the value of the School Config will be taken):");
        }
        if (!avisoMinSizePerSectionCourse.contains(course)) {
            avisoMinSizePerSectionCourse.add(course);
        }
    }

    public void addAvisoMinSizePerSectionSchool(String schoolCode) {
        if (!avisoMinSizePerSectionSchool.contains("There is no right config for MaxSizePerSection in School Config (default value=50% of MaxSizePerSection):")) {
            avisoMinSizePerSectionSchool.add("There is no right config for MaxSizePerSection in School Config (default value=50% of MaxSizePerSection):");
        }
        if (!avisoMinSizePerSectionSchool.contains(schoolCode)) {
            avisoMinSizePerSectionSchool.add(schoolCode);
        }
    }

    public ArrayList<String> getAvisoCourseWithoutTemplate() {
        return avisoCourseWithoutTemplate;
    }

    public void setAvisoCourseWithoutTemplate(ArrayList<String> avisoCourseWithoutTemplate) {
        this.avisoCourseWithoutTemplate = avisoCourseWithoutTemplate;
    }

    public HashMap<String, ArrayList<String>> getAvisoCadena() {
        return avisoCadena;
    }

    public void setAvisoCadena(HashMap<String, ArrayList<String>> avisoCadena) {
        this.avisoCadena = avisoCadena;
    }

    public ArrayList<String> getAvisoSchoolWithoutScheduleActive() {
        return avisoSchoolWithoutScheduleActive;
    }

    public void setAvisoSchoolWithoutScheduleActive(ArrayList<String> avisoSchoolWithoutScheduleActive) {
        this.avisoSchoolWithoutScheduleActive = avisoSchoolWithoutScheduleActive;
    }

    public ArrayList<String> getAvisoWitouthMatches() {
        return avisoWitouthMatches;
    }

    public void setAvisoWitouthMatches(ArrayList<String> avisoWitouthMatches) {
        this.avisoWitouthMatches = avisoWitouthMatches;
    }

//---------------------------------------------------------
//GETTERS Y SETTERS
//---------------------------------------------------------
    public void setCoursesWithoutStudents(HashMap<Integer, String> coursesWithoutStudents) {
        this.coursesWithoutStudents = coursesWithoutStudents;
    }

    public ArrayList<String> getRoomsFull() {
        return roomsFull;
    }

    public void setRoomsFull(ArrayList<String> roomsFull) {
        this.roomsFull = roomsFull;
    }

    public void addRoomsFull(String roomFull) {
        this.roomsFull.add(roomFull);
    }

    public ArrayList<String> getTeachersFull() {
        return teachersFull;
    }

    public void setTeachersFull(ArrayList<String> teachersFull) {
        this.teachersFull = teachersFull;
    }

    public void addTeachersFull(String teacherFull) {
        this.teachersFull.add(teacherFull);
    }

    public void addAvisoCadena(String course, String campo, String cadena) {

        if (avisoCadena.isEmpty()) {
            avisoCadena.put("Title", new ArrayList());
            avisoCadena.get("Title").add("There are fields with wrong configuration in Course User Defined:");
        }
        if (!avisoCadena.containsKey(course)) {
            avisoCadena.put(course, new ArrayList());
            avisoCadena.get(course).add(campo + " has wrong configuration: " + cadena);
        } else {
            avisoCadena.get(course).add(campo + " has wrong configuration: " + cadena);
        }
    }
//---------------------ROOMS------------------------------
//--------------------------------------------------------
//--------------------------------------------------------
//--------------------------------------------------------
//--------------------------------------------------------       

    public ArrayList<String> getAvisosSectionRoomsAvailable() {
        return avisosSectionRoomsAvailable;
    }

    public void setAvisosSectionRoomsAvailable(ArrayList<String> avisosSectionRoomsAvailable) {
        this.avisosSectionRoomsAvailable = avisosSectionRoomsAvailable;
    }

    public ArrayList<String> getAvisosSectionRoomsSize() {
        return avisosSectionRoomsSize;
    }

    public void setAvisosSectionRoomsSize(ArrayList<String> avisosSectionRoomsSize) {
        this.avisosSectionRoomsSize = avisosSectionRoomsSize;
    }

    public ArrayList<String> getAvisosSectionRoomsFull() {
        return avisosSectionRoomsFull;
    }

    public void setAvisosSectionRoomsFull(ArrayList<String> avisosSectionRoomsFull) {
        this.avisosSectionRoomsFull = avisosSectionRoomsFull;
    }

    public ArrayList<String> getAvisosCourseRoomsAvailable() {
        return avisosCourseRoomsAvailable;
    }

    public void setAvisosCourseRoomsAvailable(ArrayList<String> avisosCourseRoomsAvailable) {
        this.avisosCourseRoomsAvailable = avisosCourseRoomsAvailable;
    }

    public HashMap<String, ArrayList<String>> getAvisosCourseRoomsSize() {
        return avisosCourseRoomsSize;
    }

    public void setAvisosCourseRoomsSize(HashMap<String, ArrayList<String>> avisosCourseRoomsSize) {
        this.avisosCourseRoomsSize = avisosCourseRoomsSize;
    }

    public HashMap<String, ArrayList<String>> getAvisosCourseRoomsFull() {
        return avisosCourseRoomsFull;
    }

    public void setAvisosCourseRoomsFull(HashMap<String, ArrayList<String>> avisosCourseRoomsFull) {
        this.avisosCourseRoomsFull = avisosCourseRoomsFull;
    }

    public ArrayList<String> getAvisosSchoolRoomsAvailable() {
        return avisosSchoolRoomsAvailable;
    }

    public void setAvisosSchoolRoomsAvailable(ArrayList<String> avisosSchoolRoomsAvailable) {
        this.avisosSchoolRoomsAvailable = avisosSchoolRoomsAvailable;
    }

    public HashMap<String, ArrayList<String>> getAvisosSchoolRoomsSize() {
        return avisosSchoolRoomsSize;
    }

    public void setAvisosSchoolRoomsSize(HashMap<String, ArrayList<String>> avisosSchoolRoomsSize) {
        this.avisosSchoolRoomsSize = avisosSchoolRoomsSize;
    }

    public HashMap<String, ArrayList<String>> getAvisosSchoolRoomsFull() {
        return avisosSchoolRoomsFull;
    }

    public void setAvisosSchoolRoomsFull(HashMap<String, ArrayList<String>> avisosSchoolRoomsFull) {
        this.avisosSchoolRoomsFull = avisosSchoolRoomsFull;
    }

//---------------------------------------------------------
//GETTERS Y SETTERS (FIN)
//---------------------------------------------------------
//--------------------------SECTION---------------------
    public void addAvisosSectionRoomsAvailable(Course c, Seccion currentSec) {
        if (this.avisosSectionRoomsAvailable.isEmpty()) {
            this.avisosSectionRoomsAvailable.add("There is no Section Room available in:");
        }
        if (!avisosSectionRoomsAvailable.contains(c.getNameCourse() + ":" + currentSec.getNameSeccion())) {
            this.avisosSectionRoomsAvailable.add(c.getNameCourse() + ":" + currentSec.getNameSeccion());
        }
    }

    public void addAvisosSectionRoomsSize(Course c, Seccion currentSec, String room) {
        if (this.avisosSectionRoomsSize.isEmpty()) {
            this.avisosSectionRoomsSize.add("Section Room size is not enough in:");
        }
        if (!avisosSectionRoomsSize.contains(c.getNameCourse() + " Section " + currentSec.getNameSeccion() + ": " + room)) {
            this.avisosSectionRoomsSize.add(c.getNameCourse() + " Section " + currentSec.getNameSeccion() + ": " + room);
        }
    }

    public void addAvisosSectionRoomsFull(Course c, Seccion currentSec, String room) {
        if (this.avisosSectionRoomsFull.isEmpty()) {
            this.avisosSectionRoomsFull.add("Section Room full:");
        }
        if (!avisosSectionRoomsFull.contains(c.getNameCourse() + " Section " + currentSec.getNameSeccion() + ": " + room)) {
            this.avisosSectionRoomsFull.add(c.getNameCourse() + " Section " + currentSec.getNameSeccion() + ": " + room);
        }
    }

//--------------------------COURSE---------------------
    public void addAvisosCourseRoomsAvailable(Course c) {
        if (this.avisosCourseRoomsAvailable.isEmpty()) {
            this.avisosCourseRoomsAvailable.add("There is no Course Rooms available in ");
        }
        if (!this.avisosCourseRoomsAvailable.contains(c.getNameCourse())) {
            this.avisosCourseRoomsAvailable.add(c.getNameCourse());
        }
    }

    public void addAvisosCourseRoomsSize(Course c, String room) {
        if (this.avisosCourseRoomsSize.isEmpty()) {
            this.avisosCourseRoomsSize.put("Title", new ArrayList());
            this.avisosCourseRoomsSize.get("Title").add("Course Room/s size is not enough in:");
        }
        if (!this.avisosCourseRoomsSize.containsKey(c.getNameCourse())) {
            this.avisosCourseRoomsSize.put(c.getNameCourse(), new ArrayList());
        }
        if (!this.avisosCourseRoomsSize.get(c.getNameCourse()).contains(room)) {
            this.avisosCourseRoomsSize.get(c.getNameCourse()).add(room);
        }

    }

    public void addAvisosCourseRoomsFull(Course c, String room) {
        if (this.avisosCourseRoomsFull.isEmpty()) {
            this.avisosCourseRoomsFull.put("Title", new ArrayList());
            this.avisosCourseRoomsFull.get("Title").add("Course Room/s full:");
        }
        if (!this.avisosCourseRoomsFull.containsKey(c.getNameCourse())) {
            this.avisosCourseRoomsFull.put(c.getNameCourse(), new ArrayList());
        }
        if (!this.avisosCourseRoomsFull.get(c.getNameCourse()).contains(room)) {
            this.avisosCourseRoomsFull.get(c.getNameCourse()).add(room);
        }

    }

//--------------------------SCHOOL---------------------
    public void addAvisosSchoolRoomsAvailable(String schoolCode) {
        if (!this.avisosSchoolRoomsAvailable.contains("There is no School Rooms in RenWeb Configuration:")) {
            this.avisosSchoolRoomsAvailable.add("There is no School Rooms in RenWeb Configuration:");
        }
        if (!this.avisosSchoolRoomsAvailable.contains(schoolCode)) {
            this.avisosSchoolRoomsAvailable.add(schoolCode);
        }
    }

    public void addAvisosSchoolRoomsSize(String templateId, String room) {
        if (this.avisosSchoolRoomsSize.isEmpty()) {
            this.avisosSchoolRoomsSize.put("Title", new ArrayList());
            this.avisosSchoolRoomsSize.get("Title").add("School Room/s size is not enough in:");
        }
        if (!this.avisosSchoolRoomsSize.containsKey(templateId)) {
            this.avisosSchoolRoomsSize.put(templateId, new ArrayList());
        }
        if (this.avisosSchoolRoomsSize.get(templateId).contains(room)) {
            this.avisosSchoolRoomsSize.get(templateId).add(room);
        }

    }

    public void addAvisosSchoolRoomsFull(String templateId, String room) {
        if (this.avisosSchoolRoomsFull.isEmpty()) {
            this.avisosSchoolRoomsFull.put("Title", new ArrayList());
            this.avisosSchoolRoomsFull.get("Title").add("School Room/s full:");
        }
        if (!this.avisosSchoolRoomsFull.containsKey(templateId)) {
            this.avisosSchoolRoomsFull.put(templateId, new ArrayList());
        }
        if (this.avisosSchoolRoomsFull.get(templateId).contains(room)) {
            this.avisosSchoolRoomsFull.get(templateId).add(room);
        }
    }
//---------------------------------------------------------------------------

    public void getRoomSection(HashMap<Integer, Room> rooms, ArrayList<Room> roomForCourse, Course c, Seccion currentSec, String templateId, HashMap<String, ArrayList<Integer>> groupRooms, String schoolCode) {

        if (rooms.get(currentSec.getIdRoom()).getDisponibilidad() > 0 && c.getMaxChildPerSection() <= currentSec.getRoom().getSize()) {
            roomForCourse.add(rooms.get(currentSec.getIdRoom()));
        } else {
            if (rooms.get(currentSec.getIdRoom()).getDisponibilidad() <= 0) {
                if (!roomsFull.contains(rooms.get(currentSec.getIdRoom()).getName())) {
                    roomsFull.add(rooms.get(currentSec.getIdRoom()).getName());
                }
                addAvisosSectionRoomsFull(c, currentSec, rooms.get(currentSec.getIdRoom()).getName());
            }
            if (c.getMaxChildPerSection() > currentSec.getRoom().getSize()) {
                addAvisosSectionRoomsSize(c, currentSec, rooms.get(currentSec.getIdRoom()).getName());
            }
        }
        if (roomForCourse.isEmpty()) {
            getRoomCourse(rooms, roomForCourse, c, currentSec, templateId, groupRooms, schoolCode);
        }

    }

    public void getRoomCourse(HashMap<Integer, Room> rooms, ArrayList<Room> roomForCourse, Course c, Seccion currentSec, String templateId, HashMap<String, ArrayList<Integer>> groupRooms, String schoolCode) {
        if (!c.getRooms().isEmpty()) {
            for (Integer idRoom : c.getRooms()) {
                if (rooms.get(idRoom).getDisponibilidad() > 0 && c.getMaxChildPerSection() <= rooms.get(idRoom).getSize()) {
                    roomForCourse.add(rooms.get(idRoom));
                } else {
                    if (rooms.get(idRoom).getDisponibilidad() <= 0) {
                        if (!roomsFull.contains(rooms.get(idRoom).getName())) {
                            roomsFull.add(rooms.get(idRoom).getName());
                        }
                        addAvisosCourseRoomsFull(c, rooms.get(idRoom).getName());
                    }
                    if (c.getMaxChildPerSection() > rooms.get(idRoom).getSize()) {
                        addAvisosCourseRoomsSize(c, rooms.get(idRoom).getName());
                    }
                }
            }
        } else {
            addAvisosCourseRoomsAvailable(c);
        }

        if (roomForCourse.isEmpty()) {
            getRoomSchool(rooms, roomForCourse, c, currentSec, templateId, groupRooms, schoolCode);
        }
    }

    public void getRoomSchool(HashMap<Integer, Room> rooms, ArrayList<Room> roomForCourse, Course c, Seccion currentSec, String templateId, HashMap<String, ArrayList<Integer>> groupRooms, String schoolCode) {
        if (groupRooms.containsKey(templateId)) {
            ArrayList<Integer> roomsArray = groupRooms.get(templateId);
            for (Integer idRoom : roomsArray) {
                if (rooms.get(idRoom).getDisponibilidad() > 0 && c.getMaxChildPerSection() <= rooms.get(idRoom).getSize()) {
                    roomForCourse.add(rooms.get(idRoom));
                } else {
                    if (rooms.get(idRoom).getDisponibilidad() <= 0) {
                        if (!roomsFull.contains(rooms.get(idRoom).getName())) {
                            roomsFull.add(rooms.get(idRoom).getName());
                        }
                        addAvisosSchoolRoomsFull(templateId, rooms.get(idRoom).getName());
                    }
                    if (c.getMaxChildPerSection() > rooms.get(idRoom).getSize()) {
                        addAvisosSchoolRoomsSize(templateId, rooms.get(idRoom).getName());
                    }
                }
            }
        } else {
            addAvisosSchoolRoomsAvailable(schoolCode);
        }

    }
//---------------------TEACHERS---------------------------
//--------------------------------------------------------
//--------------------------------------------------------
//--------------------------------------------------------
//--------------------------------------------------------  

    public ArrayList<String> getAvisosSectionTeachersAvailable() {
        return avisosSectionTeachersAvailable;
    }

    public void setAvisosSectionTeachersAvailable(ArrayList<String> avisosSectionTeachersAvailable) {
        this.avisosSectionTeachersAvailable = avisosSectionTeachersAvailable;
    }

    public ArrayList<String> getAvisosCourseTeachersAvailable() {
        return avisosCourseTeachersAvailable;
    }

    public void setAvisosCourseTeachersAvailable(ArrayList<String> avisosCourseTeachersAvailable) {
        this.avisosCourseTeachersAvailable = avisosCourseTeachersAvailable;
    }

    public ArrayList<String> getAvisosSectionTeachersFull() {
        return avisosSectionTeachersFull;
    }

    public void setAvisosSectionTeachersFull(ArrayList<String> avisosSectionTeachersFull) {
        this.avisosSectionTeachersFull = avisosSectionTeachersFull;
    }

    public HashMap<String, ArrayList<String>> getAvisosCourseTeachersFull() {
        return avisosCourseTeachersFull;
    }

    public void setAvisosCourseTeachersFull(HashMap<String, ArrayList<String>> avisosCourseTeachersFull) {
        this.avisosCourseTeachersFull = avisosCourseTeachersFull;
    }

    public void addAvisosSectionTeachersAvailable(Course c, Seccion currentSec) {
        if (this.avisosSectionTeachersAvailable.isEmpty()) {
            this.avisosSectionTeachersAvailable.add("There is no Section Teacher available in:");
        }
        if (!avisosSectionTeachersAvailable.contains(c.getNameCourse() + ":" + currentSec.getNameSeccion())) {
            this.avisosSectionTeachersAvailable.add(c.getNameCourse() + ":" + currentSec.getNameSeccion());
        }
    }

    public void addAvisosCourseTeachersAvailable(Course c) {

        if (this.avisosCourseTeachersAvailable.isEmpty()) {
            this.avisosCourseTeachersAvailable.add("There is no Course Teachers available in ");
        }
        if (!this.avisosCourseTeachersAvailable.contains(c.getNameCourse())) {
            this.avisosCourseTeachersAvailable.add(c.getNameCourse());
        }
    }

    public void addAvisosSectionTeachersFull(Course c, Seccion currentSec, String Teacher) {
        if (this.avisosSectionTeachersFull.isEmpty()) {
            this.avisosSectionTeachersFull.add("Section Teacher full:");
        }
        if (!avisosSectionTeachersFull.contains(c.getNameCourse() + ":" + currentSec.getNameSeccion())) {
            this.avisosSectionTeachersFull.add(c.getNameCourse() + ":" + currentSec.getNameSeccion());
        }
    }

    public void addAvisosCourseTeachersFull(Course c, String teacher) {
        if (this.avisosCourseTeachersFull.isEmpty()) {
            this.avisosCourseTeachersFull.put("Title", new ArrayList());
            this.avisosCourseTeachersFull.get("Title").add("Course Teacher/s full:");
        }
        if (!this.avisosCourseTeachersFull.containsKey(c.getNameCourse())) {
            this.avisosCourseTeachersFull.put(c.getNameCourse(), new ArrayList());
        }
        if (!this.avisosCourseTeachersFull.get(c.getNameCourse()).contains(teacher)) {
            this.avisosCourseTeachersFull.get(c.getNameCourse()).add(teacher);
        }

    }

    public HashMap<String, ArrayList<String>> getAvisosCourseTeachersEB() {
        return avisosCourseTeachersEB;
    }

    public void setAvisosCourseTeachersEB(HashMap<String, ArrayList<String>> avisosCourseTeachersEB) {
        this.avisosCourseTeachersEB = avisosCourseTeachersEB;
    }

    public void addAvisosCourseTeachersEB(Course c, String teacher) {
        if (this.avisosCourseTeachersEB.isEmpty()) {
            this.avisosCourseTeachersEB.put("Title", new ArrayList());
            this.avisosCourseTeachersEB.get("Title").add("Course Teacher/s with incompatible blocks in Schedule:");
        }
        if (!this.avisosCourseTeachersEB.containsKey(c.getNameCourse())) {
            this.avisosCourseTeachersEB.put(c.getNameCourse(), new ArrayList());
        }
        if (!this.avisosCourseTeachersEB.get(c.getNameCourse()).contains(teacher)) {
            this.avisosCourseTeachersEB.get(c.getNameCourse()).add(teacher);
        }

    }

    public void getTeacherSection(HashMap<Integer, Teacher> hashTeachers, ArrayList<Teacher> teacherForCourse, Course c, Seccion currentSec) {
        if (!hashTeachers.get(currentSec.getIdTeacher()).asignaturaCursable(c.getIdCourse())) {
            if (teachersFull.contains(hashTeachers.get(currentSec.getIdTeacher()).getName() + ";")) {
                addTeachersFull(hashTeachers.get(currentSec.getIdTeacher()).getName() + ";");
            }
            addAvisosSectionTeachersFull(c, currentSec, hashTeachers.get(currentSec.getIdTeacher()).getName());
            getTeacherCourse(hashTeachers, teacherForCourse, c, currentSec);
        } else {
            teacherForCourse.add(hashTeachers.get(currentSec.getIdTeacher()));
        }
    }

    public void getTeacherCourse(HashMap<Integer, Teacher> hashTeachers, ArrayList<Teacher> teachersForCourse, Course c, Seccion currentSec) {
        if (!c.getTrestricctions().isEmpty()) {
            for (Integer i : c.getTrestricctions()) {
                if (hashTeachers.get(i).patronCompatibleEB(currentSec.getPatronUsado(), hashTeachers.get(i).getExcludeBlocks())
                        && hashTeachers.get(i).asignaturaCursable(c.getIdCourse())) {
                    teachersForCourse.add(hashTeachers.get(i));
                } else {
                    if (!hashTeachers.get(i).asignaturaCursable(c.getIdCourse())) {
                        if (teachersFull.contains(hashTeachers.get(i).getName() + ";")) {
                            addTeachersFull(hashTeachers.get(i).getName() + ";");
                        }
                        addAvisosCourseTeachersFull(c, hashTeachers.get(i).getName());
                    }
                    //Prueba:
                    if (hashTeachers.get(i).patronCompatibleEB(currentSec.getPatronUsado(), hashTeachers.get(i).getExcludeBlocks())) {
                        addAvisosCourseTeachersEB(c, hashTeachers.get(i).getName());
                    }
                }
            }
        }
        if (teachersForCourse.isEmpty()) {
            addAvisosCourseTeachersAvailable(c);
            Teacher t_Aux = new Teacher();
            t_Aux.setName("No found in template courses");
            teachersForCourse.add(t_Aux);

        }
    }

    public void CourseWithoutStudents(HashMap<Integer, ArrayList<Integer>> stCourse, ArrayList<Integer> CoursesScheduleActive, HashMap<Integer, String> nameCourses) {
        if (stCourse.size() != CoursesScheduleActive.size()) {
            for (int i = 0; i < CoursesScheduleActive.size(); i++) {
                if (!stCourse.containsKey(CoursesScheduleActive.get(i))) {
                    coursesWithoutStudents.put(CoursesScheduleActive.get(i), nameCourses.get(CoursesScheduleActive.get(i)));

                }
            }

        }
    }

    public String cargaTabla(ArrayList<String> datos, boolean swapcolor) {
        String tabla = "";
        tabla += "<div class='col-xs-12 students'>";
        tabla += "<table id='table_id' class='table'>";
        tabla += "<h4>" + datos.get(0) + "</h4>";
        datos.remove(0);
        swapcolor = true;
        for (String avis : datos) {
            if (swapcolor) {
                tabla += "<tr class='tcolores'>";
                swapcolor = false;
            } else {
                tabla += "<tr>";
                swapcolor = true;
            }
            tabla += "<td>" + avis + "</td></tr>";
        }
        tabla += "</table>";
        tabla += "</div>";

        return tabla;
    }

    public String cargaTablaHash(HashMap<String, ArrayList<String>> datos, boolean swapcolor) {
        String tabla = "";
        tabla += "<div class='col-xs-12 students'>";
        tabla += "<table id='table_id' class='table'>";
        swapcolor = true;
        String datosString = datos.get("Title").toString();
        datosString = datosString.substring(1, datosString.length() - 1);

        tabla += "<h4>" + datosString + "</h4>";
        datos.remove("Title");

        for (String avis : datos.keySet()) {

            if (swapcolor) {
                tabla += "<tr class='tcolores'>";
                swapcolor = false;
            } else {
                tabla += "<tr>";
                swapcolor = true;
            }

            tabla += "<td>" + avis + ": ";
            for (String datos2 : datos.get(avis)) {
                if (!datos.get(avis).get(datos.get(avis).size() - 1).equals(datos2)) {
                    if (!datos2.contains(",")) {
                        tabla += datos2 + ", ";
                    } else {
                        tabla += datos2 + "; ";
                    }
                } else {
                    tabla += datos2;
                }

            }
            tabla += "</td></tr>";
        }
        tabla += "</table>";
        tabla += "</div>";

        return tabla;
    }

    public void templateIdSection(HashMap<Integer, String> nameCourses, Course course, ResultSet rs) throws SQLException {
        String p1 = nameCourses.get(course.getIdCourse());
        String p2 = rs.getString(1);

        if (!templateIdSection.containsKey(p1)) {
            templateIdSection.put(p1, new ArrayList());
            templateIdSection.get(p1).add(p2);
        } else {
            templateIdSection.get(p1).add(p2);

        }
    }

    public void addAvisoCourseWithoutTemplate(String template) {

        if (!avisoCourseWithoutTemplate.contains("There is templateId without courses: ")) {
            avisoCourseWithoutTemplate.add("There is templateId without courses: ");
        }
        if (!avisoCourseWithoutTemplate.contains(template)) {
            avisoCourseWithoutTemplate.add(template);
        }
    }

    public void addAvisoSchoolWithoutScheduleActive(String schoolCode) {

        if (!avisoSchoolWithoutScheduleActive.contains("There is no Schedule Active course for this school configuration: ")) {
            avisoSchoolWithoutScheduleActive.add("There is no Schedule Active course for this school configuration: ");
        }
        if (!avisoSchoolWithoutScheduleActive.contains(schoolCode)) {
            avisoSchoolWithoutScheduleActive.add(schoolCode);
        }
    }
    
        public void addAvisoWithoutMatches(String template, String schoolCode) {

        if (!avisoWitouthMatches.contains("There is no coincidences between template and Schedule Active in this school: ")) {
            avisoWitouthMatches.add("There is no coincidences between template and Schedule Active in this school: ");
        }
        if (!avisoWitouthMatches.contains(template+", "+schoolCode)) {
            avisoWitouthMatches.add(template+", "+schoolCode);
        }
    }

}
