import { TestBed } from '@angular/core/testing';

import { AgentContextService } from './agent-context';

describe('AgentContextService', () => {
  let service: AgentContextService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AgentContextService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
