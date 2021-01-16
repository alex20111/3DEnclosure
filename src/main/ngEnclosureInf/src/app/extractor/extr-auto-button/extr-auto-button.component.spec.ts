import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExtrAutoButtonComponent } from './extr-auto-button.component';

describe('ExtrAutoButtonComponent', () => {
  let component: ExtrAutoButtonComponent;
  let fixture: ComponentFixture<ExtrAutoButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExtrAutoButtonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExtrAutoButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
