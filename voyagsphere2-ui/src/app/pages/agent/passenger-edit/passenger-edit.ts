import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Location } from '@angular/common';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { ProfileService } from '../../../core/services/profile';
import { PassengerProfileRequestDTO, PassengerProfileResponseDTO } from '../../../core/models/travel.model';
import { AgentContextService } from '../../../core/services/agent-context';

@Component({
  selector: 'app-passenger-edit',
  standalone: true,
  imports: [CommonModule, FormsModule, AgentNavbar],
  templateUrl: './passenger-edit.html',
  styleUrl: './passenger-edit.css'
})
export class PassengerEdit implements OnInit {
  profile: any = {
    passengerName: '',
    dateOfBirth: '',
    gender: 'OTHER',
    emailAddress: '',
    contactNo: '',
    nationality: '',
    identificationType: '',
    identificationNumber: ''
  };
  isLoading = true;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private profileService: ProfileService,
    private location: Location,
    private agentContext:AgentContextService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.loadProfile(id);
    } else {
      this.isLoading = false;
    }
  }

  loadProfile(id: string): void {
    this.isLoading = true;
    this.profileService.getProfileById(
      Number(id),
      this.agentContext.selectedCustomerId
    ).subscribe({
      next: (data: PassengerProfileResponseDTO) => {
        this.profile = {
          passengerName: data.passengerName,
          dateOfBirth: data.dateOfBirth,
          gender: data.gender,
          emailAddress: data.emailAddress,
          contactNo: data.contactNo,
          nationality: data.nationality,
          identificationType: data.identificationType,
          identificationNumber: data.identificationNumber
        };
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching profile:', err);
        this.isLoading = false;
        alert('Profile not found!');
        this.router.navigate(['/agent/passengers']);
      }
    });
  }

  saveProfile(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      // Update Mode
      this.profileService.updateProfile(
        Number(id),
        this.profile,
        this.agentContext.selectedCustomerId
      ).subscribe({
        next: () => {
          alert('Profile updated successfully!');
          this.router.navigate(['/agent/passengers']);
        },
        error: (err) => {
          console.error('Error updating profile:', err);
          alert('Failed to update profile.');
        }
      });
    } else {
      // Create Mode
      const profileData = {
        ...this.profile,
        userId: this.agentContext.selectedCustomerId
      };
      this.profileService.createProfile(profileData).subscribe({
        next: () => {
          alert('Profile created successfully!');
          this.router.navigate(['/agent/passengers']);
        },
        error: (err) => {
          console.error('Error creating profile:', err);
          alert('Failed to create profile.');
        }
      });
    }
  }

  goBack(): void {
    this.location.back();
  }
}
