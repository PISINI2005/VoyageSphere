import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdatePackage } from './update-travelpackage';

describe('UpdateTravelpackage', () => {
  let component: UpdatePackage;
  let fixture: ComponentFixture<UpdatePackage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdatePackage],
    }).compileComponents();

    fixture = TestBed.createComponent(UpdatePackage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
