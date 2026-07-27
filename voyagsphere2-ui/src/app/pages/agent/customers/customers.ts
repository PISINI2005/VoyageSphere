import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';



import { UserResponseDTO } from '../../../core/models/travel.model';
import { AgentNavbar } from '../../../layout/agent-navbar/agent-navbar';
import { AgentContextService } from '../../../core/services/agent-context';
import { UserService } from '../../../core/services/user';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [
    CommonModule,
    AgentNavbar
  ],
  templateUrl: './customers.html',
  styleUrl: './customers.css'
})
export class CustomersComponent implements OnInit {

  customers: UserResponseDTO[] = [];
  isLoading = true;

  constructor(
    private userService: UserService,
    private agentContext: 
    AgentContextService,
    private router: Router
  ) {}

  ngOnInit(): void {

    this.userService
        .getCustomers()
        .subscribe({
          next: (data) => {
            this.customers = data;
            this.isLoading = false;
          },
          error: (err) => {
            console.error(err);
            this.isLoading = false;
          }
        });
}

  loadCustomers() {
    this.userService.getCustomers()
      .subscribe(data => {
        this.customers = data;
      });
  }

 selectCustomer(customer: UserResponseDTO): void {

  console.log('Customer selected:', customer);

  this.agentContext.setCustomer(
    customer.userId,
    customer.email
  );

  console.log(
    'Stored ID:',
    this.agentContext.selectedCustomerId
  );

  this.router.navigate([
    '/agent/customer',
    customer.userId
  ]);
}
}