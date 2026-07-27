import { Component } from '@angular/core';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-agent-dashboard',
  standalone: true,
  imports: [
    AgentNavbar,RouterLink
  ],
  templateUrl: './agent-dashboard.html',
  styleUrl: './agent-dashboard.css'
})
export class AgentDashboardComponent {}

