import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { UserResponseDTO } from '../../../../core/models/admin.model';
import { UserService } from '../../../../core/services/user';
import { UserStatus } from '../../../../core/enums/admin-enums';



@Component({
  selector: 'app-view-user',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './view-user.html',
  styleUrl: './view-user.css'
})
export class ViewUsers implements OnInit {

  users: UserResponseDTO[] = [];

  allUsers: UserResponseDTO[] = [];

  loading = false;

  selectedRole = '';

  selectedStatus = '';

  roles: string[] = [];

  statuses: string[] = [
    'ACTIVE',
    'INACTIVE',
    'SUSPENDED',
    'BLOCKED'
  ];

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  constructor(
    private readonly userService: UserService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.loadUsers();

  }

  loadUsers(): void {

    this.loading = true;

    this.userService
      .getAllUsers()
      .subscribe({

        next: (response: UserResponseDTO[]) => {

          this.allUsers = response;

          this.users = response;

          this.roles = Array.from(
            new Set(
              response.map(
                (user: UserResponseDTO) => user.role
              )
            )
          );

          this.loading = false;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(
            'Error loading users',
            error
          );

          this.loading = false;

          this.cdr.detectChanges();

        }

      });

  }

  filterUsers(): void {

    this.users =
      this.allUsers.filter(
        (user: UserResponseDTO) => {

          const matchesRole =
            !this.selectedRole ||
            user.role === this.selectedRole;

          const matchesStatus =
            !this.selectedStatus ||
            user.status === this.selectedStatus;

          return (
            matchesRole &&
            matchesStatus
          );

        }
      );

    this.cdr.detectChanges();

  }

  updateStatus(
    user: UserResponseDTO,
    status: any
  ): void {
    const typedStatus = status as UserStatus;

    const payload = {
      status: typedStatus
    };

    this.userService
      .updateUserStatus(
        user.userId,
        payload
      )
      .subscribe({

        next: () => {

          user.status = typedStatus as any;

          this.popupTitle = 'Success';

          this.popupMessage =
            'User status updated successfully';

          this.showPopup = true;

          this.cdr.detectChanges();

        },

        error: (error) => {

          this.popupTitle = 'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to update user status';

          this.showPopup = true;

          this.cdr.detectChanges();

        }

      });

  }

  closePopup(): void {

    this.showPopup = false;

  }

}