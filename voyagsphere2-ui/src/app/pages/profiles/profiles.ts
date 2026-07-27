import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { NavbarComponent } from '../../layout/navbar/navbar';
import { ProfileService } from '../../core/services/profile';
import { PassengerProfileRequestDTO, PassengerProfileResponseDTO } from '../../core/models/travel.model';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { AgentContextService } from '../../core/services/agent-context';
@Component({
  selector: 'app-profiles',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent
  ],
  templateUrl: './profiles.html',
  styleUrl: './profiles.css'
})
export class ProfilesComponent implements OnInit {

  profiles: PassengerProfileResponseDTO[] = [];

  profileForm: PassengerProfileRequestDTO = {
    passengerName: '',
    dateOfBirth: '',
    gender: 'MALE',
    contactNo: '',
    emailAddress: '',
    nationality: 'INDIAN',
    identificationType: 'PASSPORT',
    identificationNumber: ''
  };

  constructor(
  private profileService: ProfileService,
  private cdr: ChangeDetectorRef,
  private router: Router,
  private route: ActivatedRoute,
  private authService:AuthService,
  public agentContext:AgentContextService
) {}
returnUrl: string | null = null;
  ngOnInit(): void {

  this.returnUrl =
    this.route.snapshot.queryParamMap.get('returnUrl');

  this.loadProfiles();
}

  loadProfiles(): void {
    

  if (
    this.authService.isTravelAgent() &&
    this.agentContext.selectedCustomerId
  ) {
console.log(this.agentContext.selectedCustomerId);
    
    this.profileService
      .getMyProfiles(
        this.agentContext.selectedCustomerId
      )
      .subscribe({
        next: (data) => {

          this.profiles = [...data];

          this.cdr.detectChanges();
        },
        error: (err) =>
          console.error('Error loading profiles', err)
      });

    return;
  }

  this.profileService.getMyProfiles().subscribe({
    next: (data) => {

      this.profiles = [...data];

      this.cdr.detectChanges();
    },
    error: (err) =>
      console.error('Error loading profiles', err)
  });
}


  addProfile(): void {
    const {
      passengerName,
      dateOfBirth,
      contactNo,
      emailAddress,
      nationality,
      identificationType,
      identificationNumber
    } = this.profileForm;

    if (!passengerName || !dateOfBirth || !contactNo || !emailAddress || !nationality || !identificationType || !identificationNumber) {
      alert('Please fill in all required fields');
      return;
    }

    if (!/^\d{10}$/.test(contactNo)) {
      alert('Please enter a valid 10-digit contact number');
      return;
    }
    if (
  this.authService.isTravelAgent() &&
  this.agentContext.selectedCustomerId
) {

  this.profileForm.userId =
    this.agentContext.selectedCustomerId;
}

    this.profileService.createProfile(this.profileForm).subscribe({
  next: () => {

    this.resetForm();

    // If user came from booking page,
    // send them back automatically
    if (this.returnUrl) {
      this.router.navigateByUrl(this.returnUrl);
      return;
    }

    this.loadProfiles();
  },
  error: (err) => {
    console.error('Error creating profile:', err);
    alert('Error creating profile. Please check your inputs.');
  }
});
  }

  deleteProfile(id: number): void {
    this.profileService.deleteProfile(id).subscribe({
      next: () => {

        // 🔥 immutable update (important for UI refresh)
        this.profiles = this.profiles.filter(
          p => p.passengerProfileId !== id
        );

        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error deleting profile:', err)
    });
  }

  private resetForm() {
    this.profileForm = {
      passengerName: '',
      dateOfBirth: '',
      gender: 'MALE',
      contactNo: '',
      emailAddress: '',
      nationality: 'INDIAN',
      identificationType: 'PASSPORT',
      identificationNumber: ''
    };
  }
}