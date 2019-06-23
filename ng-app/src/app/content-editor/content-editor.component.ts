import { Component, OnInit } from '@angular/core';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-content-editor',
  templateUrl: './content-editor.component.html',
  styleUrls: ['./content-editor.component.css']
})
export class ContentEditorComponent implements OnInit {

  model: any = {};

  constructor() { }

  ngOnInit() {
    this.model.editorData = '<p>Hello, world!</p>';
    this.model.config = {
      // extraPlugins: "filebrowser",
      filebrowserBrowseUrl: '#browser',
      filebrowserUploadUrl: environment.serviceUrl + 'uploadFile?type=files',
      filebrowserImageUploadUrl: environment.serviceUrl + 'uploadFile?type=images'
    };
    document.getElementsByClassName('editorPreview')[0].innerHTML = this.model.editorData;
    window.addEventListener("message", this.onFileBrowserFileClick, false);
  }

  private onFileBrowserFileClick(event) {
    //put you origin checks as required from cross-origin File Manager frame posting message to CKEditor frame

    // if (event.origin == "http://localhost:8080" || event.origin == "http://localhost:4200")
    //   window['CKEDITOR'].tools.callFunction(event.data.ckEditorFuncNum, event.data.fileUrl);
    // else return;

    window['CKEDITOR'].tools.callFunction(event.data.ckEditorFuncNum, event.data.fileUrl);
  }

  onEditorChange(event: any) {
    document.getElementsByClassName('editorPreview')[0].innerHTML = event.editor.getData();
  }

}
