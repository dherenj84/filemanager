package com.demo.filemanager;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.demo.filemanager.model.FileView;
import com.demo.filemanager.service.FileService;
import com.demo.filemanager.service.FileServiceFactory;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FilemanagerApplicationTests {
	@Autowired
	FileServiceFactory serviceFactory;

	@Test
	public void testLoads() {
		FileService service = serviceFactory.getInstance();
		List<FileView> dirs = service.listDirectories(Optional.empty());
		assertEquals(dirs.size(), 2);
	}

}
