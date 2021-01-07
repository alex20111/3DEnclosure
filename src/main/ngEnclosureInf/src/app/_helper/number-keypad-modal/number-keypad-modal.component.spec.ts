import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NumberKeypadModalComponent } from './number-keypad-modal.component';

describe('NumberKeypadModalComponent', () => {
  let component: NumberKeypadModalComponent;
  let fixture: ComponentFixture<NumberKeypadModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NumberKeypadModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NumberKeypadModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
