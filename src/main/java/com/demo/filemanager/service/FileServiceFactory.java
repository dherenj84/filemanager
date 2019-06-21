package com.demo.filemanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileServiceFactory {

	@Autowired
	@Qualifier("fileServiceImpl")
	private FileService nativeFileService;

	@Autowired
	@Qualifier("azureFileService")
	private FileService azureFileService;

	@Value("${storage.mode}")
	private String STORAGE_MODE;

	public FileService getInstance() {
		FileService fileServiceInstance = null;
		switch (STORAGE_MODE) {
		case FileService.STORAGE_MODE_NATIVE:
			fileServiceInstance = nativeFileService;
			break;
		case FileService.STORAGE_MODE_CLOUD:
			fileServiceInstance = azureFileService;
			break;
		default:
			fileServiceInstance = nativeFileService;
			break;
		}
		return fileServiceInstance;
	}

	public String getFileSeparator() {
		if (STORAGE_MODE.equals(FileService.STORAGE_MODE_CLOUD))
			return AzureFileService.PATH_SEPARATOR;
		else
			return FileServiceImpl.PATH_SEPARATOR;
	}

}
