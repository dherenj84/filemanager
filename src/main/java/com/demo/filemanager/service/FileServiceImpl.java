package com.demo.filemanager.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.demo.filemanager.model.AjaxResponse;
import com.demo.filemanager.model.FileView;

@Service
public class FileServiceImpl implements FileService {
	Logger log = LoggerFactory.getLogger(getClass());

	@Value("${file.directory}")
	private String FILE_DIRECTORY;

	@Value("${file.root}")
	private String FILE_ROOT;

	static final String PATH_SEPARATOR = "/";

	@Override
	public List<FileView> listDirectories(Optional<String> dir) {
		List<FileView> files = new ArrayList<>();
		try {
			File root = new File(getFilePath(dir));
			if (root.exists()) {
				files = Arrays.asList(root.listFiles()).stream().filter(file -> file.isDirectory())
						.map(file -> new FileView(file.getName(), true, getFileViewPath(dir), PATH_SEPARATOR))
						.collect(Collectors.toList());
			}
		} catch (Exception e) {
			log.error("error occured. exception is ---->", e);
		}
		return files;
	}

	@Override
	public List<FileView> listFiles(Optional<String> dir) {
		List<FileView> files = new ArrayList<>();
		try {
			File root = new File(getFilePath(dir));
			if (root.exists()) {
				files = Arrays.asList(root.listFiles()).stream().filter(file -> !file.isDirectory())
						.filter(file -> !file.getName().contains(".db"))
						.map(file -> new FileView(file.getName(), false, getFileViewPath(dir), PATH_SEPARATOR))
						.collect(Collectors.toList());
			}
		} catch (Exception e) {
			log.error("error occured. exception is ---->", e);
		}
		return files;
	}

	@Override
	public File getFile(String filePath) {
		return FileUtils.getFile(getFilePath(Optional.of(filePath)));
	}

	@Override
	public AjaxResponse upload(MultipartFile file, Optional<String> dir, Optional<String> type) {
		AjaxResponse response = new AjaxResponse();
		String message = null;
		try {
			File toUpload = new File(getFilePath(dir, type) + PATH_SEPARATOR + file.getOriginalFilename());
			if (toUpload.exists()) {
				message = "a file by that name already exists";
				response.setError(new AjaxResponse.Error(message));
				log.error(message);
			} else {
				FileUtils.writeByteArrayToFile(toUpload, file.getBytes());
				response.setFileName(file.getOriginalFilename());
				response.setUploaded(true);
				response.setMessage("File uploaded successfully!");
			}
		} catch (Exception e) {
			message = "unable to upload file::" + file.getName() + " at this time.";
			response.setError(new AjaxResponse.Error(message));
			log.error(message.concat("Exception is---->"), e);
		}
		return response;
	}

	@Override
	public AjaxResponse delete(String filePath) {
		AjaxResponse response = new AjaxResponse();
		String message = null;
		try {
			File file = new File(getFilePath(Optional.of(filePath)));
			if (!file.delete()) {
				message = "Error Deleting the File.";
				response.setError(new AjaxResponse.Error(message));
				log.error(message);
			}
		} catch (Exception e) {
			message = "Error Deleting the File.";
			response.setError(new AjaxResponse.Error(message));
			log.error(message.concat("Exception is---->"), e);
		}
		response.setMessage("File Deleted Successfully.");
		return response;
	}

	@SafeVarargs
	private final String getFilePath(Optional<String>... dirs) {
		StringBuilder base = new StringBuilder(FILE_DIRECTORY + PATH_SEPARATOR + FILE_ROOT);
		if (dirs.length > 0) {
			for (Optional<String> dir : dirs) {
				if (StringUtils.isNotEmpty(dir.orElse("")))
					base.append(PATH_SEPARATOR).append(stripFileRootFromPath(dir.get()));
			}
		}
		return base.toString();
	}

	@SafeVarargs
	private final String getFileViewPath(Optional<String>... dirs) {
		StringBuilder base = new StringBuilder(FILE_ROOT);
		if (dirs.length > 0) {
			for (Optional<String> dir : dirs) {
				if (StringUtils.isNotEmpty(dir.orElse("")))
					base.append(PATH_SEPARATOR).append(stripFileRootFromPath(dir.get()));
			}
		}
		return base.toString();
	}

	private String stripFileRootFromPath(String path) {
		String strippedPath = path;
		if (path.contains(FILE_ROOT.concat(PATH_SEPARATOR)))
			strippedPath = path.replace(FILE_ROOT.concat(PATH_SEPARATOR), "");
		else if (path.contains(FILE_ROOT))
			strippedPath = path.replace(FILE_ROOT, "");
		return strippedPath;
	}

}
