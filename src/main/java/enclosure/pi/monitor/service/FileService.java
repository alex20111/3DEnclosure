package enclosure.pi.monitor.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.jsoniter.JsonIterator;
import com.jsoniter.any.Any;

import enclosure.pi.monitor.common.Constants;
import enclosure.pi.monitor.printer.PrinterHandler;
import enclosure.pi.monitor.service.model.FileList;
import enclosure.pi.monitor.service.model.Message;
import enclosure.pi.monitor.service.model.Message.MessageType;


@Path("file")
public class FileService {

	private static final Logger logger = LogManager.getLogger(FileService.class);

	//TODO handle auto print!!!
	@Path("upload")
	@POST
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@Produces(MediaType.APPLICATION_JSON)
	public Response upload(  @FormDataParam("file") InputStream fileInputStream,
			@FormDataParam("file") FormDataContentDisposition fileMetaData,
			@FormDataParam("formField") String formFields) throws Exception
	{

		logger.debug("updateItem: " + formFields);

		Message msg = null;
		Status status = Status.FORBIDDEN;

		if (formFields == null || formFields.trim().length() == 0) {
			status = Status.BAD_REQUEST;			
			msg = new Message(MessageType.ERROR, "Missing form fields");
			return Response.status(status).entity(msg).build();	
		}

		try {

			if (fileInputStream != null && fileMetaData != null) {

				String fileName = fileMetaData.getFileName();				

				int read = 0;
				byte[] bytes = new byte[1024];

				OutputStream out = new FileOutputStream(new File(Constants.GCODE_DIR + fileName));
				while ((read = fileInputStream.read(bytes)) != -1) 
				{
					out.write(bytes, 0, read);
				}
				out.flush();
				out.close();

			}
			
			Any fields = JsonIterator.deserialize(formFields);
			boolean autoPrint = fields.get(0).toBoolean();
			
			if (autoPrint) {
				logger.debug("Auto print enabled.. staring print");
				//TODO auto printing here
			}
			

			msg = new Message(MessageType.SUCCESS, "File Uploaded");

			return Response.ok().entity(msg).build();


		}catch(Throwable ex) {
			logger.error("error in upload" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message(MessageType.ERROR, ex.getMessage());
		}

		return Response.status(status).entity(msg).build();
	}
	@Path("gcodeList")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response gcodeFileList() {
		logger.debug("gcodeFileList");

		Message msg = new Message(MessageType.ERROR, "Forbidden");
		Status status = Status.FORBIDDEN;

	

		try {

			 List<FileList> fileList = Stream.of(new File(Constants.GCODE_DIR).listFiles())
				      .filter(file -> !file.isDirectory())
				      .map(file -> new FileList(file.getName(), file.length(), true, false))
				      .collect(Collectors.toList());
			 logger.debug("File list: " + fileList);
			 
			return Response.ok().entity(fileList).build();


		}catch(Throwable ex) {
			logger.error("error in upload" , ex);
			status = Status.INTERNAL_SERVER_ERROR;
			msg = new Message(MessageType.ERROR, ex.getMessage());
		}

		return Response.status(status).entity(msg).build();
	}
	
	@GET
	@Path("/downloadPrintLog")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFileWithGet() {
//		System.out.println("Download file "+file);
//		File fileDownload = new File(Config.UPLOAD_FOLDER + File.separator + file);
		 java.nio.file.Path logFile = PrinterHandler.getInstance().getPrintLogFile();
		 
		 ResponseBuilder response = null;
		 
		 if (Files.exists(logFile)) {
			 File fileDownload = null;
				 response = Response.ok((Object) fileDownload);
				response.header("Content-Disposition", "attachment;filename=" + fileDownload);
		 }else {
			 Message msg = new Message(MessageType.ERROR, "File does not exist");
			 response = Response.status(Status.FORBIDDEN).entity(msg);
		 }
		
		
		
		return response.build();
	}
}
