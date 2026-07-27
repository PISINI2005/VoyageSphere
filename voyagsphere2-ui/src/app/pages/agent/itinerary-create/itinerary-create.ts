import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { ItineraryService } from '../../../core/services/itinerary';
import { AgentContextService } from '../../../core/services/agent-context';
import { CreateItineraryDTO } from '../../../core/models/travel.model';

@Component({
  selector: 'app-itinerary-create',
  standalone: true,
  imports: [CommonModule, FormsModule, AgentNavbar],
  templateUrl: './itinerary-create.html',
  styleUrl: './itinerary-create.css'
})
export class ItineraryCreate {
  itinerary: CreateItineraryDTO = {
    tripName: '',
    description: '',
    startDate: '',
    endDate: '',
    userId: 0
  };

  constructor(
    private itineraryService: ItineraryService,
    private agentContext: AgentContextService,
    private router: Router
  ) {}

  saveItinerary(): void {
    this.itinerary.userId = this.agentContext.selectedCustomerId || 0;

    if (!this.itinerary.tripName || !this.itinerary.startDate || !this.itinerary.endDate) {
      alert('Please fill in all required fields.');
      return;
    }

    this.itineraryService.createItinerary(this.itinerary).subscribe({
      next: () => {
        alert('Itinerary created successfully!');
        this.router.navigate(['/agent/itineraries']);
      },
      error: (err) => {
        console.error('Error creating itinerary:', err);
        alert('Failed to create itinerary.');
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/agent/itineraries']);
  }
}
