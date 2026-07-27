import {
  ChangeDetectorRef,
  Component,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TransportService } from '../../../../core/services/transport';
import { TransportStatus } from '../../../../core/enums/admin-enums';



@Component({
  selector: 'app-view-transport',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink
  ],
  templateUrl: './view-transport.html',
  styleUrl: './view-transport.css'
})
export class ViewTransport implements OnInit {

  transports: any[] = [];

  source = '';

  destination = '';

  status = '';

  loading = false;

  showPopup = false;

  popupTitle = '';

  popupMessage = '';

  constructor(
    private readonly transportService: TransportService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.searchTransports();

  }

  searchTransports(): void {

    this.loading = true;

    this.transportService
      .getTransports(
        this.source,
        this.destination,
        this.status
      )
      .subscribe({

        next: (response) => {

          this.transports =
            response.content ??
            response;

          this.loading = false;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(error);

          this.loading = false;

          this.popupTitle = 'Error';

          this.popupMessage =
            'Failed to load transports';

          this.showPopup = true;

          this.cdr.detectChanges();

        }

      });

  }

  resetSearch(): void {

    this.source = '';

    this.destination = '';

    this.status = '';

    this.searchTransports();

  }

  updateStatus(
    transport: any,
    status: any
  ): void {
    const typedStatus = status as TransportStatus;

    this.transportService
      .updateTransportStatus(
        transport.transportId,
        {
          status: typedStatus
        }
      )
      .subscribe({

        next: () => {

          transport.transportStatus =
            typedStatus;

          this.popupTitle =
            'Success';

          this.popupMessage =
            'Transport status updated successfully';

          this.showPopup = true;

          this.cdr.detectChanges();

        },

        error: (error) => {

          console.error(error);

          this.popupTitle =
            'Error';

          this.popupMessage =
            error?.error?.message ??
            'Failed to update transport status';

          this.showPopup = true;

          this.cdr.detectChanges();

        }

      });

  }

  closePopup(): void {

    this.showPopup = false;

    this.cdr.detectChanges();

  }

}