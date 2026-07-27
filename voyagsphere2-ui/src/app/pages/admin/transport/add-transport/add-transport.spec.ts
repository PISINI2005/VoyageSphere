import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddTransport } from './add-transport';

describe('AddTransport', () => {
  let component: AddTransport;
  let fixture: ComponentFixture<AddTransport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddTransport],
    }).compileComponents();

    fixture = TestBed.createComponent(AddTransport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
