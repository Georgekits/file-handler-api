package gr.unisystems.uploader;

import javax.ws.rs.GET;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import javax.ws.rs.PathParam;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import java.io.InputStream;
import org.glassfish.jersey.media.multipart.FormDataParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

@Path("/")
public class FileUploadService
{
    Utils utils;
    
    public FileUploadService() {
        this.utils = new Utils();
    }
    
    @POST
    @Consumes({ "multipart/form-data; charset=UTF-8" })
    @Path("fileReceival")
    @RolesAllowed({ "admin" })
    public Response uploadFile(@HeaderParam("content-length") final long contentLength, @FormDataParam("afm") final String afm, @FormDataParam("fileId") final String fileId, @FormDataParam("phaseId") final String phaseId, @FormDataParam("reqId") final String reqId, @FormDataParam("fileName") final String eteanfileName, @FormDataParam("file") final InputStream uploadedInputStream, @FormDataParam("file") final FormDataContentDisposition fileDetail) {
        if (uploadedInputStream == null || fileDetail == null || afm == null || afm.equals("") || fileId == null || fileId.equals("") || phaseId == null || phaseId.equals("") || reqId == null || reqId.equals("") || eteanfileName == null || eteanfileName.equals("")) {
            return Response.status(500).entity((Object)("All parameters must be set. afm: " + afm + " fileId: :" + fileId + " phaseId: " + phaseId + " reqId: " + reqId + " fileName: " + eteanfileName)).build();
        }
        System.out.println("File size: " + contentLength);
        if (contentLength > this.utils.MAX_FILE_SIZE) {
            return Response.status(500).entity((Object)("File exceeds the maximum file size of: " + this.utils.MAX_FILE_SIZE + " bytes.")).build();
        }
        if (phaseId.length() != 1) {
            return Response.status(500).entity((Object)"phaseId field - fixed lenght 1 restriction not followed").build();
        }
        if (!phaseId.equals("1") && !phaseId.equals("2") && !phaseId.equals("3") && !phaseId.equals("4") && !phaseId.equals("5") && !phaseId.equals("6")) {
            return Response.status(500).entity((Object)"phaseId field - values '1', '2', '3', '4', '5' or '6' restriction not followed").build();
        }
        try {
            final Utils utils = this.utils;
            this.utils.getClass();
            utils.createPOSTFolderIfNotExists("C:\\restFileHandler\\files\\", afm, phaseId, reqId);
        }
        catch (SecurityException se) {
            return Response.status(500).entity((Object)"Can not create destination folder on server").build();
        }
        final String fileType = this.utils.getFileExtension(fileDetail.getFileName());
        final String filename = String.valueOf(fileId) + "_" + eteanfileName + "." + fileType;
        this.utils.getClass();
        final String uploadedFileLocation = String.valueOf("C:\\restFileHandler\\files\\") + "\\" + afm + "\\" + reqId + "\\" + phaseId + "\\" + filename + "\\";
        System.out.println("filename: " + filename);
        System.out.println("uploadedFileLocation: " + uploadedFileLocation);
        try {
            this.utils.saveToFile(uploadedInputStream, uploadedFileLocation);
        }
        catch (IOException e) {
            return Response.status(500).entity((Object)"Cannot save file").build();
        }
        return Response.status(200).entity((Object)("File saved to " + uploadedFileLocation + " with parameters: " + " afm: " + afm + " fileId: " + fileId + " phaseId: " + phaseId + " reqId: " + reqId + " eteanfileName: " + eteanfileName)).build();
    }
    
    @GET
    @Path("download/{afm}/{reqId}/{phaseId}")
    @RolesAllowed({ "admin" })
    public Response downloadFiles(@PathParam("afm") final String afm, @PathParam("reqId") final String reqId, @PathParam("phaseId") final int phaseId) {
        this.utils.getClass();
        final String uploadedFileLocation = String.valueOf("C:\\restFileHandler\\files\\") + "\\" + afm + "\\" + reqId + "\\" + phaseId + "\\";
        final File directoryToZip = new File(uploadedFileLocation);
        final List<File> fileList = new ArrayList<File>();
        try {
            System.out.println("---Getting references to all files in: " + directoryToZip.getCanonicalPath());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.utils.getAllFiles(directoryToZip, fileList);
        }
        catch (Exception e2) {
            e2.printStackTrace();
            return Response.status(200).entity((Object)"The folder requested does not exists").build();
        }
        System.out.println("---Creating zip file");
        this.utils.writeZipFile(directoryToZip, fileList, afm, reqId, phaseId);
        this.utils.getClass();
        final File file = new File(String.valueOf("C:\\restFileHandler\\downloads\\") + "\\" + afm + "_" + reqId + "_" + phaseId + ".zip");
        final Response.ResponseBuilder response = Response.ok((Object)file);
        response.header("Content-Disposition", (Object)("attachment; filename=" + file.getName()));
        return response.build();
    }
}
