import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AgentNavbar} from '../../../layout/agent-navbar/agent-navbar';
import { AgentContextService } from '../../../core/services/agent-context';


@Component({
  selector: 'app-customer-details',
  standalone: true,
  imports: [
    RouterModule,
    AgentNavbar
  ],
  templateUrl: './customer-details.html',
  styleUrl: './customer-details.css'
})
export class CustomerDetailsComponent {

  constructor(
    public agentContext: AgentContextService
  ) {}

}