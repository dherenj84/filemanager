# File Manager
A File Manager/Browser with the ability to list/upload/delete files and add/delete directories within a root directory that you can specify either from a <b>Native File System</b> or one of the <b>Cloud Storage</b> Providers(currently configured for Microsoft Azure Storage Account using a File Share). Additionally, the app also demonstrates the integration between <b>CKEditor</b> which is a popular <b>WYSIWYG</b> editor with this file system for browsing files using the File Manager and uploading files and images.

<img src="https://dherenj84.azurewebsites.net/filemanager/getFile/me62619.png?filePath=assets%2Fimages%2Fme62619.png"> 

## Demo
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
There are 3 simple configuration changes that would be required to configure the app based on what storage mode you want to use,
1. Under src/main/resources/application.properties change the <b>storage.mode</b> to either native for Native File System or cloud for Cloud Storage Accounts(currently Microsoft Azure Storage Supported) .
2. If storage.mode is set as native in step 1 above, specifiy <b>file.directory</b> and <b>file.root</b> properties otherwise skip this step.
3. If storage.mode is set as cloud in step 1, specify <b>azure.storage.connection-string</b> property in the application.properties. This value can be generated using the Access keys feature under your Storage Account in Microsoft Azure Portal.

<b>Please note</b> that I am using my own Azure account's Access Key to access the File Storage Account. The Access Key that you will find under application.properties is just a placeholder for you to refer the syntax but may not be valid anymore.

## Sources
The Angular App resides in the <b>ng-app</b> folder under project root and the Java App resides in the conventional <b>src</b> folder under project root.

## Build & Run the app
The App runs as one single java web application and can be deployed as a war in a container of your choice. The complied Angular UI code resides in <b>src/main/resources/static</b> folder. Because the backend being Spring Boot, the app can be quickly run using the following command on the project root,<br>
<b>mvn spring-boot:run</b><br>

Angular Code is built using the command <b>ng build --prod --base-href</b> . As you may have noted I am removing the base-href upon production build of Angular Code. This is necessary to make sure that the app runs correctly with it's current project structure.
