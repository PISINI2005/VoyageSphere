import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { ProfileService } from '../../../core/services/profile';
import { AgentContextService } from '../../../core/services/agent-context';
import { PassengerProfileResponseDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-passengers',
  standalone: true,
  imports: [CommonModule, AgentNavbar, RouterLink],
  templateUrl: './passengers.html',
  styleUrl: './passengers.css',
})
export class Passengers implements OnInit {
  profiles: PassengerProfileResponseDTO[] = [];

  constructor(
    private profileService: ProfileService,
    public agentContext: AgentContextService
  ) {}

 ngOnInit(): void {
  console.log('Customer ID:', this.agentContext.selectedCustomerId);
  this.loadCustomerProfiles();
}

  loadCustomerProfiles(): void {
    if (!this.agentContext.selectedCustomerId) {
      return;
    }

    this.profileService
      .getMyProfiles(this.agentContext.selectedCustomerId)
      .subscribe({
        next: (data) => {
          this.profiles = data;
        },
        error: (err) => {
          console.error('Error loading customer profiles:', err);
        },
      });
  }

 deleteProfile(profileId: number, profileName: string): void {
  if (confirm(`Are you sure you want to delete the profile for ${profileName}?`)) {

    this.profileService.deleteProfile(
      profileId,
      this.agentContext.selectedCustomerId
    ).subscribe({
      next: () => {
        alert('Profile deleted successfully!');
        this.loadCustomerProfiles();
      },
      error: (err) => {
        console.error('Error deleting profile:', err);
        alert('Failed to delete profile.');
      }
    });

  }
}
}