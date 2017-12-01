package controladores;

import Montessori.Resource;
import static Montessori.Resource.RUTA_FTP;
import controladores.ResourcesControlador;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Worker;
import javax.servlet.ServletContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.tomcat.util.http.fileupload.IOUtils;

@WebServlet(name = "upload", urlPatterns = {"/upload"})
@MultipartConfig
public class upload extends HttpServlet {

   /**
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
    * methods.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
    protected void processResponse(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ClassNotFoundException
    {  
            String presentationName = request.getParameter("lessonsName");
            String idFile  = request.getParameter("idNameFileDown");
            Resource rLoaded = new Resource();
            ResourcesControlador rCont = new ResourcesControlador();
            
            try {
                rLoaded = rCont.loadResource(idFile,request);
            } catch (Exception ex) {
                Logger.getLogger(upload.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            String filePath = "/MontessoriTesting/"+rLoaded.getLesson_id()+"-"+presentationName+"/"+rLoaded.getLink();
            String url2 = request.getContextPath();
            
            String url3 = ""+request.getRequestURL();
            String url =  "/lessonresources/loadResources.htm?LessonsSelected="+rLoaded.getLesson_id()+"-"+presentationName;
            String server = "192.168.1.36";
            int port = 21;
            String user = "david";
            String pass = "david";
            
            
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(server,port);
            ftpClient.login(user, pass);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            InputStream inStream = ftpClient.retrieveFileStream(filePath);
           
            // obtains ServletContext
            ServletContext context = request.getServletContext();
            String appPath = context.getRealPath("");
            System.out.println("appPath = " + appPath);

            // gets MIME type of the file
            String mimeType = context.getMimeType(rLoaded.getLink());
            if (mimeType == null) {        
                // set to binary type if MIME mapping not found
                mimeType = "application/octet-stream";
            }
            System.out.println("MIME type: " + mimeType);

            // modifies response
            response.setContentType(mimeType);
            
            // forces download
            String headerKey = "Content-Disposition";
            String headerValue = String.format("attachment; filename=\"%s\"", rLoaded.getLink());
            response.setHeader(headerKey, headerValue);
            IOUtils.copy(inStream, response.getOutputStream());
            
            response.flushBuffer();
            response.sendRedirect(request.getContextPath()+url);
    }
        
   
   protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, ClassNotFoundException
   {
            //get the file chosen by the user
            Part filePart = request.getPart("fileToUpload");
            ResourcesControlador rCont = new ResourcesControlador();
		//get the InputStream to store the file somewhere
	    InputStream fileInputStream = filePart.getInputStream();
            String urlBase = request.getParameter("txtUrl"); // CAMBIAR 
	    String name  = request.getParameter("idNameFile");
            String lessonId = request.getParameter("lessonid");
            String presentationName = request.getParameter("lessonsName");
            

            String url =  "/lessonresources/loadResources.htm?LessonsSelected="+lessonId+"-"+presentationName;
            String server = "192.168.1.36";
		int port = 21;
		String user = "david";
		String pass = "david";

		FTPClient ftpClient = new FTPClient();
         FileInputStream fis = null;
            try {
                ftpClient.connect(server, port);
			ftpClient.login(user, pass);
//			ftpClient.enterLocalPassiveMode();
//
//			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                //if(!rCont.existe(name+"-"+ filePart.getSubmittedFileName(), request)){
//                    File fileToSave = new File(RUTA_FTP +lessonId+"/"+name+"-"+ filePart.getSubmittedFileName());
//                    Files.copy(fileInputStream, fileToSave.toPath(), StandardCopyOption.REPLACE_EXISTING);   
//                    //GET VALUES FOR SAVE IN BBDD
//
                //String path = name+"-"+ filePart.getSubmittedFileName();
                // String filename = filePart.getSubmittedFileName();
                String filename = name+"-"+ filePart.getSubmittedFileName();
//                fis = new FileInputStream(filename);
                if(!ftpClient.changeWorkingDirectory("/MontessoriTesting/"+lessonId+"-"+presentationName));
                {
                    ftpClient.changeWorkingDirectory("/MontessoriTesting");
                    ftpClient.mkd(lessonId+"-"+presentationName);
                    ftpClient.changeWorkingDirectory(lessonId+"-"+presentationName);
                }
                              
                ftpClient.storeFile(filename, fileInputStream);       
                ftpClient.logout();
                Resource r = new Resource();

                r.setLesson_id(lessonId);
                r.setLink(filename);
                r.setName(name);
                r.setType("File");
                String idResource = rCont.addResources(r,request,response);
               // }
               
            } catch (Exception ex) {
                Logger.getLogger(upload.class.getName()).log(Level.SEVERE, null, ex);
            }
            //create output HTML that uses the 
            //response.reset(); 
            //response.encodeRedirectURL("http://localhost:8080/QuickBooks_1/lessonresources/loadResources.htm?LessonsSelected=19");
            
            response.sendRedirect(request.getContextPath()+url);
            
  }
  @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
       try {
           processResponse(request, response);
       } catch (ClassNotFoundException ex) {
     
       }    
   }

   /**
    * Handles the HTTP <code>POST</code> method.
    *
    * @param request servlet request
    * @param response servlet response
    * @throws ServletException if a servlet-specific error occurs
    * @throws IOException if an I/O error occurs
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
       try {
           processRequest(request, response);
       } catch (ClassNotFoundException ex) {
       }
   }
      
   /**
    * Returns a short description of the servlet.
    *
    * @return a String containing servlet description
    */
   
   
   @Override
   public String getServletInfo() {
       return "Short description";
   }// </editor-fold>
}
	
