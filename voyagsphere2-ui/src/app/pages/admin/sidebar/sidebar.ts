import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Router,
  RouterLink,
  RouterLinkActive
} from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css'
})
export class Sidebar {

  role =
    localStorage.getItem('role');

  showUsers = false;

  showBookings = false;

  showPartners = false;

  showManagePartners = false;

  showFlights = false;

  showHotels = false;

  showTransports = false;

  showPackages = false;

  constructor(
    private readonly router: Router
  ) {}

  toggleUsers(): void {

    this.showUsers =
      !this.showUsers;

  }

  toggleBookings(): void {

    this.showBookings =
      !this.showBookings;

  }

  togglePartners(): void {

    this.showPartners =
      !this.showPartners;

  }

  toggleManagePartners(): void {

    this.showManagePartners =
      !this.showManagePartners;

  }

  toggleFlights(): void {

    this.showFlights =
      !this.showFlights;

  }

  toggleHotels(): void {

    this.showHotels =
      !this.showHotels;

  }

  toggleTransports(): void {

    this.showTransports =
      !this.showTransports;

  }

  togglePackages(): void {

    this.showPackages =
      !this.showPackages;

  }

  logout(): void {

    localStorage.clear();

    this.router.navigate([
      '/login'
    ]);

  }

}