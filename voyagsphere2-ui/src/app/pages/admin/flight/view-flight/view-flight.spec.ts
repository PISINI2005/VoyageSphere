import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewFlight } from './view-flight';

describe('ViewFlight', () => {
  let component: ViewFlight;
  let fixture: ComponentFixture<ViewFlight>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewFlight],
    }).compileComponents();

    fixture = TestBed.createComponent(ViewFlight);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
