import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { NotificationDropdownComponent } from '../notification-dropdown/notification-dropdown';
import { NotificationService } from '../../core/services/notification';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule, NotificationDropdownComponent, CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent {
  constructor(public notificationService: NotificationService,private router:Router, public authService: AuthService) {}

  get unreadNotifications(): number {
    return this.notificationService.unreadCount;
  }
  
logout(): void {

    // Clear auth data
    localStorage.clear();
    sessionStorage.clear();

    // Navigate to login page
    this.router.navigate(['/']);
  }

}
