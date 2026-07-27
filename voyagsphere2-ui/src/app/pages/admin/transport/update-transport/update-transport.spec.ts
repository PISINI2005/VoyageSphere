import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateTransport } from './update-transport';

describe('UpdateTransport', () => {
  let component: UpdateTransport;
  let fixture: ComponentFixture<UpdateTransport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateTransport],
    }).compileComponents();

    fixture = TestBed.createComponent(UpdateTransport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
