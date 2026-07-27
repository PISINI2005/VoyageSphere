import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddPackage } from './add-travelpackage';

describe('AddTravelpackage', () => {
  let component: AddPackage;
  let fixture: ComponentFixture<AddPackage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddPackage],
    }).compileComponents();

    fixture = TestBed.createComponent(AddPackage);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
