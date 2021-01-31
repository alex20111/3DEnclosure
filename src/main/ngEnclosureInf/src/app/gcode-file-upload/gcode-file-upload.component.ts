import { FileService } from './../services/file.service';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { faFileAlt, faTrashAlt, faUpload } from '@fortawesome/free-solid-svg-icons';
import { HttpEvent, HttpEventType } from '@angular/common/http';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-gcode-file-upload',
  templateUrl: './gcode-file-upload.component.html',
  styleUrls: ['./gcode-file-upload.component.css']
})
export class GcodeFileUploadComponent implements OnInit {

  errFile: string = "";

  uploadForm!: FormGroup;

  faUpload = faUpload;
  faTrashAlt = faTrashAlt;
  faFileAlt = faFileAlt;

  ngOnInit(): void {
    this.uploadForm = this.formBuilder.group({
      frm_print_auto_start: [false]
    });

  }

  @ViewChild("fileDropRef", { static: false }) fileDropEl: ElementRef;
  files: any[] = [];

  constructor(private fileService: FileService, private formBuilder: FormBuilder) {

  }
  /**
   * on file drop handler
   */
  onFileDropped($event) {
    console.log("onFileDropped: ", $event)
    this.files = [];
    this.prepareFilesList($event);
  }

  /**
   * handle file from browsing
   */
  fileBrowseHandler(files) {
    // console.log("fileBrowseHandler: " , files, files.path[0].files)
    this.files = [];
    this.prepareFilesList(files.path[0].files);
  }

  /**
   * Delete file from files list
   * @param index (File index)
   */
  deleteFile(index: number) {
    this.files.splice(index, 1);
  }

  /**
   * Convert Files list to normal array list
   * @param files (Files List)
   */
  prepareFilesList(files: Array<any>) {

    let fileNameExt = files[0].name as string;
    fileNameExt = fileNameExt.substring(fileNameExt.lastIndexOf('.') + 1, fileNameExt.length);

    if (fileNameExt === "gcode") {
      this.errFile = "";

      for (const item of files) {
        item.progress = 0;
        this.files.push(item);
      }
      this.fileDropEl.nativeElement.value = "";
      // this.uploadFilesSimulator(0);
    } else {
      this.errFile = "File is not a gcode file, cannot upload";
    }
  }

  /**
   * format bytes
   * @param bytes (File size in bytes)
   * @param decimals (Decimals point)
   */
  formatBytes(bytes, decimals = 2) {
    if (bytes === 0) {
      return "0 Bytes";
    }
    const k = 1024;
    const dm = decimals <= 0 ? 0 : decimals;
    const sizes = ["Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + " " + sizes[i];
  }

  uploadFile() {
    // this.uploadFilesSimulator(0);
    console.log("File to upload: ", this.files[0]);

    if (this.files.length == 0) {
      console.log("no files");
      return;
    }


    const formFields = {
      printFile: this.uploadForm.value.frm_print_auto_start
    }
    const formData = new FormData();
    formData.append('file', this.files[0]);
    formData.append('formField', JSON.stringify(formFields));

    this.fileService.fileUpload(formData).subscribe((event: HttpEvent<any>) => {
      switch (event.type) {
        case HttpEventType.Sent:
          console.log('Request has been made!');
          break;
        case HttpEventType.ResponseHeader:
          console.log('Response header has been received!');
          break;
        case HttpEventType.UploadProgress:
          const progress = Math.round(event.loaded / event.total * 100);
          console.log(`Uploaded! ${progress}%`);
          this.files[0].progress = progress;
          break;
        case HttpEventType.Response:
          console.log('User successfully created!', event.body);

      }
    },
      err => {
        console.log("File upload error: ", err)
      }
    );
  }
}
