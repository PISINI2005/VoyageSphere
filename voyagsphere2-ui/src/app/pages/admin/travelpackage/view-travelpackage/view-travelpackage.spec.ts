import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewPackage } from './view-travelpackage';

describe('ViewTravelpackage', () => {
  let component: ViewPackage;
  let fixture: ComponentFixture<ViewPackage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewPackage],
    }).compileComponents();

    fixture = TestBed.createComponent(ViewPackage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
