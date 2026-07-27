import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComplaintUpdateStatus } from './complaint-update-status';

describe('ComplaintUpdateStatus', () => {
  let component: ComplaintUpdateStatus;
  let fixture: ComponentFixture<ComplaintUpdateStatus>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComplaintUpdateStatus],
    }).compileComponents();

    fixture = TestBed.createComponent(ComplaintUpdateStatus);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
