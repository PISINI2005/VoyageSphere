import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Kpireport } from './kpireport';

describe('Kpireport', () => {
  let component: Kpireport;
  let fixture: ComponentFixture<Kpireport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Kpireport],
    }).compileComponents();

    fixture = TestBed.createComponent(Kpireport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
