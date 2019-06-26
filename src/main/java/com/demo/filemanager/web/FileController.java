package com.demo.filemanager.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.demo.filemanager.model.AjaxResponse;
import com.demo.filemanager.model.ApplicationView;
import com.demo.filemanager.model.FileView;
import com.demo.filemanager.service.FileServiceFactory;

@RestController
@CrossOrigin
public class FileController {

	Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	FileServiceFactory serviceFactory;

	@Value("${file.root}")
	private String FILE_ROOT;

	@GetMapping("/listDirectories")
	public List<FileView> listDirectories(@RequestParam Optional<String> dir) {
		return serviceFactory.getInstance().listDirectories(dir);
	}

	@GetMapping("/listFiles")
	public List<FileView> listFiles(@RequestParam Optional<String> dir) {
		return serviceFactory.getInstance().listFiles(dir);
	}

	private final List<String> inlineFileTypes = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".pdf");

	@PostMapping("/uploadFile")
	public ResponseEntity<?> UploadFile(MultipartHttpServletRequest request, @RequestParam Optional<String> dir,
			@RequestParam Optional<String> type, @RequestParam Optional<Long> appId, HttpServletRequest servletRequest)
			throws IOException {
		Iterator<String> itr = request.getFileNames();
		MultipartFile file = request.getFile(itr.next());
		if (appId.isPresent() && !dir.isPresent()) {
			// todo code to fetch any application sub-directory should go here
		}
		AjaxResponse body = serviceFactory.getInstance().upload(file, dir, type);
		String url = servletRequest.getRequestURL().substring(0, servletRequest.getRequestURL().lastIndexOf("/") + 1)
				.concat("getFile/").concat(file.getOriginalFilename()).concat("?filePath=");
		String urlAppend = "";
		if (dir.isPresent()) {
			if (type.isPresent())
				urlAppend = urlAppend.concat(dir.get().concat(serviceFactory.getFileSeparator()).concat(type.get()));
			else
				urlAppend = urlAppend.concat(dir.get());
		} else if (type.isPresent())
			urlAppend = urlAppend.concat(type.get());
		urlAppend = urlAppend.concat(serviceFactory.getFileSeparator().concat(file.getOriginalFilename()));
		body.setUrl(url.concat(urlAppend));
		return ResponseEntity.ok(body);

	}

	@GetMapping("/deleteFile")
	public ResponseEntity<?> deleteFile(@RequestParam String filePath) {
		return ResponseEntity.ok(serviceFactory.getInstance().delete(filePath));
	}

	@GetMapping("/getFile/{fileName}")
	public void getFile(@PathVariable String fileName, @RequestParam String filePath, HttpServletResponse response) {
		File file = serviceFactory.getInstance().getFile(filePath);
		try (InputStream in = FileUtils.openInputStream(file)) {
			response.setContentType(Files.probeContentType(file.toPath()));
			response.getOutputStream().write(IOUtils.toByteArray(in));
			response.flushBuffer();
		} catch (Exception e) {
			log.error("error occured getting the file. Exception is ---->", e);
		}
	}

	@GetMapping("/addFolder/{folderName}")
	public ResponseEntity<?> addFolder(@PathVariable String folderName, @RequestParam String folderPath) {
		return ResponseEntity.ok(serviceFactory.getInstance().addFolder(folderName, folderPath));
	}

	@GetMapping("/deleteFolder/{folderName}")
	public ResponseEntity<?> deleteFolder(@PathVariable String folderName, @RequestParam String folderPath) {
		return ResponseEntity.ok(serviceFactory.getInstance().deleteFolder(folderName, folderPath));
	}

	@GetMapping("/getApplication/{id}")
	public ApplicationView getApplication(@PathVariable Long id) {
		// todo code to fetch any application sub-directory should go here
		return null;
	}

	@GetMapping("/getFileRoot")
	public String getFileRoot() {
		return FILE_ROOT;
	}

}
