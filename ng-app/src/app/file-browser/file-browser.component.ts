import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http'
import { ActivatedRoute, Router } from '@angular/router';
import { isNullOrUndefined } from 'util';
import { environment } from '../../environments/environment';

declare var $: any;

@Component({
  selector: 'app-file-browser',
  templateUrl: './file-browser.component.html',
  styleUrls: ['./file-browser.component.css']
})
export class FileBrowserComponent implements OnInit {
  docbase: string;
  contentRoot: string;
  dirs: any[] = [];
  files: any[] = []
  filesCopy: any = [];
  breadcrumbs: any[] = [];
  selectedDir: any = {};
  alertMessage: string = '';
  alertSuccess: boolean = true;
  dirMap: any = {};
  appId: string;
  ckEditorFuncNum: string;
  filter: string = ''
  loading: boolean = false;
  loggedInUser: any;

  constructor(private http: HttpClient, private router: Router, private route: ActivatedRoute) {
    this.ckEditorFuncNum = route.snapshot.queryParamMap.get('CKEditorFuncNum');
    this.appId = route.snapshot.queryParamMap.get('appId');
  }

  ngOnInit() {
    this.loggedInUser = null;
    if (!isNullOrUndefined(this.ckEditorFuncNum)) {
      this.loading = false;
      this.setContentRoot()
    }
    else if (isNullOrUndefined(sessionStorage.getItem('loggedInUser'))) {
      this.router.navigate(['login'], {
        queryParams: {
          'logout': 'false'
        }
      });
      return;
    }
    else {
      this.loggedInUser = JSON.parse(sessionStorage.getItem('loggedInUser'));
      this.setContentRoot();
    }
  }

  setContentRoot() {
    this.loading = true;
    this.http.get(environment.serviceUrl + 'getFileRoot', {
      responseType: 'text'
    }).subscribe(resp => {
      this.docbase = resp;
      this.listFilesAndDirectories();
    })
  }

  listFilesAndDirectories() {
    if (!this.contentRoot)
      this.contentRoot = this.docbase;
    let dirUrl = environment.serviceUrl + "listDirectories?dir=" + (this.contentRoot != this.docbase ? this.contentRoot : '');
    let fileUrl = environment.serviceUrl + "listFiles?dir=" + (this.contentRoot != this.docbase ? this.contentRoot : '');
    this.http.get<any[]>(dirUrl).subscribe(resp => {
      this.loading = false;
      //console.log(resp);
      if (resp.length > 0) {
        this.dirs = resp;
        this.breadcrumbs = [{
          path: this.contentRoot == this.docbase ? '' : this.docbase,
          name: this.contentRoot,
          pathSeparator: resp[0].pathSeparator
        }];
        this.selectedDir = this.breadcrumbs[0];
        this.setDirMap(this.selectedDir);
        this.setBreadCrumbState();
      }
    });
    this.http.get<any[]>(fileUrl).subscribe(resp => {
      //console.log(resp);
      this.files = resp;
      this.filesCopy = resp;
    });
  }

  setDirMap(dir: any) {
    if (dir.path.length > 0)
      this.dirMap[dir.path + dir.pathSeparator + dir.name] = dir;
    else
      this.dirMap[dir.name] = dir;
  }

  onDirClick(dir: any, e?: Event) {
    this.loading = true;
    if (e)
      e.preventDefault();
    let urlPath = this.determineUrlPath(dir);
    this.setDirMap(dir);
    let dirUrl = environment.serviceUrl + "listDirectories?dir=" + (urlPath != this.contentRoot ? urlPath : '');
    let fileUrl = environment.serviceUrl + "listFiles?dir=" + (urlPath != this.contentRoot ? urlPath : '');
    this.http.get<any[]>(dirUrl).subscribe(resp => {
      this.loading = false;
      //console.log(resp);
      this.dirs = resp;
    });
    this.http.get<any[]>(fileUrl).subscribe(resp => {
      //console.log(resp);
      this.files = resp;
      this.filesCopy = resp;
    });
    this.resetBreadcrumbs(dir);
    this.selectedDir = dir;
  }

  private resetBreadcrumbs(dir: any) {
    this.breadcrumbs = [];
    let paths: string[] = dir.path.split(dir.pathSeparator);
    if (this.contentRoot != this.docbase)
      paths = paths.filter(path => path != this.docbase);
    paths.push(dir.name)
    let dirPath = '';
    for (let index = 0; index < paths.length; index++) {
      const path = paths[index];
      if (path != this.docbase) {
        if (dirPath.length == 0)
          dirPath = dirPath + path;
        else
          dirPath = dirPath + dir.pathSeparator + path;
      }
      this.breadcrumbs.push({
        name: path,
        path: dirPath,
        pathSeparator: dir.pathSeparator
      });
      if (this.breadcrumbs[0].name.trim() == '')
        this.breadcrumbs.splice(0, 1);
      this.setBreadCrumbState();
    }
  }

  setBreadCrumbState() {
    setTimeout(() => {
      $('.breadcrumb-item-link').attr('href', '#');
      if ($('.breadcrumb-item-link').length > 0)
        $('.breadcrumb-item-link').last().removeAttr('href').addClass('disabled');
    }, 250);
  }

  onBreadcrumbClick(selected: any, e: Event) {
    e.preventDefault();
    let selectedIndex = this.breadcrumbs.findIndex(ele => ele.path == selected.path);
    if (selectedIndex != -1) {
      this.breadcrumbs = this.breadcrumbs.slice(0, selectedIndex + 1);
      if (selected.name == this.docbase)
        this.onDirClick(this.dirMap[this.docbase + selected.path]);
      else
        this.onDirClick(this.dirMap[this.docbase + selected.pathSeparator + selected.path]);
    }
  }

  getFileUrl(fileView: any) {
    if (location.host.indexOf('localhost') != -1)
      return "http://localhost:8080/filemanager/" + "getFile/" + encodeURIComponent(fileView.name) + "?filePath=" + this.determineUrlPath(fileView);
    else return location.href.substring(0, location.href.indexOf('#')) + "getFile/" + encodeURIComponent(fileView.name) + "?filePath=" + this.determineUrlPath(fileView);
  }

  uploadFile() {
    this.alertMessage = '';
    this.alertSuccess = true;
    var fileInput = <HTMLInputElement>document.getElementById('fileUpload');
    var file = fileInput.files[0];
    var formData = new FormData();
    formData.append('file', file);
    this.loading = true;
    this.http.post<any>(environment.serviceUrl + 'uploadFile?dir=' + this.determineUrlPath(this.selectedDir), formData).subscribe(resp => this.processResponse(resp));
    setTimeout(() => {
      $('#fileUpload').val('');
    }, 0);
  }

  determineUrlPath(dir: any) {
    // if (dir.name == this.contentRoot)
    //   return "";
    let urlPath = dir.name;
    if (dir.path != dir.name && dir.path.length > 0) {
      urlPath = dir.path + dir.pathSeparator + dir.name;
    }
    return encodeURIComponent(urlPath);
  }

  onFileDelete(fileView: any, e: Event) {
    if (e)
      e.preventDefault();
    if (confirm("Delete File " + fileView.name + "?")) {
      this.alertMessage = '';
      this.alertSuccess = true;
      this.loading = true
      this.http.get<any>(environment.serviceUrl + "deleteFile?filePath=" + this.determineUrlPath(fileView)).subscribe(resp => this.processResponse(resp));
    }
  }

  onFileDownload(fileView: any, e: Event) {
    if (e)
      e.preventDefault();
    this.alertSuccess = true;
    this.alertMessage = '';
    this.loading = true
    this.http.get(environment.serviceUrl + "getFile/" + fileView.name + "?filePath=" + this.determineUrlPath(fileView), {
      responseType: 'blob'
    }).subscribe(resp => {
      this.loading = false;
      let blob = new Blob([resp], { type: 'application/octet-stream' });
      let link = document.createElement('a');
      link.href = window.URL.createObjectURL(blob);
      link.download = fileView.name;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    });
  }

  onFileClick(fileView: any, e: Event) {
    e.preventDefault();
    let fileUrl = this.getFileUrl(fileView);
    if (this.ckEditorFuncNum) {
      //window.opener.CKEDITOR.tools.callFunction(this.ckEditorFuncNum, fileUrl);
      window.opener.postMessage({
        ckEditorFuncNum: this.ckEditorFuncNum,
        fileUrl: fileUrl
      }, "*");
      window.close();
    }
    else
      window.open(this.getFileUrl(fileView), '_blank');
  }

  onFilter(e: Event) {
    if (this.filesCopy.length > 0) {
      if (this.filter.length > 0) {
        let filtered = this.filesCopy.filter(file => file.name.toLowerCase().indexOf(this.filter.toLowerCase()) != -1);
        this.files = filtered;
      }
      else
        this.files = [...this.filesCopy];
    }
  }

  validFile(fileName: string) {
    let valid = false;
    let validFileExtentions = ['.jpg', '.jpeg', '.png', '.gif', '.pdf'];
    for (let index = 0; index < validFileExtentions.length; index++) {
      const extension = validFileExtentions[index];
      if (fileName.toLowerCase().indexOf(extension) != -1) {
        valid = true;
        break;
      }
    }
    return valid;
  }

  validImgFile(fileName: string) {
    let valid = false;
    let validFileExtentions = ['.jpg', '.jpeg', '.png', '.gif'];
    for (let index = 0; index < validFileExtentions.length; index++) {
      const extension = validFileExtentions[index];
      if (fileName.toLowerCase().indexOf(extension) != -1) {
        valid = true;
        break;
      }
    }
    return valid;
  }

  addFolder() {
    if ($('#addFolderInput').val().trim().length == 0) {
      this.processResponse({
        error: {
          message: 'Folder name is empty'
        }
      });
      return;
    }
    this.loading = true;
    this.alertSuccess = true;
    this.alertMessage = '';
    $('#addFolder').modal('hide');
    $('#collapseAddFolder').collapse('hide');
    this.http.get<any>(environment.serviceUrl + "addFolder/" + $('#addFolderInput').val() + '?folderPath=' + this.determineUrlPath(this.selectedDir)).subscribe(resp => this.processResponse(resp));
  }

  deleteFolder(dir: any, e: Event) {
    e.preventDefault();
    this.loading = true;
    this.alertSuccess = true;
    this.alertMessage = '';
    if (confirm("Delete folder " + dir.name + "?")) {
      this.http.get<any>(environment.serviceUrl + "deleteFolder/" + dir.name + "?folderPath=" + this.determineUrlPath(this.selectedDir)).subscribe(resp => this.processResponse(resp))
    }
    else {
      this.loading = false;
      return;
    }
  }

  processResponse(resp: any) {
    this.loading = false;
    if (resp.error) {
      this.alertSuccess = false;
      this.alertMessage = resp.error.message;
    }
    else {
      this.alertMessage = resp.message;
      this.onDirClick(this.selectedDir);
    }
    $('.alert').show();
    setTimeout(() => {
      $('.alert').hide();
    }, 3000);
  }

  copyPath(file: any, e: Event) {
    e.preventDefault();
    $('body').append("<input id='inputFileUrl' value=" + this.getFileUrl(file) + ">");
    $('#inputFileUrl').select();
    document.execCommand("copy");
    $('.toast').toast('show');
    $('#inputFileUrl').remove();
  }

  refresh(e: Event) {
    e.preventDefault();
    this.onDirClick(this.selectedDir);
  }
}
