import { TestBed } from '@angular/core/testing';

import { ComplaintService } from './complaint';

describe('Complaint', () => {
  let service: ComplaintService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ComplaintService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
