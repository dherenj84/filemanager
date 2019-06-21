package com.demo.filemanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileView {
	private String name;
	private boolean isDirectory;
	private String path;
	private String pathSeparator;
}
