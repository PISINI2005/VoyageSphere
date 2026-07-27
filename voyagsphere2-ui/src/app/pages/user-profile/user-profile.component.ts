import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../../layout/navbar/navbar';
import { UserService, UpdateProfileDTO, ChangePasswordDTO } from '../../core/services/user.service';
import { UserResponseDTO } from '../../core/models/travel.model';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent
  ],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css'
})
export class UserProfileComponent implements OnInit {
  userProfile: UserResponseDTO | null = null;

  profileForm: UpdateProfileDTO = {
    firstName: '',
    lastName: '',
    phoneNo: 0
  };

  passwordForm: ChangePasswordDTO = {
    oldPassword: '',
    newPassword: ''
  };
  confirmPassword = '';

  constructor(
    private userService: UserService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.userService.getProfile().subscribe({
      next: (data) => {
        this.userProfile = data;
        this.profileForm = {
          firstName: data.firstName,
          lastName: data.lastName,
          phoneNo: data.phoneNo
        };
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading profile:', err);
        alert('Error loading profile data.');
      }
    });
  }

  saveProfile(): void {
    if (!this.profileForm.firstName || !this.profileForm.lastName) {
      alert('First name and last name are required.');
      return;
    }

    this.userService.updateProfile(this.profileForm).subscribe({
      next: (updatedUser) => {
        this.userProfile = updatedUser;
        alert('Profile updated successfully!');
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error updating profile:', err);
        alert('Error updating profile. Please try again.');
      }
    });
  }

  updatePassword(): void {
    if (!this.passwordForm.oldPassword || !this.passwordForm.newPassword) {
      alert('Old and new passwords are required.');
      return;
    }

    if (this.passwordForm.newPassword !== this.confirmPassword) {
      alert('New password and confirmation do not match.');
      return;
    }

    this.userService.changePassword(this.passwordForm).subscribe({
      next: () => {
        alert('Password changed successfully!');
        this.passwordForm = { oldPassword: '', newPassword: '' };
        this.confirmPassword = '';
      },
      error: (err) => {
        console.error('Error changing password:', err);
        alert('Error changing password. Please check your old password.');
      }
    });
  }
}
