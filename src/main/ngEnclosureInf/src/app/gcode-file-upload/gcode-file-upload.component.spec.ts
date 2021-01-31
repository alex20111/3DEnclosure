import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GcodeFileUploadComponent } from './gcode-file-upload.component';

describe('GcodeFileUploadComponent', () => {
  let component: GcodeFileUploadComponent;
  let fixture: ComponentFixture<GcodeFileUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GcodeFileUploadComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GcodeFileUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
