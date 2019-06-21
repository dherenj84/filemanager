package com.demo.filemanager.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.demo.filemanager.model.AjaxResponse;
import com.demo.filemanager.model.FileView;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.ListFileItem;

@Service
public class AzureFileService implements FileService {

	Logger log = LoggerFactory.getLogger(this.getClass());

	@Value("${azure.storage.connection-string}")
	private String AZURE_STORAGE_CONNECTION;

	private CloudStorageAccount storageAccount;

	private CloudFileClient fileClient;

	@Value("${file.root}")
	private String FILE_SHARE;

	static final String PATH_SEPARATOR = "/";

	private final ExecutorService executor = Executors.newCachedThreadPool();

	@Override
	public List<FileView> listDirectories(Optional<String> dir) {
		List<FileView> fileList = new ArrayList<>();
		try {
			if (dir.isPresent())
				dir = Optional.of(stripFileShareFromPath(dir.get()));
			CloudFileDirectory contentDir = getCloudFileDirectory(dir, Optional.empty());
			if (contentDir != null) {
				for (ListFileItem fileItem : contentDir.listFilesAndDirectories()) {
					if (fileItem.getClass() == CloudFileDirectory.class) {
						String fileName = null, filePath = null;
						List<String> pathItems = new ArrayList<>();
						pathItems.addAll(Arrays.asList(fileItem.getUri().getPath().split(PATH_SEPARATOR)));
						fileName = pathItems.remove(pathItems.size() - 1);
						filePath = StringUtils.join(pathItems.stream().filter(item -> StringUtils.isNotEmpty(item))
								.collect(Collectors.toList()), PATH_SEPARATOR);
						fileList.add(
								new FileView(fileName, fileItem.getClass() == CloudFileDirectory.class, filePath, "/"));
					}
				}
			}
		} catch (Exception e) {
			log.error("listFiles::error occured--------->", e);
		}
		return fileList;
	}

	@Override
	public List<FileView> listFiles(Optional<String> dir) {
		List<FileView> fileList = new ArrayList<>();
		try {
			if (dir.isPresent())
				dir = Optional.of(stripFileShareFromPath(dir.get()));
			CloudFileDirectory contentDir = getCloudFileDirectory(dir, Optional.empty());
			if (contentDir != null) {
				for (ListFileItem fileItem : contentDir.listFilesAndDirectories()) {
					if (fileItem.getClass() != CloudFileDirectory.class) {
						String fileName = null, filePath = null;
						List<String> pathItems = new ArrayList<>();
						pathItems.addAll(Arrays.asList(fileItem.getUri().getPath().split(PATH_SEPARATOR)));
						fileName = pathItems.remove(pathItems.size() - 1);
						filePath = StringUtils.join(pathItems.stream().filter(item -> StringUtils.isNotEmpty(item))
								.collect(Collectors.toList()), PATH_SEPARATOR);
						if (fileName.indexOf(".db") == -1)
							fileList.add(new FileView(fileName, fileItem.getClass() != CloudFileDirectory.class,
									filePath, "/"));
					}
				}
			}
		} catch (Exception e) {
			log.error("listFiles::error occured--------->", e);
		}
		return fileList;
	}

	@Override
	public File getFile(String filePath) {
		File response = null;
		try {
			filePath = stripFileShareFromPath(filePath);
			CloudFile cloudFile = getCloudFileReference(filePath, Optional.empty(), Optional.empty(), false);
			File file = File.createTempFile(filePath.split("\\.")[0], ".".concat(filePath.split("\\.")[1]));
			cloudFile.downloadToFile(file.getCanonicalPath());
			executor.execute(() -> {
				try {
					Thread.sleep(100l);
					file.delete();
				} catch (Exception e) {
					log.error("error deleting temp file", e);
				}
			});
			response = file;
		} catch (StorageException | IOException e) {
			log.error("getFile::error occured--------->", e);
		}
		return response;
	}

	private CloudFile getCloudFileReference(String fileName, Optional<String> dir, Optional<String> bucket,
			boolean upload) {
		CloudFile file = null;
		CloudFileDirectory contentDir;
		try {
			contentDir = getCloudFileDirectory(dir, bucket);
			if (upload && !contentDir.exists())
				contentDir.createIfNotExists();
			file = contentDir.getFileReference(fileName);
		} catch (URISyntaxException | StorageException e) {
			log.error("getCloudFileReference::error occured--------->", e);
		}
		return file;
	}

	private CloudFileDirectory getCloudFileDirectory(Optional<String> dir, Optional<String> bucket) {
		CloudFileDirectory directory = null;
		try {
			if (dir.isPresent() && StringUtils.isNotEmpty(dir.get())) {
				directory = bucket.isPresent()
						? fileClient.getShareReference(FILE_SHARE).getRootDirectoryReference()
								.getDirectoryReference(dir.get()).getDirectoryReference(bucket.get())
						: fileClient.getShareReference(FILE_SHARE).getRootDirectoryReference()
								.getDirectoryReference(dir.get());
			} else if (bucket.isPresent() && StringUtils.isNotEmpty(bucket.get()))
				directory = fileClient.getShareReference(FILE_SHARE).getRootDirectoryReference()
						.getDirectoryReference(bucket.get());
			else
				directory = fileClient.getShareReference(FILE_SHARE).getRootDirectoryReference();
		} catch (URISyntaxException | StorageException e) {
			log.error("getCloudFileDirectory::error occured--------->", e);
		}
		return directory;
	}

	@PostConstruct
	public void init() {
		try {
			storageAccount = CloudStorageAccount.parse(AZURE_STORAGE_CONNECTION);
		} catch (InvalidKeyException | URISyntaxException e) {
			log.error("init::FATAL---------->Couldn't create AZURE connection");
		}
		fileClient = storageAccount.createCloudFileClient();
	}

	@Override
	public AjaxResponse upload(MultipartFile file, Optional<String> dir, Optional<String> type) {
		AjaxResponse response = new AjaxResponse();
		String message = null;
		try {
			if (dir.isPresent())
				dir = Optional.of(stripFileShareFromPath(dir.get()));
			CloudFile cloudFile = getCloudFileReference(file.getOriginalFilename(), dir, type, true);
			if (cloudFile.exists()) {
				message = "a file by that name already exists";
				response.setError(new AjaxResponse.Error(message));
				log.error(message);
			} else {
				cloudFile.upload(file.getInputStream(), file.getSize());
				response.setFileName(file.getOriginalFilename());
				response.setUploaded(true);
				response.setMessage("File uploaded successfully!");
			}
		} catch (StorageException | IOException | URISyntaxException e) {
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
			filePath = stripFileShareFromPath(filePath);
			CloudFile cloudFile = getCloudFileReference(filePath, Optional.empty(), Optional.empty(), false);
			if (!cloudFile.deleteIfExists()) {
				message = "Error Deleting the File.";
				response.setError(new AjaxResponse.Error(message));
				log.error(message);
			}
		} catch (StorageException | URISyntaxException e) {
			message = "Error Deleting the File.";
			response.setError(new AjaxResponse.Error(message));
			log.error(message.concat("Exception is---->"), e);
		}
		response.setMessage("File Deleted Successfully.");
		return response;
	}

	private String stripFileShareFromPath(String path) {
		String strippedPath = path;
		if (path.contains(FILE_SHARE.concat(PATH_SEPARATOR)))
			strippedPath = path.replaceAll(FILE_SHARE.concat(PATH_SEPARATOR), "");
		else if (path.contains(FILE_SHARE))
			strippedPath = path.replaceAll(FILE_SHARE, "");
		return strippedPath;
	}
}
