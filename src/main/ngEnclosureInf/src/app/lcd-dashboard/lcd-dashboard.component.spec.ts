import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LcdDashboardComponent } from './lcd-dashboard.component';

describe('LcdDashboardComponent', () => {
  let component: LcdDashboardComponent;
  let fixture: ComponentFixture<LcdDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LcdDashboardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LcdDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
