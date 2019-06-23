# File Manager
A File Manager/Browser with the ability to list/upload/delete files and directories from a root directory that you can specify either from a Native File System or one of the Cloud Storage Providers(currently configured for Microsoft Azure Storage Account using a File Share). Additionally, the app also demonstrates the integration between CKEditor which is a popular WYSIWYG editor with this file system for browsing files using the File Manager and uploading files and images.

# Demo
<a href="https://dherenj84.azurewebsites.net/filemanager" target="_blank">https://dherenj84.azurewebsites.net/filemanager</a>
<br>
Username: sample
<br>
Password: sample

## Tech Stack
1. Angular for the UIX (https://angular.io/)
2. Spring Boot for the service backend (https://spring.io/projects/spring-boot)
3. Bootstrap for styling (https://getbootstrap.com/)
4. Font Awesone for Icons (https://fontawesome.com/)
5. CKEditor (https://ckeditor.com/ckeditor-4/)
6. Maven for buliding, packaging and running the app (https://maven.apache.org/)

## Configuration
There are 3 simple changes that would be required to configure the app based on what storage mode you want the app to use,
1. Under src/main/resources/application.properties change the storage.mode to either native for Native File System or cloud for Cloud Storage Accounts(currently Microsoft Azure Storage Supported) .
2. If storage.mode is set as native in step 1 above, specifiy file.directory and file.root properties otherwise skip this step.
3. If storage.mode is set as cloud in step 1, specify azure.storage.connection-string property in the application.properties. This value can be generated using the Access keys feature under your Storage Account in Microsoft Azure Portal.

<b>PLEASE NOTE</b> that I am using my own Azure account's Access Key to access the File Storage Account. The Access Key that you will find under application.properties is just a placeholder for you to refer the syntax but may not be valid anymore.
