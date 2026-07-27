import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-agent-navbar',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './agent-navbar.html',
  styleUrl: './agent-navbar.css'
})
export class AgentNavbar {

  constructor(
    private router: Router
  ) {}

  logout(): void {

    localStorage.removeItem('token');
    localStorage.removeItem('user');

    sessionStorage.clear();

    this.router.navigate(['/']);
  }
}